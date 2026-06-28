package dao.inventory;

import model.Inventory;
import util.database.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class InventoryDAO {

    public List<Inventory> findAll(int offset, int limit, String keyword, String status, Integer categoryId, Integer unitId, Integer warehouseId, String sortParam) throws SQLException {
        List<Inventory> items = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT i.inventory_id, i.warehouse_id, i.product_id, i.quantity_in_stock, i.status, i.updated_at, " +
            "p.product_name, p.product_codebar, p.selling_price, " +
            "c.category_name, u.unit_name, w.warehouse_name " +
            "FROM inventory i " +
            "JOIN [product] p ON i.product_id = p.product_id " +
            "LEFT JOIN category c ON p.category_id = c.category_id " +
            "LEFT JOIN unit u ON p.unit_id = u.unit_id " +
            "JOIN warehouse w ON i.warehouse_id = w.warehouse_id " +
            "WHERE 1=1"
        );

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (p.product_name LIKE ? OR p.product_codebar LIKE ?)");
        }
        if (status != null && !status.trim().isEmpty()) {
            if ("LOW_STOCK".equals(status)) {
                sql.append(" AND i.quantity_in_stock > 0 AND i.quantity_in_stock <= 10");
            } else if ("OUT_OF_STOCK".equals(status)) {
                sql.append(" AND i.quantity_in_stock = 0");
            } else {
                sql.append(" AND i.status = ?");
            }
        }
        if (categoryId != null && categoryId > 0) sql.append(" AND p.category_id = ?");
        if (unitId != null && unitId > 0) sql.append(" AND p.unit_id = ?");
        if (warehouseId != null && warehouseId > 0) sql.append(" AND i.warehouse_id = ?");

        // Sorting
        if ("qty_desc".equals(sortParam)) {
            sql.append(" ORDER BY i.quantity_in_stock DESC");
        } else if ("name_asc".equals(sortParam)) {
            sql.append(" ORDER BY p.product_name ASC");
        } else if ("updated_desc".equals(sortParam)) {
            sql.append(" ORDER BY i.updated_at DESC");
        } else {
            // Default sort: qty_asc (Lowest stock first)
            sql.append(" ORDER BY i.quantity_in_stock ASC");
        }

        sql.append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");

        try (Connection conn = DBContext.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            int idx = 1;
            if (keyword != null && !keyword.trim().isEmpty()) {
                stmt.setString(idx++, "%" + keyword.trim() + "%");
                stmt.setString(idx++, "%" + keyword.trim() + "%");
            }
            if (status != null && !status.trim().isEmpty() && !"LOW_STOCK".equals(status) && !"OUT_OF_STOCK".equals(status)) {
                stmt.setString(idx++, status);
            }
            if (categoryId != null && categoryId > 0) stmt.setInt(idx++, categoryId);
            if (unitId != null && unitId > 0) stmt.setInt(idx++, unitId);
            if (warehouseId != null && warehouseId > 0) stmt.setInt(idx++, warehouseId);
            
            stmt.setInt(idx++, offset);
            stmt.setInt(idx, limit);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(extractInventory(rs));
                }
            }
        }
        return items;
    }

    public int getTotalCount(String keyword, String status, Integer categoryId, Integer unitId, Integer warehouseId) throws SQLException {
        StringBuilder sql = new StringBuilder(
            "SELECT COUNT(*) FROM inventory i " +
            "JOIN [product] p ON i.product_id = p.product_id " +
            "WHERE 1=1"
        );

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (p.product_name LIKE ? OR p.product_codebar LIKE ?)");
        }
        if (status != null && !status.trim().isEmpty()) {
            if ("LOW_STOCK".equals(status)) {
                sql.append(" AND i.quantity_in_stock > 0 AND i.quantity_in_stock <= 10");
            } else if ("OUT_OF_STOCK".equals(status)) {
                sql.append(" AND i.quantity_in_stock = 0");
            } else {
                sql.append(" AND i.status = ?");
            }
        }
        if (categoryId != null && categoryId > 0) sql.append(" AND p.category_id = ?");
        if (unitId != null && unitId > 0) sql.append(" AND p.unit_id = ?");
        if (warehouseId != null && warehouseId > 0) sql.append(" AND i.warehouse_id = ?");

        try (Connection conn = DBContext.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            int idx = 1;
            if (keyword != null && !keyword.trim().isEmpty()) {
                stmt.setString(idx++, "%" + keyword.trim() + "%");
                stmt.setString(idx++, "%" + keyword.trim() + "%");
            }
            if (status != null && !status.trim().isEmpty() && !"LOW_STOCK".equals(status) && !"OUT_OF_STOCK".equals(status)) {
                stmt.setString(idx++, status);
            }
            if (categoryId != null && categoryId > 0) stmt.setInt(idx++, categoryId);
            if (unitId != null && unitId > 0) stmt.setInt(idx++, unitId);
            if (warehouseId != null && warehouseId > 0) stmt.setInt(idx++, warehouseId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    private Inventory extractInventory(ResultSet rs) throws SQLException {
        Inventory inv = new Inventory();
        inv.setInventoryId(rs.getInt("inventory_id"));
        inv.setWarehouseId(rs.getInt("warehouse_id"));
        inv.setProductId(rs.getInt("product_id"));
        inv.setQuantityInStock(rs.getInt("quantity_in_stock"));
        inv.setStatus(rs.getString("status"));
        if (rs.getTimestamp("updated_at") != null) {
            inv.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        inv.setProductName(rs.getString("product_name"));
        inv.setProductCodebar(rs.getString("product_codebar"));
        inv.setSellingPrice(rs.getBigDecimal("selling_price"));
        inv.setCategoryName(rs.getString("category_name"));
        inv.setUnitName(rs.getString("unit_name"));
        inv.setWarehouseName(rs.getString("warehouse_name"));
        return inv;
    }
}
