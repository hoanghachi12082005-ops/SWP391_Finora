package category.dao;

import category.model.Category;
import common.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Category DAO
 * Data Access Object cho bảng Category trong DBFinora.
 * Cung cấp các phương thức CRUD và truy vấn dữ liệu.
 * 
 * @author Finora Team
 * @version 1.0
 */
public class CategoryDAO {

    // ==================== COLUMNS ====================
    private static final String COLUMNS = "CategoryID, Name, Description, ParentID, Status";
    private static final String COLUMNS_WITH_PARENT = "c.CategoryID, c.Name, c.Description, c.ParentID, c.Status, p.Name AS ParentName, COUNT(pr.ProductID) AS ProductCount";

    // ==================== INSERT ====================
    
    /**
     * Thêm mới một category.
     * 
     * @param category Category cần thêm
     * @return true nếu thêm thành công, false nếu thất bại
     */
    public boolean insert(Category category) {
        String sql = "INSERT INTO Category (Name, Description, ParentID, Status) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, category.getName());
            stmt.setString(2, category.getDescription());
            setNullableInt(stmt, 3, category.getParentId());
            stmt.setString(4, category.getStatus());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logError("insert", e);
        }
        return false;
    }

    // ==================== UPDATE ====================
    
    /**
     * Cập nhật thông tin category.
     * 
     * @param category Category cần cập nhật
     * @return true nếu cập nhật thành công, false nếu thất bại
     */
    public boolean update(Category category) {
        String sql = "UPDATE Category SET Name = ?, Description = ?, ParentID = ?, Status = ? WHERE CategoryID = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, category.getName());
            stmt.setString(2, category.getDescription());
            setNullableInt(stmt, 3, category.getParentId());
            stmt.setString(4, category.getStatus());
            stmt.setInt(5, category.getCategoryId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logError("update", e);
        }
        return false;
    }

    // ==================== DELETE ====================
    
    /**
     * Xóa category (hard delete - chỉ xóa nếu không có sản phẩm liên kết).
     * 
     * @param categoryId ID của category cần xóa
     * @return true nếu xóa thành công, false nếu thất bại
     */
    public boolean delete(int categoryId) {
        // Kiểm tra xem có sản phẩm liên kết không
        if (hasLinkedProducts(categoryId)) {
            return false;
        }
        
        String sql = "DELETE FROM Category WHERE CategoryID = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, categoryId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logError("delete", e);
        }
        return false;
    }

    // ==================== FIND BY ID ====================
    
    /**
     * Tìm category theo ID.
     * 
     * @param categoryId ID của category
     * @return Category tìm được hoặc null nếu không tồn tại
     */
    public Category findById(int categoryId) {
        String sql = "SELECT " + COLUMNS_WITH_PARENT + " FROM Category c " +
                     "LEFT JOIN Category p ON c.ParentID = p.CategoryID " +
                     "LEFT JOIN Product pr ON c.CategoryID = pr.CategoryID " +
                     "WHERE c.CategoryID = ? " +
                     "GROUP BY c.CategoryID, c.Name, c.Description, c.ParentID, c.Status, p.Name";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, categoryId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractCategory(rs);
                }
            }
        } catch (SQLException e) {
            logError("findById", e);
        }
        return null;
    }

    // ==================== FIND ALL ====================
    
    /**
     * Lấy danh sách tất cả category.
     * 
     * @return Danh sách category
     */
    public List<Category> findAll() {
        return findAllWithPagination(null, null, null, 1, Integer.MAX_VALUE);
    }

    /**
     * Lấy danh sách category đang hoạt động.
     * 
     * @return Danh sách category active
     */
    public List<Category> findAllActive() {
        return findAllWithPagination(null, "active", null, 1, Integer.MAX_VALUE);
    }

    /**
     * Lấy danh sách category với phân trang và bộ lọc.
     * 
     * @param keyword   Từ khóa tìm kiếm (name, description)
     * @param status    Trạng thái lọc (active/inactive/null)
     * @param parentName Tên nhóm cha để lọc
     * @param page      Trang hiện tại (1-indexed)
     * @param limit     Số bản ghi mỗi trang
     * @return Danh sách category
     */
    public List<Category> findAllWithPagination(String keyword, String status, String parentName, int page, int limit) {
        List<Category> categories = new ArrayList<>();
        int offset = (page - 1) * limit;
        
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ").append(COLUMNS_WITH_PARENT).append(" FROM Category c ");
        sql.append("LEFT JOIN Category p ON c.ParentID = p.CategoryID ");
        sql.append("LEFT JOIN Product pr ON c.CategoryID = pr.CategoryID ");
        sql.append("WHERE 1 = 1 ");
        
        List<Object> params = new ArrayList<>();
        appendFilters(sql, params, keyword, status, parentName);
        
        sql.append("GROUP BY c.CategoryID, c.Name, c.Description, c.ParentID, c.Status, p.Name ");
        sql.append("ORDER BY c.CategoryID ASC ");
        sql.append("OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            setParameters(stmt, params);
            stmt.setInt(params.size() + 1, offset); 
            stmt.setInt(params.size() + 2, limit);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    categories.add(extractCategory(rs));
                }
            }
        } catch (SQLException e) {
            logError("findAllWithPagination", e);
        }
        return categories;
    }

    // ==================== COUNT ====================
    
    /**
     * Đếm tổng số category theo bộ lọc.
     * 
     * @param keyword    Từ khóa tìm kiếm
     * @param status     Trạng thái
     * @param parentName Tên nhóm cha
     * @return Số lượng category
     */
    public int count(String keyword, String status, String parentName) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(1) FROM Category c ");
        sql.append("LEFT JOIN Category p ON c.ParentID = p.CategoryID ");
        sql.append("WHERE 1 = 1 ");
        
        List<Object> params = new ArrayList<>();
        appendFilters(sql, params, keyword, status, parentName);
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            setParameters(stmt, params);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logError("count", e);
        }
        return 0;
    }

    /**
     * Đếm số category là nhóm gốc.
     * 
     * @param keyword Từ khóa tìm kiếm
     * @param status  Trạng thái
     * @return Số lượng nhóm gốc
     */
    public int countRootCategories(String keyword, String status) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(1) FROM Category c ");
        sql.append("LEFT JOIN Category p ON c.ParentID = p.CategoryID ");
        sql.append("WHERE c.ParentID IS NULL ");
        
        List<Object> params = new ArrayList<>();
        appendFilters(sql, params, keyword, status, null);
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            setParameters(stmt, params);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logError("countRootCategories", e);
        }
        return 0;
    }

    /**
     * Đếm số sản phẩm liên kết với category.
     * 
     * @param keyword    Từ khóa tìm kiếm
     * @param status     Trạng thái
     * @param parentName Tên nhóm cha
     * @return Số lượng sản phẩm liên kết
     */
    public int countLinkedProducts(String keyword, String status, String parentName) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(pr.ProductID) FROM Category c ");
        sql.append("LEFT JOIN Category p ON c.ParentID = p.CategoryID ");
        sql.append("LEFT JOIN Product pr ON c.CategoryID = pr.CategoryID ");
        sql.append("WHERE 1 = 1 ");
        
        List<Object> params = new ArrayList<>();
        appendFilters(sql, params, keyword, status, parentName);
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            setParameters(stmt, params);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logError("countLinkedProducts", e);
        }
        return 0;
    }

    // ==================== VALIDATION ====================
    
    /**
     * Kiểm tra category ID có tồn tại không.
     * 
     * @param categoryId ID cần kiểm tra
     * @return true nếu tồn tại
     */
    public boolean existsById(int categoryId) {
        String sql = "SELECT 1 FROM Category WHERE CategoryID = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, categoryId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logError("existsById", e);
        }
        return false;
    }

    /**
     * Kiểm tra tên category đã tồn tại chưa (không phân biệt hoa thường, trim).
     * 
     * @param name              Tên cần kiểm tra
     * @param excludeCategoryId ID category cần loại trừ (khi update)
     * @return true nếu đã tồn tại
     */
    public boolean existsByName(String name, Integer excludeCategoryId) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT 1 FROM Category WHERE LOWER(LTRIM(RTRIM(Name))) = LOWER(LTRIM(RTRIM(?)))");
        
        if (excludeCategoryId != null) {
            sql.append(" AND CategoryID <> ?");
        }
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            stmt.setString(1, name);
            if (excludeCategoryId != null) {
                stmt.setInt(2, excludeCategoryId);
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logError("existsByName", e);
        }
        return false;
    }

    /**
     * Kiểm tra category có sản phẩm liên kết không.
     * 
     * @param categoryId ID category cần kiểm tra
     * @return true nếu có sản phẩm liên kết
     */
    public boolean hasLinkedProducts(int categoryId) {
        String sql = "SELECT 1 FROM Product WHERE CategoryID = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, categoryId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logError("hasLinkedProducts", e);
        }
        return false;
    }

    /**
     * Kiểm tra category có phải là ancestor của category khác không.
     * Sử dụng recursive CTE để kiểm tra cây phân cấp.
     * 
     * @param categoryId      ID category cần kiểm tra
     * @param candidateParentId ID candidate parent
     * @return true nếu categoryId là ancestor của candidateParentId
     */
    public boolean isAncestorOf(int categoryId, int candidateParentId) {
        String sql = "WITH CategoryTree AS ( " +
                      "  SELECT CategoryID, ParentID FROM Category WHERE ParentID = ? " +
                      "  UNION ALL " +
                      "  SELECT c.CategoryID, c.ParentID FROM Category c " +
                      "  INNER JOIN CategoryTree ct ON c.ParentID = ct.CategoryID " +
                      ") SELECT 1 FROM CategoryTree WHERE CategoryID = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, categoryId);
            stmt.setInt(2, candidateParentId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logError("isAncestorOf", e);
        }
        return false;
    }

    // ==================== PARENT OPTIONS ====================
    
    /**
     * Lấy danh sách category dùng làm parent options (chỉ lấy nhóm gốc và active).
     * 
     * @return Danh sách category
     */
    public List<Category> findParentOptions() {
        List<Category> parents = new ArrayList<>();
        String sql = "SELECT CategoryID, Name FROM Category WHERE ParentID IS NULL AND Status = 'active' ORDER BY Name";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Category parent = new Category();
                parent.setCategoryId(rs.getInt("CategoryID"));
                parent.setName(rs.getString("Name"));
                parents.add(parent);
            }
        } catch (SQLException e) {
            logError("findParentOptions", e);
        }
        return parents;
    }

    /**
     * Lấy ID category theo tên.
     * 
     * @param name Tên category
     * @return ID hoặc null nếu không tìm thấy
     */
    public Integer findIdByName(String name) {
        String sql = "SELECT CategoryID FROM Category WHERE LOWER(LTRIM(RTRIM(Name))) = LOWER(LTRIM(RTRIM(?)))";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, name);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("CategoryID");
                }
            }
        } catch (SQLException e) {
            logError("findIdByName", e);
        }
        return null;
    }

    // ==================== PRIVATE HELPERS ====================
    
    /**
     * Nối các điều kiện lọc vào câu SQL.
     */
    private void appendFilters(StringBuilder sql, List<Object> params, String keyword, String status, String parentName) {
        // Keyword filter
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (c.Name LIKE ? OR c.Description LIKE ?) ");
            String pattern = "%" + keyword.trim() + "%";
            params.add(pattern);
            params.add(pattern);
        }
        
        // Status filter
        if (status != null && !status.trim().isEmpty()) {
            sql.append("AND c.Status = ? ");
            params.add(status.trim());
        }
        
        // Parent filter
        if (parentName != null && !parentName.trim().isEmpty()) {
            if (isRootParentFilter(parentName)) {
                sql.append("AND c.ParentID IS NULL ");
            } else {
                sql.append("AND p.Name = ? ");
                params.add(parentName.trim());
            }
        }
    }

    /**
     * Kiểm tra có phải lọc nhóm gốc không.
     */
    private boolean isRootParentFilter(String parentName) {
        String trimmed = parentName == null ? "" : parentName.trim();
        return trimmed.isEmpty() || "goc".equalsIgnoreCase(trimmed) || "gốc".equalsIgnoreCase(trimmed);
    }

    /**
     * Thiết lập giá trị nullable int cho PreparedStatement.
     */
    private void setNullableInt(PreparedStatement stmt, int index, Integer value) throws SQLException {
        if (value == null) {
            stmt.setNull(index, java.sql.Types.INTEGER);
        } else {
            stmt.setInt(index, value);
        }
    }

    /**
     * Thiết lập parameters cho PreparedStatement.
     */
    private void setParameters(PreparedStatement stmt, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            stmt.setObject(i + 1, params.get(i));
        }
    }

    /**
     * Trích xuất Category từ ResultSet.
     */
    private Category extractCategory(ResultSet rs) throws SQLException {
        Category category = new Category();
        category.setCategoryId(rs.getInt("CategoryID"));
        category.setName(rs.getString("Name"));
        category.setDescription(rs.getString("Description"));
        
        int parentId = rs.getInt("ParentID");
        category.setParentId(rs.wasNull() ? null : parentId);
        
        category.setParentName(rs.getString("ParentName"));
        category.setStatus(rs.getString("Status"));
        category.setProductCount(rs.getInt("ProductCount"));
        
        return category;
    }

    /**
     * Log lỗi SQL.
     */
    private void logError(String method, SQLException e) {
        System.err.println("CategoryDAO." + method + " - SQL Error: " + e.getMessage());
        e.printStackTrace();
    }
}
