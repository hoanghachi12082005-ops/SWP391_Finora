package controller.report;

import controller.common.BaseController;
import dao.user.UserManagementDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Employee;
import model.SalesTransaction;
import model.SalesTransactionFilter;
import model.SalesTransactionKpi;
import service.finance.PaymentService;
import util.pagination.PaginationHelper;
import util.pagination.PaginationHelper.PageResult;
import util.report.ExcelExportUtil;
import util.report.ExportUtil;
import util.report.PdfReportUtil;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@WebServlet(name = "SalesTransactionReportController", urlPatterns = {
    "/reports/finance-detail",
    "/reports/finance-detail/export-excel",
    "/reports/finance-detail/export-pdf",
    "/reports/finance-detail-preview",
    "/reports/finance-detail-export-excel"
})
public class SalesTransactionReportController extends BaseController {

    private PaymentService paymentService;
    private UserManagementDao userManagementDao;

    @Override
    public void init() throws ServletException {
        paymentService = new PaymentService();
        userManagementDao = new UserManagementDao();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isOwnerOrManager(request, response)) return;
        applyBranchFilterForManager(request);

        String path = request.getServletPath();

        // Redirect old bookmarked URLs
        if ("/reports/finance-detail-preview".equals(path)) {
            response.sendRedirect(request.getContextPath() + "/reports/finance-detail");
            return;
        }
        if ("/reports/finance-detail-export-excel".equals(path)) {
            response.sendRedirect(request.getContextPath() + "/reports/finance-detail/export-excel");
            return;
        }

        if ("/reports/finance-detail/export-excel".equals(path)) {
            exportExcel(request, response);
            return;
        }
        if ("/reports/finance-detail/export-pdf".equals(path)) {
            exportPdf(request, response);
            return;
        }

        SalesTransactionFilter f = buildFilter(request);
        SalesTransactionKpi kpi = paymentService.getTransactionKpi(f);
        request.setAttribute("kpi", kpi);

        int page = parseInt(request.getParameter("page"), 1);
        int sizeValue = parseInt(request.getParameter("sizeValue"), 30);
        int total = paymentService.countTransactions(f);
        PageResult pr = PaginationHelper.compute(total, page, sizeValue);
        pr.setAttributes(request);

        List<SalesTransaction> transactions = paymentService.searchTransactions(f, pr.getCurrentPage(), pr.getPageSize());
        request.setAttribute("transactions", transactions);
        request.setAttribute("branches", userManagementDao.getAllBranches());
        request.setAttribute("employees", userManagementDao.getEmployees(null, null, null, null, 1, 9999));
        request.setAttribute("transactionTypes", paymentService.getDistinctTransactionTypes());
        request.setAttribute("baseUrl", request.getContextPath() + "/reports/finance-detail");

        request.setAttribute("filter", f);
        request.setAttribute("datePreset", request.getParameter("datePreset"));
        request.setAttribute("managerBranchId", request.getAttribute("managerBranchId"));
        request.setAttribute("pageTitle", "Báo cáo giao dịch & doanh thu");
        request.setAttribute("pageSubtitle", "Xem doanh thu bán hàng và lịch sử giao dịch chi tiết trên toàn hệ thống.");

        forward(request, response, "reports/sales-transaction-report");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    private SalesTransactionFilter buildFilter(HttpServletRequest request) {
        SalesTransactionFilter f = new SalesTransactionFilter();
        String datePreset = trim(request.getParameter("datePreset"));
        if (datePreset != null && !datePreset.isEmpty()) {
            LocalDate[] range = resolveDatePreset(datePreset);
            f.setDateFrom(range[0]);
            f.setDateTo(range[1]);
        } else {
            f.setDateFrom(parseDate(request.getParameter("dateFrom")));
            f.setDateTo(parseDate(request.getParameter("dateTo")));
        }
        f.setTransactionCode(trim(request.getParameter("transactionCode")));
        f.setTransactionType(trim(request.getParameter("transactionType")));
        f.setOrderType(trim(request.getParameter("orderType")));
        f.setPaymentMethod(trim(request.getParameter("paymentMethod")));
        f.setAmountFrom(parseDoubleNull(request.getParameter("amountFrom")));
        f.setAmountTo(parseDoubleNull(request.getParameter("amountTo")));
        f.setBranchId(parseIntNull(request.getParameter("branchId")));
        f.setEmpId(parseIntNull(request.getParameter("empId")));
        f.setKeyword(trim(request.getParameter("keyword")));
        f.setSortBy(trim(request.getParameter("sortBy")));
        f.setSortDir(trim(request.getParameter("sortDir")));

        if (request.getAttribute("managerBranchId") != null) {
            f.setBranchId((Integer) request.getAttribute("managerBranchId"));
        }
        return f;
    }

    private LocalDate[] resolveDatePreset(String preset) {
        LocalDate today = LocalDate.now();
        return switch (preset) {
            case "today" -> new LocalDate[]{today, today};
            case "yesterday" -> new LocalDate[]{today.minusDays(1), today.minusDays(1)};
            case "this_week" -> new LocalDate[]{today.with(java.time.DayOfWeek.MONDAY), today};
            case "this_month" -> new LocalDate[]{today.withDayOfMonth(1), today};
            default -> new LocalDate[]{null, null};
        };
    }

    private void exportExcel(HttpServletRequest request, HttpServletResponse response) throws IOException {
        SalesTransactionFilter f = buildFilter(request);
        SalesTransactionKpi kpi = paymentService.getTransactionKpi(f);
        List<SalesTransaction> allData = paymentService.searchTransactions(f, 1, Integer.MAX_VALUE);

        Employee u = (Employee) request.getSession().getAttribute("currentUser");
        String generatedBy = u != null ? u.getFullName() : "Unknown";

        String filterDesc = buildFilterDescription(f);

        byte[] excelBytes = ExcelExportUtil.generateSalesTransactionReport(generatedBy, allData, kpi, filterDesc);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"" +
            ExportUtil.buildExportFileName("SalesTransactionReport") + ".xlsx\"");
        response.getOutputStream().write(excelBytes);
        response.getOutputStream().flush();
    }

    private void exportPdf(HttpServletRequest request, HttpServletResponse response) throws IOException {
        SalesTransactionFilter f = buildFilter(request);
        SalesTransactionKpi kpi = paymentService.getTransactionKpi(f);
        List<SalesTransaction> allData = paymentService.searchTransactions(f, 1, Integer.MAX_VALUE);

        Employee u = (Employee) request.getSession().getAttribute("currentUser");
        String generatedBy = u != null ? u.getFullName() : "Unknown";
        String filterDesc = buildFilterDescription(f);

        byte[] pdfBytes = PdfReportUtil.generateSalesTransactionReport("Finora", generatedBy, allData, kpi, filterDesc);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"" +
            ExportUtil.buildExportFileName("SalesTransactionReport") + ".pdf\"");
        response.getOutputStream().write(pdfBytes);
        response.getOutputStream().flush();
    }

    private String buildFilterDescription(SalesTransactionFilter f) {
        StringBuilder sb = new StringBuilder();
        if (f.getDatePreset() != null) sb.append("Khoảng thời gian: ").append(f.getDatePreset());
        else if (f.getDateFrom() != null || f.getDateTo() != null)
            sb.append("Từ: ").append(f.getDateFrom()).append(" Đến: ").append(f.getDateTo());
        if (f.getTransactionType() != null) sb.append(" | Loại: ").append(f.getTransactionType());
        if (f.getPaymentMethod() != null) sb.append(" | Thanh toán: ").append(f.getPaymentMethod());
        if (f.getBranchId() != null) sb.append(" | Chi nhánh: ").append(f.getBranchId());
        if (f.getEmpId() != null) sb.append(" | Nhân viên: ").append(f.getEmpId());
        if (f.getKeyword() != null) sb.append(" | Từ khóa: ").append(f.getKeyword());
        return sb.toString().trim();
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
            Integer bId = u.getBranchId() != null ? u.getBranchId() : u.getBranchID();
            request.setAttribute("managerBranchId", bId);
        }
    }

    private String trim(String s) { return s == null || s.trim().isEmpty() ? null : s.trim(); }
    private int parseInt(String s, int def) { try { return Integer.parseInt(s); } catch (Exception e) { return def; } }
    private Integer parseIntNull(String s) { try { return Integer.parseInt(s); } catch (Exception e) { return null; } }
    private Double parseDoubleNull(String s) { try { return Double.parseDouble(s); } catch (Exception e) { return null; } }
    private LocalDate parseDate(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try { return LocalDate.parse(s.trim(), DateTimeFormatter.ISO_LOCAL_DATE); } catch (DateTimeParseException e) { return null; }
    }
}
