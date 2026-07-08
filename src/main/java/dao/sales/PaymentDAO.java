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
