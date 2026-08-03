package dao.finance;

import model.Payment;
import util.database.DBContext;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PaymentDAO {

    /**
     * Lấy danh sách giao dịch có phân trang và bộ lọc từ bảng order
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
        StringBuilder sql = new StringBuilder("""
            SELECT 
                o.order_id AS PaymentID,
                o.order_id AS OrderID,
                o.order_code AS OrderCode,
                o.order_type AS OrderType,
                o.total_amount AS PaymentAmount,
                o.created_at AS PaymentDate,
                o.status AS PaymentStatus,
                o.order_code AS TransactionCode,
                CASE WHEN o.order_type IN ('SALE', 'RECEIPT') THEN 'INCOME' ELSE 'EXPENSE' END AS PaymentType,
                o.description AS Description,
                o.emp_id AS EmployeeID,
                o.branch_id AS BranchID,
                o.payment_method AS PaymentMethod,
                e.fullName AS CreatorName,
                b.branch_name AS BranchName
            FROM [order] o WITH (NOLOCK)
            LEFT JOIN Employee e WITH (NOLOCK) ON o.emp_id = e.emp_id
            LEFT JOIN Branch b WITH (NOLOCK) ON o.branch_id = b.branch_id
            WHERE o.status IN ('COMPLETED', 'PAID')
              AND o.order_type IN ('SALE', 'PURCHASE', 'IMPORT', 'RECEIPT', 'EXPENSE')
        """);

        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (o.order_code LIKE ? OR o.description LIKE ?) ");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
        }

        if (type != null && !type.isBlank()) {
            if ("INCOME".equalsIgnoreCase(type.trim())) {
                sql.append(" AND o.order_type IN ('SALE', 'RECEIPT') ");
            } else if ("EXPENSE".equalsIgnoreCase(type.trim())) {
                sql.append(" AND o.order_type IN ('PURCHASE', 'IMPORT', 'EXPENSE') ");
            }
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

        StringBuilder sql = new StringBuilder("""
            SELECT COUNT(*)
            FROM [order] o WITH (NOLOCK)
            WHERE o.status IN ('COMPLETED', 'PAID')
              AND o.order_type IN ('SALE', 'PURCHASE', 'IMPORT', 'RECEIPT', 'EXPENSE')
        """);

        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (o.order_code LIKE ? OR o.description LIKE ?) ");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
        }

        if (type != null && !type.isBlank()) {
            if ("INCOME".equalsIgnoreCase(type.trim())) {
                sql.append(" AND o.order_type IN ('SALE', 'RECEIPT') ");
            } else if ("EXPENSE".equalsIgnoreCase(type.trim())) {
                sql.append(" AND o.order_type IN ('PURCHASE', 'IMPORT', 'EXPENSE') ");
            }
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
                SELECT SUM(CASE WHEN o.order_type IN ('SALE', 'RECEIPT') THEN o.total_amount ELSE -o.total_amount END)
                FROM [order] o
                WHERE o.payment_method = 'CASH' AND o.branch_id = ? AND o.status IN ('COMPLETED', 'PAID')
                  AND o.order_type IN ('SALE', 'PURCHASE', 'IMPORT', 'RECEIPT', 'EXPENSE')
            """;
            return getDoubleScalarWithIntParam(sql, branchId);
        }
        String sql = """
            SELECT SUM(CASE WHEN o.order_type IN ('SALE', 'RECEIPT') THEN o.total_amount ELSE -o.total_amount END)
            FROM [order] o
            WHERE o.payment_method = 'CASH' AND o.status IN ('COMPLETED', 'PAID')
              AND o.order_type IN ('SALE', 'PURCHASE', 'IMPORT', 'RECEIPT', 'EXPENSE')
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
                SELECT SUM(CASE WHEN o.order_type IN ('SALE', 'RECEIPT') THEN o.total_amount ELSE -o.total_amount END)
                FROM [order] o
                WHERE o.payment_method = 'BANK_TRANSFER' AND o.branch_id = ? AND o.status IN ('COMPLETED', 'PAID')
                  AND o.order_type IN ('SALE', 'PURCHASE', 'IMPORT', 'RECEIPT', 'EXPENSE')
            """;
            return getDoubleScalarWithIntParam(sql, branchId);
        }
        String sql = """
            SELECT SUM(CASE WHEN o.order_type IN ('SALE', 'RECEIPT') THEN o.total_amount ELSE -o.total_amount END)
            FROM [order] o
            WHERE o.payment_method = 'BANK_TRANSFER' AND o.status IN ('COMPLETED', 'PAID')
              AND o.order_type IN ('SALE', 'PURCHASE', 'IMPORT', 'RECEIPT', 'EXPENSE')
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
                WHERE o.order_type IN ('SALE', 'RECEIPT') AND o.payment_method = ? AND o.branch_id = ? AND o.status IN ('COMPLETED', 'PAID')
            """;
            return getDoubleScalarWithParams(sql, paymentMethod, branchId);
        }
        String sql = """
            SELECT SUM(o.total_amount)
            FROM [order] o
            WHERE o.order_type IN ('SALE', 'RECEIPT') AND o.payment_method = ? AND o.status IN ('COMPLETED', 'PAID')
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
                WHERE o.order_type IN ('PURCHASE', 'IMPORT', 'EXPENSE') AND o.payment_method = ? AND o.branch_id = ? AND o.status IN ('COMPLETED', 'PAID')
            """;
            return getDoubleScalarWithParams(sql, paymentMethod, branchId);
        }
        String sql = """
            SELECT SUM(o.total_amount)
            FROM [order] o
            WHERE o.order_type IN ('PURCHASE', 'IMPORT', 'EXPENSE') AND o.payment_method = ? AND o.status IN ('COMPLETED', 'PAID')
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
        StringBuilder sql = new StringBuilder("""
            SELECT 
                DATEPART(week, o.created_at) - DATEPART(week, DATEADD(month, DATEDIFF(month, 0, o.created_at), 0)) + 1 AS WeekNum,
                SUM(CASE WHEN o.order_type IN ('SALE', 'RECEIPT') THEN o.total_amount ELSE 0 END) AS TotalIncome,
                SUM(CASE WHEN o.order_type IN ('PURCHASE', 'IMPORT', 'EXPENSE') THEN o.total_amount ELSE 0 END) AS TotalExpense
            FROM [order] o
            WHERE o.status IN ('COMPLETED', 'PAID')
              AND o.order_type IN ('SALE', 'PURCHASE', 'IMPORT', 'RECEIPT', 'EXPENSE')
        """);

        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (o.order_code LIKE ? OR o.description LIKE ?) ");
            params.add("%" + keyword.trim() + "%");
            params.add("%" + keyword.trim() + "%");
        }

        if (type != null && !type.isBlank()) {
            if ("INCOME".equalsIgnoreCase(type.trim())) {
                sql.append(" AND o.order_type IN ('SALE', 'RECEIPT') ");
            } else if ("EXPENSE".equalsIgnoreCase(type.trim())) {
                sql.append(" AND o.order_type IN ('PURCHASE', 'IMPORT', 'EXPENSE') ");
            }
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

    /**
     * Thêm mới một phiếu thu hoặc phiếu chi thủ công (Tạo/Cập nhật vào bảng order)
     */
    public boolean insert(Payment payment) {
        if (payment.getOrderId() != null) {
            try (Connection conn = DBContext.getConnection()) {
                return insert(conn, payment) > 0;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Sinh mã giao dịch tăng tự động (ví dụ: PT00001, PC00001) trong cùng Connection
     */
    public String generateTransactionCode(Connection conn, String type, String prefix) {
        String orderType = "INCOME".equalsIgnoreCase(type) ? "RECEIPT" : "EXPENSE";
        String sql = "SELECT MAX(order_code) FROM [order] WITH (NOLOCK) WHERE order_type = ? AND order_code LIKE ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, orderType);
            ps.setString(2, prefix + "%");

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String maxCode = rs.getString(1);
                    if (maxCode != null && maxCode.length() > prefix.length()) {
                        String numberStr = maxCode.substring(prefix.length());
                        try {
                            int nextNum = Integer.parseInt(numberStr) + 1;
                            return String.format("%s%05d", prefix, nextNum);
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return prefix + "00001";
    }

    /**
     * Sinh mã giao dịch tăng tự động (ví dụ: PT00001, PC00001)
     */
    public String generateTransactionCode(String type, String prefix) {
        try (Connection conn = DBContext.getConnection()) {
            return generateTransactionCode(conn, type, prefix);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return prefix + "00001";
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

    // Cập nhật thông tin mô tả/phương thức thanh toán cho đơn hàng
    public int insert(Connection conn, Payment p) throws SQLException {
        String sql = "UPDATE [order] SET description = ?, payment_method = ? WHERE order_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getDescription() != null ? p.getDescription() : "Thanh toán đơn hàng " + p.getOrderId());
            ps.setString(2, p.getMethod() != null ? p.getMethod() : "CASH");
            ps.setInt(3, p.getOrderId());
            ps.executeUpdate();
            return p.getOrderId();
        }
    }
}
