package controller.report;

import controller.common.BaseController;
import dao.sales.OrderDAO;
import dao.sales.OrderReportDAO;
import dao.user.UserManagementDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.BranchKpi;
import model.Employee;
import model.EmployeeKpi;
import model.Order;
import model.OrderReportFilter;
import model.OrderReportKpi;
import util.pagination.PaginationHelper;
import util.pagination.PaginationHelper.PageResult;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import util.report.PdfReportUtil;

@WebServlet(name = "OrderReportController", urlPatterns = {"/reports/orders", "/reports/orders/export-excel", "/reports/orders/export-pdf"})
public class OrderReportController extends BaseController {

    private OrderReportDAO orderReportDAO;
    private OrderDAO orderDAO;
    private UserManagementDao userManagementDao;

    @Override
    public void init() throws ServletException {
        orderReportDAO = new OrderReportDAO();
        orderDAO = new OrderDAO();
        userManagementDao = new UserManagementDao();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isOwnerOrManager(request, response)) return;
        applyBranchFilterForManager(request);

        String path = request.getServletPath();

        if ("/reports/orders/export-excel".equals(path)) {
            exportExcel(request, response);
            return;
        }
        if ("/reports/orders/export-pdf".equals(path)) {
            exportPdf(request, response);
            return;
        }

        OrderReportFilter f = buildFilter(request);
        String tab = trim(request.getParameter("tab"));
        if (tab == null) tab = "orders";

        // Always compute KPI data (used by KPI dashboard tab and exports)
        OrderReportKpi kpi = orderReportDAO.calculateKpiSummary(f);
        request.setAttribute("kpi", kpi);

        Employee u = (Employee) request.getSession().getAttribute("currentUser");
        boolean isOwner = u != null && ("Owner".equalsIgnoreCase(u.getRoleName()) || "Admin".equalsIgnoreCase(u.getRoleName()));

        if (f.getEmpId() != null) {
            EmployeeKpi empKpi = orderReportDAO.calculateEmployeeKpi(f);
            request.setAttribute("employeeKpi", empKpi);
        }

        if (isOwner) {
            List<BranchKpi> branchKpis = orderReportDAO.calculateBranchKpi(f);
            double totalRev = kpi.getTotalRevenue();
            for (BranchKpi bk : branchKpis) {
                bk.setRevenuePercent(totalRev > 0 ? bk.getRevenue() / totalRev * 100 : 0);
            }
            request.setAttribute("branchKpis", branchKpis);
        }

        request.setAttribute("isOwner", isOwner);

        String dp = trim(request.getParameter("datePreset"));
        String baseQueryString = buildBaseQueryString(f, dp);

        if ("kpi".equals(tab)) {
            request.setAttribute("activeTab", "kpi");
            request.setAttribute("pageTitle", "KPI Dashboard");
            request.setAttribute("pageSubtitle", "Tổng quan chỉ số hiệu suất đơn hàng");
            request.setAttribute("branches", userManagementDao.getAllBranches());
            request.setAttribute("employees", userManagementDao.getEmployees(null, null, null, null, 1, 9999));
            request.setAttribute("baseUrl", request.getContextPath() + "/reports/orders");
            request.setAttribute("filter", f);
            request.setAttribute("datePreset", request.getParameter("datePreset"));
            request.setAttribute("managerBranchId", request.getAttribute("managerBranchId"));
            request.setAttribute("baseQueryString", baseQueryString);
            forward(request, response, "reports/order-report");
            return;
        }

        int page = parseInt(request.getParameter("page"), 1);
        int sizeValue = parseInt(request.getParameter("sizeValue"), 30);

        int total = orderReportDAO.countOrders(f);
        PageResult pr = PaginationHelper.compute(total, page, sizeValue);
        pr.setAttributes(request);

        List<Order> orders = orderReportDAO.searchOrders(f, pr.getCurrentPage(), pr.getPageSize());
        request.setAttribute("orders", orders);
        request.setAttribute("activeTab", "orders");
        request.setAttribute("branches", userManagementDao.getAllBranches());
        request.setAttribute("employees", userManagementDao.getEmployees(null, null, null, null, 1, 9999));
        request.setAttribute("baseUrl", request.getContextPath() + "/reports/orders");

        request.setAttribute("filter", f);
        request.setAttribute("datePreset", request.getParameter("datePreset"));
        request.setAttribute("pageTitle", "Trung tâm báo cáo đơn hàng");
        request.setAttribute("pageSubtitle", "Tra cứu, lọc và xuất báo cáo đơn hàng");
        request.setAttribute("managerBranchId", request.getAttribute("managerBranchId"));
        request.setAttribute("baseQueryString", baseQueryString);

        forward(request, response, "reports/order-report");
    }

    private OrderReportFilter buildFilter(HttpServletRequest request) {
        OrderReportFilter f = new OrderReportFilter();

        String datePreset = trim(request.getParameter("datePreset"));
        if (datePreset != null && !datePreset.isEmpty()) {
            LocalDate[] range = resolveDatePreset(datePreset);
            f.setDateFrom(range[0]);
            f.setDateTo(range[1]);
        } else {
            f.setDateFrom(parseDate(request.getParameter("dateFrom")));
            f.setDateTo(parseDate(request.getParameter("dateTo")));
        }

        f.setEmpId(parseIntNull(request.getParameter("empId")));
        f.setBranchId(parseIntNull(request.getParameter("branchId")));
        f.setCustomerId(parseIntNull(request.getParameter("customerId")));
        f.setOrderId(parseIntNull(request.getParameter("orderId")));
        f.setOrderStatus(trim(request.getParameter("orderStatus")));
        f.setPaymentMethod(trim(request.getParameter("paymentMethod")));
        f.setKeyword(trim(request.getParameter("keyword")));
        f.setSortBy(trim(request.getParameter("sortBy")));
        f.setSortDir(trim(request.getParameter("sortDir")));

        // StoreManager can only see own branch
        if (request.getAttribute("managerBranchId") != null && f.getBranchId() == null) {
            f.setBranchId((Integer) request.getAttribute("managerBranchId"));
        }

        return f;
    }

    private LocalDate[] resolveDatePreset(String preset) {
        LocalDate today = LocalDate.now();
        return switch (preset) {
            case "today" -> new LocalDate[]{today, today};
            case "yesterday" -> new LocalDate[]{today.minusDays(1), today.minusDays(1)};
            case "7days" -> new LocalDate[]{today.minusDays(7), today};
            case "30days" -> new LocalDate[]{today.minusDays(30), today};
            case "this_month" -> new LocalDate[]{today.withDayOfMonth(1), today};
            case "last_month" -> new LocalDate[]{today.minusMonths(1).withDayOfMonth(1), today.withDayOfMonth(1).minusDays(1)};
            case "this_year" -> new LocalDate[]{today.withDayOfYear(1), today};
            case "1year" -> new LocalDate[]{today.minusYears(1), today};
            default -> new LocalDate[]{null, null};
        };
    }

    private void exportExcel(HttpServletRequest request, HttpServletResponse response) throws IOException {
        OrderReportFilter f = buildFilter(request);
        OrderReportKpi kpi = orderReportDAO.calculateKpiSummary(f);
        List<Order> orders = orderReportDAO.searchOrders(f, 1, Integer.MAX_VALUE);

        Employee u = (Employee) request.getSession().getAttribute("currentUser");
        boolean isOwner = u != null && ("Owner".equalsIgnoreCase(u.getRoleName()) || "Admin".equalsIgnoreCase(u.getRoleName()));

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Order Report");

            CellStyle bold = wb.createCellStyle();
            Font boldFont = wb.createFont();
            boldFont.setBold(true);
            boldFont.setFontHeightInPoints((short) 12);
            bold.setFont(boldFont);

            CellStyle label = wb.createCellStyle();
            Font labelFont = wb.createFont();
            labelFont.setBold(true);
            labelFont.setFontHeightInPoints((short) 10);
            label.setFont(labelFont);

            int r = 0;
            Row titleRow = sheet.createRow(r++);
            titleRow.createCell(0).setCellValue("KPI SUMMARY");
            titleRow.getCell(0).setCellStyle(bold);

            r = writeKpiExcelRow(sheet, r, label, "Tổng số đơn hàng", String.valueOf(kpi.getTotalOrders()));
            r = writeKpiExcelRow(sheet, r, label, "Tổng doanh thu", String.format("%,.0f ₫", kpi.getTotalRevenue()));
            r = writeKpiExcelRow(sheet, r, label, "Giá trị trung bình (AOV)", String.format("%,.0f ₫", kpi.getAov()));
            r = writeKpiExcelRow(sheet, r, label, "Đơn hoàn thành", String.valueOf(kpi.getCompletedOrders()));
            r = writeKpiExcelRow(sheet, r, label, "Đơn đã hủy", String.valueOf(kpi.getCancelledOrders()));
            r = writeKpiExcelRow(sheet, r, label, "Tỉ lệ hoàn thành", String.format("%.1f%%", kpi.getCompletionRate()));

            if (f.getEmpId() != null) {
                EmployeeKpi ek = orderReportDAO.calculateEmployeeKpi(f);
                if (ek != null) {
                    r = writeKpiExcelRow(sheet, r, label, "Nhân viên", ek.getEmployeeName());
                    r = writeKpiExcelRow(sheet, r, label, "Đơn hoàn thành (NV)", String.valueOf(ek.getCompletedOrders()));
                    r = writeKpiExcelRow(sheet, r, label, "Đơn đã hủy (NV)", String.valueOf(ek.getCancelledOrders()));
                    r = writeKpiExcelRow(sheet, r, label, "Doanh thu (NV)", String.format("%,.0f ₫", ek.getRevenue()));
                    r = writeKpiExcelRow(sheet, r, label, "AOV (NV)", String.format("%,.0f ₫", ek.getAov()));
                    r = writeKpiExcelRow(sheet, r, label, "Tỉ lệ hoàn thành (NV)", String.format("%.1f%%", ek.getCompletionRate()));
                }
            }

            if (isOwner) {
                List<BranchKpi> branchKpis = orderReportDAO.calculateBranchKpi(f);
                double totalRev = kpi.getTotalRevenue();
                if (!branchKpis.isEmpty()) {
                    r = writeKpiExcelRow(sheet, r, label, "--- CHI NHÁNH ---", "");
                    for (BranchKpi bk : branchKpis) {
                        double pct = totalRev > 0 ? bk.getRevenue() / totalRev * 100 : 0;
                        r = writeKpiExcelRow(sheet, r, label, bk.getBranchName(),
                            String.format("%,.0f ₫ (%.1f%%)", bk.getRevenue(), pct));
                    }
                }
            }

            r++; // blank row
            Row header = sheet.createRow(r++);
            String[] cols = {"Mã đơn", "Chi nhánh", "Nhân viên", "Khách hàng", "Tổng tiền", "Phương thức", "Trạng thái", "Ngày tạo"};
            for (int i = 0; i < cols.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(cols[i]);
                cell.setCellStyle(label);
            }
            for (Order o : orders) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(o.getOrderCode());
                row.createCell(1).setCellValue(o.getBranchName());
                row.createCell(2).setCellValue(o.getEmployeeName());
                row.createCell(3).setCellValue(o.getCustomerName() != null ? o.getCustomerName() : "Khách vãng lai");
                row.createCell(4).setCellValue(o.getTotalAmount());
                row.createCell(5).setCellValue(o.getPaymentMethod());
                row.createCell(6).setCellValue(o.getStatus() != null ? o.getStatus().name() : "");
                row.createCell(7).setCellValue(o.getCreatedAt());
            }
            for (int i = 0; i < cols.length; i++) sheet.autoSizeColumn(i);
            sheet.autoSizeColumn(0);

            String fileName = "OrderReport_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            OutputStream out = response.getOutputStream();
            wb.write(out);
            out.flush();
        }
    }

    private int writeKpiExcelRow(Sheet sheet, int r, CellStyle style, String label, String value) {
        Row row = sheet.createRow(r);
        row.createCell(0).setCellValue(label);
        row.getCell(0).setCellStyle(style);
        row.createCell(1).setCellValue(value);
        return r + 1;
    }

    private void exportPdf(HttpServletRequest request, HttpServletResponse response) throws IOException {
        OrderReportFilter f = buildFilter(request);
        OrderReportKpi kpi = orderReportDAO.calculateKpiSummary(f);
        List<Order> orders = orderReportDAO.searchOrders(f, 1, Integer.MAX_VALUE);

        Employee u = (Employee) request.getSession().getAttribute("currentUser");
        String generatedBy = u != null ? u.getFullName() : "Unknown";
        boolean isOwner = u != null && ("Owner".equalsIgnoreCase(u.getRoleName()) || "Admin".equalsIgnoreCase(u.getRoleName()));

        EmployeeKpi empKpi = null;
        if (f.getEmpId() != null) {
            empKpi = orderReportDAO.calculateEmployeeKpi(f);
        }

        List<BranchKpi> branchKpis = null;
        if (isOwner) {
            branchKpis = orderReportDAO.calculateBranchKpi(f);
            double totalRev = kpi.getTotalRevenue();
            for (BranchKpi bk : branchKpis) {
                bk.setRevenuePercent(totalRev > 0 ? bk.getRevenue() / totalRev * 100 : 0);
            }
        }

        String datePreset = trim(request.getParameter("datePreset"));
        String filterLines = buildFilterSummary(f, datePreset);

        byte[] pdfBytes = PdfReportUtil.generateOrderReportPdf(
            "FINORA", generatedBy, orders, f, datePreset, filterLines, kpi, empKpi, branchKpis, isOwner);

        String fileName = "Order_Report_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".pdf";
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        response.getOutputStream().write(pdfBytes);
        response.getOutputStream().flush();
    }

    private String buildFilterSummary(OrderReportFilter f, String datePreset) {
        StringBuilder sb = new StringBuilder();
        String datePresetLabel = getDatePresetLabel(datePreset);
        if (datePresetLabel != null) {
            sb.append("  • Khoảng thời gian: ").append(datePresetLabel);
            if (f.getDateFrom() != null && f.getDateTo() != null) {
                sb.append(" (").append(f.getDateFrom()).append(" - ").append(f.getDateTo()).append(")");
            }
            sb.append("\n");
        }
        if (f.getDateFrom() != null && datePresetLabel == null) {
            sb.append("  • Từ ngày: ").append(f.getDateFrom()).append("\n");
        }
        if (f.getDateTo() != null && datePresetLabel == null) {
            sb.append("  • Đến ngày: ").append(f.getDateTo()).append("\n");
        }
        if (f.getEmpId() != null) {
            sb.append("  • Nhân viên (ID: ").append(f.getEmpId()).append(")\n");
        }
        if (f.getBranchId() != null) {
            sb.append("  • Chi nhánh (ID: ").append(f.getBranchId()).append(")\n");
        }
        if (f.getOrderId() != null) {
            sb.append("  • Mã đơn: ").append(f.getOrderId()).append("\n");
        }
        if (f.getOrderStatus() != null) {
            sb.append("  • Trạng thái: ").append(getStatusLabel(f.getOrderStatus())).append("\n");
        }
        if (f.getPaymentMethod() != null) {
            sb.append("  • Phương thức thanh toán: ").append(getPaymentLabel(f.getPaymentMethod())).append("\n");
        }
        if (f.getKeyword() != null && !f.getKeyword().isEmpty()) {
            sb.append("  • Từ khóa: ").append(f.getKeyword()).append("\n");
        }
        sb.append("  • Sắp xếp: ").append(getSortLabel(f.getSortBy(), f.getSortDir()));
        return sb.toString().trim();
    }

    private static String getDatePresetLabel(String preset) {
        if (preset == null || preset.isEmpty()) return null;
        return switch (preset) {
            case "today" -> "Hôm nay";
            case "yesterday" -> "Hôm qua";
            case "7days" -> "7 ngày qua";
            case "30days" -> "30 ngày qua";
            case "this_month" -> "Tháng này";
            case "last_month" -> "Tháng trước";
            case "this_year" -> "Năm nay";
            case "1year" -> "1 năm qua";
            default -> null;
        };
    }

    private static String getStatusLabel(String s) {
        if (s == null) return "";
        return switch (s) {
            case "PENDING" -> "Chờ thanh toán";
            case "PAID" -> "Đã thanh toán";
            case "COMPLETED" -> "Hoàn thành";
            case "CANCELLED" -> "Đã hủy";
            default -> s;
        };
    }

    private static String getPaymentLabel(String s) {
        if (s == null) return "";
        return switch (s) {
            case "CASH" -> "Tiền mặt";
            case "CARD" -> "Thẻ";
            case "TRANSFER" -> "Chuyển khoản";
            default -> s;
        };
    }

    private static String getSortLabel(String sortBy, String sortDir) {
        String by = "Ngày tạo";
        if ("total_amount".equals(sortBy)) by = "Tổng tiền";
        String dir = "Mới nhất / Cao nhất";
        if ("ASC".equalsIgnoreCase(sortDir)) dir = "Cũ nhất / Thấp nhất";
        return by + " - " + dir;
    }

    private boolean isOwnerOrManager(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null) { response.sendError(401); return false; }
        Employee u = (Employee) session.getAttribute("currentUser");
        if (u == null) { response.sendError(401); return false; }
        String role = u.getRoleName();
        if (role == null || (!role.equalsIgnoreCase("Owner") && !role.equalsIgnoreCase("Admin") && !role.equalsIgnoreCase("StoreManager"))) {
            response.sendError(403, "Bạn không có quyền truy cập chức năng này.");
            return false;
        }
        return true;
    }

    private void applyBranchFilterForManager(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return;
        Employee u = (Employee) session.getAttribute("currentUser");
        if (u != null && "StoreManager".equalsIgnoreCase(u.getRoleName())) {
            if (request.getParameter("branchId") == null) {
                request.setAttribute("managerBranchId", u.getBranchID());
            }
        }
    }

    private String buildBaseQueryString(OrderReportFilter f, String datePreset) {
        StringBuilder sb = new StringBuilder();
        if (datePreset != null) sb.append("&datePreset=").append(datePreset);
        if (f.getDateFrom() != null) sb.append("&dateFrom=").append(f.getDateFrom());
        if (f.getDateTo() != null) sb.append("&dateTo=").append(f.getDateTo());
        if (f.getEmpId() != null) sb.append("&empId=").append(f.getEmpId());
        if (f.getBranchId() != null) sb.append("&branchId=").append(f.getBranchId());
        if (f.getOrderId() != null) sb.append("&orderId=").append(f.getOrderId());
        if (f.getCustomerId() != null) sb.append("&customerId=").append(f.getCustomerId());
        if (f.getOrderStatus() != null) sb.append("&orderStatus=").append(f.getOrderStatus());
        if (f.getPaymentMethod() != null) sb.append("&paymentMethod=").append(f.getPaymentMethod());
        if (f.getKeyword() != null) sb.append("&keyword=").append(f.getKeyword());
        if (f.getSortBy() != null) sb.append("&sortBy=").append(f.getSortBy());
        if (f.getSortDir() != null) sb.append("&sortDir=").append(f.getSortDir());
        return sb.length() > 0 ? sb.substring(1) : "";
    }

    private String trim(String s) { return s == null || s.trim().isEmpty() ? null : s.trim(); }
    private int parseInt(String s, int def) { try { return Integer.parseInt(s); } catch (Exception e) { return def; } }
    private Integer parseIntNull(String s) { try { return Integer.parseInt(s); } catch (Exception e) { return null; } }
    private LocalDate parseDate(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try { return LocalDate.parse(s.trim(), DateTimeFormatter.ISO_LOCAL_DATE); } catch (DateTimeParseException e) { return null; }
    }
}
