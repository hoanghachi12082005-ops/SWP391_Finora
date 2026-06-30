package dao.product;

import dao.common.ICrudDAO;
import model.Product;
import util.database.DBContext;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO implements ICrudDAO<Product> {

    public List<Product> findAll() {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT p.product_id, p.product_codebar, p.product_name, p.category_id, p.unit_id, p.selling_price, p.created_at, p.update_at, "
                   + "c.category_name, u.unit_name "
                   + "FROM [product] p "
                   + "LEFT JOIN category c ON p.category_id = c.category_id "
                   + "LEFT JOIN unit u ON p.unit_id = u.unit_id "
                   + "ORDER BY p.product_name";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public Product findById(int id) {
        String sql = "SELECT p.product_id, p.product_codebar, p.product_name, p.category_id, p.unit_id, p.selling_price, p.created_at, p.update_at, "
                   + "c.category_name, u.unit_name "
                   + "FROM [product] p "
                   + "LEFT JOIN category c ON p.category_id = c.category_id "
                   + "LEFT JOIN unit u ON p.unit_id = u.unit_id "
                   + "WHERE p.product_id = ?";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return map(rs); }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public boolean insert(Product p) {
        String sql = "INSERT INTO [product] (product_codebar, product_name, category_id, unit_id, selling_price, created_at, update_at) "
                   + "VALUES (?, ?, ?, ?, ?, GETDATE(), GETDATE())";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getProductCodebar());
            ps.setString(2, p.getProductName());
            if (p.getCategoryId() != null) ps.setInt(3, p.getCategoryId()); else ps.setNull(3, Types.INTEGER);
            if (p.getUnitId() != null) ps.setInt(4, p.getUnitId()); else ps.setNull(4, Types.INTEGER);
            ps.setBigDecimal(5, p.getSellingPrice() != null ? p.getSellingPrice() : java.math.BigDecimal.ZERO);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean update(Product p) {
        String sql = "UPDATE [product] SET product_codebar = ?, product_name = ?, category_id = ?, unit_id = ?, selling_price = ?, update_at = GETDATE() WHERE product_id = ?";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getProductCodebar());
            ps.setString(2, p.getProductName());
            if (p.getCategoryId() != null) ps.setInt(3, p.getCategoryId()); else ps.setNull(3, Types.INTEGER);
            if (p.getUnitId() != null) ps.setInt(4, p.getUnitId()); else ps.setNull(4, Types.INTEGER);
            ps.setBigDecimal(5, p.getSellingPrice() != null ? p.getSellingPrice() : java.math.BigDecimal.ZERO);
            ps.setInt(6, p.getProductId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean delete(int id) {
        return softDelete(id);
    }

    public boolean softDelete(int id) {
        String sql = "UPDATE [product] SET update_at = GETDATE() WHERE product_id = ?";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    private Product map(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setProductId(rs.getInt("product_id"));
        p.setProductCodebar(rs.getString("product_codebar"));
        p.setProductName(rs.getString("product_name"));
        int catId = rs.getInt("category_id"); if (!rs.wasNull()) p.setCategoryId(catId);
        int unitId = rs.getInt("unit_id"); if (!rs.wasNull()) p.setUnitId(unitId);
        p.setSellingPrice(rs.getBigDecimal("selling_price"));
        p.setCategoryName(rs.getString("category_name"));
        p.setUnitName(rs.getString("unit_name"));
        return p;
    }
}
