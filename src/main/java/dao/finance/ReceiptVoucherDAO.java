package dao.finance;

import model.ReceiptVoucher;

import java.sql.*;

public class ReceiptVoucherDAO {

    public int insert(Connection conn, ReceiptVoucher v) throws SQLException {
        String sql = "INSERT INTO receipt_voucher (payment_id, voucher_number, amount, created_by, created_at) "
                   + "VALUES (?, ?, ?, ?, GETDATE())";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, v.getPaymentId());
            ps.setString(2, v.getVoucherNumber());
            ps.setDouble(3, v.getAmount());
            if (v.getCreatedBy() != null) {
                ps.setInt(4, v.getCreatedBy());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Tạo phiếu thu thất bại.");
            }
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
            throw new SQLException("Không lấy được ID phiếu thu.");
        }
    }
}
