package dao.report;

import model.SalesTransaction;
import model.SalesTransactionFilter;
import model.SalesTransactionKpi;
import util.database.DBContext;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Bảng payment đã được gỡ bỏ, báo cáo giao dịch giờ đọc trực tiếp từ bảng [order].
 */
public class SalesTransactionReportDAO {

    /** Suy ra loại thu/chi (INCOME/EXPENSE) từ dữ liệu bảng order */
    private static final String PT_CASE = """
        CASE
            WHEN o.order_type = 'SALE' THEN 'INCOME'
            WHEN o.order_type = 'PURCHASE' THEN 'EXPENSE'
            WHEN o.order_type = 'OTHER' AND o.order_code LIKE 'ORD-PV-%' THEN 'EXPENSE'
            WHEN o.order_type = 'OTHER' AND o.order_code LIKE 'ORD-RV-%' THEN 'INCOME'
            ELSE 'OTHER'
        END""";

    /** Suy ra nội dung giao dịch khi order không lưu description */
    private static final String DESC_EXPR = """
        CASE
            WHEN o.description IS NOT NULL AND o.description <> '' THEN o.description
            WHEN o.order_type = 'SALE' THEN N'Thanh toán đơn hàng ' + o.order_code
            WHEN o.order_type = 'PURCHASE' THEN N'Chi tiền nhập hàng cho đơn ' + o.order_code
            ELSE o.order_code
        END""";

    private static final String SELECT =
        "SELECT o.order_id AS payment_id, o.order_code AS transaction_code, o.created_at AS payment_date, " +
        PT_CASE + " AS PaymentType, " +
        "o.payment_method AS payment_method, o.total_amount AS payment_amount, " +
        DESC_EXPR + " AS Description, o.status AS payment_status, " +
        "o.order_id AS order_id, o.order_code AS orderCode, o.order_type AS orderType, " +
        "e.fullName AS employeeName, b.branch_name AS branchName " +
        "FROM [order] o " +
        "LEFT JOIN Employee e ON o.emp_id = e.emp_id " +
        "LEFT JOIN Branch b ON o.branch_id = b.branch_id " +
        "WHERE o.status IN ('COMPLETED', 'PAID') " +
        "  AND o.order_type IN ('SALE', 'PURCHASE', 'OTHER')";

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
            switch (f.getSortBy()) {
                case "payment_date": sortCol = "o.created_at"; break;
                case "payment_amount": sortCol = "o.total_amount"; break;
                case "PaymentType": sortCol = "PaymentType"; break;
                case "branch_name": sortCol = "b.branch_name"; break;
                case "employee_name": sortCol = "e.fullName"; break;
                default: sortCol = "o.created_at";
            }
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
        StringBuilder sql = new StringBuilder("""
            SELECT COUNT(*)
            FROM [order] o
            LEFT JOIN Employee e ON o.emp_id = e.emp_id
            WHERE o.status IN ('COMPLETED', 'PAID')
              AND o.order_type IN ('SALE', 'PURCHASE', 'OTHER')
        """);
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
        StringBuilder sql = new StringBuilder();
        sql.append("""
            SELECT
                COUNT(*) AS total_transactions,
                COALESCE(SUM(CASE WHEN """ + PT_CASE + """
                 = 'INCOME' THEN o.total_amount ELSE 0 END), 0) AS total_revenue,
                COALESCE(SUM(CASE WHEN """ + PT_CASE + """
                 = 'EXPENSE' THEN o.total_amount ELSE 0 END), 0) AS total_expense,
                COALESCE(AVG(o.total_amount), 0) AS avg_transaction_value
            FROM [order] o
            LEFT JOIN Employee e ON o.emp_id = e.emp_id
            WHERE o.status IN ('COMPLETED', 'PAID')
              AND o.order_type IN ('SALE', 'PURCHASE', 'OTHER')
        """);
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
        types.add("INCOME");
        types.add("EXPENSE");
        return types;
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
            sql.append(" AND ").append(PT_CASE).append(" = ?");
            params.add(f.getTransactionType());
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
            sql.append(" AND (o.order_code LIKE ? OR ").append(DESC_EXPR).append(" LIKE ? OR e.fullName LIKE ?)");
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
