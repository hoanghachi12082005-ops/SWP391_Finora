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
     * Lấy danh sách giao dịch có phân trang và bộ lọc
     */
    public List<Payment> getTransactionsPaging(
            String keyword,
            String type,
            String paymentMethod,
            String timeRange,
            int page,
            int pageSize) {

        List<Payment> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
            SELECT 
                p.payment_id AS PaymentID,
                p.order_id AS OrderID,
                p.payment_amount AS PaymentAmount,
                p.payment_date AS PaymentDate,
                p.payment_status AS PaymentStatus,
                p.transaction_code AS TransactionCode,
                p.PaymentType AS PaymentType,
                p.Description AS Description,
                p.EmployeeID AS EmployeeID,
                p.BranchID AS BranchID,
                p.payment_method AS PaymentMethod,
                e.fullName AS CreatorName,
                b.branch_name AS BranchName
            FROM payment p
            LEFT JOIN Employee e ON p.EmployeeID = e.emp_id
            LEFT JOIN Branch b ON p.BranchID = b.branch_id
            WHERE 1=1
        """);

        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (p.transaction_code LIKE ? OR p.description LIKE ?) ");
            params.add("%" + keyword.trim() + "%");
            params.add("%" + keyword.trim() + "%");
        }

        if (type != null && !type.isBlank()) {
            sql.append(" AND p.PaymentType = ? ");
            params.add(type);
        }

        if (paymentMethod != null && !paymentMethod.isBlank()) {
            sql.append(" AND p.payment_method = ? ");
            params.add(paymentMethod);
        }

        applyTimeRangeFilter(sql, timeRange);

        sql.append(" ORDER BY p.payment_date DESC, p.payment_id DESC ");
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

        StringBuilder sql = new StringBuilder("""
            SELECT COUNT(*)
            FROM payment p
            WHERE 1=1
        """);

        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (p.transaction_code LIKE ? OR p.description LIKE ?) ");
            params.add("%" + keyword.trim() + "%");
            params.add("%" + keyword.trim() + "%");
        }

        if (type != null && !type.isBlank()) {
            sql.append(" AND p.PaymentType = ? ");
            params.add(type);
        }

        if (paymentMethod != null && !paymentMethod.isBlank()) {
            sql.append(" AND p.payment_method = ? ");
            params.add(paymentMethod);
        }

        applyTimeRangeFilter(sql, timeRange);

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
        String sql = """
            SELECT SUM(CASE WHEN PaymentType = 'INCOME' THEN payment_amount ELSE -payment_amount END)
            FROM payment
            WHERE payment_method = 'CASH'
        """;
        return getDoubleScalar(sql);
    }

    /**
     * Tính tổng số dư quỹ ngân hàng (Thu - Chi bằng BANK_TRANSFER)
     */
    public double getTotalBankBalance() {
        String sql = """
            SELECT SUM(CASE WHEN PaymentType = 'INCOME' THEN payment_amount ELSE -payment_amount END)
            FROM payment
            WHERE payment_method = 'BANK_TRANSFER'
        """;
        return getDoubleScalar(sql);
    }

    /**
     * Tính tổng thu theo phương thức
     */
    public double getSumIncome(String paymentMethod) {
        String sql = """
            SELECT SUM(payment_amount)
            FROM payment
            WHERE PaymentType = 'INCOME' AND payment_method = ?
        """;
        return getDoubleScalarWithParam(sql, paymentMethod);
    }

    /**
     * Tính tổng chi theo phương thức
     */
    public double getSumExpense(String paymentMethod) {
        String sql = """
            SELECT SUM(payment_amount)
            FROM payment
            WHERE PaymentType = 'EXPENSE' AND payment_method = ?
        """;
        return getDoubleScalarWithParam(sql, paymentMethod);
    }

    /**
     * Lấy dữ liệu tổng quan thu chi theo từng tuần trong tháng hiện tại
     */
    public List<Map<String, Object>> getWeeklyOverview() {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = """
            SELECT 
                DATEPART(week, payment_date) - DATEPART(week, DATEADD(month, DATEDIFF(month, 0, payment_date), 0)) + 1 AS WeekNum,
                SUM(CASE WHEN PaymentType = 'INCOME' THEN payment_amount ELSE 0 END) AS TotalIncome,
                SUM(CASE WHEN PaymentType = 'EXPENSE' THEN payment_amount ELSE 0 END) AS TotalExpense
            FROM payment
            WHERE MONTH(payment_date) = MONTH(GETDATE()) AND YEAR(payment_date) = YEAR(GETDATE())
            GROUP BY DATEPART(week, payment_date) - DATEPART(week, DATEADD(month, DATEDIFF(month, 0, payment_date), 0)) + 1
            ORDER BY WeekNum
        """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("weekNum", rs.getInt("WeekNum"));
                map.put("totalIncome", rs.getDouble("TotalIncome"));
                map.put("totalExpense", rs.getDouble("TotalExpense"));
                list.add(map);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Thêm mới một phiếu thu hoặc phiếu chi thủ công
     */
    public boolean insert(Payment payment) {
        if (payment.getPaymentType() == null) {
            payment.setPaymentType("INCOME");
        }

        // Tự động sinh mã phiếu
        String prefix = "INCOME".equalsIgnoreCase(payment.getPaymentType()) ? "PT" : "PC";
        String autoCode = generateTransactionCode(payment.getPaymentType(), prefix);
        payment.setName(autoCode); // BaseModel.name maps to TransactionCode

        String sql = """
            INSERT INTO payment (
                order_id, payment_method, payment_amount, payment_date, 
                payment_status, transaction_code, PaymentType, Description, 
                EmployeeID, BranchID
            )
            VALUES (?, ?, ?, GETDATE(), 'PAID', ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (payment.getOrderId() == null) {
                ps.setNull(1, Types.INTEGER);
            } else {
                ps.setInt(1, payment.getOrderId());
            }

            ps.setString(2, payment.getMethod());
            ps.setDouble(3, payment.getAmount());
            ps.setString(4, payment.getName());
            ps.setString(5, payment.getPaymentType());
            ps.setString(6, payment.getDescription());

            if (payment.getEmployeeId() == null) {
                ps.setNull(7, Types.INTEGER);
            } else {
                ps.setInt(7, payment.getEmployeeId());
            }

            if (payment.getBranchId() == null) {
                ps.setNull(8, Types.INTEGER);
            } else {
                ps.setInt(8, payment.getBranchId());
            }

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Sinh mã giao dịch tăng tự động (ví dụ: PT00001, PC00001)
     */
    private String generateTransactionCode(String type, String prefix) {
        String sql = "SELECT MAX(transaction_code) FROM payment WHERE PaymentType = ? AND transaction_code LIKE ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, type);
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

    private void applyTimeRangeFilter(StringBuilder sql, String timeRange) {
        if (timeRange == null || timeRange.isBlank() || "all".equalsIgnoreCase(timeRange)) {
            return;
        }

        switch (timeRange.toLowerCase()) {
            case "today":
                sql.append(" AND CAST(p.payment_date AS DATE) = CAST(GETDATE() AS DATE) ");
                break;
            case "yesterday":
                sql.append(" AND CAST(p.payment_date AS DATE) = CAST(DATEADD(day, -1, GETDATE()) AS DATE) ");
                break;
            case "this_month":
                sql.append(" AND MONTH(p.payment_date) = MONTH(GETDATE()) AND YEAR(p.payment_date) = YEAR(GETDATE()) ");
                break;
            case "last_month":
                sql.append(" AND p.payment_date >= DATEADD(month, DATEDIFF(month, 0, GETDATE()) - 1, 0) ")
                   .append(" AND p.payment_date < DATEADD(month, DATEDIFF(month, 0, GETDATE()), 0) ");
                break;
        }
    }

    private Payment extractPayment(ResultSet rs) throws SQLException {
        Payment p = new Payment();
        p.setId(rs.getInt("PaymentID"));
        p.setName(rs.getString("TransactionCode")); // BaseModel.name maps to TransactionCode
        p.setOrderId(rs.getObject("OrderID") != null ? rs.getInt("OrderID") : null);
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

    // --- Compatibility method for sales/POS transaction ---
    public void insert(Connection conn, Payment p) throws SQLException {
        String sql = "INSERT INTO Payment (OrderID, PaymentAmount, PaymentDate, PaymentStatus, TransactionCode, PaymentType, Description, EmployeeID, BranchID, PaymentMethod) "
                   + "VALUES (?, ?, GETDATE(), ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, p.getOrderId());
            ps.setDouble(2, p.getPaymentAmount());
            ps.setString(3, p.getPaymentStatus() != null ? p.getPaymentStatus() : "PAID");
            ps.setString(4, p.getTransactionCode());
            ps.setString(5, p.getPaymentType() != null ? p.getPaymentType() : "INCOME");
            ps.setString(6, p.getDescription() != null ? p.getDescription() : "Thanh toán đơn hàng " + p.getOrderId());
            if (p.getEmployeeId() != null) {
                ps.setInt(7, p.getEmployeeId());
            } else {
                ps.setNull(7, java.sql.Types.INTEGER);
            }
            if (p.getBranchId() != null) {
                ps.setInt(8, p.getBranchId());
            } else {
                ps.setNull(8, java.sql.Types.INTEGER);
            }
            ps.setString(9, p.getMethod() != null ? p.getMethod() : "CASH");
            ps.executeUpdate();
        }
    }
}
