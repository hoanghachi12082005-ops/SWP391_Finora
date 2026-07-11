package dao.inventory;

import util.database.DBContext;
import model.StockTransfer;
import model.StockTransferDetail;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StockTransferDAO {
    
    public List<StockTransfer> findAllByStatus(int warehouseId, String status) throws Exception {
        List<StockTransfer> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT st.*, " +
                "fw.warehouse_name as from_warehouse_name, tw.warehouse_name as to_warehouse_name, e.fullName as created_by_name " +
                "FROM stock_transfer st " +
                "LEFT JOIN warehouse fw ON st.from_warehouse_id = fw.warehouse_id " +
                "LEFT JOIN warehouse tw ON st.to_warehouse_id = tw.warehouse_id " +
                "LEFT JOIN Employee e ON st.created_by = e.emp_id " +
                "WHERE 1=1 ");
        
        if (warehouseId > 0) {
            sql.append("AND (st.from_warehouse_id = ? OR st.to_warehouse_id = ?) ");
        }
        if (status != null && !status.isEmpty()) {
            if (status.equals("PENDING_IN_TRANSIT")) {
                sql.append("AND st.status IN ('PENDING', 'IN_TRANSIT') ");
            } else {
                sql.append("AND st.status = ? ");
            }
        }
        sql.append("ORDER BY st.transfer_date DESC");

        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int paramIndex = 1;
            if (warehouseId > 0) {
                ps.setInt(paramIndex++, warehouseId);
                ps.setInt(paramIndex++, warehouseId);
            }
            if (status != null && !status.isEmpty() && !status.equals("PENDING_IN_TRANSIT")) {
                ps.setString(paramIndex++, status);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StockTransfer t = new StockTransfer();
                    t.setStockTransferId(rs.getInt("stock_transfer_id"));
                    t.setFromWarehouseId(rs.getInt("from_warehouse_id"));
                    t.setToWarehouseId(rs.getInt("to_warehouse_id"));
                    t.setTransferCode(rs.getString("transfer_code"));
                    t.setTransferDate(rs.getTimestamp("transfer_date"));
                    t.setStatus(rs.getString("status"));
                    t.setNote(rs.getString("note"));
                    t.setCreatedBy(rs.getInt("created_by"));
                    t.setFromWarehouseName(rs.getString("from_warehouse_name"));
                    t.setToWarehouseName(rs.getString("to_warehouse_name"));
                    t.setCreatedByName(rs.getString("created_by_name"));
                    list.add(t);
                }
            }
        }
        return list;
    }

    public boolean updateStatus(int transferId, String status) throws Exception {
        try (Connection conn = new DBContext().getConnection()) {
            return updateStatus(conn, transferId, status);
        }
    }

    public boolean updateStatus(Connection conn, int transferId, String status) throws Exception {
        String sql = "UPDATE stock_transfer SET status = ? WHERE stock_transfer_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, transferId);
            return ps.executeUpdate() > 0;
        }
    }

    public StockTransfer findById(int id) throws Exception {
        String sql = "SELECT st.*, fw.warehouse_name as from_warehouse_name, tw.warehouse_name as to_warehouse_name, e.fullName as created_by_name " +
                     "FROM stock_transfer st " +
                     "LEFT JOIN warehouse fw ON st.from_warehouse_id = fw.warehouse_id " +
                     "LEFT JOIN warehouse tw ON st.to_warehouse_id = tw.warehouse_id " +
                     "LEFT JOIN Employee e ON st.created_by = e.emp_id " +
                     "WHERE st.stock_transfer_id = ?";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    StockTransfer t = new StockTransfer();
                    t.setStockTransferId(rs.getInt("stock_transfer_id"));
                    t.setFromWarehouseId(rs.getInt("from_warehouse_id"));
                    t.setToWarehouseId(rs.getInt("to_warehouse_id"));
                    t.setTransferCode(rs.getString("transfer_code"));
                    t.setTransferDate(rs.getTimestamp("transfer_date"));
                    t.setStatus(rs.getString("status"));
                    t.setNote(rs.getString("note"));
                    t.setCreatedBy(rs.getInt("created_by"));
                    t.setFromWarehouseName(rs.getString("from_warehouse_name"));
                    t.setToWarehouseName(rs.getString("to_warehouse_name"));
                    t.setCreatedByName(rs.getString("created_by_name"));
                    return t;
                }
            }
        }
        return null;
    }

    public List<StockTransferDetail> getTransferDetails(int stockTransferId) throws Exception {
        List<StockTransferDetail> list = new ArrayList<>();
        String sql = "SELECT d.*, p.product_codebar, p.product_name, u.unit_name, c.category_name " +
                     "FROM stock_transfer_detail d " +
                     "JOIN [product] p ON d.product_id = p.product_id " +
                     "LEFT JOIN unit u ON p.unit_id = u.unit_id " +
                     "LEFT JOIN category c ON p.category_id = c.category_id " +
                     "WHERE d.stock_transfer_id = ?";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, stockTransferId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StockTransferDetail d = new StockTransferDetail();
                    d.setStockTransferDetailId(rs.getInt("stock_transfer_detail_id"));
                    d.setStockTransferId(rs.getInt("stock_transfer_id"));
                    d.setProductId(rs.getInt("product_id"));
                    d.setQuantity(rs.getInt("quantity"));
                    d.setProductCodebar(rs.getString("product_codebar"));
                    d.setProductName(rs.getString("product_name"));
                    d.setUnitName(rs.getString("unit_name"));
                    d.setCategoryName(rs.getString("category_name"));
                    list.add(d);
                }
            }
        }
        return list;
    }

    public int getPendingCount(int warehouseId) throws Exception {
        String sql = "SELECT COUNT(*) FROM stock_transfer WHERE status IN ('PENDING_DISPATCH', 'APPROVED_DISPATCH', 'IN_TRANSIT')";
        if (warehouseId > 0) {
            sql += " AND (from_warehouse_id = ? OR to_warehouse_id = ?)";
        }
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (warehouseId > 0) {
                ps.setInt(1, warehouseId);
                ps.setInt(2, warehouseId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    public String getTransferProgress(int stockTransferId) throws Exception {
        StockTransfer t = findById(stockTransferId);
        if (t != null) {
            return t.getStatus(); // Can be expanded to be more descriptive based on logic
        }
        return "Unknown";
    }

    public int createTransfer(StockTransfer t, List<StockTransferDetail> details) throws Exception {
        String sql = "INSERT INTO stock_transfer (from_warehouse_id, to_warehouse_id, transfer_code, status, note, created_by) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = new DBContext().getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, t.getFromWarehouseId());
                ps.setInt(2, t.getToWarehouseId());
                ps.setString(3, t.getTransferCode());
                ps.setString(4, t.getStatus());
                ps.setString(5, t.getNote());
                ps.setInt(6, t.getCreatedBy());
                ps.executeUpdate();
                
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        t.setStockTransferId(id);
                        
                        String sqlDetail = "INSERT INTO stock_transfer_detail (stock_transfer_id, product_id, quantity) VALUES (?, ?, ?)";
                        try (PreparedStatement psDetail = conn.prepareStatement(sqlDetail)) {
                            for (StockTransferDetail d : details) {
                                psDetail.setInt(1, id);
                                psDetail.setInt(2, d.getProductId());
                                psDetail.setInt(3, d.getQuantity());
                                psDetail.addBatch();
                            }
                            psDetail.executeBatch();
                        }
                        conn.commit();
                        return id;
                    }
                }
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
        return 0;
    }
}
