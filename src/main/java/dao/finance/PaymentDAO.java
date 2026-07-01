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
                p.transaction_code AS TransactionCode
            FROM payment p
            WHERE 1=1
        """);

        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND p.transaction_code LIKE ? ");
            params.add("%" + keyword.trim() + "%");
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
            sql.append(" AND p.transaction_code LIKE ? ");
            params.add("%" + keyword.trim() + "%");
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
        return 0.0;
    }

    /**
     * Tính tổng số dư quỹ ngân hàng (Thu - Chi bằng BANK_TRANSFER)
     */
    public double getTotalBankBalance() {
        return 0.0;
    }

    /**
     * Tính tổng thu theo phương thức
     */
    public double getSumIncome(String paymentMethod) {
        return 0.0;
    }

    /**
     * Tính tổng chi theo phương thức
     */
    public double getSumExpense(String paymentMethod) {
        return 0.0;
    }

    /**
     * Lấy dữ liệu tổng quan thu chi theo từng tuần trong tháng hiện tại
     */
    public List<Map<String, Object>> getWeeklyOverview() {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = """
            SELECT 
                DATEPART(week, payment_date) - DATEPART(week, DATEADD(month, DATEDIFF(month, 0, payment_date), 0)) + 1 AS WeekNum,
                SUM(payment_amount) AS TotalIncome,
                0 AS TotalExpense
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
                order_id, payment_amount, payment_date, 
                payment_status, transaction_code
            )
            VALUES (?, ?, GETDATE(), 'PAID', ?)
        """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (payment.getOrderId() == null) {
                ps.setNull(1, Types.INTEGER);
            } else {
                ps.setInt(1, payment.getOrderId());
            }

            ps.setDouble(2, payment.getAmount());
            ps.setString(3, payment.getName());

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
        String sql = "SELECT MAX(transaction_code) FROM payment WHERE transaction_code LIKE ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, prefix + "%");

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
        p.setName(rs.getString("TransactionCode"));
        p.setOrderId(rs.getObject("OrderID") != null ? rs.getInt("OrderID") : null);
        p.setAmount(rs.getDouble("PaymentAmount"));
        p.setPaymentDate(rs.getTimestamp("PaymentDate"));
        p.setStatus(rs.getString("PaymentStatus"));

        try {
            p.setCreatorName(rs.getString("CreatorName"));
        } catch (SQLException ignored) {}

        try {
            p.setBranchName(rs.getString("BranchName"));
        } catch (SQLException ignored) {}

        return p;
    }
}
