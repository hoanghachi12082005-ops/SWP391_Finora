package controller.report;

import controller.common.BaseController;
import dao.report.EmployeeSalesReportDAO;
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
import model.Branch;
import model.Employee;
import model.EmployeeOverview;
import model.EmployeeSalesSummary;
import util.report.ExportUtil;
import util.report.PdfReportUtil;

@WebServlet(name = "ReportController", urlPatterns = {
        "/reports/employee-sales",
        "/reports/employee-sales-preview",
        "/reports/employee-sales-export",
        "/reports/customer-loyal",
        "/reports/sales-by-store",
        "/reports/inventory",
        "/reports/export"
})
public class ReportController extends BaseController {

    private EmployeeSalesReportDAO employeeSalesReportDAO;
    private UserManagementDao userManagementDao;

    @Override
    public void init() throws ServletException {
        employeeSalesReportDAO = new EmployeeSalesReportDAO();
        userManagementDao = new UserManagementDao();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();

        if ("/reports/employee-sales".equals(path)) {
            if (!isOwnerOrManager(request, response)) return;
            applyBranchFilterForManager(request);
            loadEmployeeSalesReport(request);
            forward(request, response, "reports/employee-sales");
            return;
        }

        if ("/reports/employee-sales-preview".equals(path)) {
            if (!isOwnerOrManager(request, response)) return;
            applyBranchFilterForManager(request);
            loadFullReportPreview(request);
            forward(request, response, "reports/employee-sales-preview");
            return;
        }

        if ("/reports/employee-sales-export".equals(path)) {
            if (!isOwnerOrManager(request, response)) return;
            applyBranchFilterForManager(request);
            exportEmployeeSalesPdf(request, response);
            return;
        }

        switch (path) {
            case "/reports/customer-loyal":
                if (!isOwnerOrManager(request, response)) return;
                forward(request, response, "reports/customer-loyal");
                break;
            case "/reports/sales-by-store":
                forward(request, response, "reports/sales-by-store");
                break;
            case "/reports/inventory":
                forward(request, response, "reports/inventory");
                break;
            case "/reports/export":
                forward(request, response, "reports/export");
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/reports/employee-sales");
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    private void loadEmployeeSalesReport(HttpServletRequest request) {
        String keyword = trim(request.getParameter("keyword"));
        String branchId = trim(request.getParameter("branchId"));
        if (isBlank(branchId) && request.getAttribute("managerBranchId") != null) {
            branchId = String.valueOf(request.getAttribute("managerBranchId"));
        }
        String dateFromRaw = trim(request.getParameter("dateFrom"));
        String dateToRaw = trim(request.getParameter("dateTo"));
        String pageSizeOption = getParam(request, "pageSize", "10");

        LocalDate dateFrom = parseDate(dateFromRaw);
        LocalDate dateTo = parseDate(dateToRaw);

        int totalEmployees = employeeSalesReportDAO.countEmployeeSalesReport(keyword, branchId);
        int pageSize = resolvePageSize(pageSizeOption, totalEmployees);
        int currentPage = parseInt(request.getParameter("page"), 1);

        if (currentPage < 1) {
            currentPage = 1;
        }

        int totalPages = (int) Math.ceil((double) totalEmployees / pageSize);
        if (totalPages < 1) {
            totalPages = 1;
        }
        if (currentPage > totalPages) {
            currentPage = totalPages;
        }

        request.setAttribute(
                "salesReports",
                employeeSalesReportDAO.getEmployeeSalesReport(
                        keyword, branchId, dateFrom, dateTo, currentPage, pageSize)
        );
        request.setAttribute(
                "reportOverview",
                employeeSalesReportDAO.getReportOverview(keyword, branchId, dateFrom, dateTo)
        );
        request.setAttribute("branches", userManagementDao.getAllBranches());

        request.setAttribute("pageTitle", "Employee Sales Report");
        request.setAttribute(
                "pageSubtitle",
                "View sales performance metrics for employees"
        );
        request.setAttribute("baseUrl", request.getContextPath() + "/reports/employee-sales");

        request.setAttribute("keyword", keyword);
        request.setAttribute("branchFilter", parseInt(branchId, -1));
        request.setAttribute("dateFrom", dateFromRaw);
        request.setAttribute("dateTo", dateToRaw);
        request.setAttribute("currentPage", currentPage);
        request.setAttribute("pageSize", pageSize);
        request.setAttribute("pageSizeOption", pageSizeOption);
        request.setAttribute("totalEmployees", totalEmployees);
        request.setAttribute("totalPages", totalPages);
    }

    private void loadFullReportPreview(HttpServletRequest request) {
        String keyword = trim(request.getParameter("keyword"));
        String branchId = trim(request.getParameter("branchId"));
        if (isBlank(branchId) && request.getAttribute("managerBranchId") != null) {
            branchId = String.valueOf(request.getAttribute("managerBranchId"));
        }
        String dateFromRaw = trim(request.getParameter("dateFrom"));
        String dateToRaw = trim(request.getParameter("dateTo"));

        LocalDate dateFrom = parseDate(dateFromRaw);
        LocalDate dateTo = parseDate(dateToRaw);

        List<EmployeeSalesSummary> allData = employeeSalesReportDAO.getAllEmployeeSalesReport(
                keyword, branchId, dateFrom, dateTo);
        EmployeeOverview overview = employeeSalesReportDAO.getReportOverview(
                keyword, branchId, dateFrom, dateTo);

        request.setAttribute("allSalesReports", allData);
        request.setAttribute("reportOverview", overview);
        request.setAttribute("branches", userManagementDao.getAllBranches());

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

        request.setAttribute("keyword", keyword);
        request.setAttribute("branchFilter", finalBranchId);
        request.setAttribute("dateFrom", dateFromRaw);
        request.setAttribute("dateTo", dateToRaw);
        request.setAttribute("pageTitle", "Employee Sales Report Preview");
    }

    private void exportEmployeeSalesPdf(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            String keyword = trim(request.getParameter("keyword"));
            String branchId = trim(request.getParameter("branchId"));
            if (isBlank(branchId) && request.getAttribute("managerBranchId") != null) {
                branchId = String.valueOf(request.getAttribute("managerBranchId"));
            }
            String dateFromRaw = trim(request.getParameter("dateFrom"));
            String dateToRaw = trim(request.getParameter("dateTo"));

            LocalDate dateFrom = parseDate(dateFromRaw);
            LocalDate dateTo = parseDate(dateToRaw);

            List<EmployeeSalesSummary> allData = employeeSalesReportDAO.getAllEmployeeSalesReport(
                    keyword, branchId, dateFrom, dateTo);
            EmployeeOverview overview = employeeSalesReportDAO.getReportOverview(
                    keyword, branchId, dateFrom, dateTo);

            String generatedBy = "Unknown";
            HttpSession session = request.getSession(false);
            if (session != null) {
                Employee currentUser = (Employee) session.getAttribute("currentUser");
                if (currentUser != null) {
                    generatedBy = currentUser.getFullName();
                }
            }

            String companyName = "Finora Retail";
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

            byte[] pdfBytes = PdfReportUtil.generateEmployeeSalesReport(
                    companyName, generatedBy, allData, overview,
                    keyword, branchName, dateFrom, dateTo);

            if (pdfBytes == null || pdfBytes.length == 0) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "PDF generation returned empty content.");
                return;
            }

            String fileName = ExportUtil.buildExportFileName("EmployeeSalesReport");

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "inline; filename=\"" + fileName + ".pdf\"");
            response.setContentLength(pdfBytes.length);
            response.getOutputStream().write(pdfBytes);
            response.getOutputStream().flush();

        } catch (Exception e) {
            e.printStackTrace();
            if (!response.isCommitted()) {
                response.reset();
                String msg = e.getMessage();
                if (e.getCause() != null) {
                    msg = e.getCause().getMessage() != null ? e.getCause().getMessage() : msg;
                }
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Failed to generate PDF: " + msg);
            }
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
        boolean isOwner = "Owner".equalsIgnoreCase(role);
        boolean isManager = "StoreManager".equalsIgnoreCase(role) || "Store Manager".equalsIgnoreCase(role);

        if (!isOwner && !isManager) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied. Owner or Manager only.");
            return false;
        }

        return true;
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

    private int resolvePageSize(String pageSizeOption, int totalRecords) {
        if (isBlank(pageSizeOption)) {
            return 10;
        }

        String option = pageSizeOption.trim().toLowerCase();
        if ("30p".equals(option) || "30%".equals(option) || "30".equals(option)) {
            return Math.max(1, (int) Math.ceil(totalRecords * 0.3));
        }
        if ("50p".equals(option) || "50%".equals(option) || "50".equals(option)) {
            return Math.max(1, (int) Math.ceil(totalRecords * 0.5));
        }

        int size = parseInt(option, 10);
        if (size != 5 && size != 10 && size != 20) {
            size = 10;
        }
        return size;
    }

    private String getParam(HttpServletRequest request, String name, String defaultValue) {
        String value = request.getParameter(name);
        return isBlank(value) ? defaultValue : value.trim();
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
}
