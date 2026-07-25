package controller.report;

import controller.common.BaseController;
import dao.report.InventoryReportDAO;
import dao.report.CustomerLoyaltyReportDAO;
import dao.user.UserManagementDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import model.Employee;
import util.pagination.PaginationHelper;
import util.pagination.PaginationHelper.PageResult;
import util.report.ExcelExportUtil;
import util.report.ExportUtil;
import util.report.PdfReportUtil;

@WebServlet(name = "ReportController", urlPatterns = {
        "/reports/employee-sales",
        "/reports/employee-sales-detail",
        "/reports/employee-sales-detail-export",
        "/reports/employee-sales-preview",
        "/reports/employee-sales-export",
        "/reports/employee-sales-export-excel",
        "/reports/customer-loyal",
        "/reports/customer-loyal-preview",
        "/reports/customer-loyal-export",
        "/reports/customer-loyal-export-excel",
        "/reports/sales-by-store",
        "/reports/sales-by-store-preview",
        "/reports/sales-by-store-export",
        "/reports/sales-by-store-export-excel",
        "/reports/inventory",
        "/reports/inventory-preview",
        "/reports/inventory-export",
        "/reports/inventory-export-excel"
    })
public class ReportController extends BaseController {

    private InventoryReportDAO inventoryReportDAO;
    private CustomerLoyaltyReportDAO customerLoyaltyReportDAO;
    private UserManagementDao userManagementDao;

    @Override
    public void init() throws ServletException {
        inventoryReportDAO = new InventoryReportDAO();
        customerLoyaltyReportDAO = new CustomerLoyaltyReportDAO();
        userManagementDao = new UserManagementDao();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        String ctx = request.getContextPath();

        // Redirect consolidated order reports to new Order Report Center
        if ("/reports/employee-sales".equals(path)
                || "/reports/employee-sales-preview".equals(path)
                || "/reports/employee-sales-detail".equals(path)
                || "/reports/employee-sales-detail-export".equals(path)
                || "/reports/employee-sales-export".equals(path)
                || "/reports/employee-sales-export-excel".equals(path)
                || "/reports/sales-by-store".equals(path)
                || "/reports/sales-by-store-preview".equals(path)
                || "/reports/sales-by-store-export".equals(path)
                || "/reports/sales-by-store-export-excel".equals(path)
                ) {
            response.sendRedirect(ctx + "/reports/orders");
            return;
        }

        if ("/reports/inventory".equals(path)) {
            if (!isOwnerOrManager(request, response)) return;
            applyBranchFilterForManager(request);
            loadInventoryReport(request);
            forward(request, response, "reports/inventory");
            return;
        }

        if ("/reports/inventory-preview".equals(path)) {
            if (!isOwnerOrManager(request, response)) return;
            applyBranchFilterForManager(request);
            loadInventoryPreview(request);
            forward(request, response, "reports/inventory-preview");
            return;
        }

        if ("/reports/inventory-export".equals(path)) {
            if (!isOwnerOrManager(request, response)) return;
            applyBranchFilterForManager(request);
            exportInventoryPdf(request, response);
            return;
        }

        if ("/reports/inventory-export-excel".equals(path)) {
            if (!isOwnerOrManager(request, response)) return;
            applyBranchFilterForManager(request);
            exportInventoryExcel(request, response);
            return;
        }

        if ("/reports/customer-loyal".equals(path)) {
            if (!isOwnerOrManager(request, response)) return;
            loadCustomerLoyaltyReport(request);
            forward(request, response, "reports/customer-loyal");
            return;
        }

        if ("/reports/customer-loyal-preview".equals(path)) {
            if (!isOwnerOrManager(request, response)) return;
            loadCustomerLoyaltyPreview(request);
            forward(request, response, "reports/customer-loyal-preview");
            return;
        }

        if ("/reports/customer-loyal-export".equals(path)) {
            if (!isOwnerOrManager(request, response)) return;
            exportCustomerLoyaltyPdf(request, response);
            return;
        }

        if ("/reports/customer-loyal-export-excel".equals(path)) {
            if (!isOwnerOrManager(request, response)) return;
            exportCustomerLoyaltyExcel(request, response);
            return;
        }

        switch (path) {
            default:
                response.sendRedirect(ctx + "/reports/orders");
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    private void loadInventoryReport(HttpServletRequest request) {
        String keyword = trim(request.getParameter("keyword"));
        String branchId = trim(request.getParameter("branchId"));
        if (isBlank(branchId) && request.getAttribute("managerBranchId") != null) {
            branchId = String.valueOf(request.getAttribute("managerBranchId"));
        }

        int page = parseInt(request.getParameter("page"), 1);
        int sizeValue = parseInt(request.getParameter("sizeValue"), 30);

        int totalProducts = inventoryReportDAO.countInventoryReport(keyword, branchId);
        PageResult pr = PaginationHelper.compute(totalProducts, page, sizeValue);
        pr.setAttributes(request);

        request.setAttribute(
                "inventoryItems",
                inventoryReportDAO.getInventoryReport(
                        keyword, branchId, pr.getCurrentPage(), pr.getPageSize())
        );
        request.setAttribute(
                "reportOverview",
                inventoryReportDAO.getReportOverview(keyword, branchId)
        );
        request.setAttribute("branches", userManagementDao.getAllBranches());

        request.setAttribute("pageTitle", "Báo cáo tồn kho");
        request.setAttribute(
                "pageSubtitle",
                "Xem số lượng tồn kho và giá trị tồn kho của các chi nhánh"
        );
        request.setAttribute("baseUrl", request.getContextPath() + "/reports/inventory");

        request.setAttribute("keyword", keyword);
        request.setAttribute("branchFilter", parseInt(branchId, -1));
        request.setAttribute("totalProducts", totalProducts);
    }

    private void loadCustomerLoyaltyReport(HttpServletRequest request) {
        String keyword = trim(request.getParameter("keyword"));
        applyBranchFilterForManager(request);
        Integer branchId = resolveCustomerLoyaltyBranch(request);

        String datePreset = trim(request.getParameter("datePreset"));
        LocalDate dateFrom = null;
        LocalDate dateTo = null;
        if (datePreset != null && !datePreset.isEmpty()) {
            LocalDate[] range = resolveDatePreset(datePreset);
            dateFrom = range[0];
            dateTo = range[1];
        } else {
            dateFrom = parseDate(request.getParameter("dateFrom"));
            dateTo = parseDate(request.getParameter("dateTo"));
        }

        int page = parseInt(request.getParameter("page"), 1);
        int sizeValue = parseInt(request.getParameter("sizeValue"), 30);

        int totalCustomers = customerLoyaltyReportDAO.countCustomerLoyaltyReport(keyword, branchId, dateFrom, dateTo);
        PageResult pr = PaginationHelper.compute(totalCustomers, page, sizeValue);
        pr.setAttributes(request);

        request.setAttribute(
                "customerReports",
                customerLoyaltyReportDAO.getCustomerLoyaltyReport(
                        keyword, pr.getCurrentPage(), pr.getPageSize(), branchId, dateFrom, dateTo)
        );
        request.setAttribute(
                "reportOverview",
                customerLoyaltyReportDAO.getReportOverview(keyword, branchId, dateFrom, dateTo)
        );

        Employee currentUser = (Employee) request.getSession().getAttribute("currentUser");
        String role = currentUser.getRoleName();
        boolean isOwner = "Owner".equalsIgnoreCase(role) || "Admin".equalsIgnoreCase(role);

        request.setAttribute("showBranch", isOwner);
        request.setAttribute("branches", userManagementDao.getAllBranches());
        request.setAttribute("branchFilter", branchId);
        request.setAttribute("datePreset", datePreset);
        request.setAttribute("dateFrom", dateFrom);
        request.setAttribute("dateTo", dateTo);

        request.setAttribute("pageTitle", "Báo cáo khách hàng thân thiết");
        request.setAttribute(
                "pageSubtitle",
                "Xem thống kê chi tiêu và tích điểm của khách hàng thân thiết"
        );
        request.setAttribute("baseUrl", request.getContextPath() + "/reports/customer-loyal");

        request.setAttribute("keyword", keyword);
        request.setAttribute("totalCustomers", totalCustomers);
    }

    private void exportInventoryExcel(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            String keyword = trim(request.getParameter("keyword"));
            String branchId = trim(request.getParameter("branchId"));
            if (isBlank(branchId) && request.getAttribute("managerBranchId") != null) {
                branchId = String.valueOf(request.getAttribute("managerBranchId"));
            }

            var allData = inventoryReportDAO.getInventoryReport(keyword, branchId, 1, 1000000);
            var overview = inventoryReportDAO.getReportOverview(keyword, branchId);

            String generatedBy = "Unknown";
            HttpSession session = request.getSession(false);
            if (session != null) {
                Employee currentUser = (Employee) session.getAttribute("currentUser");
                if (currentUser != null) generatedBy = currentUser.getFullName();
            }

            final int excelBranchId = parseInt(branchId, -1);
            String branchName = null;
            if (!isBlank(branchId)) {
                var branches = userManagementDao.getAllBranches();
                if (branches != null) {
                    branchName = branches.stream()
                            .filter(b -> b.getBranchID() == excelBranchId)
                            .findFirst()
                            .map(b -> b.getName())
                            .orElse(null);
                }
            }

            byte[] excelBytes = ExcelExportUtil.generateInventoryReport(
                    generatedBy, allData, overview, keyword, branchName);

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=\"" +
                    ExportUtil.buildExportFileName("InventoryReport") + ".xlsx\"");
            response.setContentLength(excelBytes.length);
            response.getOutputStream().write(excelBytes);
            response.getOutputStream().flush();
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(500, "Excel export failed: " + e.getMessage());
        }
    }

    private void exportCustomerLoyaltyExcel(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            String keyword = trim(request.getParameter("keyword"));
            applyBranchFilterForManager(request);
            Integer branchId = resolveCustomerLoyaltyBranch(request);

            String datePreset = trim(request.getParameter("datePreset"));
            LocalDate dateFrom = null;
            LocalDate dateTo = null;
            if (datePreset != null && !datePreset.isEmpty()) {
                LocalDate[] range = resolveDatePreset(datePreset);
                dateFrom = range[0];
                dateTo = range[1];
            } else {
                dateFrom = parseDate(request.getParameter("dateFrom"));
                dateTo = parseDate(request.getParameter("dateTo"));
            }

            var allData = customerLoyaltyReportDAO.getCustomerLoyaltyReport(keyword, 1, 1000000, branchId, dateFrom, dateTo);
            var overview = customerLoyaltyReportDAO.getReportOverview(keyword, branchId, dateFrom, dateTo);

            String generatedBy = "Unknown";
            HttpSession session = request.getSession(false);
            if (session != null) {
                Employee currentUser = (Employee) session.getAttribute("currentUser");
                if (currentUser != null) generatedBy = currentUser.getFullName();
            }

            String branchName = null;
            if (branchId != null && branchId > 0) {
                var branches = userManagementDao.getAllBranches();
                if (branches != null) {
                    branchName = branches.stream()
                            .filter(b -> b.getBranchID() == branchId)
                            .findFirst()
                            .map(b -> b.getName())
                            .orElse(null);
                }
            }

            byte[] excelBytes = ExcelExportUtil.generateCustomerLoyaltyReport(
                    generatedBy, allData, overview, keyword, branchName, dateFrom, dateTo);

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=\"" +
                    ExportUtil.buildExportFileName("CustomerLoyaltyReport") + ".xlsx\"");
            response.setContentLength(excelBytes.length);
            response.getOutputStream().write(excelBytes);
            response.getOutputStream().flush();
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(500, "Excel export failed: " + e.getMessage());
        }
    }

    private boolean isOwnerOrManager(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }

        Employee currentUser = (Employee) session.getAttribute("currentUser");
        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }

        String role = currentUser.getRoleName();
        boolean isOwner = "Owner".equalsIgnoreCase(role) || "Admin".equalsIgnoreCase(role);
        boolean isManager = "StoreManager".equalsIgnoreCase(role) || "Store Manager".equalsIgnoreCase(role);

        if (!isOwner && !isManager) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied. Owner or Manager only.");
            return false;
        }

        return true;
    }

    /**
     * Resolve branch filter for Customer Loyalty report.
     * Store Managers are always locked to their own branch (URL param bypass blocked).
     * Owners use optional URL param or null (all branches).
     */
    private Integer resolveCustomerLoyaltyBranch(HttpServletRequest request) {
        // 1. Store Manager: forced to own branch (security: ignore URL override)
        HttpSession session = request.getSession(false);
        if (session != null) {
            Employee currentUser = (Employee) session.getAttribute("currentUser");
            if (currentUser != null) {
                String role = currentUser.getRoleName();
                if ("StoreManager".equalsIgnoreCase(role) || "Store Manager".equalsIgnoreCase(role)) {
                    return currentUser.getBranchID();
                }
            }
        }
        // 2. Non-manager: use URL param or applyBranchFilterForManager fallback
        String branchIdStr = trim(request.getParameter("branchId"));
        if (isBlank(branchIdStr) && request.getAttribute("managerBranchId") != null) {
            return (Integer) request.getAttribute("managerBranchId");
        }
        Integer branchId = parseInt(branchIdStr, -1);
        return (branchId <= 0) ? null : branchId;
    }

    private void applyBranchFilterForManager(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return;
        Employee currentUser = (Employee) session.getAttribute("currentUser");
        if (currentUser == null) return;
        String role = currentUser.getRoleName();
        if ("StoreManager".equalsIgnoreCase(role) || "Store Manager".equalsIgnoreCase(role)) {
            if (request.getParameter("branchId") == null) {
                request.setAttribute("managerBranchId", currentUser.getBranchID());
            }
        }
    }

    private int parseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private LocalDate parseDate(String value) {
        if (isBlank(value)) {
            return null;
        }
        try {
            return LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private void loadInventoryPreview(HttpServletRequest request) {
        String keyword = trim(request.getParameter("keyword"));
        String branchId = trim(request.getParameter("branchId"));
        if (isBlank(branchId) && request.getAttribute("managerBranchId") != null) {
            branchId = String.valueOf(request.getAttribute("managerBranchId"));
        }

        request.setAttribute(
                "allReports",
                inventoryReportDAO.getInventoryReport(
                        keyword, branchId, 1, 1000000)
        );
        request.setAttribute(
                "reportOverview",
                inventoryReportDAO.getReportOverview(keyword, branchId)
        );

        final int finalBranchId = parseInt(branchId, -1);
        String branchName = null;
        if (!isBlank(branchId)) {
            var branches = userManagementDao.getAllBranches();
            if (branches != null) {
                branchName = branches.stream()
                        .filter(b -> b.getBranchID() == finalBranchId)
                        .findFirst()
                        .map(b -> b.getName())
                        .orElse(null);
            }
        }
        request.setAttribute("reportBranchName", branchName);
        request.setAttribute("pageTitle", "Xem trước báo cáo tồn kho");
    }

    private void exportInventoryPdf(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            String keyword = trim(request.getParameter("keyword"));
            String branchId = trim(request.getParameter("branchId"));
            if (isBlank(branchId) && request.getAttribute("managerBranchId") != null) {
                branchId = String.valueOf(request.getAttribute("managerBranchId"));
            }

            var allData = inventoryReportDAO.getInventoryReport(keyword, branchId, 1, 1000000);
            var overview = inventoryReportDAO.getReportOverview(keyword, branchId);

            String generatedBy = "Unknown";
            HttpSession session = request.getSession(false);
            if (session != null) {
                Employee currentUser = (Employee) session.getAttribute("currentUser");
                if (currentUser != null) {
                    generatedBy = currentUser.getFullName();
                }
            }

            final int pdfBranchId = parseInt(branchId, -1);
            String branchName = null;
            if (!isBlank(branchId)) {
                var branches = userManagementDao.getAllBranches();
                if (branches != null) {
                    branchName = branches.stream()
                            .filter(b -> b.getBranchID() == pdfBranchId)
                            .findFirst()
                            .map(b -> b.getName())
                            .orElse(null);
                }
            }

            byte[] pdfBytes = PdfReportUtil.generateInventoryReport(
                    "Finora Retail", generatedBy, allData, overview,
                    keyword, branchName);

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "inline; filename=\"InventoryReport.pdf\"");
            response.setContentLength(pdfBytes.length);
            response.getOutputStream().write(pdfBytes);
            response.getOutputStream().flush();
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(500, e.getMessage());
        }
    }

    private void loadCustomerLoyaltyPreview(HttpServletRequest request) {
        String keyword = trim(request.getParameter("keyword"));
        applyBranchFilterForManager(request);
        Integer branchId = resolveCustomerLoyaltyBranch(request);

        String datePreset = trim(request.getParameter("datePreset"));
        LocalDate dateFrom = null;
        LocalDate dateTo = null;
        if (datePreset != null && !datePreset.isEmpty()) {
            LocalDate[] range = resolveDatePreset(datePreset);
            dateFrom = range[0];
            dateTo = range[1];
        } else {
            dateFrom = parseDate(request.getParameter("dateFrom"));
            dateTo = parseDate(request.getParameter("dateTo"));
        }

        request.setAttribute(
                "allReports",
                customerLoyaltyReportDAO.getCustomerLoyaltyReport(
                        keyword, 1, 1000000, branchId, dateFrom, dateTo)
        );
        request.setAttribute(
                "reportOverview",
                customerLoyaltyReportDAO.getReportOverview(keyword, branchId, dateFrom, dateTo)
        );

        Employee currentUser = (Employee) request.getSession().getAttribute("currentUser");
        String role = currentUser.getRoleName();
        boolean isOwner = "Owner".equalsIgnoreCase(role) || "Admin".equalsIgnoreCase(role);

        request.setAttribute("showBranch", isOwner);
        request.setAttribute("branches", userManagementDao.getAllBranches());
        request.setAttribute("branchFilter", branchId);
        request.setAttribute("datePreset", datePreset);
        request.setAttribute("dateFrom", dateFrom);
        request.setAttribute("dateTo", dateTo);

        request.setAttribute("pageTitle", "Xem trước báo cáo khách hàng thân thiết");
    }

    private void exportCustomerLoyaltyPdf(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            String keyword = trim(request.getParameter("keyword"));
            applyBranchFilterForManager(request);
            Integer branchId = resolveCustomerLoyaltyBranch(request);

            String datePreset = trim(request.getParameter("datePreset"));
            LocalDate dateFrom = null;
            LocalDate dateTo = null;
            if (datePreset != null && !datePreset.isEmpty()) {
                LocalDate[] range = resolveDatePreset(datePreset);
                dateFrom = range[0];
                dateTo = range[1];
            } else {
                dateFrom = parseDate(request.getParameter("dateFrom"));
                dateTo = parseDate(request.getParameter("dateTo"));
            }

            var allData = customerLoyaltyReportDAO.getCustomerLoyaltyReport(keyword, 1, 1000000, branchId, dateFrom, dateTo);
            var overview = customerLoyaltyReportDAO.getReportOverview(keyword, branchId, dateFrom, dateTo);

            String generatedBy = "Unknown";
            HttpSession session = request.getSession(false);
            if (session != null) {
                Employee currentUser = (Employee) session.getAttribute("currentUser");
                if (currentUser != null) {
                    generatedBy = currentUser.getFullName();
                }
            }

            String branchName = null;
            if (branchId != null && branchId > 0) {
                var branches = userManagementDao.getAllBranches();
                if (branches != null) {
                    branchName = branches.stream()
                            .filter(b -> b.getBranchID() == branchId)
                            .findFirst()
                            .map(b -> b.getName())
                            .orElse(null);
                }
            }

            byte[] pdfBytes = PdfReportUtil.generateCustomerLoyaltyReport(
                    "Finora Retail", generatedBy, allData, overview, keyword, branchName, dateFrom, dateTo, datePreset);

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "inline; filename=\"CustomerLoyaltyReport.pdf\"");
            response.setContentLength(pdfBytes.length);
            response.getOutputStream().write(pdfBytes);
            response.getOutputStream().flush();
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(500, e.getMessage());
        }
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

}