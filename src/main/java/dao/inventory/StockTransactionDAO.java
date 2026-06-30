package dao.inventory;

import model.StockTransaction;
import util.database.DBContext;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StockTransactionDAO {

    public List<StockTransaction> findByWarehouseId(int warehouseId) {
        List<StockTransaction> list = new ArrayList<>();
        String sql = "SELECT t.stock_transaction_id, t.warehouse_id, t.product_id, t.reference_type, t.reference_id, "
                   + "t.transaction_type, t.quantity, t.before_quantity, t.after_quantity, t.note, t.created_by, t.created_at, "
                   + "w.warehouse_name, p.product_name "
                   + "FROM stock_transaction t "
                   + "LEFT JOIN warehouse w ON t.warehouse_id = w.warehouse_id "
                   + "LEFT JOIN [product] p ON t.product_id = p.product_id "
                   + "WHERE t.warehouse_id = ? ORDER BY t.created_at DESC";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, warehouseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StockTransaction t = new StockTransaction();
                    t.setStockTransactionId(rs.getInt("stock_transaction_id"));
                    t.setWarehouseId(rs.getInt("warehouse_id"));
                    t.setProductId(rs.getInt("product_id"));
                    t.setReferenceType(rs.getString("reference_type"));
                    int refId = rs.getInt("reference_id"); if (!rs.wasNull()) t.setReferenceId(refId);
                    t.setTransactionType(rs.getString("transaction_type"));
                    t.setQuantity(rs.getInt("quantity"));
                    t.setBeforeQuantity(rs.getInt("before_quantity"));
                    t.setAfterQuantity(rs.getInt("after_quantity"));
                    t.setNote(rs.getString("note"));
                    int cb = rs.getInt("created_by"); if (!rs.wasNull()) t.setCreatedBy(cb);
                    if (rs.getTimestamp("created_at") != null) t.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    t.setWarehouseName(rs.getString("warehouse_name"));
                    t.setProductName(rs.getString("product_name"));
                    list.add(t);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ponytail: simple insert within checkout transaction
    public void insert(int warehouseId, int productId, String refType, Integer refId, String txType, int quantity, int beforeQty, int afterQty, Integer createdBy, Connection conn) throws SQLException {
        String sql = "INSERT INTO stock_transaction (warehouse_id, product_id, reference_type, reference_id, transaction_type, quantity, before_quantity, after_quantity, created_by, created_at) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, GETDATE())";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, warehouseId);
            ps.setInt(2, productId);
            ps.setString(3, refType);
            if (refId != null) ps.setInt(4, refId); else ps.setNull(4, Types.INTEGER);
            ps.setString(5, txType);
            ps.setInt(6, quantity);
            ps.setInt(7, beforeQty);
            ps.setInt(8, afterQty);
            if (createdBy != null) ps.setInt(9, createdBy); else ps.setNull(9, Types.INTEGER);
            ps.executeUpdate();
        }
    }
}
