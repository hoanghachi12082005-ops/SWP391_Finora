package dao.sales;

import model.CartItem;
import model.OrderDetail;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO thao tác bảng order_detail — chèn chi tiết đơn hàng.
 */
public class OrderDetailDAO {

    /**
     * Chèn batch chi tiết đơn hàng trong cùng Connection/Transaction.
     */
    public void insertBatch(Connection conn, int orderId, List<CartItem> items) throws SQLException {
        String sql = "INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price, import_price) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (CartItem item : items) {
                ps.setInt(1, orderId);
                ps.setInt(2, item.getProductId());
                ps.setInt(3, item.getQuantity());
                ps.setDouble(4, item.getSellingPrice());
                ps.setDouble(5, item.getLineTotal());
                ps.setDouble(6, 0.0);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /**
     * Chèn batch chi tiết đơn nhập hàng (PURCHASE) trong cùng Connection/Transaction.
     */
    public void insertBatchPurchase(Connection conn, int orderId, List<model.OrderDetail> items) throws SQLException {
        String sql = "INSERT INTO order_detail (order_id, product_id, quantity, unit_price, total_price, import_price, supplier_id, supplier_status) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (model.OrderDetail item : items) {
                ps.setInt(1, orderId);
                ps.setInt(2, item.getProductId());
                ps.setInt(3, item.getQuantity());
                ps.setDouble(4, item.getUnitPrice());
                ps.setDouble(5, item.getTotalPrice());
                ps.setDouble(6, item.getImportPrice());
                if (item.getSupplierId() != null) {
                    ps.setInt(7, item.getSupplierId());
                } else {
                    ps.setNull(7, java.sql.Types.INTEGER);
                }
                ps.setString(8, item.getSupplierStatus() != null ? item.getSupplierStatus() : "PENDING");
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /**
     * Lấy danh sách sản phẩm trong đơn hàng (dùng cho hoàn kho khi hủy đơn).
     */
    public List<OrderDetail> findByOrderId(Connection conn, int orderId) throws SQLException {
        String sql = "SELECT product_id, quantity FROM order_detail WHERE order_id = ?";
        List<OrderDetail> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderDetail d = new OrderDetail();
                    d.setProductId(rs.getInt("product_id"));
                    d.setQuantity(rs.getInt("quantity"));
                    list.add(d);
                }
            }
        }
        return list;
    }
}
