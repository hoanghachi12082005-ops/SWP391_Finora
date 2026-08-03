package dao.inventory;

import model.InventoryItem;
import util.database.DBContext;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InventoryItemDAO {

    public List<InventoryItem> findByWarehouseId(int warehouseId) {
        List<InventoryItem> list = new ArrayList<>();
        String sql = "SELECT i.inventory_id, i.warehouse_id, i.product_id, i.quantity_in_stock, i.status, i.updated_at, "
                   + "w.warehouse_name, p.product_name, p.product_codebar "
                   + "FROM inventory i "
                   + "LEFT JOIN warehouse w ON i.warehouse_id = w.warehouse_id "
                   + "LEFT JOIN [product] p ON i.product_id = p.product_id "
                   + "WHERE i.warehouse_id = ? ORDER BY p.product_name";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, warehouseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<InventoryItem> findAll() {
        List<InventoryItem> list = new ArrayList<>();
        String sql = "SELECT i.inventory_id, i.warehouse_id, i.product_id, i.quantity_in_stock, i.status, i.updated_at, "
                   + "w.warehouse_name, p.product_name, p.product_codebar "
                   + "FROM inventory i "
                   + "LEFT JOIN warehouse w ON i.warehouse_id = w.warehouse_id "
                   + "LEFT JOIN [product] p ON i.product_id = p.product_id "
                   + "ORDER BY w.warehouse_name, p.product_name";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public InventoryItem findById(int id) {
        String sql = "SELECT i.inventory_id, i.warehouse_id, i.product_id, i.quantity_in_stock, i.status, i.updated_at, "
                   + "w.warehouse_name, p.product_name, p.product_codebar "
                   + "FROM inventory i "
                   + "LEFT JOIN warehouse w ON i.warehouse_id = w.warehouse_id "
                   + "LEFT JOIN [product] p ON i.product_id = p.product_id "
                   + "WHERE i.inventory_id = ?";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return map(rs); }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // ponytail: deduct stock within checkout transaction; caller owns the connection
    public void deductStock(int productId, int warehouseId, int quantity, Connection conn) throws SQLException {
        String getSql = "SELECT inventory_id, quantity_in_stock FROM inventory WHERE warehouse_id = ? AND product_id = ?";
        String updateSql = "UPDATE inventory SET quantity_in_stock = ?, updated_at = GETDATE() WHERE inventory_id = ?";

        int invId;
        int beforeQty;
        try (PreparedStatement ps = conn.prepareStatement(getSql)) {
            ps.setInt(1, warehouseId);
            ps.setInt(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("Sản phẩm " + productId + " không tồn tại trong kho " + warehouseId);
                invId = rs.getInt("inventory_id");
                beforeQty = rs.getInt("quantity_in_stock");
            }
        }
        int afterQty = beforeQty - quantity;
        if (afterQty < 0) throw new SQLException("Không đủ tồn kho cho sản phẩm " + productId + ": có " + beforeQty + ", cần " + quantity);
        try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
            ps.setInt(1, afterQty);
            ps.setInt(2, invId);
            ps.executeUpdate();
        }
    }

    private InventoryItem map(ResultSet rs) throws SQLException {
        InventoryItem item = new InventoryItem();
        item.setInventoryId(rs.getInt("inventory_id"));
        item.setWarehouseId(rs.getInt("warehouse_id"));
        item.setProductId(rs.getInt("product_id"));
        item.setQuantityInStock(rs.getInt("quantity_in_stock"));
        item.setStatus(rs.getString("status"));
        if (rs.getTimestamp("updated_at") != null) item.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        item.setWarehouseName(rs.getString("warehouse_name"));
        item.setProductName(rs.getString("product_name"));
        item.setProductCodebar(rs.getString("product_codebar"));
        return item;
    }
}
