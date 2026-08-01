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
        "SELECT o.order_id AS payment_id, o.order_code AS transaction_code, o.created_at AS payment_date, " +
        "CASE WHEN o.order_type IN ('SALE', 'RECEIPT') THEN 'INCOME' ELSE 'EXPENSE' END AS PaymentType, " +
        "o.payment_method, o.total_amount AS payment_amount, o.description AS Description, o.status AS payment_status, " +
        "o.order_id, o.order_code AS orderCode, o.order_type AS orderType, " +
        "e.fullName AS employeeName, b.branch_name AS branchName " +
        "FROM [order] o " +
        "LEFT JOIN Employee e ON o.emp_id = e.emp_id " +
        "LEFT JOIN Branch b ON o.branch_id = b.branch_id " +
        "WHERE o.status IN ('COMPLETED', 'PAID') " +
        "  AND o.order_type IN ('SALE', 'PURCHASE', 'IMPORT', 'RECEIPT', 'EXPENSE')";

    private static final Set<String> ALLOWED_SORT = Set.of(
        "payment_date", "payment_amount", "PaymentType", "branch_name", "employee_name"
    );

    public List<SalesTransaction> searchTransactions(SalesTransactionFilter f, int page, int pageSize) {
        List<SalesTransaction> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(SELECT);
        List<Object> params = new ArrayList<>();
        appendFilters(sql, params, f);

        String sortCol = "o.created_at";
        if (f.getSortBy() != null && ALLOWED_SORT.contains(f.getSortBy())) {
            if ("payment_date".equals(f.getSortBy())) sortCol = "o.created_at";
            else if ("payment_amount".equals(f.getSortBy())) sortCol = "o.total_amount";
            else if ("PaymentType".equals(f.getSortBy())) sortCol = "o.order_type";
            else if ("branch_name".equals(f.getSortBy())) sortCol = "b.branch_name";
            else if ("employee_name".equals(f.getSortBy())) sortCol = "e.fullName";
        }
        String sortDir = "DESC";
        if ("ASC".equalsIgnoreCase(f.getSortDir())) sortDir = "ASC";

        sql.append(" ORDER BY ").append(sortCol).append(" ").append(sortDir)
           .append(", o.order_id DESC");
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
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM [order] o ");
        sql.append("LEFT JOIN Employee e ON o.emp_id = e.emp_id WHERE o.status IN ('COMPLETED', 'PAID') ");
        sql.append("  AND o.order_type IN ('SALE', 'PURCHASE', 'IMPORT', 'RECEIPT', 'EXPENSE') ");
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
            "COALESCE(SUM(CASE WHEN o.order_type IN ('SALE', 'RECEIPT') THEN o.total_amount ELSE 0 END), 0) AS total_revenue, " +
            "COALESCE(SUM(CASE WHEN o.order_type IN ('PURCHASE', 'IMPORT', 'EXPENSE') THEN o.total_amount ELSE 0 END), 0) AS total_expense, " +
            "COALESCE(AVG(o.total_amount), 0) AS avg_transaction_value " +
            "FROM [order] o " +
            "LEFT JOIN Employee e ON o.emp_id = e.emp_id " +
            "WHERE o.status IN ('COMPLETED', 'PAID') " +
            "  AND o.order_type IN ('SALE', 'PURCHASE', 'IMPORT', 'RECEIPT', 'EXPENSE')"
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
        return List.of("INCOME", "EXPENSE");
    }

    private int countSalesOrders(SalesTransactionFilter f) {
        StringBuilder sql = new StringBuilder(
            "SELECT COUNT(*) FROM [order] WHERE order_type = 'SALE'"
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
            sql.append(" AND CAST(o.created_at AS DATE) >= ?");
            params.add(Date.valueOf(f.getDateFrom()));
        }
        if (f.getDateTo() != null) {
            sql.append(" AND CAST(o.created_at AS DATE) <= ?");
            params.add(Date.valueOf(f.getDateTo()));
        }
        if (f.getTransactionCode() != null && !f.getTransactionCode().isBlank()) {
            sql.append(" AND o.order_code LIKE ?");
            params.add("%" + f.getTransactionCode().trim() + "%");
        }
        if (f.getTransactionType() != null && !f.getTransactionType().isBlank()) {
            if ("INCOME".equalsIgnoreCase(f.getTransactionType().trim())) {
                sql.append(" AND o.order_type IN ('SALE', 'RECEIPT')");
            } else if ("EXPENSE".equalsIgnoreCase(f.getTransactionType().trim())) {
                sql.append(" AND o.order_type IN ('PURCHASE', 'IMPORT', 'EXPENSE')");
            }
        }
        if (f.getOrderType() != null && !f.getOrderType().isBlank()) {
            sql.append(" AND o.order_type = ?");
            params.add(f.getOrderType().trim());
        }
        if (f.getPaymentMethod() != null && !f.getPaymentMethod().isBlank()) {
            sql.append(" AND o.payment_method = ?");
            params.add(f.getPaymentMethod());
        }
        if (f.getAmountFrom() != null) {
            sql.append(" AND o.total_amount >= ?");
            params.add(f.getAmountFrom());
        }
        if (f.getAmountTo() != null) {
            sql.append(" AND o.total_amount <= ?");
            params.add(f.getAmountTo());
        }
        if (f.getBranchId() != null) {
            sql.append(" AND o.branch_id = ?");
            params.add(f.getBranchId());
        }
        if (f.getEmpId() != null) {
            sql.append(" AND o.emp_id = ?");
            params.add(f.getEmpId());
        }
        if (f.getKeyword() != null && !f.getKeyword().isBlank()) {
            String kw = "%" + f.getKeyword().trim() + "%";
            sql.append(" AND (o.order_code LIKE ? OR o.description LIKE ? OR e.fullName LIKE ?)");
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
        try {
            t.setOrderCode(rs.getString("orderCode"));
        } catch (SQLException ignored) {}
        try {
            t.setOrderType(rs.getString("orderType"));
        } catch (SQLException ignored) {}
        t.setEmployeeName(rs.getString("employeeName"));
        t.setBranchName(rs.getString("branchName"));
        return t;
    }
}
