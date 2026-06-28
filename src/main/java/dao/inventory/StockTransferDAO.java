package dao.inventory;

import model.StockTransfer;
import model.StockTransferDetail;
import util.database.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StockTransferDAO {

    public List<StockTransfer> findAll(int warehouseId) throws SQLException {
        List<StockTransfer> transfers = new ArrayList<>();
        String sql = 
            "SELECT st.*, " +
            "fw.warehouse_name as from_warehouse_name, " +
            "tw.warehouse_name as to_warehouse_name, " +
            "e.fullName as created_by_name " +
            "FROM stock_transfer st " +
            "JOIN warehouse fw ON st.from_warehouse_id = fw.warehouse_id " +
            "JOIN warehouse tw ON st.to_warehouse_id = tw.warehouse_id " +
            "LEFT JOIN Employee e ON st.created_by = e.emp_id " +
            "WHERE (st.from_warehouse_id = ? OR st.to_warehouse_id = ?) " +
            "ORDER BY st.transfer_date DESC";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, warehouseId);
            stmt.setInt(2, warehouseId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    transfers.add(extractTransfer(rs));
                }
            }
        }
        return transfers;
    }

    public List<StockTransfer> findAllGlobal() throws SQLException {
        List<StockTransfer> transfers = new ArrayList<>();
        String sql = 
            "SELECT st.*, " +
            "fw.warehouse_name as from_warehouse_name, " +
            "tw.warehouse_name as to_warehouse_name, " +
            "e.fullName as created_by_name " +
            "FROM stock_transfer st " +
            "JOIN warehouse fw ON st.from_warehouse_id = fw.warehouse_id " +
            "JOIN warehouse tw ON st.to_warehouse_id = tw.warehouse_id " +
            "LEFT JOIN Employee e ON st.created_by = e.emp_id " +
            "ORDER BY st.transfer_date DESC";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    transfers.add(extractTransfer(rs));
                }
            }
        }
        return transfers;
    }

    public boolean createTransfer(StockTransfer transfer, List<StockTransferDetail> details) throws SQLException {
        String sql = "INSERT INTO stock_transfer (from_warehouse_id, to_warehouse_id, transfer_code, status, note, created_by) VALUES (?, ?, ?, 'PENDING', ?, ?)";
        String detailSql = "INSERT INTO stock_transfer_detail (stock_transfer_id, product_id, quantity) VALUES (?, ?, ?)";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        PreparedStatement detailStmt = null;
        ResultSet rs = null;

        try {
            conn = DBContext.getConnection();
            conn.setAutoCommit(false);

            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, transfer.getFromWarehouseId());
            stmt.setInt(2, transfer.getToWarehouseId());
            stmt.setString(3, transfer.getTransferCode());
            stmt.setString(4, transfer.getNote());
            stmt.setInt(5, transfer.getCreatedBy());
            stmt.executeUpdate();

            rs = stmt.getGeneratedKeys();
            int transferId = 0;
            if (rs.next()) {
                transferId = rs.getInt(1);
            }

            detailStmt = conn.prepareStatement(detailSql);
            for (StockTransferDetail d : details) {
                detailStmt.setInt(1, transferId);
                detailStmt.setInt(2, d.getProductId());
                detailStmt.setInt(3, d.getQuantity());
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

    public void updateStatus(int transferId, String newStatus) throws SQLException {
        String sql = "UPDATE stock_transfer SET status = ? WHERE stock_transfer_id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newStatus);
            stmt.setInt(2, transferId);
            stmt.executeUpdate();
        }
    }

    private StockTransfer extractTransfer(ResultSet rs) throws SQLException {
        StockTransfer t = new StockTransfer();
        t.setStockTransferId(rs.getInt("stock_transfer_id"));
        t.setFromWarehouseId(rs.getInt("from_warehouse_id"));
        t.setToWarehouseId(rs.getInt("to_warehouse_id"));
        t.setTransferCode(rs.getString("transfer_code"));
        if (rs.getTimestamp("transfer_date") != null) {
            t.setTransferDate(rs.getTimestamp("transfer_date").toLocalDateTime());
        }
        t.setStatus(rs.getString("status"));
        t.setNote(rs.getString("note"));
        t.setCreatedBy(rs.getInt("created_by"));
        t.setFromWarehouseName(rs.getString("from_warehouse_name"));
        t.setToWarehouseName(rs.getString("to_warehouse_name"));
        t.setCreatedByName(rs.getString("created_by_name"));
        return t;
    }
}
