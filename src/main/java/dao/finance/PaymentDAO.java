package dao.finance;

import model.Payment;
import util.database.DBContext;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaymentDAO {

    public List<Payment> findByOrderId(int orderId) {
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT payment_id, order_id, payment_amount, payment_date, payment_status, transaction_code FROM payment WHERE order_id = ? ORDER BY payment_date DESC";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Payment p = new Payment();
                    p.setPaymentId(rs.getInt("payment_id"));
                    p.setOrderId(rs.getInt("order_id"));
                    p.setPaymentAmount(rs.getBigDecimal("payment_amount"));
                    if (rs.getTimestamp("payment_date") != null) p.setPaymentDate(rs.getTimestamp("payment_date").toLocalDateTime());
                    p.setPaymentStatus(rs.getString("payment_status"));
                    p.setTransactionCode(rs.getString("transaction_code"));
                    list.add(p);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean insert(Payment p) {
        String sql = "INSERT INTO payment (order_id, payment_amount, payment_date, payment_status, transaction_code) VALUES (?, ?, GETDATE(), ?, ?)";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, p.getOrderId());
            ps.setBigDecimal(2, p.getPaymentAmount() != null ? p.getPaymentAmount() : java.math.BigDecimal.ZERO);
            ps.setString(3, p.getPaymentStatus() != null ? p.getPaymentStatus() : "PAID");
            ps.setString(4, p.getTransactionCode());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public void insert(Payment p, Connection conn) throws SQLException {
        String sql = "INSERT INTO payment (order_id, payment_amount, payment_date, payment_status, transaction_code) VALUES (?, ?, GETDATE(), ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, p.getOrderId());
            ps.setBigDecimal(2, p.getPaymentAmount() != null ? p.getPaymentAmount() : java.math.BigDecimal.ZERO);
            ps.setString(3, p.getPaymentStatus() != null ? p.getPaymentStatus() : "PAID");
            ps.setString(4, p.getTransactionCode());
            ps.executeUpdate();
        }
    }

    public boolean updateStatus(int paymentId, String status) {
        String sql = "UPDATE payment SET payment_status = ? WHERE payment_id = ?";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, paymentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }
}
