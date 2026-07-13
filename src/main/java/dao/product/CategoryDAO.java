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
        String sql = "SELECT c.category_id AS CategoryID, c.category_name AS Name, c.description AS Description, c.parent_category_id AS ParentCategoryID, c.status AS Status, " +
                "p.category_name AS ParentName, COUNT(pr.product_id) AS ProductCount " +
                "FROM category c " +
                "LEFT JOIN category p ON c.parent_category_id = p.category_id " +
                "LEFT JOIN product pr ON c.category_id = pr.category_id " +
                "GROUP BY c.category_id, c.category_name, c.description, c.parent_category_id, c.status, p.category_name " +
                "ORDER BY c.category_id ASC";

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
        String sql = "SELECT c.category_id AS CategoryID, c.category_name AS Name, c.description AS Description, c.parent_category_id AS ParentCategoryID, c.status AS Status, " +
                "p.category_name AS ParentName, COUNT(pr.product_id) AS ProductCount " +
                "FROM category c " +
                "LEFT JOIN category p ON c.parent_category_id = p.category_id " +
                "LEFT JOIN product pr ON c.category_id = pr.category_id " +
                "WHERE c.status = 'active' " +
                "GROUP BY c.category_id, c.category_name, c.description, c.parent_category_id, c.status, p.category_name " +
                "ORDER BY c.category_id ASC";

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
        String sql = "SELECT c.category_id AS CategoryID, c.category_name AS Name, c.description AS Description, c.parent_category_id AS ParentCategoryID, c.status AS Status, " +
                "p.category_name AS ParentName, COUNT(pr.product_id) AS ProductCount " +
                "FROM category c " +
                "LEFT JOIN category p ON c.parent_category_id = p.category_id " +
                "LEFT JOIN product pr ON c.category_id = pr.category_id " +
                "WHERE c.category_id = ? " +
                "GROUP BY c.category_id, c.category_name, c.description, c.parent_category_id, c.status, p.category_name";

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

    public boolean deleteCategory(int id) {
        String sql = "DELETE FROM category WHERE category_id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addCategory(Category category) {
        String sql = "INSERT INTO category (category_name, description, parent_category_id, status) VALUES (?, ?, ?, ?)";
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
        String sql = "UPDATE category SET category_name = ?, description = ?, parent_category_id = ?, status = ?, update_at = GETDATE() WHERE category_id = ?";
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
        String sql = "SELECT category_id FROM category WHERE category_id = ?";
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
        StringBuilder sql = new StringBuilder("SELECT category_id FROM category WHERE LOWER(LTRIM(RTRIM(category_name))) = LOWER(LTRIM(RTRIM(?)))");
        if (excludeCategoryId != null) {
            sql.append(" AND category_id <> ?");
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
        String sql = "SELECT category_id AS CategoryID FROM category WHERE LOWER(LTRIM(RTRIM(category_name))) = LOWER(LTRIM(RTRIM(?)))";
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
                " SELECT category_id, parent_category_id FROM category WHERE parent_category_id = ?" +
                " UNION ALL" +
                " SELECT c.category_id, c.parent_category_id FROM category c" +
                " INNER JOIN CategoryTree ct ON c.parent_category_id = ct.category_id" +
                ") SELECT category_id FROM CategoryTree WHERE category_id = ?";

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

    public int[] getCategoryStatistics(String keyword, String status) {
        int[] stats = new int[3]; // [totalItems, totalRootCategories, totalLinkedProducts]
        
        StringBuilder sql = new StringBuilder(
                "WITH CategoryStats AS (" +
                " SELECT c.category_id, c.parent_category_id AS ParentCategoryID, c.status, c.category_name, c.description, p.category_name AS ParentName, " +
                " COUNT(pr.product_id) AS ProductCount " +
                " FROM category c " +
                " LEFT JOIN category p ON c.parent_category_id = p.category_id " +
                " LEFT JOIN product pr ON c.category_id = pr.category_id " +
                " WHERE 1=1"
        );

        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (LOWER(c.category_name) LIKE ? OR LOWER(c.description) LIKE ? OR LOWER(p.category_name) LIKE ? OR EXISTS (SELECT 1 FROM product pr2 WHERE pr2.category_id = c.category_id AND LOWER(pr2.product_name) LIKE ?))");
            String kwPattern = "%" + keyword.trim().toLowerCase() + "%";
            params.add(kwPattern);
            params.add(kwPattern);
            params.add(kwPattern);
            params.add(kwPattern);
        }

        if (status != null && !status.trim().isEmpty()) {
            sql.append(" AND c.status = ?");
            params.add(status.trim());
        }

        sql.append(" GROUP BY c.category_id, c.parent_category_id, c.status, c.category_name, c.description, p.category_name");
        sql.append(") " +
                   "SELECT " +
                   " COUNT(*) AS TotalItems, " +
                   " SUM(CASE WHEN ParentCategoryID IS NULL THEN 1 ELSE 0 END) AS TotalRootCategories, " +
                   " SUM(ProductCount) AS TotalLinkedProducts " +
                   "FROM CategoryStats");

        try (Connection conn = DBContext.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            setParameters(stmt, params);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats[0] = rs.getInt("TotalItems");
                    stats[1] = rs.getInt("TotalRootCategories");
                    stats[2] = rs.getInt("TotalLinkedProducts");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }

    public List<Category> getPaginatedCategories(String keyword, String status, int offset, int limit) {
        List<Category> categories = new ArrayList<>();
        
        StringBuilder sql = new StringBuilder(
                "SELECT c.category_id AS CategoryID, c.category_name AS Name, c.description AS Description, c.parent_category_id AS ParentCategoryID, c.status AS Status, " +
                "p.category_name AS ParentName, COUNT(pr.product_id) AS ProductCount " +
                "FROM category c " +
                "LEFT JOIN category p ON c.parent_category_id = p.category_id " +
                "LEFT JOIN product pr ON c.category_id = pr.category_id " +
                "WHERE 1=1"
        );

        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (LOWER(c.category_name) LIKE ? OR LOWER(c.description) LIKE ? OR LOWER(p.category_name) LIKE ? OR EXISTS (SELECT 1 FROM product pr2 WHERE pr2.category_id = c.category_id AND LOWER(pr2.product_name) LIKE ?))");
            String kwPattern = "%" + keyword.trim().toLowerCase() + "%";
            params.add(kwPattern);
            params.add(kwPattern);
            params.add(kwPattern);
            params.add(kwPattern);
        }

        if (status != null && !status.trim().isEmpty()) {
            sql.append(" AND c.status = ?");
            params.add(status.trim());
        }

        sql.append(" GROUP BY c.category_id, c.category_name, c.description, c.parent_category_id, c.status, p.category_name ");
        sql.append(" ORDER BY c.category_id DESC "); // DESC in SQL to avoid reversing later
        sql.append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        
        params.add(offset);
        params.add(limit);

        try (Connection conn = DBContext.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
             
            setParameters(stmt, params);

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
