package dao.product;

import model.Product;
import model.Category;
import model.Unit;
import util.database.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
//check
public class ProductDAO {

    public List<Product> findAll(int offset, int limit) throws SQLException {
        return findAll(offset, limit, null, null);
    }

    public List<Product> findAll(int offset, int limit, String keyword, String status) throws SQLException {
        return findAll(offset, limit, keyword, status, null, null);
    }

    public List<Product> findAll(int offset, int limit, String keyword, String status, Integer categoryID, Integer unitID) throws SQLException {
        return findAll(offset, limit, keyword, status, categoryID, unitID, null);
    }

    public List<Product> findAll(int offset, int limit, String keyword, String status, Integer categoryID, Integer unitID, Integer supplierID) throws SQLException {
        List<Product> items = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT p.ProductID, p.Name, p.Quantity, p.CategoryID, c.Name AS CategoryName, " +
            "p.UnitID, u.Name AS UnitName, p.SupplierIDs, p.SellingPrice, p.ImportPrice, p.Status, p.CreatedAt, p.UpdatedAt " +
            "FROM Product p " +
            "LEFT JOIN Category c ON p.CategoryID = c.CategoryID " +
            "LEFT JOIN Unit u ON p.UnitID = u.UnitID " +
            "WHERE 1=1"
        );
        String cleanedKeyword = null;
        if (keyword != null && !keyword.isBlank()) {
            cleanedKeyword = keyword.trim().replaceAll("\\s+", " ");
            sql.append(" AND p.Name LIKE ?");
        }
        if (status != null && !status.isBlank())  sql.append(" AND p.Status = ?");
        if (categoryID != null) sql.append(" AND p.CategoryID = ?");
        if (unitID != null) sql.append(" AND p.UnitID = ?");
        if (supplierID != null) sql.append(" AND p.SupplierIDs LIKE ?");
        sql.append(" ORDER BY p.ProductID ASC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");

        try (Connection conn = DBContext.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (cleanedKeyword != null && !cleanedKeyword.isBlank()) {
                stmt.setString(idx++, "%" + cleanedKeyword + "%");
            }
            if (status != null && !status.isBlank()) stmt.setString(idx++, status);
            if (categoryID != null) stmt.setInt(idx++, categoryID);
            if (unitID != null) stmt.setInt(idx++, unitID);
            if (supplierID != null) stmt.setString(idx++, "%[" + supplierID + "]%");
            stmt.setInt(idx++, offset);
            stmt.setInt(idx, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) items.add(extractProduct(rs));
            }
        }
        return items;
    }

    public int getTotalCount(String keyword, String status) throws SQLException {
        return getTotalCount(keyword, status, null, null, null);
    }

    public int getTotalCount(String keyword, String status, Integer categoryID, Integer unitID) throws SQLException {
        return getTotalCount(keyword, status, categoryID, unitID, null);
    }

    public int getTotalCount(String keyword, String status, Integer categoryID, Integer unitID, Integer supplierID) throws SQLException {
        StringBuilder sql = new StringBuilder(
            "SELECT COUNT(*) FROM Product p " +
            "WHERE 1=1"
        );
        String cleanedKeyword = null;
        if (keyword != null && !keyword.isBlank()) {
            cleanedKeyword = keyword.trim().replaceAll("\\s+", " ");
            sql.append(" AND p.Name LIKE ?");
        }
        if (status != null && !status.isBlank())  sql.append(" AND p.Status = ?");
        if (categoryID != null) sql.append(" AND p.CategoryID = ?");
        if (unitID != null) sql.append(" AND p.UnitID = ?");
        if (supplierID != null) sql.append(" AND p.SupplierIDs LIKE ?");

        try (Connection conn = DBContext.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (cleanedKeyword != null && !cleanedKeyword.isBlank()) {
                stmt.setString(idx++, "%" + cleanedKeyword + "%");
            }
            if (status != null && !status.isBlank()) stmt.setString(idx++, status);
            if (categoryID != null) stmt.setInt(idx++, categoryID);
            if (unitID != null) stmt.setInt(idx++, unitID);
            if (supplierID != null) stmt.setString(idx++, "%[" + supplierID + "]%");
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public Product findById(int id) throws SQLException {
        String sql = 
            "SELECT p.ProductID, p.Name, p.Quantity, p.CategoryID, c.Name AS CategoryName, " +
            "p.UnitID, u.Name AS UnitName, p.SupplierIDs, p.SellingPrice, p.ImportPrice, p.Status, p.CreatedAt, p.UpdatedAt " +
            "FROM Product p " +
            "LEFT JOIN Category c ON p.CategoryID = c.CategoryID " +
            "LEFT JOIN Unit u ON p.UnitID = u.UnitID " +
            "WHERE p.ProductID = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? extractProduct(rs) : null;
            }
        }
    }

    public int insert(Product product) throws SQLException {
        String sql = "INSERT INTO Product (Name, Quantity, CategoryID, UnitID, SupplierIDs, SellingPrice, ImportPrice, Status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, product.getName());
            stmt.setInt(2, product.getQuantity());
            if (product.getCategoryID() > 0) stmt.setInt(3, product.getCategoryID()); else stmt.setNull(3, java.sql.Types.INTEGER);
            if (product.getUnitID() > 0) stmt.setInt(4, product.getUnitID()); else stmt.setNull(4, java.sql.Types.INTEGER);
            stmt.setString(5, product.getSupplierIDs());
            stmt.setBigDecimal(6, product.getSellingPrice());
            stmt.setBigDecimal(7, product.getImportPrice());
            stmt.setString(8, product.getStatus());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    int newId = keys.getInt(1);
                    product.setProductID(newId);
                    return newId;
                }
            }
        }
        return -1;
    }

    public void update(Product product) throws SQLException {
        String sql = "UPDATE Product SET Name=?, Quantity=?, CategoryID=?, UnitID=?, SupplierIDs=?, SellingPrice=?, ImportPrice=?, Status=?, UpdatedAt=GETDATE() WHERE ProductID=?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, product.getName());
            stmt.setInt(2, product.getQuantity());
            if (product.getCategoryID() > 0) stmt.setInt(3, product.getCategoryID()); else stmt.setNull(3, java.sql.Types.INTEGER);
            if (product.getUnitID() > 0) stmt.setInt(4, product.getUnitID()); else stmt.setNull(4, java.sql.Types.INTEGER);
            stmt.setString(5, product.getSupplierIDs());
            stmt.setBigDecimal(6, product.getSellingPrice());
            stmt.setBigDecimal(7, product.getImportPrice());
            stmt.setString(8, product.getStatus());
            stmt.setInt(9, product.getProductID());
            stmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM Inventory WHERE ProductID = ?")) {
                    stmt.setInt(1, id);
                    stmt.executeUpdate();
                }
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM StockTransaction WHERE ProductID = ?")) {
                    stmt.setInt(1, id);
                    stmt.executeUpdate();
                }
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM inventory_ticket_detail WHERE product_id = ?")) {
                    stmt.setInt(1, id);
                    stmt.executeUpdate();
                }
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM OrderDetail WHERE ProductID = ?")) {
                    stmt.setInt(1, id);
                    stmt.executeUpdate();
                }
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM Product WHERE ProductID = ?")) {
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
        String sql = "SELECT CategoryID, Name FROM Category ORDER BY CategoryID ASC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(new Category(rs.getInt("CategoryID"), rs.getString("Name")));
            }
        }
        return list;
    }

    public List<Unit> findAllUnits() throws SQLException {
        List<Unit> list = new ArrayList<>();
        String sql = "SELECT UnitID, Name FROM Unit ORDER BY UnitID ASC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(new Unit(rs.getInt("UnitID"), rs.getString("Name")));
            }
        }
        return list;
    }

    private Product extractProduct(ResultSet rs) throws SQLException {
        Product item = new Product();
        item.setProductID(rs.getInt("ProductID"));
        item.setName(rs.getString("Name"));
        item.setQuantity(rs.getInt("Quantity"));
        item.setCategoryID(rs.getInt("CategoryID"));
        item.setCategoryName(rs.getString("CategoryName"));
        item.setUnitID(rs.getInt("UnitID"));
        item.setUnitName(rs.getString("UnitName"));
        
        try {
            item.setSupplierIDs(rs.getString("SupplierIDs"));
            item.setImportPrice(rs.getBigDecimal("ImportPrice"));
        } catch (SQLException e) {
            // ignore if columns are not selected or don't exist yet
        }

        item.setSellingPrice(rs.getBigDecimal("SellingPrice"));
        item.setStatus(rs.getString("Status"));
        Timestamp ct = rs.getTimestamp("CreatedAt");
        if (ct != null) item.setCreatedAt(ct.toLocalDateTime());
        Timestamp ut = rs.getTimestamp("UpdatedAt");
        if (ut != null) item.setUpdatedAt(ut.toLocalDateTime());
        return item;
    }
}

