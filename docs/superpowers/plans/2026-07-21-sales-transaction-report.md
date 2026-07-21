# Sales & Transaction Report Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace `/reports/finance-detail` redirect with a fully implemented Sales & Transaction Report in the Reports module.

**Architecture:** New `SalesTransactionReportController` uses existing `PaymentService` for payment data + new `SalesTransactionReportDAO` for report-specific aggregation queries. JSP partials follow Order Report patterns. Excel/PDF receive pre-computed data from Service.

**Tech Stack:** Jakarta EE Servlets, JSP/JSTL, SQL Server, Apache POI, OpenPDF (iText)

## Global Constraints
- Do NOT duplicate SQL queries
- Do NOT modify PaymentDAO — create new SalesTransactionReportDAO instead
- KPI computed in minimal SQL (1 payment query + 1 order count query)
- Excel/PDF utilities must NOT query database — only render data from Service
- sortBy whitelist enforced in DAO: `payment_date`, `payment_amount`, `PaymentType`, `branch_name`, `employee_name`
- Transaction types read from `SELECT DISTINCT PaymentType FROM payment` — no hardcode
- All filters must be reused across KPI, table, export

---

### Task 1: Create Model Classes

**Files:**
- Create: `src/main/java/model/SalesTransactionFilter.java`
- Create: `src/main/java/model/SalesTransactionKpi.java`
- Create: `src/main/java/model/SalesTransaction.java`

**Interfaces:**
- Produces: `SalesTransactionFilter` (used by DAO, Controller, JSP), `SalesTransactionKpi` (used by Controller, JSP, export), `SalesTransaction` (used by DAO, Controller, JSP, export)

- [ ] **Step 1: Create `SalesTransactionFilter.java`**

```java
package model;

import java.time.LocalDate;

public class SalesTransactionFilter {
    private String datePreset;
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private String transactionCode;
    private String transactionType;
    private String paymentMethod;
    private Double amountFrom;
    private Double amountTo;
    private Integer branchId;
    private Integer empId;
    private String keyword;
    private String sortBy;
    private String sortDir;

    public SalesTransactionFilter() {}

    public String getDatePreset() { return datePreset; }
    public void setDatePreset(String datePreset) { this.datePreset = datePreset; }
    public LocalDate getDateFrom() { return dateFrom; }
    public void setDateFrom(LocalDate dateFrom) { this.dateFrom = dateFrom; }
    public LocalDate getDateTo() { return dateTo; }
    public void setDateTo(LocalDate dateTo) { this.dateTo = dateTo; }
    public String getTransactionCode() { return transactionCode; }
    public void setTransactionCode(String transactionCode) { this.transactionCode = transactionCode; }
    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public Double getAmountFrom() { return amountFrom; }
    public void setAmountFrom(Double amountFrom) { this.amountFrom = amountFrom; }
    public Double getAmountTo() { return amountTo; }
    public void setAmountTo(Double amountTo) { this.amountTo = amountTo; }
    public Integer getBranchId() { return branchId; }
    public void setBranchId(Integer branchId) { this.branchId = branchId; }
    public Integer getEmpId() { return empId; }
    public void setEmpId(Integer empId) { this.empId = empId; }
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public String getSortBy() { return sortBy; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy; }
    public String getSortDir() { return sortDir; }
    public void setSortDir(String sortDir) { this.sortDir = sortDir; }
}
```

- [ ] **Step 2: Create `SalesTransactionKpi.java`**

```java
package model;

public class SalesTransactionKpi {
    private int totalTransactions;
    private double totalRevenue;
    private double totalExpense;
    private double netCashFlow;
    private double avgTransactionValue;
    private int totalSalesOrders;

    public int getTotalTransactions() { return totalTransactions; }
    public void setTotalTransactions(int v) { this.totalTransactions = v; }
    public double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(double v) { this.totalRevenue = v; }
    public double getTotalExpense() { return totalExpense; }
    public void setTotalExpense(double v) { this.totalExpense = v; }
    public double getNetCashFlow() { return netCashFlow; }
    public void setNetCashFlow(double v) { this.netCashFlow = v; }
    public double getAvgTransactionValue() { return avgTransactionValue; }
    public void setAvgTransactionValue(double v) { this.avgTransactionValue = v; }
    public int getTotalSalesOrders() { return totalSalesOrders; }
    public void setTotalSalesOrders(int v) { this.totalSalesOrders = v; }
}
```

- [ ] **Step 3: Create `SalesTransaction.java`**

```java
package model;

import java.sql.Timestamp;

public class SalesTransaction {
    private int id;
    private String transactionCode;
    private Timestamp paymentDate;
    private String transactionType;
    private String paymentMethod;
    private double amount;
    private String description;
    private String branchName;
    private String employeeName;
    private String status;
    private Integer orderId;

    public int getId() { return id; }
    public void setId(int v) { this.id = v; }
    public String getTransactionCode() { return transactionCode; }
    public void setTransactionCode(String v) { this.transactionCode = v; }
    public Timestamp getPaymentDate() { return paymentDate; }
    public void setPaymentDate(Timestamp v) { this.paymentDate = v; }
    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String v) { this.transactionType = v; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String v) { this.paymentMethod = v; }
    public double getAmount() { return amount; }
    public void setAmount(double v) { this.amount = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }
    public String getBranchName() { return branchName; }
    public void setBranchName(String v) { this.branchName = v; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String v) { this.employeeName = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer v) { this.orderId = v; }
}
```

---

### Task 2: Create SalesTransactionReportDAO

**Files:**
- Create: `src/main/java/dao/report/SalesTransactionReportDAO.java`

**Interfaces:**
- Consumes: `SalesTransactionFilter`, `SalesTransactionKpi`, `SalesTransaction`
- Produces: `searchTransactions(f, page, pageSize) → List<SalesTransaction>`, `countTransactions(f) → int`, `calculateKpi(f) → SalesTransactionKpi`, `getDistinctTransactionTypes() → List<String>`, `countSalesOrders(f) → int`

- [ ] **Step 1: Create `SalesTransactionReportDAO.java`**

```java
package dao.report;

import model.SalesTransaction;
import model.SalesTransactionFilter;
import model.SalesTransactionKpi;
import util.database.DBContext;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SalesTransactionReportDAO {

    private static final String SELECT =
        "SELECT p.payment_id, p.transaction_code, p.payment_date, p.PaymentType, " +
        "p.payment_method, p.payment_amount, p.Description, p.payment_status, " +
        "p.order_id, e.fullName AS employeeName, b.branch_name AS branchName " +
        "FROM payment p " +
        "LEFT JOIN Employee e ON p.EmployeeID = e.emp_id " +
        "LEFT JOIN Branch b ON p.BranchID = b.branch_id";

    private static final java.util.Set<String> ALLOWED_SORT = java.util.Set.of(
        "payment_date", "payment_amount", "PaymentType", "branch_name", "employee_name"
    );

    public List<SalesTransaction> searchTransactions(SalesTransactionFilter f, int page, int pageSize) {
        List<SalesTransaction> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(SELECT);
        List<Object> params = new ArrayList<>();
        appendFilters(sql, params, f);

        String sortCol = "p.payment_date";
        if (f.getSortBy() != null && ALLOWED_SORT.contains(f.getSortBy())) {
            sortCol = "p." + f.getSortBy();
            if ("branch_name".equals(f.getSortBy())) sortCol = "b.branch_name";
            if ("employee_name".equals(f.getSortBy())) sortCol = "e.fullName";
        }
        String sortDir = "DESC";
        if ("ASC".equalsIgnoreCase(f.getSortDir())) sortDir = "ASC";

        sql.append(" ORDER BY ").append(sortCol).append(" ").append(sortDir)
           .append(", p.payment_id DESC");
        sql.append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            for (Object p : params) ps.setObject(idx++, p);
            ps.setInt(idx++, (page - 1) * pageSize);
            ps.setInt(idx, pageSize);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public int countTransactions(SalesTransactionFilter f) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM payment p ");
        sql.append("LEFT JOIN Employee e ON p.EmployeeID = e.emp_id ");
        List<Object> params = new ArrayList<>();
        appendFilters(sql, params, f);
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            for (Object p : params) ps.setObject(idx++, p);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public SalesTransactionKpi calculateKpi(SalesTransactionFilter f) {
        SalesTransactionKpi kpi = new SalesTransactionKpi();
        StringBuilder sql = new StringBuilder(
            "SELECT " +
            "COUNT(*) AS total_transactions, " +
            "COALESCE(SUM(CASE WHEN p.PaymentType = 'INCOME' THEN p.payment_amount ELSE 0 END), 0) AS total_revenue, " +
            "COALESCE(SUM(CASE WHEN p.PaymentType = 'EXPENSE' THEN p.payment_amount ELSE 0 END), 0) AS total_expense, " +
            "COALESCE(AVG(p.payment_amount), 0) AS avg_transaction_value " +
            "FROM payment p LEFT JOIN Employee e ON p.EmployeeID = e.emp_id"
        );
        List<Object> params = new ArrayList<>();
        appendFilters(sql, params, f);

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            for (Object p : params) ps.setObject(idx++, p);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    kpi.setTotalTransactions(rs.getInt("total_transactions"));
                    kpi.setTotalRevenue(rs.getDouble("total_revenue"));
                    kpi.setTotalExpense(rs.getDouble("total_expense"));
                    kpi.setAvgTransactionValue(rs.getDouble("avg_transaction_value"));
                    kpi.setNetCashFlow(kpi.getTotalRevenue() - kpi.getTotalExpense());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        kpi.setTotalSalesOrders(countSalesOrders(f));
        return kpi;
    }

    public List<String> getDistinctTransactionTypes() {
        List<String> types = new ArrayList<>();
        String sql = "SELECT DISTINCT PaymentType FROM payment ORDER BY PaymentType";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) types.add(rs.getString(1));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return types;
    }

    private int countSalesOrders(SalesTransactionFilter f) {
        StringBuilder sql = new StringBuilder(
            "SELECT COUNT(*) FROM [Order] WHERE order_type = 'SALE'"
        );
        List<Object> params = new ArrayList<>();
        if (f.getDateFrom() != null) {
            sql.append(" AND CAST(created_at AS DATE) >= ?");
            params.add(Date.valueOf(f.getDateFrom()));
        }
        if (f.getDateTo() != null) {
            sql.append(" AND CAST(created_at AS DATE) <= ?");
            params.add(Date.valueOf(f.getDateTo()));
        }
        if (f.getBranchId() != null) {
            sql.append(" AND branch_id = ?");
            params.add(f.getBranchId());
        }
        if (f.getEmpId() != null) {
            sql.append(" AND emp_id = ?");
            params.add(f.getEmpId());
        }
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            for (Object p : params) ps.setObject(idx++, p);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void appendFilters(StringBuilder sql, List<Object> params, SalesTransactionFilter f) {
        if (f.getDateFrom() != null) {
            sql.append(" AND CAST(p.payment_date AS DATE) >= ?");
            params.add(Date.valueOf(f.getDateFrom()));
        }
        if (f.getDateTo() != null) {
            sql.append(" AND CAST(p.payment_date AS DATE) <= ?");
            params.add(Date.valueOf(f.getDateTo()));
        }
        if (f.getTransactionCode() != null && !f.getTransactionCode().isBlank()) {
            sql.append(" AND p.transaction_code LIKE ?");
            params.add("%" + f.getTransactionCode().trim() + "%");
        }
        if (f.getTransactionType() != null && !f.getTransactionType().isBlank()) {
            sql.append(" AND p.PaymentType = ?");
            params.add(f.getTransactionType());
        }
        if (f.getPaymentMethod() != null && !f.getPaymentMethod().isBlank()) {
            sql.append(" AND p.payment_method = ?");
            params.add(f.getPaymentMethod());
        }
        if (f.getAmountFrom() != null) {
            sql.append(" AND p.payment_amount >= ?");
            params.add(f.getAmountFrom());
        }
        if (f.getAmountTo() != null) {
            sql.append(" AND p.payment_amount <= ?");
            params.add(f.getAmountTo());
        }
        if (f.getBranchId() != null) {
            sql.append(" AND p.BranchID = ?");
            params.add(f.getBranchId());
        }
        if (f.getEmpId() != null) {
            sql.append(" AND p.EmployeeID = ?");
            params.add(f.getEmpId());
        }
        if (f.getKeyword() != null && !f.getKeyword().isBlank()) {
            String kw = "%" + f.getKeyword().trim() + "%";
            sql.append(" AND (p.transaction_code LIKE ? OR p.Description LIKE ? OR e.fullName LIKE ?)");
            params.add(kw); params.add(kw); params.add(kw);
        }
    }

    private SalesTransaction mapRow(ResultSet rs) throws SQLException {
        SalesTransaction t = new SalesTransaction();
        t.setId(rs.getInt("payment_id"));
        t.setTransactionCode(rs.getString("transaction_code"));
        t.setPaymentDate(rs.getTimestamp("payment_date"));
        t.setTransactionType(rs.getString("PaymentType"));
        t.setPaymentMethod(rs.getString("payment_method"));
        t.setAmount(rs.getDouble("payment_amount"));
        t.setDescription(rs.getString("Description"));
        t.setStatus(rs.getString("payment_status"));
        t.setOrderId(rs.getObject("order_id") != null ? rs.getInt("order_id") : null);
        t.setEmployeeName(rs.getString("employeeName"));
        t.setBranchName(rs.getString("branchName"));
        return t;
    }
}
```

---

### Task 3: Update PaymentService with passthrough methods

**Files:**
- Modify: `src/main/java/service/finance/PaymentService.java`

**Interfaces:**
- Consumes: `SalesTransactionReportDAO`
- Produces: `getTransactionKpi(SalesTransactionFilter)`, `searchTransactions(f, page, pageSize)`, `countTransactions(f)`, `getDistinctTransactionTypes()`

- [ ] **Step 1: Add fields and passthrough methods to `PaymentService.java`**

Add these imports at the top:
```java
import dao.report.SalesTransactionReportDAO;
import model.SalesTransaction;
import model.SalesTransactionFilter;
import model.SalesTransactionKpi;
import java.util.List;
```

Add after the existing fields:
```java
private final SalesTransactionReportDAO reportDAO = new SalesTransactionReportDAO();
```

Add after existing methods:
```java
public SalesTransactionKpi getTransactionKpi(SalesTransactionFilter f) {
    return reportDAO.calculateKpi(f);
}

public List<SalesTransaction> searchTransactions(SalesTransactionFilter f, int page, int pageSize) {
    return reportDAO.searchTransactions(f, page, pageSize);
}

public int countTransactions(SalesTransactionFilter f) {
    return reportDAO.countTransactions(f);
}

public List<String> getDistinctTransactionTypes() {
    return reportDAO.getDistinctTransactionTypes();
}
```

Note: The existing `countTransactions(String, String, String, String)` overload will coexist. No conflict.

---

### Task 4: Create SalesTransactionReportController

**Files:**
- Create: `src/main/java/controller/report/SalesTransactionReportController.java`

**Interfaces:**
- Consumes: `PaymentService`, `SalesTransactionFilter`, `SalesTransactionKpi`, `SalesTransaction`, `UserManagementDao`
- URL: `/reports/finance-detail`, `/reports/finance-detail/export-excel`, `/reports/finance-detail/export-pdf`

- [ ] **Step 1: Create `SalesTransactionReportController.java`**

```java
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
    "/reports/finance-detail/export-pdf"
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
        request.setAttribute("pageTitle", "Sales & Transaction Report");
        request.setAttribute("pageSubtitle", "View sales revenue and detailed transaction history across the system.");

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
        f.setPaymentMethod(trim(request.getParameter("paymentMethod")));
        f.setAmountFrom(parseDoubleNull(request.getParameter("amountFrom")));
        f.setAmountTo(parseDoubleNull(request.getParameter("amountTo")));
        f.setBranchId(parseIntNull(request.getParameter("branchId")));
        f.setEmpId(parseIntNull(request.getParameter("empId")));
        f.setKeyword(trim(request.getParameter("keyword")));
        f.setSortBy(trim(request.getParameter("sortBy")));
        f.setSortDir(trim(request.getParameter("sortDir")));

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
        if (f.getDatePreset() != null) sb.append("Period: ").append(f.getDatePreset());
        else if (f.getDateFrom() != null || f.getDateTo() != null)
            sb.append("From: ").append(f.getDateFrom()).append(" To: ").append(f.getDateTo());
        if (f.getTransactionType() != null) sb.append(" | Type: ").append(f.getTransactionType());
        if (f.getPaymentMethod() != null) sb.append(" | Payment: ").append(f.getPaymentMethod());
        if (f.getBranchId() != null) sb.append(" | Branch ID: ").append(f.getBranchId());
        if (f.getEmpId() != null) sb.append(" | Employee ID: ").append(f.getEmpId());
        if (f.getKeyword() != null) sb.append(" | Keyword: ").append(f.getKeyword());
        return sb.toString().trim();
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

    private String trim(String s) { return s == null || s.trim().isEmpty() ? null : s.trim(); }
    private int parseInt(String s, int def) { try { return Integer.parseInt(s); } catch (Exception e) { return def; } }
    private Integer parseIntNull(String s) { try { return Integer.parseInt(s); } catch (Exception e) { return null; } }
    private Double parseDoubleNull(String s) { try { return Double.parseDouble(s); } catch (Exception e) { return null; } }
    private LocalDate parseDate(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try { return LocalDate.parse(s.trim(), DateTimeFormatter.ISO_LOCAL_DATE); } catch (DateTimeParseException e) { return null; }
    }
}
```

---

### Task 5: Create JSP Views

**Files:**
- Create: `src/main/webapp/views/reports/sales-transaction-report.jsp`
- Create: `src/main/webapp/views/reports/_transaction_filter.jsp`
- Create: `src/main/webapp/views/reports/_transaction_kpi.jsp`
- Create: `src/main/webapp/views/reports/_transaction_table.jsp`

**Interfaces:**
- Consumes: `pageTitle`, `pageSubtitle`, `baseUrl`, `kpi` (SalesTransactionKpi), `filter` (SalesTransactionFilter), `datePreset`, `transactions` (List<SalesTransaction>), `branches`, `employees`, `transactionTypes`, `managerBranchId`, pagination attributes

- [ ] **Step 1: Create `sales-transaction-report.jsp`**

```jsp
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <meta content="width=device-width, initial-scale=1.0" name="viewport"/>
    <title>${pageTitle} - Finora</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/assets/css/base.css?v=20260601"/>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/assets/css/layout.css?v=20260601"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/user-management.css?v=2"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/report-kpi.css?v=1"/>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet"/>
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet"/>
    <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap" rel="stylesheet"/>
</head>
<body class="user-page">
<div class="app-layout">
    <jsp:include page="/views/common/sidebar.jsp"/>
    <div class="main-wrapper">
        <main class="page-content">
            <section class="page-header">
                <div>
                    <h2>${pageTitle}</h2>
                    <p>${pageSubtitle}</p>
                </div>
                <div class="filter-actions">
                    <a class="btn-primary" style="font-size:13px;padding:6px 14px;"
                       href="${baseUrl}/export-excel${not empty pageContext.request.queryString ? '?' : ''}${pageContext.request.queryString}">
                        <span class="material-symbols-outlined" style="font-size:16px;">file_download</span> Export Excel
                    </a>
                    <a class="btn-primary" style="font-size:13px;padding:6px 14px;background:#d32f2f;"
                       href="${baseUrl}/export-pdf${not empty pageContext.request.queryString ? '?' : ''}${pageContext.request.queryString}">
                        <span class="material-symbols-outlined" style="font-size:16px;">picture_as_pdf</span> Export PDF
                    </a>
                </div>
            </section>

            <jsp:include page="/views/reports/_transaction_filter.jsp"/>
            <jsp:include page="/views/reports/_transaction_kpi.jsp"/>
            <jsp:include page="/views/reports/_transaction_table.jsp"/>

            <jsp:include page="/views/common/pagination.jsp">
                <jsp:param name="baseUrl" value="${baseUrl}"/>
                <jsp:param name="queryString" value="&datePreset=${empty datePreset ? '' : datePreset}&dateFrom=${empty filter.dateFrom ? '' : filter.dateFrom}&dateTo=${empty filter.dateTo ? '' : filter.dateTo}&transactionCode=${empty filter.transactionCode ? '' : filter.transactionCode}&transactionType=${empty filter.transactionType ? '' : filter.transactionType}&paymentMethod=${empty filter.paymentMethod ? '' : filter.paymentMethod}&amountFrom=${empty filter.amountFrom ? '' : filter.amountFrom}&amountTo=${empty filter.amountTo ? '' : filter.amountTo}&branchId=${empty filter.branchId ? '' : filter.branchId}&empId=${empty filter.empId ? '' : filter.empId}&keyword=${empty filter.keyword ? '' : filter.keyword}&sortBy=${empty filter.sortBy ? '' : filter.sortBy}&sortDir=${empty filter.sortDir ? '' : filter.sortDir}"/>
            </jsp:include>
        </main>
    </div>
</div>
</body>
</html>
```

- [ ] **Step 2: Create `_transaction_filter.jsp`**

```jsp
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<form class="filter-card" method="get" action="${baseUrl}">
    <input type="hidden" name="page" value="1"/>
    <input type="hidden" name="sizeValue" value="${sizeValue}"/>

    <div class="filter-grid">
        <div class="form-group">
            <label>Time Range</label>
            <select name="datePreset" id="datePreset" onchange="toggleDateRange()">
                <option value="">Custom Range</option>
                <option value="today" ${datePreset == 'today' ? 'selected' : ''}>Today</option>
                <option value="yesterday" ${datePreset == 'yesterday' ? 'selected' : ''}>Yesterday</option>
                <option value="this_week" ${datePreset == 'this_week' ? 'selected' : ''}>This Week</option>
                <option value="this_month" ${datePreset == 'this_month' ? 'selected' : ''}>This Month</option>
            </select>
        </div>

        <div class="form-group">
            <label>From Date</label>
            <input type="date" name="dateFrom" id="dateFrom" value="${filter.dateFrom}"/>
        </div>

        <div class="form-group">
            <label>To Date</label>
            <input type="date" name="dateTo" id="dateTo" value="${filter.dateTo}"/>
        </div>

        <div class="form-group">
            <label>Transaction ID / Invoice</label>
            <input name="transactionCode" value="${filter.transactionCode}" type="text" placeholder="Search code..."/>
        </div>

        <div class="form-group">
            <label>Transaction Type</label>
            <select name="transactionType">
                <option value="">All Types</option>
                <c:forEach var="t" items="${transactionTypes}">
                    <option value="${t}" ${filter.transactionType == t ? 'selected' : ''}>${t}</option>
                </c:forEach>
            </select>
        </div>

        <div class="form-group">
            <label>Payment Method</label>
            <select name="paymentMethod">
                <option value="">All</option>
                <option value="CASH" ${filter.paymentMethod == 'CASH' ? 'selected' : ''}>Cash</option>
                <option value="BANK_TRANSFER" ${filter.paymentMethod == 'BANK_TRANSFER' ? 'selected' : ''}>Bank Transfer</option>
            </select>
        </div>

        <div class="form-group">
            <label>Amount From</label>
            <input type="number" name="amountFrom" value="${filter.amountFrom}" placeholder="Min..." step="0.01"/>
        </div>

        <div class="form-group">
            <label>Amount To</label>
            <input type="number" name="amountTo" value="${filter.amountTo}" placeholder="Max..." step="0.01"/>
        </div>

        <c:choose>
            <c:when test="${not empty managerBranchId}">
                <input type="hidden" name="branchId" value="${managerBranchId}"/>
            </c:when>
            <c:otherwise>
                <div class="form-group">
                    <label>Branch</label>
                    <select name="branchId">
                        <option value="">All Branches</option>
                        <c:forEach var="branch" items="${branches}">
                            <option value="${branch.branchID}" ${filter.branchId == branch.branchID ? 'selected' : ''}>${branch.name}</option>
                        </c:forEach>
                    </select>
                </div>
            </c:otherwise>
        </c:choose>

        <div class="form-group">
            <label>Employee</label>
            <select name="empId">
                <option value="">All Employees</option>
                <c:forEach var="emp" items="${employees}">
                    <option value="${emp.employeeID}" ${filter.empId == emp.employeeID ? 'selected' : ''}>${emp.fullName}</option>
                </c:forEach>
            </select>
        </div>

        <div class="form-group filter-search">
            <label>Keyword</label>
            <input name="keyword" value="${filter.keyword}" type="text" placeholder="Code, description, employee..."/>
        </div>

        <div class="form-group">
            <label>Sort By</label>
            <select name="sortBy">
                <option value="payment_date" ${empty filter.sortBy || filter.sortBy == 'payment_date' ? 'selected' : ''}>Date</option>
                <option value="payment_amount" ${filter.sortBy == 'payment_amount' ? 'selected' : ''}>Amount</option>
                <option value="PaymentType" ${filter.sortBy == 'PaymentType' ? 'selected' : ''}>Transaction Type</option>
                <option value="branch_name" ${filter.sortBy == 'branch_name' ? 'selected' : ''}>Branch</option>
                <option value="employee_name" ${filter.sortBy == 'employee_name' ? 'selected' : ''}>Employee</option>
            </select>
            <select name="sortDir" style="margin-top:4px;">
                <option value="DESC" ${empty filter.sortDir || filter.sortDir == 'DESC' ? 'selected' : ''}>Newest / Highest</option>
                <option value="ASC" ${filter.sortDir == 'ASC' ? 'selected' : ''}>Oldest / Lowest</option>
            </select>
        </div>

        <div class="filter-actions" style="align-self:flex-end;">
            <button class="btn-primary" type="submit">Apply</button>
            <a class="btn-secondary" href="${baseUrl}">Reset</a>
        </div>
    </div>
</form>

<script>
function toggleDateRange() {
    var preset = document.getElementById('datePreset').value;
    var dateFrom = document.getElementById('dateFrom');
    var dateTo = document.getElementById('dateTo');
    if (preset) {
        dateFrom.disabled = true;
        dateTo.disabled = true;
    } else {
        dateFrom.disabled = false;
        dateTo.disabled = false;
    }
}
toggleDateRange();
</script>
```

- [ ] **Step 3: Create `_transaction_kpi.jsp`**

```jsp
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:choose>
    <c:when test="${kpi.totalTransactions == 0}">
        <div class="empty-state" style="margin:40px 0;">
            <span class="material-symbols-outlined" style="font-size:48px;color:#94a3b8;">payments</span>
            <h4>No transaction data found.</h4>
        </div>
    </c:when>
    <c:otherwise>
        <div class="kpi-grid">
            <div class="kpi-card">
                <div class="kpi-card-info">
                    <p>Total Transactions</p>
                    <h3><fmt:formatNumber value="${kpi.totalTransactions}"/></h3>
                </div>
                <div class="kpi-card-icon blue">
                    <span class="material-symbols-outlined">receipt_long</span>
                </div>
            </div>
            <div class="kpi-card">
                <div class="kpi-card-info">
                    <p>Total Revenue</p>
                    <h3><fmt:formatNumber value="${kpi.totalRevenue}" maxFractionDigits="0"/> ₫</h3>
                </div>
                <div class="kpi-card-icon green">
                    <span class="material-symbols-outlined">payments</span>
                </div>
            </div>
            <div class="kpi-card">
                <div class="kpi-card-info">
                    <p>Total Expense</p>
                    <h3><fmt:formatNumber value="${kpi.totalExpense}" maxFractionDigits="0"/> ₫</h3>
                </div>
                <div class="kpi-card-icon red">
                    <span class="material-symbols-outlined">money_off</span>
                </div>
            </div>
            <div class="kpi-card">
                <div class="kpi-card-info">
                    <p>Net Cash Flow</p>
                    <h3><fmt:formatNumber value="${kpi.netCashFlow}" maxFractionDigits="0"/> ₫</h3>
                </div>
                <div class="kpi-card-icon ${kpi.netCashFlow >= 0 ? 'green' : 'red'}">
                    <span class="material-symbols-outlined">trending_up</span>
                </div>
            </div>
            <div class="kpi-card">
                <div class="kpi-card-info">
                    <p>Average Transaction Value</p>
                    <h3><fmt:formatNumber value="${kpi.avgTransactionValue}" maxFractionDigits="0"/> ₫</h3>
                </div>
                <div class="kpi-card-icon blue">
                    <span class="material-symbols-outlined">bar_chart</span>
                </div>
            </div>
            <div class="kpi-card">
                <div class="kpi-card-info">
                    <p>Total Sales Orders</p>
                    <h3><fmt:formatNumber value="${kpi.totalSalesOrders}"/></h3>
                </div>
                <div class="kpi-card-icon orange">
                    <span class="material-symbols-outlined">shopping_cart</span>
                </div>
            </div>
        </div>
    </c:otherwise>
</c:choose>
```

- [ ] **Step 4: Create `_transaction_table.jsp`**

```jsp
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<section class="table-card">
    <div class="table-scroll">
        <table class="data-table">
            <thead>
            <tr>
                <th>#</th>
                <th>Transaction ID / Invoice</th>
                <th>Date Time</th>
                <th>Transaction Type</th>
                <th>Payment Method</th>
                <th class="text-right">Amount</th>
                <th>Description</th>
                <th>Branch</th>
                <th>Employee</th>
                <th>Status</th>
            </tr>
            </thead>
            <tbody>
            <c:choose>
                <c:when test="${empty transactions}">
                    <tr>
                        <td colspan="10" class="empty-row">
                            <div class="empty-state">
                                <span class="material-symbols-outlined">payments</span>
                                <h4>No transaction data found.</h4>
                                <p>Adjust your filters or time range.</p>
                            </div>
                        </td>
                    </tr>
                </c:when>
                <c:otherwise>
                    <c:forEach var="t" items="${transactions}" varStatus="st">
                        <tr>
                            <td>${(currentPage - 1) * pageSize + st.index + 1}</td>
                            <td><strong>${t.transactionCode}</strong></td>
                            <td>${fn:substring(t.paymentDate, 0, 19)}</td>
                            <td>
                                <c:choose>
                                    <c:when test="${t.transactionType == 'INCOME'}">Income</c:when>
                                    <c:when test="${t.transactionType == 'EXPENSE'}">Expense</c:when>
                                    <c:otherwise>${t.transactionType}</c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${t.paymentMethod == 'CASH'}">Cash</c:when>
                                    <c:when test="${t.paymentMethod == 'BANK_TRANSFER'}">Bank Transfer</c:when>
                                    <c:otherwise>${t.paymentMethod}</c:otherwise>
                                </c:choose>
                            </td>
                            <td class="text-right"><fmt:formatNumber value="${t.amount}" type="number" groupingUsed="true"/> ₫</td>
                            <td>${empty t.description ? '—' : t.description}</td>
                            <td>${empty t.branchName ? '—' : t.branchName}</td>
                            <td>${empty t.employeeName ? '—' : t.employeeName}</td>
                            <td>
                                <span class="status-badge ${t.status == 'PAID' ? 'completed' : ''} ${t.status == 'PENDING' ? 'pending' : ''} ${t.status == 'FAILED' ? 'cancelled' : ''}">
                                    ${t.status}
                                </span>
                            </td>
                        </tr>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
            </tbody>
        </table>
    </div>
</section>
```

---

### Task 6: Update Excel and PDF Export Utilities

**Files:**
- Modify: `src/main/java/util/report/ExcelExportUtil.java`
- Modify: `src/main/java/util/report/PdfReportUtil.java`

**Interfaces:**
- Consumes: `SalesTransaction`, `SalesTransactionKpi` (from Service, already fetched)
- Produces: Excel workbook bytes / PDF document bytes

- [ ] **Step 1: Add `generateSalesTransactionReport` to `ExcelExportUtil.java`**

Add after the existing methods:

```java
public static byte[] generateSalesTransactionReport(
        String generatedBy, List<SalesTransaction> rows, SalesTransactionKpi kpi, String filterDesc) {
    try (Workbook wb = new XSSFWorkbook()) {
        Sheet sheet = wb.createSheet("Sales Transaction Report");
        CellStyle titleStyle = wb.createCellStyle();
        Font titleFont = wb.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 16);
        titleStyle.setFont(titleFont);

        int r = 0;
        Row titleRow = sheet.createRow(r++);
        titleRow.createCell(0).setCellValue("Sales & Transaction Report");
        titleRow.getCell(0).setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 8));

        sheet.createRow(r++).createCell(0).setCellValue("Generated: " + LocalDateTime.now().format(DATETIME_FMT) + " | By: " + generatedBy);
        sheet.createRow(r++).createCell(0).setCellValue("Filters: " + filterDesc);

        r++; // blank row
        Row kpiTitle = sheet.createRow(r++);
        kpiTitle.createCell(0).setCellValue("KPI SUMMARY");
        kpiTitle.getCell(0).setCellStyle(titleStyle);

        CellStyle labelStyle = wb.createCellStyle();
        Font labelFont = wb.createFont();
        labelFont.setBold(true);
        labelFont.setFontHeightInPoints((short) 10);
        labelStyle.setFont(labelFont);

        r = writeKpiRow(sheet, r, labelStyle, "Total Transactions", String.valueOf(kpi.getTotalTransactions()));
        r = writeKpiRow(sheet, r, labelStyle, "Total Revenue", String.format("%,.0f ₫", kpi.getTotalRevenue()));
        r = writeKpiRow(sheet, r, labelStyle, "Total Expense", String.format("%,.0f ₫", kpi.getTotalExpense()));
        r = writeKpiRow(sheet, r, labelStyle, "Net Cash Flow", String.format("%,.0f ₫", kpi.getNetCashFlow()));
        r = writeKpiRow(sheet, r, labelStyle, "Average Transaction Value", String.format("%,.0f ₫", kpi.getAvgTransactionValue()));
        r = writeKpiRow(sheet, r, labelStyle, "Total Sales Orders", String.valueOf(kpi.getTotalSalesOrders()));

        r++; // blank row
        CellStyle headerStyle = createHeaderStyle(wb);
        Row header = sheet.createRow(r++);
        String[] cols = {"Transaction Code", "Date", "Type", "Payment Method", "Amount", "Description", "Branch", "Employee", "Status"};
        for (int i = 0; i < cols.length; i++) {
            Cell c = header.createCell(i);
            c.setCellValue(cols[i]);
            c.setCellStyle(headerStyle);
        }

        CellStyle currencyStyle = createCurrencyStyle(wb);
        for (SalesTransaction t : rows) {
            Row row = sheet.createRow(r++);
            row.createCell(0).setCellValue(t.getTransactionCode());
            row.createCell(1).setCellValue(t.getPaymentDate() != null ? t.getPaymentDate().toString() : "");
            row.createCell(2).setCellValue(t.getTransactionType());
            row.createCell(3).setCellValue(t.getPaymentMethod());
            Cell amt = row.createCell(4);
            amt.setCellValue(t.getAmount());
            amt.setCellStyle(currencyStyle);
            row.createCell(5).setCellValue(t.getDescription() != null ? t.getDescription() : "");
            row.createCell(6).setCellValue(t.getBranchName() != null ? t.getBranchName() : "");
            row.createCell(7).setCellValue(t.getEmployeeName() != null ? t.getEmployeeName() : "");
            row.createCell(8).setCellValue(t.getStatus() != null ? t.getStatus() : "");
        }

        for (int i = 0; i < cols.length; i++) sheet.autoSizeColumn(i);
        return toBytes(wb);
    } catch (Exception e) {
        throw new RuntimeException("Excel generation failed", e);
    }
}

private static int writeKpiRow(Sheet sheet, int r, CellStyle style, String label, String value) {
    Row row = sheet.createRow(r);
    row.createCell(0).setCellValue(label);
    row.getCell(0).setCellStyle(style);
    row.createCell(1).setCellValue(value);
    return r + 1;
}
```

Make `toBytes` method accessible (change from private to static or keep as-is since it's in the same class).

- [ ] **Step 2: Add `generateSalesTransactionReport` to `PdfReportUtil.java`**

Add after the existing methods:

```java
public static byte[] generateSalesTransactionReport(
        String companyName, String generatedBy, List<SalesTransaction> rows,
        SalesTransactionKpi kpi, String filterDesc) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    Document document = new Document(PageSize.A4.rotate(), 36, 36, 50, 50);
    try {
        initFonts();
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        writer.setPageEvent(new HeaderFooter());
        document.open();

        Paragraph title = new Paragraph("SALES & TRANSACTION REPORT", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(4);
        document.add(title);
        document.add(new Paragraph(companyName, HEADER_FONT));
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        Paragraph meta = new Paragraph("Generated: " + now + " | By: " + generatedBy, SMALL_FONT);
        meta.setAlignment(Element.ALIGN_CENTER);
        meta.setSpacingAfter(6);
        document.add(meta);
        addHorizontalRule(document);
        document.add(new Paragraph(" "));

        if (filterDesc != null && !filterDesc.isEmpty()) {
            Paragraph fp = new Paragraph("Filters: " + filterDesc, SMALL_FONT);
            fp.setSpacingAfter(8);
            document.add(fp);
        }

        // KPI
        if (kpi != null) {
            Paragraph kpiTitle = new Paragraph("KPI SUMMARY", HEADER_FONT);
            kpiTitle.setSpacingAfter(6);
            document.add(kpiTitle);
            PdfPTable kt = new PdfPTable(2);
            kt.setWidthPercentage(60);
            kt.setHorizontalAlignment(Element.ALIGN_LEFT);
            kt.setWidths(new float[]{3, 3});
            NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
            nf.setGroupingUsed(true);
            nf.setMaximumFractionDigits(0);
            addSummaryRow(kt, "Total Transactions", String.valueOf(kpi.getTotalTransactions()));
            addSummaryRow(kt, "Total Revenue", nf.format(kpi.getTotalRevenue()) + " ₫");
            addSummaryRow(kt, "Total Expense", nf.format(kpi.getTotalExpense()) + " ₫");
            addSummaryRow(kt, "Net Cash Flow", nf.format(kpi.getNetCashFlow()) + " ₫");
            addSummaryRow(kt, "Average Transaction Value", nf.format(kpi.getAvgTransactionValue()) + " ₫");
            addSummaryRow(kt, "Total Sales Orders", String.valueOf(kpi.getTotalSalesOrders()));
            document.add(kt);
            addHorizontalRule(document);
            document.add(new Paragraph(" "));
        }

        // Table
        PdfPTable table = new PdfPTable(9);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2, 2.5f, 1.5f, 2, 2, 3, 2.5f, 2.5f, 1.5f});
        table.setHeaderRows(1);
        String[] headers = {"Code", "Date", "Type", "Method", "Amount", "Description", "Branch", "Employee", "Status"};
        Color bg = new Color(0x1a, 0x1a, 0x2e);
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, TABLE_HEADER_FONT));
            cell.setBackgroundColor(bg);
            cell.setPadding(5);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);
        }
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        nf.setGroupingUsed(true);
        nf.setMaximumFractionDigits(0);
        Color altColor = new Color(0xF5, 0xF5, 0xFA);
        if (rows == null || rows.isEmpty()) {
            PdfPCell emptyCell = new PdfPCell(new Phrase("No transaction data found.", TABLE_CELL_FONT));
            emptyCell.setColspan(9);
            emptyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            emptyCell.setPadding(12);
            table.addCell(emptyCell);
        } else {
            int idx = 1;
            for (SalesTransaction t : rows) {
                boolean odd = (idx++ % 2 != 0);
                table.addCell(colCell(nullToDash(t.getTransactionCode()), Element.ALIGN_LEFT, odd, altColor));
                table.addCell(colCell(t.getPaymentDate() != null ? t.getPaymentDate().toString() : "", Element.ALIGN_LEFT, odd, altColor));
                table.addCell(colCell(t.getTransactionType(), Element.ALIGN_CENTER, odd, altColor));
                table.addCell(colCell(t.getPaymentMethod(), Element.ALIGN_CENTER, odd, altColor));
                table.addCell(colCell(nf.format(t.getAmount()) + " ₫", Element.ALIGN_RIGHT, odd, altColor));
                table.addCell(colCell(nullToDash(t.getDescription()), Element.ALIGN_LEFT, odd, altColor));
                table.addCell(colCell(nullToDash(t.getBranchName()), Element.ALIGN_LEFT, odd, altColor));
                table.addCell(colCell(nullToDash(t.getEmployeeName()), Element.ALIGN_LEFT, odd, altColor));
                table.addCell(colCell(nullToDash(t.getStatus()), Element.ALIGN_CENTER, odd, altColor));
            }
        }
        document.add(table);
        document.close();
        return baos.toByteArray();
    } catch (Exception e) {
        try { if (document.isOpen()) document.close(); } catch (Exception ignored) {}
        throw new RuntimeException(e);
    }
}
```

Add import for `SalesTransactionKpi` and `SalesTransaction`:
```java
import model.SalesTransaction;
import model.SalesTransactionKpi;
```

---

### Task 7: Update Sidebar and ReportController

**Files:**
- Modify: `src/main/webapp/views/common/sidebar.jsp`
- Modify: `src/main/java/controller/report/ReportController.java`

- [ ] **Step 1: Add Sales & Transaction Report link to sidebar**

In `sidebar.jsp`, find the Reports dropdown section (around line 378-398). Add a new link after the Order Report link:

```jsp
<a href="${pageContext.request.contextPath}/reports/orders"
    class="sidebar-submenu-item ${originalUri.contains('/reports/orders') ? 'active' : ''}">
    <span class="material-icons"
        style="font-size: 1rem; margin-right: 4px;">receipt_long</span>
    Đơn hàng
</a>
<a href="${pageContext.request.contextPath}/reports/finance-detail"
    class="sidebar-submenu-item ${originalUri.contains('/reports/finance-detail') ? 'active' : ''}">
    <span class="material-icons"
        style="font-size: 1rem; margin-right: 4px;">account_balance</span>
    Sales & Transaction
</a>
```

- [ ] **Step 2: Remove finance-detail redirect from `ReportController.java`**

In `ReportController.java`, remove `/reports/finance-detail`, `/reports/finance-detail-preview`, `/reports/finance-detail-export-excel` from:
1. The `@WebServlet` urlPatterns
2. The redirect check block in `doGet()` (lines 77-79)

Also remove these paths from the `@WebServlet` annotation's `urlPatterns`.

---

### Task 8: Verify Build

**Files:**
- No file changes — build and verify the project compiles

- [ ] **Step 1: Build project**

Run from project root:
```bash
cd D:\SWP391\Finora
mvn clean compile
```

Expected: BUILD SUCCESS

- [ ] **Step 2: Verify files are in place**

Check that all new files exist:
- `src/main/java/model/SalesTransactionFilter.java`
- `src/main/java/model/SalesTransactionKpi.java`
- `src/main/java/model/SalesTransaction.java`
- `src/main/java/dao/report/SalesTransactionReportDAO.java`
- `src/main/java/controller/report/SalesTransactionReportController.java`
- `src/main/webapp/views/reports/sales-transaction-report.jsp`
- `src/main/webapp/views/reports/_transaction_filter.jsp`
- `src/main/webapp/views/reports/_transaction_kpi.jsp`
- `src/main/webapp/views/reports/_transaction_table.jsp`

And that modified files don't have syntax errors.
