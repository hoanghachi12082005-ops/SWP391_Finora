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
            "p.Name as product_name, '' as product_codebar, p.SellingPrice as selling_price, " +
            "c.Name as category_name, u.Name as unit_name, w.warehouse_name " +
            "FROM inventory i " +
            "JOIN Product p ON i.product_id = p.ProductID " +
            "LEFT JOIN Category c ON p.CategoryID = c.CategoryID " +
            "LEFT JOIN Unit u ON p.UnitID = u.UnitID " +
            "JOIN warehouse w ON i.warehouse_id = w.warehouse_id " +
            "WHERE 1=1"
        );

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND p.Name LIKE ?");
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
        if (categoryId != null && categoryId > 0) sql.append(" AND p.CategoryID = ?");
        if (unitId != null && unitId > 0) sql.append(" AND p.UnitID = ?");
        if (warehouseId != null && warehouseId > 0) sql.append(" AND i.warehouse_id = ?");

        // Sorting
        if ("qty_desc".equals(sortParam)) {
            sql.append(" ORDER BY i.quantity_in_stock DESC");
        } else if ("name_asc".equals(sortParam)) {
            sql.append(" ORDER BY p.Name ASC");
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
            "JOIN Product p ON i.product_id = p.ProductID " +
            "WHERE 1=1"
        );

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND p.Name LIKE ?");
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
        if (categoryId != null && categoryId > 0) sql.append(" AND p.CategoryID = ?");
        if (unitId != null && unitId > 0) sql.append(" AND p.UnitID = ?");
        if (warehouseId != null && warehouseId > 0) sql.append(" AND i.warehouse_id = ?");

        try (Connection conn = DBContext.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            int idx = 1;
            if (keyword != null && !keyword.trim().isEmpty()) {
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

    public static class DashboardKPI {
        public int totalProducts;
        public int totalCategories;
        public int lowStockCount;
    }

    public DashboardKPI getDashboardKPI(List<Integer> allowedWarehouseIds, Integer specificWarehouseId) throws SQLException {
        DashboardKPI kpi = new DashboardKPI();
        
        StringBuilder whereClause = new StringBuilder(" WHERE 1=1 ");
        if (specificWarehouseId != null && specificWarehouseId > 0) {
            whereClause.append(" AND i.warehouse_id = ").append(specificWarehouseId);
        } else if (allowedWarehouseIds != null && !allowedWarehouseIds.isEmpty()) {
            whereClause.append(" AND i.warehouse_id IN (");
            for (int i = 0; i < allowedWarehouseIds.size(); i++) {
                whereClause.append(allowedWarehouseIds.get(i));
                if (i < allowedWarehouseIds.size() - 1) whereClause.append(",");
            }
            whereClause.append(")");
        } else {
            // No specific warehouse and no allowed warehouses -> return 0
            return kpi;
        }

        // Query 1: Total unique products and unique categories
        String sql1 = "SELECT COUNT(DISTINCT i.product_id) as totalProducts, COUNT(DISTINCT p.CategoryID) as totalCategories " +
                      "FROM inventory i JOIN Product p ON i.product_id = p.ProductID" + whereClause.toString();

        // Query 2: Low stock count
        String sql2 = "SELECT COUNT(i.inventory_id) FROM inventory i " + whereClause.toString() + " AND i.quantity_in_stock > 0 AND i.quantity_in_stock <= 10";

        try (Connection conn = DBContext.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(sql1);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    kpi.totalProducts = rs.getInt("totalProducts");
                    kpi.totalCategories = rs.getInt("totalCategories");
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql2);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    kpi.lowStockCount = rs.getInt(1);
                }
            }
        }
        return kpi;
    }

    public List<dto.inventory.ExchangeProductDTO> searchExchangeProducts(int myWarehouseId, String keyword) throws SQLException {
        List<dto.inventory.ExchangeProductDTO> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT p.ProductID, p.Name as ProductName, " +
            "COALESCE(i1.quantity_in_stock, 0) as MyStock, " +
            "w.warehouse_id as PartnerWarehouseId, w.warehouse_name as PartnerWarehouseName, " +
            "i2.quantity_in_stock as PartnerStock " +
            "FROM Product p " +
            "LEFT JOIN inventory i1 ON p.ProductID = i1.product_id AND i1.warehouse_id = ? " +
            "JOIN inventory i2 ON p.ProductID = i2.product_id AND i2.warehouse_id != ? " +
            "JOIN warehouse w ON i2.warehouse_id = w.warehouse_id " +
            "WHERE w.status = 'ACTIVE' "
        );

        if (keyword == null || keyword.trim().isEmpty()) {
            sql.append("AND COALESCE(i1.quantity_in_stock, 0) <= 10 AND i2.quantity_in_stock > 0 ");
        } else {
            sql.append("AND p.Name LIKE ? ");
        }

        sql.append("ORDER BY p.Name ASC, w.warehouse_name ASC");

        try (Connection conn = DBContext.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            stmt.setInt(1, myWarehouseId);
            stmt.setInt(2, myWarehouseId);
            if (keyword != null && !keyword.trim().isEmpty()) {
                stmt.setString(3, "%" + keyword + "%");
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    dto.inventory.ExchangeProductDTO dto = new dto.inventory.ExchangeProductDTO();
                    dto.setProductId(rs.getInt("ProductID"));
                    dto.setProductName(rs.getString("ProductName"));
                    dto.setMyStock(rs.getInt("MyStock"));
                    dto.setPartnerWarehouseId(rs.getInt("PartnerWarehouseId"));
                    dto.setPartnerWarehouseName(rs.getString("PartnerWarehouseName"));
                    dto.setPartnerStock(rs.getInt("PartnerStock"));
                    list.add(dto);
                }
            }
        }
        return list;
    }
}
