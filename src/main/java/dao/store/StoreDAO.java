package dao.store;

import model.Store;
import util.database.DBContext;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StoreDAO {
    // ponytail: maps to Branch table since V3 has no Store table

    public List<Store> findAll() {
        List<Store> list = new ArrayList<>();
        String sql = "SELECT branch_id, branch_name, branch_code, address, district, city, phone, email, status, created_at FROM Branch ORDER BY branch_name";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    private Store map(ResultSet rs) throws SQLException {
        Store s = new Store();
        s.setBranchId(rs.getInt("branch_id"));
        s.setBranchName(rs.getString("branch_name"));
        s.setBranchCode(rs.getString("branch_code"));
        s.setAddress(rs.getString("address"));
        s.setDistrict(rs.getString("district"));
        s.setCity(rs.getString("city"));
        s.setPhone(rs.getString("phone"));
        s.setEmail(rs.getString("email"));
        s.setStatus(rs.getString("status"));
        return s;
    }
}
