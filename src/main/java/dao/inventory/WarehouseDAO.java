package dao.inventory;

import model.Warehouse;
import util.database.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class WarehouseDAO {

    public List<Warehouse> findByBranch(int branchId) throws SQLException {
        List<Warehouse> warehouses = new ArrayList<>();
        String sql = "SELECT * FROM warehouse WHERE branch_id = ? AND status = 'ACTIVE'";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, branchId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    warehouses.add(extractWarehouse(rs));
                }
            }
        }
        return warehouses;
    }

    public List<Warehouse> findAll() throws SQLException {
        List<Warehouse> warehouses = new ArrayList<>();
        String sql = "SELECT * FROM warehouse WHERE status = 'ACTIVE'";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    warehouses.add(extractWarehouse(rs));
                }
            }
        }
        return warehouses;
    }

    private Warehouse extractWarehouse(ResultSet rs) throws SQLException {
        Warehouse w = new Warehouse();
        w.setWarehouseId(rs.getInt("warehouse_id"));
        w.setWarehouseName(rs.getString("warehouse_name"));
        w.setBranchId(rs.getInt("branch_id"));
        w.setAddress(rs.getString("address"));
        w.setStatus(rs.getString("status"));
        if (rs.getTimestamp("created_at") != null) {
            w.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        return w;
    }
}
