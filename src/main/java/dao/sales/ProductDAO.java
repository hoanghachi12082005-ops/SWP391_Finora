package dao.sales;

import model.Product;
import util.database.DBContext;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    public List<Product> findAllActive() {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT p.*, c.category_name FROM Product p LEFT JOIN Category c ON p.category_id = c.category_id WHERE p.status = 'ACTIVE' ORDER BY p.product_id DESC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
              ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Product findById(int id) {
        String sql = "SELECT p.*, c.category_name FROM Product p LEFT JOIN Category c ON p.category_id = c.category_id WHERE p.product_id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Product findByCode(String code) {
        String sql = "SELECT p.*, c.category_name FROM Product p LEFT JOIN Category c ON p.category_id = c.category_id WHERE p.product_codebar = ? AND p.status = 'ACTIVE'";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Product> searchActive(String keyword) {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT p.*, c.category_name FROM Product p LEFT JOIN Category c ON p.category_id = c.category_id WHERE p.status = 'ACTIVE' AND (p.product_name LIKE ? OR c.category_name LIKE ? OR p.product_codebar LIKE ?) ORDER BY p.product_id DESC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private Product mapRow(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setProductId(rs.getInt("product_id"));
        p.setProductName(rs.getString("product_name"));
        p.setProductCode(rs.getString("product_codebar"));
        p.setCategoryId(rs.getInt("category_id"));
        p.setUnitId(rs.getInt("unit_id"));
        
        java.math.BigDecimal sp = rs.getBigDecimal("selling_price");
        p.setSellingPrice(sp != null ? sp : java.math.BigDecimal.ZERO);
        
        Timestamp ct = rs.getTimestamp("created_at");
        if (ct != null) p.setCreatedAt(ct.toLocalDateTime());
        
        Timestamp ut = rs.getTimestamp("update_at");
        if (ut != null) p.setUpdatedAt(ut.toLocalDateTime());
        
        try {
            p.setCategoryName(rs.getString("category_name"));
        } catch (SQLException ignored) {}
        
        return p;
    }

    // ── POS-specific methods (warehouse-aware) ─────────────────

    /**
     * Lấy tất cả sản phẩm kèm tồn kho tại kho chỉ định.
     */
    public List<Product> getAllActiveByWarehouse(int warehouseId) {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT p.product_id, p.product_codebar, p.product_name, p.category_id, c.category_name, "
                   + "p.unit_id, p.selling_price, p.created_at, p.update_at, "
                   + "ISNULL(i.quantity_in_stock, 0) AS quantity_in_stock "
                   + "FROM product p "
                   + "LEFT JOIN category c ON p.category_id = c.category_id "
                   + "LEFT JOIN inventory i ON p.product_id = i.product_id AND i.warehouse_id = ? "
                   + "ORDER BY p.product_name";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, warehouseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Product p = mapRow(rs);
                    p.setQuantityInStock(rs.getInt("quantity_in_stock"));
                    list.add(p);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Tìm kiếm sản phẩm theo từ khóa kèm tồn kho tại kho chỉ định.
     */
    public List<Product> searchByKeyword(String keyword, int warehouseId) {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT p.product_id, p.product_codebar, p.product_name, p.category_id, c.category_name, "
                   + "p.unit_id, p.selling_price, p.created_at, p.update_at, "
                   + "ISNULL(i.quantity_in_stock, 0) AS quantity_in_stock "
                   + "FROM product p "
                   + "LEFT JOIN category c ON p.category_id = c.category_id "
                   + "LEFT JOIN inventory i ON p.product_id = i.product_id AND i.warehouse_id = ? "
                   + "WHERE p.product_name LIKE ? OR c.category_name LIKE ? OR p.product_codebar LIKE ? "
                   + "ORDER BY p.product_name";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, warehouseId);
            String pattern = "%" + keyword + "%";
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ps.setString(4, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Product p = mapRow(rs);
                    p.setQuantityInStock(rs.getInt("quantity_in_stock"));
                    list.add(p);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Tìm sản phẩm theo mã barcode chính xác kèm tồn kho.
     */
    public Product findByCodebar(String codebar, int warehouseId) {
        String sql = "SELECT p.product_id, p.product_codebar, p.product_name, p.category_id, c.category_name, "
                   + "p.unit_id, p.selling_price, p.created_at, p.update_at, "
                   + "ISNULL(i.quantity_in_stock, 0) AS quantity_in_stock "
                   + "FROM product p "
                   + "LEFT JOIN category c ON p.category_id = c.category_id "
                   + "LEFT JOIN inventory i ON p.product_id = i.product_id AND i.warehouse_id = ? "
                   + "WHERE p.product_codebar = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, warehouseId);
            ps.setString(2, codebar.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Product p = mapRow(rs);
                    p.setQuantityInStock(rs.getInt("quantity_in_stock"));
                    return p;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
