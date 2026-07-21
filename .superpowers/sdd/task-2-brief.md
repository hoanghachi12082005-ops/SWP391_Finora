# Task 2: Create SalesTransactionReportDAO

**File to create:**
- `src/main/java/dao/report/SalesTransactionReportDAO.java`

**Package:** `dao.report`

**Context:** This DAO owns all report-specific queries. It reads from the `payment` table (joined with `Employee` and `Branch`), and from the `[Order]` table for sales order counts. It must NOT duplicate queries that already exist in PaymentDAO — these are NEW report-specific queries.

**Key requirements:**
- sortBy must be whitelisted — only allow: `payment_date`, `payment_amount`, `PaymentType`, `branch_name`, `employee_name`
- KPI must use a single SQL query for all payment aggregates (COUNT, SUM, AVG)
- Transaction types must be read via `SELECT DISTINCT PaymentType FROM payment`
- All filters must be reusable across search, count, and KPI methods

**Exact code:**

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
import java.util.Set;

public class SalesTransactionReportDAO {

    private static final String SELECT =
        "SELECT p.payment_id, p.transaction_code, p.payment_date, p.PaymentType, " +
        "p.payment_method, p.payment_amount, p.Description, p.payment_status, " +
        "p.order_id, e.fullName AS employeeName, b.branch_name AS branchName " +
        "FROM payment p " +
        "LEFT JOIN Employee e ON p.EmployeeID = e.emp_id " +
        "LEFT JOIN Branch b ON p.BranchID = b.branch_id";

    private static final Set<String> ALLOWED_SORT = Set.of(
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
