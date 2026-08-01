package dao.finance;

import model.Payment;
import util.database.DBContext;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Bảng payment đã được gỡ bỏ, mọi truy vấn sổ quỹ (thu/chi) giờ đọc trực tiếp
 * từ bảng [order]. Loại thu/chi (INCOME/EXPENSE) được suy ra từ order_type và order_code.
 */
public class PaymentDAO {

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

    /**
     * Lấy danh sách giao dịch có phân trang và bộ lọc
     */
    public List<Payment> getTransactionsPaging(
            String keyword,
            String type,
            String paymentMethod,
            String timeRange,
            int page,
            int pageSize) {
        return getTransactionsPaging(keyword, type, paymentMethod, null, null, timeRange, null, page, pageSize);
    }

    public List<Payment> getTransactionsPaging(
            String keyword,
            String type,
            String paymentMethod,
            String timeRange,
            Integer branchId,
            int page,
            int pageSize) {
        return getTransactionsPaging(keyword, type, paymentMethod, null, null, timeRange, branchId, page, pageSize);
    }

    public List<Payment> getTransactionsPaging(
            String keyword,
            String type,
            String paymentMethod,
            String fromDate,
            String toDate,
            String timeRange,
            Integer branchId,
            int page,
            int pageSize) {
        return getTransactionsPaging(keyword, type, null, paymentMethod, null, null, null, fromDate, toDate, timeRange, branchId, page, pageSize);
    }

    public List<Payment> getTransactionsPaging(
            String keyword,
            String type,
            String orderType,
            String paymentMethod,
            Integer employeeId,
            Double amountFrom,
            Double amountTo,
            String fromDate,
            String toDate,
            String timeRange,
            Integer branchId,
            int page,
            int pageSize) {

        List<Payment> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("""
            SELECT 
                o.order_id AS PaymentID,
                o.order_id AS OrderID,
                o.order_code AS OrderCode,
                o.order_type AS OrderType,
                o.total_amount AS PaymentAmount,
                o.created_at AS PaymentDate,
                o.status AS PaymentStatus,
                o.order_code AS TransactionCode,
            """);
        sql.append(PT_CASE).append(" AS PaymentType,\n");
        sql.append(DESC_EXPR).append(" AS Description,\n");
        sql.append("""
                o.emp_id AS EmployeeID,
                o.branch_id AS BranchID,
                o.payment_method AS PaymentMethod,
                e.fullName AS CreatorName,
                b.branch_name AS BranchName
            FROM [order] o WITH (NOLOCK)
            LEFT JOIN Employee e WITH (NOLOCK) ON o.emp_id = e.emp_id
            LEFT JOIN Branch b WITH (NOLOCK) ON o.branch_id = b.branch_id
            WHERE o.status IN ('COMPLETED', 'PAID')
              AND o.order_type IN ('SALE', 'PURCHASE', 'OTHER')
        """);

        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (o.order_code LIKE ? OR ").append(DESC_EXPR).append(" LIKE ?) ");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
        }

        if (type != null && !type.isBlank()) {
            sql.append(" AND ").append(PT_CASE).append(" = ? ");
            params.add(type.trim());
        }

        if (orderType != null && !orderType.isBlank()) {
            sql.append(" AND o.order_type = ? ");
            params.add(orderType.trim());
        }

        if (paymentMethod != null && !paymentMethod.isBlank()) {
            sql.append(" AND o.payment_method = ? ");
            params.add(paymentMethod.trim());
        }

        if (employeeId != null && employeeId > 0) {
            sql.append(" AND o.emp_id = ? ");
            params.add(employeeId);
        }

        if (amountFrom != null) {
            sql.append(" AND o.total_amount >= ? ");
            params.add(amountFrom);
        }

        if (amountTo != null) {
            sql.append(" AND o.total_amount <= ? ");
            params.add(amountTo);
        }

        if (branchId != null && branchId > 0) {
            sql.append(" AND o.branch_id = ? ");
            params.add(branchId);
        }

        applyDateFilter(sql, params, fromDate, toDate, timeRange);

        sql.append(" ORDER BY o.created_at DESC, o.order_id DESC ");
        sql.append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY ");

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            for (Object param : params) {
                ps.setObject(idx++, param);
            }
            ps.setInt(idx++, (page - 1) * pageSize);
            ps.setInt(idx, pageSize);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(extractPayment(rs));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Đếm tổng số giao dịch thỏa mãn bộ lọc
     */
    public int countTransactions(
            String keyword,
            String type,
            String paymentMethod,
            String timeRange) {
        return countTransactions(keyword, type, paymentMethod, null, null, timeRange, null);
    }

    public int countTransactions(
            String keyword,
            String type,
            String paymentMethod,
            String timeRange,
            Integer branchId) {
        return countTransactions(keyword, type, paymentMethod, null, null, timeRange, branchId);
    }

    public int countTransactions(
            String keyword,
            String type,
            String fromDate,
            String toDate,
            String timeRange,
            Integer branchId) {
        return countTransactions(keyword, type, null, null, null, null, null, fromDate, toDate, timeRange, branchId);
    }

    public int countTransactions(
            String keyword,
            String type,
            String paymentMethod,
            String fromDate,
            String toDate,
            String timeRange,
            Integer branchId) {
        return countTransactions(keyword, type, null, paymentMethod, null, null, null, fromDate, toDate, timeRange, branchId);
    }

    public int countTransactions(
            String keyword,
            String type,
            String orderType,
            String paymentMethod,
            Integer employeeId,
            Double amountFrom,
            Double amountTo,
            String fromDate,
            String toDate,
            String timeRange,
            Integer branchId) {

        StringBuilder sql = new StringBuilder();
        sql.append("""
            SELECT COUNT(*)
            FROM [order] o WITH (NOLOCK)
            WHERE o.status IN ('COMPLETED', 'PAID')
              AND o.order_type IN ('SALE', 'PURCHASE', 'OTHER')
        """);

        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (o.order_code LIKE ? OR ").append(DESC_EXPR).append(" LIKE ?) ");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
        }

        if (type != null && !type.isBlank()) {
            sql.append(" AND ").append(PT_CASE).append(" = ? ");
            params.add(type.trim());
        }

        if (orderType != null && !orderType.isBlank()) {
            sql.append(" AND o.order_type = ? ");
            params.add(orderType.trim());
        }

        if (paymentMethod != null && !paymentMethod.isBlank()) {
            sql.append(" AND o.payment_method = ? ");
            params.add(paymentMethod.trim());
        }

        if (employeeId != null && employeeId > 0) {
            sql.append(" AND o.emp_id = ? ");
            params.add(employeeId);
        }

        if (amountFrom != null) {
            sql.append(" AND o.total_amount >= ? ");
            params.add(amountFrom);
        }

        if (amountTo != null) {
            sql.append(" AND o.total_amount <= ? ");
            params.add(amountTo);
        }

        if (branchId != null && branchId > 0) {
            sql.append(" AND o.branch_id = ? ");
            params.add(branchId);
        }

        applyDateFilter(sql, params, fromDate, toDate, timeRange);

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            for (Object param : params) {
                ps.setObject(idx++, param);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * Tính tổng số dư quỹ tiền mặt (Thu - Chi bằng CASH)
     */
    public double getTotalCashBalance() {
        return getTotalCashBalance(null);
    }

    public double getTotalCashBalance(Integer branchId) {
        if (branchId != null && branchId > 0) {
            String sql = """
                SELECT SUM(CASE WHEN """ + PT_CASE + """
                 = 'INCOME' THEN o.total_amount ELSE -o.total_amount END)
                FROM [order] o
                WHERE o.payment_method = 'CASH' AND o.status IN ('COMPLETED','PAID')
                  AND o.order_type IN ('SALE','PURCHASE','OTHER') AND o.branch_id = ?
            """;
            return getDoubleScalarWithIntParam(sql, branchId);
        }
        String sql = """
            SELECT SUM(CASE WHEN """ + PT_CASE + """
             = 'INCOME' THEN o.total_amount ELSE -o.total_amount END)
            FROM [order] o
            WHERE o.payment_method = 'CASH' AND o.status IN ('COMPLETED','PAID')
              AND o.order_type IN ('SALE','PURCHASE','OTHER')
        """;
        return getDoubleScalar(sql);
    }

    /**
     * Tính tổng số dư quỹ ngân hàng (Thu - Chi bằng BANK_TRANSFER)
     */
    public double getTotalBankBalance() {
        return getTotalBankBalance(null);
    }

    public double getTotalBankBalance(Integer branchId) {
        if (branchId != null && branchId > 0) {
            String sql = """
                SELECT SUM(CASE WHEN """ + PT_CASE + """
                 = 'INCOME' THEN o.total_amount ELSE -o.total_amount END)
                FROM [order] o
                WHERE o.payment_method = 'BANK_TRANSFER' AND o.status IN ('COMPLETED','PAID')
                  AND o.order_type IN ('SALE','PURCHASE','OTHER') AND o.branch_id = ?
            """;
            return getDoubleScalarWithIntParam(sql, branchId);
        }
        String sql = """
            SELECT SUM(CASE WHEN """ + PT_CASE + """
             = 'INCOME' THEN o.total_amount ELSE -o.total_amount END)
            FROM [order] o
            WHERE o.payment_method = 'BANK_TRANSFER' AND o.status IN ('COMPLETED','PAID')
              AND o.order_type IN ('SALE','PURCHASE','OTHER')
        """;
        return getDoubleScalar(sql);
    }

    /**
     * Tính tổng thu theo phương thức
     */
    public double getSumIncome(String paymentMethod) {
        return getSumIncome(paymentMethod, null);
    }

    public double getSumIncome(String paymentMethod, Integer branchId) {
        if (branchId != null && branchId > 0) {
            String sql = """
                SELECT SUM(o.total_amount)
                FROM [order] o
                WHERE """ + PT_CASE + """
                 = 'INCOME' AND o.payment_method = ? AND o.status IN ('COMPLETED','PAID')
                  AND o.order_type IN ('SALE','PURCHASE','OTHER') AND o.branch_id = ?
            """;
            return getDoubleScalarWithParams(sql, paymentMethod, branchId);
        }
        String sql = """
            SELECT SUM(o.total_amount)
            FROM [order] o
            WHERE """ + PT_CASE + """
             = 'INCOME' AND o.payment_method = ? AND o.status IN ('COMPLETED','PAID')
              AND o.order_type IN ('SALE','PURCHASE','OTHER')
        """;
        return getDoubleScalarWithParam(sql, paymentMethod);
    }

    /**
     * Tính tổng chi theo phương thức
     */
    public double getSumExpense(String paymentMethod) {
        return getSumExpense(paymentMethod, null);
    }

    public double getSumExpense(String paymentMethod, Integer branchId) {
        if (branchId != null && branchId > 0) {
            String sql = """
                SELECT SUM(o.total_amount)
                FROM [order] o
                WHERE """ + PT_CASE + """
                 = 'EXPENSE' AND o.payment_method = ? AND o.status IN ('COMPLETED','PAID')
                  AND o.order_type IN ('SALE','PURCHASE','OTHER') AND o.branch_id = ?
            """;
            return getDoubleScalarWithParams(sql, paymentMethod, branchId);
        }
        String sql = """
            SELECT SUM(o.total_amount)
            FROM [order] o
            WHERE """ + PT_CASE + """
             = 'EXPENSE' AND o.payment_method = ? AND o.status IN ('COMPLETED','PAID')
              AND o.order_type IN ('SALE','PURCHASE','OTHER')
        """;
        return getDoubleScalarWithParam(sql, paymentMethod);
    }

    /**
     * Lấy dữ liệu tổng quan thu chi theo từng tuần trong tháng hiện tại
     */
    public List<Map<String, Object>> getWeeklyOverview(String keyword, String type, String paymentMethod, String timeRange) {
        return getWeeklyOverview(keyword, type, paymentMethod, null, null, timeRange, null);
    }

    public List<Map<String, Object>> getWeeklyOverview(String keyword, String type, String paymentMethod, String timeRange, Integer branchId) {
        return getWeeklyOverview(keyword, type, paymentMethod, null, null, timeRange, branchId);
    }

    public List<Map<String, Object>> getWeeklyOverview(
            String keyword,
            String type,
            String paymentMethod,
            String fromDate,
            String toDate,
            String timeRange,
            Integer branchId) {
        List<Map<String, Object>> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("""
            SELECT 
                DATEPART(week, o.created_at) - DATEPART(week, DATEADD(month, DATEDIFF(month, 0, o.created_at), 0)) + 1 AS WeekNum,
                SUM(CASE WHEN """ + PT_CASE + """
                 = 'INCOME' THEN o.total_amount ELSE 0 END) AS TotalIncome,
                SUM(CASE WHEN """ + PT_CASE + """
                 = 'EXPENSE' THEN o.total_amount ELSE 0 END) AS TotalExpense
            FROM [order] o
            WHERE o.status IN ('COMPLETED','PAID')
              AND o.order_type IN ('SALE','PURCHASE','OTHER')
        """);

        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (o.order_code LIKE ? OR ").append(DESC_EXPR).append(" LIKE ?) ");
            params.add("%" + keyword.trim() + "%");
            params.add("%" + keyword.trim() + "%");
        }

        if (type != null && !type.isBlank()) {
            sql.append(" AND ").append(PT_CASE).append(" = ? ");
            params.add(type);
        }

        if (paymentMethod != null && !paymentMethod.isBlank()) {
            sql.append(" AND o.payment_method = ? ");
            params.add(paymentMethod);
        }

        if (branchId != null && branchId > 0) {
            sql.append(" AND o.branch_id = ? ");
            params.add(branchId);
        }

        applyDateFilter(sql, params, fromDate, toDate, timeRange);

        sql.append("""
             GROUP BY DATEPART(week, o.created_at) - DATEPART(week, DATEADD(month, DATEDIFF(month, 0, o.created_at), 0)) + 1
             ORDER BY WeekNum
        """);

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            for (Object param : params) {
                ps.setObject(idx++, param);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("weekNum", rs.getInt("WeekNum"));
                    map.put("totalIncome", rs.getDouble("TotalIncome"));
                    map.put("totalExpense", rs.getDouble("TotalExpense"));
                    list.add(map);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    private double getDoubleScalar(String sql) {
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    private double getDoubleScalarWithParam(String sql, String param) {
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    private double getDoubleScalarWithIntParam(String sql, int param) {
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    private double getDoubleScalarWithParams(String sql, String p1, int p2) {
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p1);
            ps.setInt(2, p2);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    private void applyDateFilter(StringBuilder sql, List<Object> params, String fromDate, String toDate, String timeRange) {
        boolean hasCustomDate = false;

        if (fromDate != null && !fromDate.isBlank()) {
            sql.append(" AND CAST(o.created_at AS DATE) >= ? ");
            params.add(fromDate.trim());
            hasCustomDate = true;
        }

        if (toDate != null && !toDate.isBlank()) {
            sql.append(" AND CAST(o.created_at AS DATE) <= ? ");
            params.add(toDate.trim());
            hasCustomDate = true;
        }

        if (!hasCustomDate && timeRange != null && !timeRange.isBlank() && !"all".equalsIgnoreCase(timeRange)) {
            switch (timeRange.toLowerCase()) {
                case "today":
                    sql.append(" AND CAST(o.created_at AS DATE) = CAST(GETDATE() AS DATE) ");
                    break;
                case "yesterday":
                    sql.append(" AND CAST(o.created_at AS DATE) = CAST(DATEADD(day, -1, GETDATE()) AS DATE) ");
                    break;
                case "this_month":
                    sql.append(" AND MONTH(o.created_at) = MONTH(GETDATE()) AND YEAR(o.created_at) = YEAR(GETDATE()) ");
                    break;
                case "last_month":
                    sql.append(" AND o.created_at >= DATEADD(month, DATEDIFF(month, 0, GETDATE()) - 1, 0) ")
                       .append(" AND o.created_at < DATEADD(month, DATEDIFF(month, 0, GETDATE()), 0) ");
                    break;
            }
        }
    }

    private Payment extractPayment(ResultSet rs) throws SQLException {
        Payment p = new Payment();
        p.setId(rs.getInt("PaymentID"));
        p.setName(rs.getString("TransactionCode")); // BaseModel.name maps to TransactionCode
        p.setOrderId(rs.getObject("OrderID") != null ? rs.getInt("OrderID") : null);
        try {
            p.setOrderCode(rs.getString("OrderCode"));
        } catch (SQLException ignored) {}
        try {
            p.setOrderType(rs.getString("OrderType"));
        } catch (SQLException ignored) {}
        p.setMethod(rs.getString("PaymentMethod"));
        p.setAmount(rs.getDouble("PaymentAmount"));
        p.setPaymentDate(rs.getTimestamp("PaymentDate"));
        p.setStatus(rs.getString("PaymentStatus"));
        p.setPaymentType(rs.getString("PaymentType"));
        p.setDescription(rs.getString("Description"));
        p.setEmployeeId(rs.getObject("EmployeeID") != null ? rs.getInt("EmployeeID") : null);
        p.setBranchId(rs.getObject("BranchID") != null ? rs.getInt("BranchID") : null);

        try {
            p.setCreatorName(rs.getString("CreatorName"));
        } catch (SQLException ignored) {}

        try {
            p.setBranchName(rs.getString("BranchName"));
        } catch (SQLException ignored) {}

        return p;
    }
}
