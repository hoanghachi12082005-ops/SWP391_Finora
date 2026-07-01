package dao.sales;

import model.Customer;
import util.database.DBContext;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {

    public List<Customer> findAllActive() {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT * FROM Customer ORDER BY cus_id DESC";
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

    public Customer findById(int id) {
        String sql = "SELECT * FROM Customer WHERE cus_id = ?";
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

    public Customer findByPhone(String phone) {
        String sql = "SELECT * FROM Customer WHERE phone = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phone);
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

    public List<Customer> searchActive(String keyword) {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT * FROM Customer WHERE (full_name LIKE ? OR phone LIKE ?) ORDER BY cus_id DESC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
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

    private Customer mapRow(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setCusId(rs.getInt("cus_id"));
        c.setFullName(rs.getString("full_name"));
        c.setGender(rs.getString("gender"));
        c.setBod(rs.getString("bod"));
        c.setAddress(rs.getString("address"));
        c.setEmail(rs.getString("email"));
        c.setPhone(rs.getString("phone"));
        c.setPasswordHash(null);
        c.setStatus(Customer.CustomerStatus.ACTIVE);
        
        String typeStr = rs.getString("cus_type");
        if (typeStr != null) {
            try {
                c.setCusType(Customer.CustomerType.valueOf(typeStr.toUpperCase()));
            } catch (IllegalArgumentException e) {
                c.setCusType(Customer.CustomerType.REGULAR);
            }
        }
        
        c.setTotalSpent(rs.getDouble("total_spent"));
        c.setCreatedAt(rs.getString("created_at"));
        c.setUpdatedAt(rs.getString("updated_at"));
        return c;
    }

    // ── Alias methods (theo spec POS) ──────────────────────────

    /** Alias cho findAllActive(). */
    public List<Customer> getAll() {
        return findAllActive();
    }

    /** Alias cho searchActive(keyword). */
    public List<Customer> search(String keyword) {
        return searchActive(keyword);
    }

    /**
     * Thêm khách hàng mới, trả về cus_id tự tăng. Trả -1 nếu thất bại.
     */
    public int insert(Customer c) {
        String sql = "INSERT INTO customer (full_name, gender, bod, address, email, phone, cus_type, total_spent, created_at, updated_at) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, 0, GETDATE(), GETDATE())";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getFullName());
            ps.setString(2, c.getGender());
            if (c.getBod() != null && !c.getBod().isBlank()) {
                ps.setString(3, c.getBod());
            } else {
                ps.setNull(3, Types.DATE);
            }
            ps.setString(4, c.getAddress());
            ps.setString(5, c.getEmail());
            ps.setString(6, c.getPhone());
            ps.setString(7, c.getCusType() != null ? c.getCusType().name() : "REGULAR");
            if (ps.executeUpdate() > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) return keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
}

