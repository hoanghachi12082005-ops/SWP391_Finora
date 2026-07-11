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
        StringBuilder sql = new StringBuilder();
        sql.append("""
            SELECT 
                i.inventory_id,
                i.warehouse_id,
                p.product_id,
                i.quantity_in_stock,
                i.status,
                i.updated_at,
                p.product_name as product_name, 
                '' as product_codebar, 
                p.selling_price as selling_price,
                c.category_name as category_name, 
                u.unit_name as unit_name, 
                w.warehouse_name as warehouse_name
            FROM inventory i
            JOIN product p ON i.product_id = p.product_id
            LEFT JOIN category c ON p.category_id = c.category_id
            LEFT JOIN unit u ON p.unit_id = u.unit_id
            JOIN warehouse w ON i.warehouse_id = w.warehouse_id
            """);

        if (warehouseId != null && warehouseId > 0) {
            sql.append(" WHERE i.warehouse_id = ").append(warehouseId).append(" ");
        } else {
            sql.append(" WHERE 1=1 ");
        }

        String cleanedKeyword = null;
        if (keyword != null && !keyword.trim().isEmpty()) {
            cleanedKeyword = keyword.trim().replaceAll("\\s+", " ");
            sql.append(" AND (p.product_name LIKE ? OR c.category_name LIKE ?)");
        }
        if (status != null && !status.trim().isEmpty()) {
            if ("LOW_STOCK".equals(status)) {
                sql.append(" AND COALESCE(i.quantity_in_stock, 0) <= 10");
            } else if ("OUT_OF_STOCK".equals(status)) {
                sql.append(" AND COALESCE(i.quantity_in_stock, 0) = 0");
            } else {
                sql.append(" AND COALESCE(i.status, 'ACTIVE') = ?");
            }
        }
        if (categoryId != null && categoryId > 0) sql.append(" AND p.category_id = ?");
        if (unitId != null && unitId > 0) sql.append(" AND p.unit_id = ?");

        // Sorting
        if ("qty_desc".equals(sortParam)) {
            sql.append(" ORDER BY COALESCE(i.quantity_in_stock, 0) DESC");
        } else if ("name_asc".equals(sortParam)) {
            sql.append(" ORDER BY p.product_name ASC");
        } else if ("updated_desc".equals(sortParam)) {
            sql.append(" ORDER BY i.updated_at DESC");
        } else {
            // Default sort: qty_asc (Lowest stock first)
            sql.append(" ORDER BY COALESCE(i.quantity_in_stock, 0) ASC");
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
        StringBuilder sql = new StringBuilder();
        sql.append("""
            SELECT COUNT(*) 
            FROM inventory i
            JOIN product p ON i.product_id = p.product_id
            LEFT JOIN category c ON p.category_id = c.category_id
            """);
        if (warehouseId != null && warehouseId > 0) {
            sql.append(" WHERE i.warehouse_id = ").append(warehouseId).append(" ");
        } else {
            sql.append(" WHERE 1=1 ");
        }

        String cleanedKeyword = null;
        if (keyword != null && !keyword.trim().isEmpty()) {
            cleanedKeyword = keyword.trim().replaceAll("\\s+", " ");
            sql.append(" AND (p.product_name LIKE ? OR c.category_name LIKE ?)");
        }
        if (status != null && !status.trim().isEmpty()) {
            if ("LOW_STOCK".equals(status)) {
                sql.append(" AND COALESCE(i.quantity_in_stock, 0) <= 10");
            } else if ("OUT_OF_STOCK".equals(status)) {
                sql.append(" AND COALESCE(i.quantity_in_stock, 0) = 0");
            } else {
                sql.append(" AND COALESCE(i.status, 'ACTIVE') = ?");
            }
        }
        if (categoryId != null && categoryId > 0) sql.append(" AND p.category_id = ?");
        if (unitId != null && unitId > 0) sql.append(" AND p.unit_id = ?");

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
        String cleanedKeyword = null;
        if (keyword != null && !keyword.trim().isEmpty()) {
            cleanedKeyword = keyword.trim().replaceAll("\\s+", " ");
        }
        List<dto.inventory.ExchangeProductDTO> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT p.product_id as ProductID, p.product_name as ProductName, " +
            "COALESCE(i1.quantity_in_stock, 0) as MyStock, " +
            "w.warehouse_id as PartnerWarehouseId, w.warehouse_name as PartnerWarehouseName, " +
            "COALESCE(i2.quantity_in_stock, 0) as PartnerStock " +
            "FROM product p " +
            "LEFT JOIN category c ON p.category_id = c.category_id " +
            "LEFT JOIN inventory i1 ON p.product_id = i1.product_id AND i1.warehouse_id = ? " +
            "CROSS JOIN warehouse w " +
            "LEFT JOIN inventory i2 ON p.product_id = i2.product_id AND i2.warehouse_id = w.warehouse_id " +
            "WHERE w.status = 'ACTIVE' AND w.warehouse_id != ? "
        );

        if (cleanedKeyword == null) {
            sql.append("AND COALESCE(i1.quantity_in_stock, 0) <= 10 ");
        } else {
            sql.append("AND (p.product_name LIKE ? OR c.category_name LIKE ? OR w.warehouse_name LIKE ?) ");
        }

        sql.append("ORDER BY p.product_name ASC, w.warehouse_name ASC");

        try (Connection conn = DBContext.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            stmt.setInt(1, myWarehouseId);
            stmt.setInt(2, myWarehouseId);
            if (cleanedKeyword != null) {
                stmt.setString(3, "%" + cleanedKeyword + "%");
                stmt.setString(4, "%" + cleanedKeyword + "%");
                stmt.setString(5, "%" + cleanedKeyword + "%");
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

    public List<dto.inventory.ExchangeProductDTO> searchTransferProducts(int fromWarehouseId, int toWarehouseId, String keyword) throws SQLException {
        String cleanedKeyword = null;
        if (keyword != null && !keyword.trim().isEmpty()) {
            cleanedKeyword = keyword.trim().replaceAll("\\s+", " ");
        }
        List<dto.inventory.ExchangeProductDTO> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT p.product_id as ProductID, p.product_name as ProductName, " +
            "COALESCE(i1.quantity_in_stock, 0) as FromStock, " +
            "COALESCE(i2.quantity_in_stock, 0) as ToStock " +
            "FROM product p " +
            "LEFT JOIN category c ON p.category_id = c.category_id " +
            "LEFT JOIN inventory i1 ON p.product_id = i1.product_id AND i1.warehouse_id = ? " +
            "LEFT JOIN inventory i2 ON p.product_id = i2.product_id AND i2.warehouse_id = ? " +
            "WHERE p.status = 'ACTIVE' "
        );

        if (cleanedKeyword == null) {
            // Suggestion mode: return 5 items with lowest stock at fromWarehouse
            sql.append("ORDER BY FromStock ASC OFFSET 0 ROWS FETCH NEXT 5 ROWS ONLY");
        } else {
            sql.append("AND (p.product_name LIKE ? OR c.category_name LIKE ?) ");
            sql.append("ORDER BY p.product_name ASC");
        }

        try (Connection conn = DBContext.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            stmt.setInt(1, fromWarehouseId);
            stmt.setInt(2, toWarehouseId);
            if (cleanedKeyword != null) {
                stmt.setString(3, "%" + cleanedKeyword + "%");
                stmt.setString(4, "%" + cleanedKeyword + "%");
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    dto.inventory.ExchangeProductDTO dto = new dto.inventory.ExchangeProductDTO();
                    dto.setProductId(rs.getInt("ProductID"));
                    dto.setProductName(rs.getString("ProductName"));
                    dto.setMyStock(rs.getInt("FromStock"));
                    dto.setPartnerWarehouseId(toWarehouseId);
                    dto.setPartnerWarehouseName("");
                    dto.setPartnerStock(rs.getInt("ToStock"));
                    list.add(dto);
                }
            }
        }
        return list;
    }


    public List<dto.inventory.ImportProductDTO> searchImportProducts(int warehouseId, String keyword) throws SQLException {
        String cleanedKeyword = null;
        if (keyword != null && !keyword.trim().isEmpty()) {
            cleanedKeyword = keyword.trim().replaceAll("\\s+", " ");
        }
        java.util.Map<Integer, dto.inventory.ImportProductDTO> map = new java.util.LinkedHashMap<>();
        StringBuilder sql = new StringBuilder(
            "WITH LatestPurchase AS ( " +
            "    SELECT o.supplier_id, od.product_id, od.import_price, " +
            "           ROW_NUMBER() OVER (PARTITION BY o.supplier_id, od.product_id ORDER BY o.created_at DESC) as rn " +
            "    FROM [order] o " +
            "    JOIN order_detail od ON o.order_id = od.order_id " +
            "    WHERE o.order_type = 'PURCHASE' AND o.status = 'COMPLETED' " +
            "), " +
            "ProductHasHistory AS ( " +
            "    SELECT DISTINCT product_id " +
            "    FROM LatestPurchase " +
            ") " +
            "SELECT p.product_id as ProductID, p.product_name as ProductName, " +
            "COALESCE(i.quantity_in_stock, 0) as MyStock, " +
            "s.supplier_id AS SupplierID, s.supplier_name as SupplierName, lp.import_price as ImportPrice " +
            "FROM product p "
        );

        if (cleanedKeyword == null) {
            sql.append("JOIN inventory i ON p.product_id = i.product_id AND i.warehouse_id = ? ");
            sql.append("CROSS JOIN supplier s ");
            sql.append("LEFT JOIN LatestPurchase lp ON p.product_id = lp.product_id AND s.supplier_id = lp.supplier_id AND lp.rn = 1 ");
            sql.append("LEFT JOIN ProductHasHistory phh ON p.product_id = phh.product_id ");
            sql.append("WHERE s.status = 'ACTIVE' ");
            sql.append("AND ( (phh.product_id IS NOT NULL AND lp.import_price IS NOT NULL) OR (phh.product_id IS NULL) ) ");
        } else {
            sql.append("LEFT JOIN inventory i ON p.product_id = i.product_id AND i.warehouse_id = ? ");
            sql.append("LEFT JOIN category c ON p.category_id = c.category_id ");
            sql.append("CROSS JOIN supplier s ");
            sql.append("LEFT JOIN LatestPurchase lp ON p.product_id = lp.product_id AND s.supplier_id = lp.supplier_id AND lp.rn = 1 ");
            sql.append("LEFT JOIN ProductHasHistory phh ON p.product_id = phh.product_id ");
            sql.append("WHERE s.status = 'ACTIVE' ");
            sql.append("AND (p.product_name LIKE ? OR c.category_name LIKE ?) ");
            sql.append("AND ( (phh.product_id IS NOT NULL AND lp.import_price IS NOT NULL) OR (phh.product_id IS NULL) ) ");
        }

        sql.append("ORDER BY p.product_name ASC");

        try (Connection conn = DBContext.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            stmt.setInt(1, warehouseId);
            if (cleanedKeyword != null) {
                stmt.setString(2, "%" + cleanedKeyword + "%");
                stmt.setString(3, "%" + cleanedKeyword + "%");
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int productId = rs.getInt("ProductID");
                    dto.inventory.ImportProductDTO dto = map.get(productId);
                    if (dto == null) {
                        dto = new dto.inventory.ImportProductDTO();
                        dto.setProductId(productId);
                        dto.setProductName(rs.getString("ProductName"));
                        dto.setMyStock(rs.getInt("MyStock"));
                        dto.setSuppliers(new ArrayList<>());
                        map.put(productId, dto);
                    }
                    
                    int supplierId = rs.getInt("SupplierID");
                    if (!rs.wasNull()) {
                        String supplierName = rs.getString("SupplierName");
                        java.math.BigDecimal importPrice = rs.getBigDecimal("ImportPrice");
                        // Chỉ thêm nhà cung cấp nếu có lịch sử giá nhập thực tế
                        if (importPrice != null && importPrice.doubleValue() > 0) {
                            dto.getSuppliers().add(new dto.inventory.ImportProductDTO.SupplierInfo(supplierId, supplierName, importPrice));
                        }
                    }
                }
            }
        }
        List<dto.inventory.ImportProductDTO> result = new ArrayList<>(map.values());
        if (cleanedKeyword == null) {
            // Sắp xếp theo tồn kho tăng dần (số lượng ít nhất ở kho mình lên đầu)
            result.sort((o1, o2) -> Integer.compare(o1.getMyStock(), o2.getMyStock()));
            // Chỉ lấy tối đa 5 sản phẩm đề xuất
            if (result.size() > 5) {
                result = new ArrayList<>(result.subList(0, 5));
            }
        }
        return result;
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
        try (Connection conn = DBContext.getConnection()) {
            increaseStock(conn, warehouseId, productId, quantity);
        }
    }

    public void increaseStock(Connection conn, int warehouseId, int productId, int quantity) throws SQLException {
        int invId = getInventoryIdInTransaction(conn, warehouseId, productId);
        if (invId != -1) {
            String sql = "UPDATE inventory SET quantity_in_stock = quantity_in_stock + ?, updated_at = GETDATE() WHERE inventory_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, quantity);
                stmt.setInt(2, invId);
                stmt.executeUpdate();
            }
        } else {
            String sql = "INSERT INTO inventory (warehouse_id, product_id, quantity_in_stock, status, updated_at) VALUES (?, ?, ?, 'ACTIVE', GETDATE())";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, warehouseId);
                stmt.setInt(2, productId);
                stmt.setInt(3, quantity);
                stmt.executeUpdate();
            }
        }
    }

    public int getInventoryIdInTransaction(Connection conn, int warehouseId, int productId) throws SQLException {
        String sql = "SELECT inventory_id FROM inventory WHERE warehouse_id = ? AND product_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, warehouseId);
            ps.setInt(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("inventory_id");
                }
            }
        }
        return -1;
    }

    // --- Compatibility methods for sales/POS ---

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

    public void logCustomStockTransaction(Connection conn, int warehouseId, int productId, 
                                        String refType, int refId, String txType,
                                        int quantity, int beforeQty, int afterQty, String note, int empId) throws SQLException {
        String sql = "INSERT INTO stock_transaction "
                   + "(warehouse_id, product_id, reference_type, reference_id, transaction_type, "
                   + " quantity, before_quantity, after_quantity, note, created_by, created_at) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, GETDATE())";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, warehouseId);
            ps.setInt(2, productId);
            ps.setString(3, refType);
            ps.setInt(4, refId);
            ps.setString(5, txType);
            ps.setInt(6, quantity);
            ps.setInt(7, beforeQty);
            ps.setInt(8, afterQty);
            ps.setString(9, note);
            ps.setInt(10, empId);
            ps.executeUpdate();
        }
    }

    public void updateStockQty(Connection conn, int warehouseId, int productId, int actualQty) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM inventory WHERE warehouse_id = ? AND product_id = ?";
        boolean exists = false;
        try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setInt(1, warehouseId);
            ps.setInt(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    exists = true;
                }
            }
        }

        if (exists) {
            String sql = "UPDATE inventory SET quantity_in_stock = ?, updated_at = GETDATE(), status = CASE WHEN ? = 0 THEN 'OUT_OF_STOCK' ELSE 'ACTIVE' END WHERE warehouse_id = ? AND product_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, actualQty);
                ps.setInt(2, actualQty);
                ps.setInt(3, warehouseId);
                ps.setInt(4, productId);
                ps.executeUpdate();
            }
        } else {
            String sql = "INSERT INTO inventory (warehouse_id, product_id, quantity_in_stock, status, updated_at) VALUES (?, ?, ?, ?, GETDATE())";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, warehouseId);
                ps.setInt(2, productId);
                ps.setInt(3, actualQty);
                ps.setString(4, actualQty == 0 ? "OUT_OF_STOCK" : "ACTIVE");
                ps.executeUpdate();
            }
        }
    }

    public void updateStockQty(int warehouseId, int productId, int actualQty) {
        try (Connection conn = DBContext.getConnection()) {
            updateStockQty(conn, warehouseId, productId, actualQty);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public List<dto.inventory.StockCheckProductDTO> searchStockCheckProducts(int warehouseId, String keyword) throws SQLException {
        List<dto.inventory.StockCheckProductDTO> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT p.product_id, p.product_name, p.product_codebar, i.quantity_in_stock as system_stock, c.category_name " +
            "FROM product p " +
            "INNER JOIN inventory i ON p.product_id = i.product_id AND i.warehouse_id = ? " +
            "LEFT JOIN category c ON p.category_id = c.category_id " +
            "WHERE 1=1"
        );
        String cleanedKeyword = null;
        if (keyword != null && !keyword.trim().isEmpty()) {
            cleanedKeyword = keyword.trim().replaceAll("\\s+", " ");
            sql.append(" AND (p.product_name LIKE ? OR c.category_name LIKE ? OR p.product_codebar LIKE ?)");
        }
        sql.append(" ORDER BY p.product_name ASC");

        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setInt(1, warehouseId);
            if (cleanedKeyword != null) {
                ps.setString(2, "%" + cleanedKeyword + "%");
                ps.setString(3, "%" + cleanedKeyword + "%");
                ps.setString(4, "%" + cleanedKeyword + "%");
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new dto.inventory.StockCheckProductDTO(
                        rs.getInt("product_id"),
                        rs.getString("product_name"),
                        rs.getInt("system_stock"),
                        rs.getString("category_name"),
                        rs.getString("product_codebar")
                    ));
                }
            }
        }
        return list;
    }
}
