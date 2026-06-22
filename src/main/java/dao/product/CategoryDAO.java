package dao.product;

import model.Category;
import util.database.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {

    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT c.CategoryID, c.Name, c.Description, c.ParentCategoryID, c.Status, " +
                "p.Name AS ParentName, COUNT(pr.ProductID) AS ProductCount " +
                "FROM Category c " +
                "LEFT JOIN Category p ON c.ParentCategoryID = p.CategoryID " +
                "LEFT JOIN Product pr ON c.CategoryID = pr.CategoryID " +
                "GROUP BY c.CategoryID, c.Name, c.Description, c.ParentCategoryID, c.Status, p.Name " +
                "ORDER BY c.CategoryID ASC";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    categories.add(extractCategory(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }

    public List<Category> getActiveCategories() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT c.CategoryID, c.Name, c.Description, c.ParentCategoryID, c.Status, " +
                "p.Name AS ParentName, COUNT(pr.ProductID) AS ProductCount " +
                "FROM Category c " +
                "LEFT JOIN Category p ON c.ParentCategoryID = p.CategoryID " +
                "LEFT JOIN Product pr ON c.CategoryID = pr.CategoryID " +
                "WHERE c.Status = 'active' " +
                "GROUP BY c.CategoryID, c.Name, c.Description, c.ParentCategoryID, c.Status, p.Name " +
                "ORDER BY c.CategoryID ASC";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    categories.add(extractCategory(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }

    public Category getCategoryById(int categoryId) {
        String sql = "SELECT c.CategoryID, c.Name, c.Description, c.ParentCategoryID, c.Status, " +
                "p.Name AS ParentName, COUNT(pr.ProductID) AS ProductCount " +
                "FROM Category c " +
                "LEFT JOIN Category p ON c.ParentCategoryID = p.CategoryID " +
                "LEFT JOIN Product pr ON c.CategoryID = pr.CategoryID " +
                "WHERE c.CategoryID = ? " +
                "GROUP BY c.CategoryID, c.Name, c.Description, c.ParentCategoryID, c.Status, p.Name";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, categoryId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractCategory(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean addCategory(Category category) {
        String sql = "INSERT INTO Category (Name, Description, ParentCategoryID, Status) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, category.getName());
            stmt.setString(2, category.getDescription());
            setNullableInt(stmt, 3, category.getParentId());
            stmt.setString(4, category.getStatus());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateCategory(Category category) {
        String sql = "UPDATE Category SET Name = ?, Description = ?, ParentCategoryID = ?, Status = ? WHERE CategoryID = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, category.getName());
            stmt.setString(2, category.getDescription());
            setNullableInt(stmt, 3, category.getParentId());
            stmt.setString(4, category.getStatus());
            stmt.setInt(5, category.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean existsById(int categoryId) {
        String sql = "SELECT CategoryID FROM Category WHERE CategoryID = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, categoryId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isCategoryNameExists(String name, Integer excludeCategoryId) {
        StringBuilder sql = new StringBuilder("SELECT CategoryID FROM Category WHERE LOWER(LTRIM(RTRIM(Name))) = LOWER(LTRIM(RTRIM(?)))");
        if (excludeCategoryId != null) {
            sql.append(" AND CategoryID <> ?");
        }

        try (Connection conn = DBContext.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            stmt.setString(1, name);
            if (excludeCategoryId != null) {
                stmt.setInt(2, excludeCategoryId);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Integer getCategoryIdByName(String name) {
        String sql = "SELECT CategoryID FROM Category WHERE LOWER(LTRIM(RTRIM(Name))) = LOWER(LTRIM(RTRIM(?)))";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("CategoryID");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isDescendant(int categoryId, int candidateParentId) {
        String sql = "WITH CategoryTree AS (" +
                " SELECT CategoryID, ParentCategoryID FROM Category WHERE ParentCategoryID = ?" +
                " UNION ALL" +
                " SELECT c.CategoryID, c.ParentCategoryID FROM Category c" +
                " INNER JOIN CategoryTree ct ON c.ParentCategoryID = ct.CategoryID" +
                ") SELECT CategoryID FROM CategoryTree WHERE CategoryID = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, categoryId);
            stmt.setInt(2, candidateParentId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void setParameters(PreparedStatement stmt, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            stmt.setObject(i + 1, params.get(i));
        }
    }

    private void setNullableInt(PreparedStatement stmt, int index, Integer value) throws SQLException {
        if (value == null) {
            stmt.setNull(index, java.sql.Types.INTEGER);
        } else {
            stmt.setInt(index, value);
        }
    }

    private Category extractCategory(ResultSet rs) throws SQLException {
        Category category = new Category();
        category.setId(rs.getInt("CategoryID"));
        category.setName(rs.getString("Name"));
        category.setDescription(rs.getString("Description"));
        int parentId = rs.getInt("ParentCategoryID");
        category.setParentId(rs.wasNull() ? null : parentId);
        category.setParentName(rs.getString("ParentName"));
        category.setStatus(rs.getString("Status"));
        category.setProductCount(rs.getInt("ProductCount"));
        return category;
    }
}
