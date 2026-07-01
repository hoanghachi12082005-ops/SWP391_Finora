package dao.sales;

import util.database.DBContext;
import java.sql.*;

/**
 * DAO thao tác bảng inventory — quản lý tồn kho.
 */
public class InventoryDAO {

    /**
     * Lấy số lượng tồn kho của sản phẩm tại kho chỉ định.
     * Trả về 0 nếu không tìm thấy bản ghi.
     */
    public int getStock(int productId, int warehouseId) {
        String sql = "SELECT quantity_in_stock FROM inventory WHERE product_id = ? AND warehouse_id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ps.setInt(2, warehouseId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("quantity_in_stock");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Trừ tồn kho trong cùng transaction.
     * Trả về số dòng bị ảnh hưởng (0 = không đủ hàng hoặc không tìm thấy).
     */
    public int deductStock(Connection conn, int productId, int warehouseId, int qty) throws SQLException {
        String sql = "UPDATE inventory SET quantity_in_stock = quantity_in_stock - ?, updated_at = GETDATE() "
                   + "WHERE product_id = ? AND warehouse_id = ? AND quantity_in_stock >= ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, qty);
            ps.setInt(2, productId);
            ps.setInt(3, warehouseId);
            ps.setInt(4, qty);
            return ps.executeUpdate();
        }
    }

    /**
     * Lấy tồn kho hiện tại trong cùng transaction (dùng cho logging).
     */
    public int getStockInTransaction(Connection conn, int productId, int warehouseId) throws SQLException {
        String sql = "SELECT quantity_in_stock FROM inventory WHERE product_id = ? AND warehouse_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ps.setInt(2, warehouseId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("quantity_in_stock");
                }
            }
        }
        return 0;
    }

    /**
     * Ghi log giao dịch kho (stock_transaction).
     */
    public void logStockTransaction(Connection conn, int warehouseId, int productId,
                                     int orderId, int quantity, int beforeQty, int empId) throws SQLException {
        String sql = "INSERT INTO stock_transaction "
                   + "(warehouse_id, product_id, reference_type, reference_id, transaction_type, "
                   + " quantity, before_quantity, after_quantity, note, created_by, created_at) "
                   + "VALUES (?, ?, 'ORDER', ?, 'SALE_DEDUCT', ?, ?, ?, N'Bán hàng POS', ?, GETDATE())";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, warehouseId);
            ps.setInt(2, productId);
            ps.setInt(3, orderId);
            ps.setInt(4, quantity);
            ps.setInt(5, beforeQty);
            ps.setInt(6, beforeQty - quantity);
            ps.setInt(7, empId);
            ps.executeUpdate();
        }
    }
}
