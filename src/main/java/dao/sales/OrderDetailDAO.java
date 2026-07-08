package dao.sales;

import model.CartItem;
import java.sql.*;
import java.util.List;

/**
 * DAO thao tác bảng order_detail — chèn chi tiết đơn hàng.
 */
public class OrderDetailDAO {

    /**
     * Chèn batch chi tiết đơn hàng trong cùng Connection/Transaction.
     */
    public void insertBatch(Connection conn, int orderId, List<CartItem> items) throws SQLException {
        String sql = "INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (CartItem item : items) {
                ps.setInt(1, orderId);
                ps.setInt(2, item.getProductId());
                ps.setInt(3, item.getQuantity());
                ps.setDouble(4, item.getSellingPrice());
                ps.setDouble(5, item.getLineTotal());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /**
     * Chèn batch chi tiết đơn nhập hàng (PURCHASE) trong cùng Connection/Transaction.
     */
    public void insertBatchPurchase(Connection conn, int orderId, List<model.OrderDetail> items) throws SQLException {
        String sql = "INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price, import_price) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (model.OrderDetail item : items) {
                ps.setInt(1, orderId);
                ps.setInt(2, item.getProductId());
                ps.setInt(3, item.getQuantity());
                ps.setDouble(4, item.getUnitPrice());
                ps.setDouble(5, item.getTotalPrice());
                ps.setDouble(6, item.getImportPrice());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
}
