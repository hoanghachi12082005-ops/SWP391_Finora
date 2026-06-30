package dao.supplier;

import model.Supplier;
import util.database.DBContext;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SupplierDAO {

    public List<Supplier> findAll() {
        List<Supplier> list = new ArrayList<>();
        String sql = "SELECT supplier_id, supplier_name, phone_number, address, status, created_at, updated_at FROM supplier ORDER BY supplier_name";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public Supplier findById(int id) {
        String sql = "SELECT supplier_id, supplier_name, phone_number, address, status, created_at, updated_at FROM supplier WHERE supplier_id = ?";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return map(rs); }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public boolean insert(Supplier s) {
        String sql = "INSERT INTO supplier (supplier_name, phone_number, address, status, created_at, updated_at) VALUES (?, ?, ?, ?, GETDATE(), GETDATE())";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getSupplierName());
            ps.setString(2, s.getPhoneNumber());
            ps.setString(3, s.getAddress());
            ps.setString(4, s.getStatus() != null ? s.getStatus() : "ACTIVE");
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean update(Supplier s) {
        String sql = "UPDATE supplier SET supplier_name = ?, phone_number = ?, address = ?, status = ?, updated_at = GETDATE() WHERE supplier_id = ?";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getSupplierName());
            ps.setString(2, s.getPhoneNumber());
            ps.setString(3, s.getAddress());
            ps.setString(4, s.getStatus());
            ps.setInt(5, s.getSupplierId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean softDelete(int id) {
        String sql = "UPDATE supplier SET status = 'INACTIVE', updated_at = GETDATE() WHERE supplier_id = ?";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    private Supplier map(ResultSet rs) throws SQLException {
        Supplier s = new Supplier();
        s.setSupplierId(rs.getInt("supplier_id"));
        s.setSupplierName(rs.getString("supplier_name"));
        s.setPhoneNumber(rs.getString("phone_number"));
        s.setAddress(rs.getString("address"));
        s.setStatus(rs.getString("status"));
        if (rs.getTimestamp("created_at") != null) s.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        if (rs.getTimestamp("updated_at") != null) s.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return s;
    }
}
