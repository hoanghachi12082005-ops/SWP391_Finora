package dao.sales;

import model.Payment;
import java.sql.*;

/**
 * DAO thao tác bảng payment — ghi nhận thanh toán.
 */
public class PaymentDAO {

    /**
     * Chèn bản ghi thanh toán trong cùng Connection/Transaction.
     */
    public void insert(Connection conn, Payment p) throws SQLException {
        String sql = "INSERT INTO payment (order_id, payment_amount, payment_date, payment_status, transaction_code) "
                   + "VALUES (?, ?, GETDATE(), ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, p.getOrderId());
            ps.setDouble(2, p.getPaymentAmount());
            ps.setString(3, p.getPaymentStatus() != null ? p.getPaymentStatus() : "PAID");
            ps.setString(4, p.getTransactionCode());
            ps.executeUpdate();
        }
    }
}
