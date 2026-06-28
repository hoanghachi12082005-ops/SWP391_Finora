package dao.inventory;

import model.StockCheck;
import model.StockCheckDetail;
import util.database.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StockCheckDAO {

    public List<StockCheck> findAll(int warehouseId) throws SQLException {
        List<StockCheck> checks = new ArrayList<>();
        String sql = 
            "SELECT sc.*, " +
            "w.warehouse_name, " +
            "c.fullName as created_by_name, " +
            "a.fullName as approved_by_name, " +
            "(SELECT SUM(difference) FROM stock_check_detail WHERE stock_check_id = sc.stock_check_id) as total_difference " +
            "FROM stock_check sc " +
            "JOIN warehouse w ON sc.warehouse_id = w.warehouse_id " +
            "LEFT JOIN Employee c ON sc.created_by = c.emp_id " +
            "LEFT JOIN Employee a ON sc.approved_by = a.emp_id " +
            "WHERE sc.warehouse_id = ? " +
            "ORDER BY sc.check_date DESC";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, warehouseId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    checks.add(extractCheck(rs));
                }
            }
        }
        return checks;
    }

    public List<StockCheck> findAllGlobal() throws SQLException {
        List<StockCheck> checks = new ArrayList<>();
        String sql = 
            "SELECT sc.*, " +
            "w.warehouse_name, " +
            "c.fullName as created_by_name, " +
            "a.fullName as approved_by_name, " +
            "(SELECT SUM(difference) FROM stock_check_detail WHERE stock_check_id = sc.stock_check_id) as total_difference " +
            "FROM stock_check sc " +
            "JOIN warehouse w ON sc.warehouse_id = w.warehouse_id " +
            "LEFT JOIN Employee c ON sc.created_by = c.emp_id " +
            "LEFT JOIN Employee a ON sc.approved_by = a.emp_id " +
            "ORDER BY sc.check_date DESC";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    checks.add(extractCheck(rs));
                }
            }
        }
        return checks;
    }

    public boolean createCheck(StockCheck check, List<StockCheckDetail> details) throws SQLException {
        String sql = "INSERT INTO stock_check (warehouse_id, check_code, status, note, created_by) VALUES (?, ?, 'PENDING', ?, ?)";
        String detailSql = "INSERT INTO stock_check_detail (stock_check_id, product_id, system_quantity, actual_quantity, note) VALUES (?, ?, ?, ?, ?)";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        PreparedStatement detailStmt = null;
        ResultSet rs = null;

        try {
            conn = DBContext.getConnection();
            conn.setAutoCommit(false);

            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, check.getWarehouseId());
            stmt.setString(2, check.getCheckCode());
            stmt.setString(3, check.getNote());
            stmt.setInt(4, check.getCreatedBy());
            stmt.executeUpdate();

            rs = stmt.getGeneratedKeys();
            int checkId = 0;
            if (rs.next()) {
                checkId = rs.getInt(1);
            }

            detailStmt = conn.prepareStatement(detailSql);
            for (StockCheckDetail d : details) {
                detailStmt.setInt(1, checkId);
                detailStmt.setInt(2, d.getProductId());
                detailStmt.setInt(3, d.getSystemQuantity());
                detailStmt.setInt(4, d.getActualQuantity());
                detailStmt.setString(5, d.getNote());
                detailStmt.addBatch();
            }
            detailStmt.executeBatch();

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (rs != null) rs.close();
            if (detailStmt != null) detailStmt.close();
            if (stmt != null) stmt.close();
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    public void approveCheck(int checkId, int approvedBy) throws SQLException {
        String sql = "UPDATE stock_check SET status = 'APPROVED', approved_by = ?, approved_at = GETDATE() WHERE stock_check_id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, approvedBy);
            stmt.setInt(2, checkId);
            stmt.executeUpdate();
        }
    }

    public void rejectCheck(int checkId, int approvedBy) throws SQLException {
        String sql = "UPDATE stock_check SET status = 'REJECTED', approved_by = ?, approved_at = GETDATE() WHERE stock_check_id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, approvedBy);
            stmt.setInt(2, checkId);
            stmt.executeUpdate();
        }
    }

    private StockCheck extractCheck(ResultSet rs) throws SQLException {
        StockCheck c = new StockCheck();
        c.setStockCheckId(rs.getInt("stock_check_id"));
        c.setWarehouseId(rs.getInt("warehouse_id"));
        c.setCheckCode(rs.getString("check_code"));
        if (rs.getTimestamp("check_date") != null) {
            c.setCheckDate(rs.getTimestamp("check_date").toLocalDateTime());
        }
        c.setStatus(rs.getString("status"));
        c.setNote(rs.getString("note"));
        c.setCreatedBy(rs.getInt("created_by"));
        if (rs.getObject("approved_by") != null) {
            c.setApprovedBy(rs.getInt("approved_by"));
        }
        if (rs.getTimestamp("approved_at") != null) {
            c.setApprovedAt(rs.getTimestamp("approved_at").toLocalDateTime());
        }
        c.setWarehouseName(rs.getString("warehouse_name"));
        c.setCreatedByName(rs.getString("created_by_name"));
        c.setApprovedByName(rs.getString("approved_by_name"));
        c.setTotalDifference(rs.getInt("total_difference"));
        return c;
    }
}
