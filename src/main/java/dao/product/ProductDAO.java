package dao.product;

import model.Product;
import model.Category;
import model.Unit;
import util.database.DBContext;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    public List<Product> findAll(int offset, int limit) throws SQLException {
        return findAll(offset, limit, null, null);
    }

    public List<Product> findAll(int offset, int limit, String keyword, String status) throws SQLException {
        return findAll(offset, limit, keyword, status, null, null);
    }

    public List<Product> findAll(int offset, int limit, String keyword, String status, Integer categoryID, Integer unitID) throws SQLException {
        List<Product> items = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT p.product_id AS ProductID, p.product_name AS Name, " +
            "ISNULL((SELECT SUM(quantity_in_stock) FROM inventory WHERE product_id = p.product_id), 0) AS Quantity, " +
            "p.category_id AS CategoryID, c.category_name AS CategoryName, " +
            "p.unit_id AS UnitID, u.unit_name AS UnitName, p.selling_price AS SellingPrice, " +
            "'active' AS Status, p.created_at AS CreatedAt, p.update_at AS UpdatedAt " +
            "FROM product p " +
            "LEFT JOIN category c ON p.category_id = c.category_id " +
            "LEFT JOIN unit u ON p.unit_id = u.unit_id " +
            "WHERE 1=1"
        );
        if (keyword != null && !keyword.isBlank()) sql.append(" AND p.product_name LIKE ?");
        if (status != null && !status.isBlank())  sql.append(" AND 'active' = ?");
        if (categoryID != null) sql.append(" AND p.category_id = ?");
        if (unitID != null) sql.append(" AND p.unit_id = ?");
        sql.append(" ORDER BY p.product_id ASC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");

        try (Connection conn = DBContext.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (keyword != null && !keyword.isBlank()) {
                stmt.setString(idx++, "%" + keyword + "%");
            }
            if (status != null && !status.isBlank()) stmt.setString(idx++, status);
            if (categoryID != null) stmt.setInt(idx++, categoryID);
            if (unitID != null) stmt.setInt(idx++, unitID);
            stmt.setInt(idx++, offset);
            stmt.setInt(idx, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) items.add(extractProduct(rs));
            }
        }
        return items;
    }

    public int getTotalCount(String keyword, String status) throws SQLException {
        return getTotalCount(keyword, status, null, null);
    }

    public int getTotalCount(String keyword, String status, Integer categoryID, Integer unitID) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM product WHERE 1=1");
        if (keyword != null && !keyword.isBlank()) sql.append(" AND product_name LIKE ?");
        if (status != null && !status.isBlank())  sql.append(" AND 'active' = ?");
        if (categoryID != null) sql.append(" AND category_id = ?");
        if (unitID != null) sql.append(" AND unit_id = ?");

        try (Connection conn = DBContext.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (keyword != null && !keyword.isBlank()) {
                stmt.setString(idx++, "%" + keyword + "%");
            }
            if (status != null && !status.isBlank()) stmt.setString(idx++, status);
            if (categoryID != null) stmt.setInt(idx++, categoryID);
            if (unitID != null) stmt.setInt(idx++, unitID);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public Product findById(int id) throws SQLException {
        String sql = 
            "SELECT p.product_id AS ProductID, p.product_name AS Name, " +
            "ISNULL((SELECT SUM(quantity_in_stock) FROM inventory WHERE product_id = p.product_id), 0) AS Quantity, " +
            "p.category_id AS CategoryID, c.category_name AS CategoryName, " +
            "p.unit_id AS UnitID, u.unit_name AS UnitName, p.selling_price AS SellingPrice, " +
            "'active' AS Status, p.created_at AS CreatedAt, p.update_at AS UpdatedAt " +
            "FROM product p " +
            "LEFT JOIN category c ON p.category_id = c.category_id " +
            "LEFT JOIN unit u ON p.unit_id = u.unit_id " +
            "WHERE p.product_id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? extractProduct(rs) : null;
            }
        }
    }

    public int insert(Product product) throws SQLException {
        String sql = "INSERT INTO product (product_name, category_id, unit_id, selling_price, created_at, update_at) VALUES (?, ?, ?, ?, GETDATE(), GETDATE())";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, product.getName());
            stmt.setInt(2, product.getCategoryID());
            stmt.setInt(3, product.getUnitID());
            stmt.setBigDecimal(4, product.getSellingPrice());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    int newProdId = keys.getInt(1);
                    product.setProductID(newProdId);
                    // Nhập tồn kho ban đầu cho Kho 1 (Kho chính)
                    String invSql = "INSERT INTO inventory (warehouse_id, product_id, quantity_in_stock, status, updated_at) VALUES (1, ?, ?, 'active', GETDATE())";
                    try (PreparedStatement invStmt = conn.prepareStatement(invSql)) {
                        invStmt.setInt(1, newProdId);
                        invStmt.setInt(2, product.getQuantity());
                        invStmt.executeUpdate();
                    }
                    return newProdId;
                }
            }
        }
        return -1;
    }

    public void update(Product product) throws SQLException {
        String sql = "UPDATE product SET product_name=?, category_id=?, unit_id=?, selling_price=?, update_at=GETDATE() WHERE product_id=?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, product.getName());
            stmt.setInt(2, product.getCategoryID());
            stmt.setInt(3, product.getUnitID());
            stmt.setBigDecimal(4, product.getSellingPrice());
            stmt.setInt(5, product.getProductID());
            stmt.executeUpdate();

            // Cập nhật số lượng trong kho 1
            String invSql = "IF EXISTS (SELECT 1 FROM inventory WHERE product_id = ? AND warehouse_id = 1) "
                          + "  UPDATE inventory SET quantity_in_stock = ?, updated_at = GETDATE() WHERE product_id = ? AND warehouse_id = 1 "
                          + "ELSE "
                          + "  INSERT INTO inventory (warehouse_id, product_id, quantity_in_stock, status, updated_at) VALUES (1, ?, ?, 'active', GETDATE())";
            try (PreparedStatement invStmt = conn.prepareStatement(invSql)) {
                invStmt.setInt(1, product.getProductID());
                invStmt.setInt(2, product.getQuantity());
                invStmt.setInt(3, product.getProductID());
                invStmt.setInt(4, product.getProductID());
                invStmt.setInt(5, product.getQuantity());
                invStmt.executeUpdate();
            }
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM inventory WHERE product_id = ?")) {
                    stmt.setInt(1, id);
                    stmt.executeUpdate();
                }
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM stock_transaction WHERE product_id = ?")) {
                    stmt.setInt(1, id);
                    stmt.executeUpdate();
                }
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM stock_transfer_detail WHERE product_id = ?")) {
                    stmt.setInt(1, id);
                    stmt.executeUpdate();
                }
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM order_detail WHERE product_id = ?")) {
                    stmt.setInt(1, id);
                    stmt.executeUpdate();
                }
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM product WHERE product_id = ?")) {
                    stmt.setInt(1, id);
                    stmt.executeUpdate();
                }
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        }
    }

    public List<Category> findAllCategories() throws SQLException {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT category_id AS CategoryID, category_name AS Name FROM category ORDER BY category_id ASC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(new Category(rs.getInt("category_id"), rs.getString("category_name")));
            }
        }
        return list;
    }

    public List<Unit> findAllUnits() throws SQLException {
        List<Unit> list = new ArrayList<>();
        String sql = "SELECT unit_id AS UnitID, unit_name AS Name FROM unit ORDER BY unit_id ASC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(new Unit(rs.getInt("unit_id"), rs.getString("unit_name")));
            }
        }
        return list;
    }

    private Product extractProduct(ResultSet rs) throws SQLException {
        Product item = new Product();
        item.setProductID(rs.getInt("ProductID"));
        item.setName(rs.getString("Name"));
        item.setCategoryID(rs.getInt("CategoryID"));
        item.setCategoryName(rs.getString("CategoryName"));
        item.setUnitID(rs.getInt("UnitID"));
        item.setUnitName(rs.getString("UnitName"));
        item.setSellingPrice(rs.getBigDecimal("SellingPrice"));
        item.setStatus(rs.getString("Status"));
        Timestamp ct = rs.getTimestamp("CreatedAt");
        if (ct != null) item.setCreatedAt(ct.toLocalDateTime());
        Timestamp ut = rs.getTimestamp("UpdatedAt");
        if (ut != null) item.setUpdatedAt(ut.toLocalDateTime());
        return item;
    }
}
