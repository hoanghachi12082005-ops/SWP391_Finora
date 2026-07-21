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
import model.Employee;
import model.Order;
import model.OrderReportFilter;
import util.pagination.PaginationHelper;
import util.pagination.PaginationHelper.PageResult;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@WebServlet(name = "OrderReportController", urlPatterns = {"/reports/orders", "/reports/orders/export-excel"})
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

        OrderReportFilter f = buildFilter(request);
        int page = parseInt(request.getParameter("page"), 1);
        int sizeValue = parseInt(request.getParameter("sizeValue"), 30);

        int total = orderReportDAO.countOrders(f);
        PageResult pr = PaginationHelper.compute(total, page, sizeValue);
        pr.setAttributes(request);

        List<Order> orders = orderReportDAO.searchOrders(f, pr.getCurrentPage(), pr.getPageSize());
        request.setAttribute("orders", orders);
        request.setAttribute("branches", userManagementDao.getAllBranches());
        request.setAttribute("employees", userManagementDao.getEmployees(null, null, null, null, 1, 9999));
        request.setAttribute("baseUrl", request.getContextPath() + "/reports/orders");

        // Preserve filter params for JSP
        request.setAttribute("filter", f);
        request.setAttribute("datePreset", request.getParameter("datePreset"));
        request.setAttribute("pageTitle", "Trung tâm báo cáo đơn hàng");
        request.setAttribute("pageSubtitle", "Tra cứu, lọc và xuất báo cáo đơn hàng");
        request.setAttribute("managerBranchId", request.getAttribute("managerBranchId"));

        forward(request, response, "reports/order-report");
    }

    private OrderReportFilter buildFilter(HttpServletRequest request) {
        OrderReportFilter f = new OrderReportFilter();

        String datePreset = trim(request.getParameter("datePreset"));
        if (datePreset != null) {
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
        List<Order> orders = orderReportDAO.searchOrders(f, 1, Integer.MAX_VALUE);

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Order Report");
            Row header = sheet.createRow(0);
            String[] cols = {"Mã đơn", "Chi nhánh", "Nhân viên", "Khách hàng", "Tổng tiền", "Phương thức", "Trạng thái", "Ngày tạo"};
            for (int i = 0; i < cols.length; i++) {
                header.createCell(i).setCellValue(cols[i]);
            }
            int r = 1;
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

            String fileName = "OrderReport_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            OutputStream out = response.getOutputStream();
            wb.write(out);
            out.flush();
        }
    }

    private boolean isOwnerOrManager(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null) { response.sendError(401); return false; }
        Employee u = (Employee) session.getAttribute("currentUser");
        if (u == null) { response.sendError(401); return false; }
        String role = u.getRoleName();
        if (role == null || (!role.equalsIgnoreCase("Owner") && !role.equalsIgnoreCase("StoreManager"))) {
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

    private String trim(String s) { return s == null ? null : s.trim(); }
    private int parseInt(String s, int def) { try { return Integer.parseInt(s); } catch (Exception e) { return def; } }
    private Integer parseIntNull(String s) { try { return Integer.parseInt(s); } catch (Exception e) { return null; } }
    private LocalDate parseDate(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try { return LocalDate.parse(s.trim(), DateTimeFormatter.ISO_LOCAL_DATE); } catch (DateTimeParseException e) { return null; }
    }
}
