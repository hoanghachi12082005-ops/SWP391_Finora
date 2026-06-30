package dao.product;

import dao.common.ICrudDAO;
import model.Category;
import util.database.DBContext;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO implements ICrudDAO<Category> {

    public List<Category> findAll() {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT c.category_id, c.category_name, c.description, c.parent_category_id, c.status, c.created_at, c.update_at, "
                   + "pc.category_name AS parent_name "
                   + "FROM category c "
                   + "LEFT JOIN category pc ON c.parent_category_id = pc.category_id "
                   + "ORDER BY c.category_name";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public Category findById(int id) {
        String sql = "SELECT c.category_id, c.category_name, c.description, c.parent_category_id, c.status, c.created_at, c.update_at, "
                   + "pc.category_name AS parent_name "
                   + "FROM category c "
                   + "LEFT JOIN category pc ON c.parent_category_id = pc.category_id "
                   + "WHERE c.category_id = ?";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return map(rs); }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public boolean insert(Category c) {
        String sql = "INSERT INTO category (category_name, description, parent_category_id, status, created_at, update_at) VALUES (?, ?, ?, ?, GETDATE(), GETDATE())";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getCategoryName());
            ps.setString(2, c.getDescription());
            if (c.getParentCategoryId() != null) ps.setInt(3, c.getParentCategoryId()); else ps.setNull(3, Types.INTEGER);
            ps.setString(4, c.getStatus() != null ? c.getStatus() : "ACTIVE");
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean update(Category c) {
        String sql = "UPDATE category SET category_name = ?, description = ?, parent_category_id = ?, status = ?, update_at = GETDATE() WHERE category_id = ?";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getCategoryName());
            ps.setString(2, c.getDescription());
            if (c.getParentCategoryId() != null) ps.setInt(3, c.getParentCategoryId()); else ps.setNull(3, Types.INTEGER);
            ps.setString(4, c.getStatus());
            ps.setInt(5, c.getCategoryId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean delete(int id) {
        return softDelete(id);
    }

    public boolean softDelete(int id) {
        String sql = "UPDATE category SET status = 'INACTIVE', update_at = GETDATE() WHERE category_id = ?";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    private Category map(ResultSet rs) throws SQLException {
        Category c = new Category();
        c.setCategoryId(rs.getInt("category_id"));
        c.setCategoryName(rs.getString("category_name"));
        c.setDescription(rs.getString("description"));
        int parentId = rs.getInt("parent_category_id"); if (!rs.wasNull()) c.setParentCategoryId(parentId);
        c.setStatus(rs.getString("status"));
        c.setParentCategoryName(rs.getString("parent_name"));
        return c;
    }
}
