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
            "p.product_name as product_name, '' as product_codebar, p.selling_price as selling_price, " +
            "c.category_name as category_name, u.unit_name as unit_name, w.warehouse_name " +
            "FROM inventory i " +
            "JOIN product p ON i.product_id = p.product_id " +
            "LEFT JOIN category c ON p.category_id = c.category_id " +
            "LEFT JOIN unit u ON p.unit_id = u.unit_id " +
            "JOIN warehouse w ON i.warehouse_id = w.warehouse_id " +
            "WHERE 1=1"
        );

        String cleanedKeyword = null;
        if (keyword != null && !keyword.trim().isEmpty()) {
            cleanedKeyword = keyword.trim().replaceAll("\\s+", " ");
            sql.append(" AND (p.product_name LIKE ? OR c.category_name LIKE ?)");
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
            if (cleanedKeyword != null && !cleanedKeyword.isEmpty()) {
                stmt.setString(idx++, "%" + cleanedKeyword + "%");
                stmt.setString(idx++, "%" + cleanedKeyword + "%");
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
            "JOIN product p ON i.product_id = p.product_id " +
            "LEFT JOIN category c ON p.category_id = c.category_id " +
            "WHERE 1=1"
        );

        String cleanedKeyword = null;
        if (keyword != null && !keyword.trim().isEmpty()) {
            cleanedKeyword = keyword.trim().replaceAll("\\s+", " ");
            sql.append(" AND (p.product_name LIKE ? OR c.category_name LIKE ?)");
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
            if (cleanedKeyword != null && !cleanedKeyword.isEmpty()) {
                stmt.setString(idx++, "%" + cleanedKeyword + "%");
                stmt.setString(idx++, "%" + cleanedKeyword + "%");
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
        String sql1 = "SELECT COUNT(DISTINCT i.product_id) as totalProducts, COUNT(DISTINCT p.category_id) as totalCategories " +
                      "FROM inventory i JOIN product p ON i.product_id = p.product_id" + whereClause.toString();

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
"SELECT p.product_id as ProductID, p.product_name as ProductName, " +
"COALESCE(i1.quantity_in_stock, 0) as MyStock, " +
"w.warehouse_id as PartnerWarehouseId, w.warehouse_name as PartnerWarehouseName, " +
"i2.quantity_in_stock as PartnerStock " +
"FROM product p " +
"LEFT JOIN category c ON p.category_id = c.category_id " +
"LEFT JOIN inventory i1 ON p.product_id = i1.product_id AND i1.warehouse_id = ? " +
"JOIN inventory i2 ON p.product_id = i2.product_id AND i2.warehouse_id != ? " +
"JOIN warehouse w ON i2.warehouse_id = w.warehouse_id " +
"WHERE w.status = 'ACTIVE' "
        );

        if (keyword == null || keyword.trim().isEmpty()) {
            sql.append("AND COALESCE(i1.quantity_in_stock, 0) <= 10 AND i2.quantity_in_stock > 0 ");
        } else {
            sql.append("AND (p.product_name LIKE ? OR c.category_name LIKE ? OR w.warehouse_name LIKE ?) ");
        }

        sql.append("ORDER BY p.product_name ASC, w.warehouse_name ASC");

        try (Connection conn = DBContext.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            stmt.setInt(1, myWarehouseId);
            stmt.setInt(2, myWarehouseId);
            if (keyword != null && !keyword.trim().isEmpty()) {
                stmt.setString(3, "%" + keyword + "%");
                stmt.setString(4, "%" + keyword + "%");
                stmt.setString(5, "%" + keyword + "%");
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

    public List<dto.inventory.ImportProductDTO> searchImportProducts(int warehouseId, String keyword) throws SQLException {
        List<dto.inventory.ImportProductDTO> list = new ArrayList<>();
        
        // ponytail: supplier map removed — SupplierIDs column doesn't exist in V3 schema
        StringBuilder sql = new StringBuilder(
"SELECT p.product_id as ProductID, p.product_name as ProductName, " +
"COALESCE(i.quantity_in_stock, 0) as MyStock " +
"FROM product p "
        );

        if (keyword == null || keyword.trim().isEmpty()) {
            // Suggestion mode: only suggest products that exist in THIS warehouse's inventory and are running low
            sql.append("INNER JOIN inventory i ON p.product_id = i.product_id AND i.warehouse_id = ? ");
            sql.append("WHERE 1=1 ");
            sql.append("AND i.quantity_in_stock <= 10 ");
        } else {
            // Search mode: allow searching all active products, even those never imported to this warehouse
            sql.append("LEFT JOIN inventory i ON p.product_id = i.product_id AND i.warehouse_id = ? ");
            sql.append("LEFT JOIN category c ON p.category_id = c.category_id ");
            sql.append("WHERE 1=1 ");
            sql.append("AND (p.product_name LIKE ? OR c.category_name LIKE ?) ");
        }

        sql.append("ORDER BY p.product_name ASC");

        try (Connection conn = DBContext.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            stmt.setInt(1, warehouseId);
            if (keyword != null && !keyword.trim().isEmpty()) {
                stmt.setString(2, "%" + keyword + "%");
                stmt.setString(3, "%" + keyword + "%");
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    dto.inventory.ImportProductDTO dto = new dto.inventory.ImportProductDTO();
                    dto.setProductId(rs.getInt("ProductID"));
                    dto.setProductName(rs.getString("ProductName"));
                    dto.setMyStock(rs.getInt("MyStock"));
                    // ponytail: removed setImportPrice and supplier parsing — columns don't exist in V3 schema
                    list.add(dto);
                }
            }
        }
        return list;
    }

    public int getInventoryId(int warehouseId, int productId) throws SQLException {
        String sql = "SELECT inventory_id FROM inventory WHERE warehouse_id = ? AND product_id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, warehouseId);
            stmt.setInt(2, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    public int getCurrentStock(int warehouseId, int productId) throws SQLException {
        String sql = "SELECT quantity_in_stock FROM inventory WHERE warehouse_id = ? AND product_id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, warehouseId);
            stmt.setInt(2, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    public void increaseStock(int warehouseId, int productId, int quantity) throws SQLException {
        // If it exists, update; if not, insert
        int invId = getInventoryId(warehouseId, productId);
        if (invId != -1) {
            String sql = "UPDATE inventory SET quantity_in_stock = quantity_in_stock + ?, updated_at = GETDATE() WHERE inventory_id = ?";
            try (Connection conn = DBContext.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, quantity);
                stmt.setInt(2, invId);
                stmt.executeUpdate();
            }
        } else {
            String sql = "INSERT INTO inventory (warehouse_id, product_id, quantity_in_stock, status, updated_at) VALUES (?, ?, ?, 'ACTIVE', GETDATE())";
            try (Connection conn = DBContext.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, warehouseId);
                stmt.setInt(2, productId);
                stmt.setInt(3, quantity);
                stmt.executeUpdate();
            }
        }
    }
}
