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
                "fw.warehouse_name as from_warehouse_name, tw.warehouse_name as to_warehouse_name, e.fullName as created_by_name, e2.fullName as approved_by_name " +
                "FROM stock_transfer st " +
                "LEFT JOIN warehouse fw ON st.from_warehouse_id = fw.warehouse_id " +
                "LEFT JOIN warehouse tw ON st.to_warehouse_id = tw.warehouse_id " +
                "LEFT JOIN Employee e ON st.created_by = e.emp_id " +
                "LEFT JOIN Employee e2 ON st.approved_by = e2.emp_id " +
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
                    int ab = rs.getInt("approved_by"); if (!rs.wasNull()) t.setApprovedBy(ab);
                    t.setApprovedByName(rs.getString("approved_by_name"));
                    list.add(t);
                }
            }
        }
        return list;
    }

    public List<StockTransfer> findAllByStatusFiltered(int warehouseId, String status, String transferCode, Integer partnerWarehouseId) throws Exception {
        List<StockTransfer> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT st.*, " +
                "fw.warehouse_name as from_warehouse_name, tw.warehouse_name as to_warehouse_name, e.fullName as created_by_name, e2.fullName as approved_by_name " +
                "FROM stock_transfer st " +
                "LEFT JOIN warehouse fw ON st.from_warehouse_id = fw.warehouse_id " +
                "LEFT JOIN warehouse tw ON st.to_warehouse_id = tw.warehouse_id " +
                "LEFT JOIN Employee e ON st.created_by = e.emp_id " +
                "LEFT JOIN Employee e2 ON st.approved_by = e2.emp_id " +
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
        if (transferCode != null && !transferCode.trim().isEmpty()) {
            sql.append("AND st.transfer_code LIKE ? ");
        }
        if (partnerWarehouseId != null && partnerWarehouseId > 0) {
            if (warehouseId > 0) {
                sql.append("AND ((st.from_warehouse_id = ? AND st.to_warehouse_id = ?) OR (st.to_warehouse_id = ? AND st.from_warehouse_id = ?)) ");
            } else {
                sql.append("AND (st.from_warehouse_id = ? OR st.to_warehouse_id = ?) ");
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
            if (transferCode != null && !transferCode.trim().isEmpty()) {
                ps.setString(paramIndex++, "%" + transferCode.trim() + "%");
            }
            if (partnerWarehouseId != null && partnerWarehouseId > 0) {
                if (warehouseId > 0) {
                    ps.setInt(paramIndex++, warehouseId);
                    ps.setInt(paramIndex++, partnerWarehouseId);
                    ps.setInt(paramIndex++, warehouseId);
                    ps.setInt(paramIndex++, partnerWarehouseId);
                } else {
                    ps.setInt(paramIndex++, partnerWarehouseId);
                    ps.setInt(paramIndex++, partnerWarehouseId);
                }
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
                    int ab = rs.getInt("approved_by"); if (!rs.wasNull()) t.setApprovedBy(ab);
                    t.setApprovedByName(rs.getString("approved_by_name"));
                    list.add(t);
                }
            }
        }
        return list;
    }

    public List<StockTransfer> findPendingTransfersFiltered(String transferCode, Integer fromWarehouseId, Integer toWarehouseId) throws Exception {
        List<StockTransfer> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT st.*, " +
                "fw.warehouse_name as from_warehouse_name, tw.warehouse_name as to_warehouse_name, " +
                "fw.branch_id as from_branch_id, tw.branch_id as to_branch_id, " +
                "e.fullName as created_by_name, e.branch_id as creator_branch_id, e2.fullName as approved_by_name " +
                "FROM stock_transfer st " +
                "LEFT JOIN warehouse fw ON st.from_warehouse_id = fw.warehouse_id " +
                "LEFT JOIN warehouse tw ON st.to_warehouse_id = tw.warehouse_id " +
                "LEFT JOIN Employee e ON st.created_by = e.emp_id " +
                "LEFT JOIN Employee e2 ON st.approved_by = e2.emp_id " +
                "WHERE st.status IN ('PENDING_OWNER', 'PENDING_PARTNER', 'APPROVED_DISPATCH', 'IN_TRANSIT', 'PENDING_DISPATCH') ");
        
        if (transferCode != null && !transferCode.trim().isEmpty()) {
            sql.append("AND st.transfer_code LIKE ? ");
        }
        if (fromWarehouseId != null && fromWarehouseId > 0) {
            sql.append("AND st.from_warehouse_id = ? ");
        }
        if (toWarehouseId != null && toWarehouseId > 0) {
            sql.append("AND st.to_warehouse_id = ? ");
        }
        sql.append("ORDER BY st.transfer_date DESC");

        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int paramIndex = 1;
            if (transferCode != null && !transferCode.trim().isEmpty()) {
                ps.setString(paramIndex++, "%" + transferCode.trim() + "%");
            }
            if (fromWarehouseId != null && fromWarehouseId > 0) {
                ps.setInt(paramIndex++, fromWarehouseId);
            }
            if (toWarehouseId != null && toWarehouseId > 0) {
                ps.setInt(paramIndex++, toWarehouseId);
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
                    int ab = rs.getInt("approved_by"); if (!rs.wasNull()) t.setApprovedBy(ab);
                    t.setApprovedByName(rs.getString("approved_by_name"));
                    
                    t.setFromBranchId(rs.getInt("from_branch_id"));
                    t.setToBranchId(rs.getInt("to_branch_id"));
                    t.setCreatorBranchId(rs.getInt("creator_branch_id"));
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

    public boolean updateStatus(int transferId, String status, Integer approvedBy) throws Exception {
        try (Connection conn = new DBContext().getConnection()) {
            return updateStatus(conn, transferId, status, approvedBy);
        }
    }

    public boolean updateStatus(Connection conn, int transferId, String status, Integer approvedBy) throws Exception {
        String sql = "UPDATE stock_transfer SET status = ?, approved_by = ? WHERE stock_transfer_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            if (approvedBy != null) {
                ps.setInt(2, approvedBy);
            } else {
                ps.setNull(2, java.sql.Types.INTEGER);
            }
            ps.setInt(3, transferId);
            return ps.executeUpdate() > 0;
        }
    }

    public StockTransfer findById(int id) throws Exception {
        String sql = "SELECT st.*, fw.warehouse_name as from_warehouse_name, tw.warehouse_name as to_warehouse_name, " +
                     "fw.branch_id as from_branch_id, tw.branch_id as to_branch_id, " +
                     "e.fullName as created_by_name, e.branch_id as creator_branch_id, e2.fullName as approved_by_name " +
                     "FROM stock_transfer st " +
                     "LEFT JOIN warehouse fw ON st.from_warehouse_id = fw.warehouse_id " +
                     "LEFT JOIN warehouse tw ON st.to_warehouse_id = tw.warehouse_id " +
                     "LEFT JOIN Employee e ON st.created_by = e.emp_id " +
                     "LEFT JOIN Employee e2 ON st.approved_by = e2.emp_id " +
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
                    int ab = rs.getInt("approved_by"); if (!rs.wasNull()) t.setApprovedBy(ab);
                    t.setApprovedByName(rs.getString("approved_by_name"));
                    
                    t.setFromBranchId(rs.getInt("from_branch_id"));
                    t.setToBranchId(rs.getInt("to_branch_id"));
                    t.setCreatorBranchId(rs.getInt("creator_branch_id"));
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

    public boolean createTransfers(List<StockTransfer> transfers) throws Exception {
        String sql = "INSERT INTO stock_transfer (from_warehouse_id, to_warehouse_id, transfer_code, status, note, created_by, approved_by) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = new DBContext().getConnection()) {
            conn.setAutoCommit(false);
            try {
                for (StockTransfer t : transfers) {
                    try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                        ps.setInt(1, t.getFromWarehouseId());
                        ps.setInt(2, t.getToWarehouseId());
                        ps.setString(3, t.getTransferCode());
                        ps.setString(4, t.getStatus());
                        ps.setString(5, t.getNote());
                        ps.setInt(6, t.getCreatedBy());
                        if (t.getApprovedBy() != null && t.getApprovedBy() > 0) {
                            ps.setInt(7, t.getApprovedBy());
                        } else {
                            ps.setNull(7, java.sql.Types.INTEGER);
                        }
                        ps.executeUpdate();
                        
                        try (ResultSet rs = ps.getGeneratedKeys()) {
                            if (rs.next()) {
                                int id = rs.getInt(1);
                                t.setStockTransferId(id);
                                
                                String sqlDetail = "INSERT INTO stock_transfer_detail (stock_transfer_id, product_id, quantity) VALUES (?, ?, ?)";
                                try (PreparedStatement psDetail = conn.prepareStatement(sqlDetail)) {
                                    for (StockTransferDetail d : t.getDetails()) {
                                        psDetail.setInt(1, id);
                                        psDetail.setInt(2, d.getProductId());
                                        psDetail.setInt(3, d.getQuantity());
                                        psDetail.addBatch();
                                    }
                                    psDetail.executeBatch();
                                }
                            }
                        }
                    }
                }
                conn.commit();
                return true;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public List<StockTransfer> findAllGrouped(int warehouseId, String status, String transferCode, Integer partnerWarehouseId) throws Exception {
        List<StockTransfer> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT st.transfer_code, MIN(st.stock_transfer_id) as first_id, MIN(st.transfer_date) as transfer_date, " +
                "MIN(st.note) as note, MIN(st.created_by) as created_by, " +
                "MIN(e.fullName) as created_by_name " +
                "FROM stock_transfer st " +
                "LEFT JOIN Employee e ON st.created_by = e.emp_id " +
                "WHERE 1=1 ");
        
        if (warehouseId > 0) {
            sql.append("AND st.transfer_code IN (SELECT DISTINCT transfer_code FROM stock_transfer WHERE from_warehouse_id = ? OR to_warehouse_id = ?) ");
        }
        if (partnerWarehouseId != null && partnerWarehouseId > 0) {
            sql.append("AND st.transfer_code IN (SELECT DISTINCT transfer_code FROM stock_transfer WHERE from_warehouse_id = ? OR to_warehouse_id = ?) ");
        }
        if (transferCode != null && !transferCode.trim().isEmpty()) {
            sql.append("AND st.transfer_code LIKE ? ");
        }
        if (status != null && !status.isEmpty()) {
            if ("PENDING_OWNER".equals(status)) {
                sql.append("AND st.transfer_code IN (SELECT DISTINCT transfer_code FROM stock_transfer WHERE status IN ('PENDING_OWNER', 'PENDING_DISPATCH')) ");
            } else {
                sql.append("AND st.transfer_code IN (SELECT DISTINCT transfer_code FROM stock_transfer WHERE status = ?) ");
            }
        }
        sql.append("GROUP BY st.transfer_code ");
        sql.append("ORDER BY MIN(st.transfer_date) DESC");

        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int paramIndex = 1;
            if (warehouseId > 0) {
                ps.setInt(paramIndex++, warehouseId);
                ps.setInt(paramIndex++, warehouseId);
            }
            if (partnerWarehouseId != null && partnerWarehouseId > 0) {
                ps.setInt(paramIndex++, partnerWarehouseId);
                ps.setInt(paramIndex++, partnerWarehouseId);
            }
            if (transferCode != null && !transferCode.trim().isEmpty()) {
                ps.setString(paramIndex++, "%" + transferCode.trim() + "%");
            }
            if (status != null && !status.isEmpty()) {
                if (!"PENDING_OWNER".equals(status)) {
                    ps.setString(paramIndex++, status);
                }
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StockTransfer t = new StockTransfer();
                    t.setStockTransferId(rs.getInt("first_id"));
                    t.setTransferCode(rs.getString("transfer_code"));
                    t.setTransferDate(rs.getTimestamp("transfer_date"));
                    t.setNote(rs.getString("note"));
                    t.setCreatedBy(rs.getInt("created_by"));
                    t.setCreatedByName(rs.getString("created_by_name"));
                    list.add(t);
                }
            }
        }

        // Load sub-transfers and details for each master transfer, and compute displayStatus
        for (StockTransfer t : list) {
            List<StockTransfer> sub = findSubTransfersByCode(t.getTransferCode());
            t.setSubTransfers(sub);
            
            List<StockTransfer> subForStatus = sub;
            if (warehouseId > 0) {
                subForStatus = new ArrayList<>();
                if (sub != null) {
                    for (StockTransfer s : sub) {
                        if (s.getFromWarehouseId() == warehouseId || s.getToWarehouseId() == warehouseId) {
                            subForStatus.add(s);
                        }
                    }
                }
            }
            t.setDisplayStatus(calculateDisplayStatus(subForStatus));
            
            if (subForStatus != null && !subForStatus.isEmpty()) {
                t.setCreatorBranchId(subForStatus.get(0).getCreatorBranchId());
                
                int userBranchId = 0;
                if (warehouseId > 0) {
                    for (StockTransfer s : subForStatus) {
                        if (s.getFromWarehouseId() == warehouseId) {
                            userBranchId = s.getFromBranchId();
                            break;
                        } else if (s.getToWarehouseId() == warehouseId) {
                            userBranchId = s.getToBranchId();
                            break;
                        }
                    }
                }
                
                // Chỉ lấy người duyệt từ các phiếu con liên quan đến kho hiện tại và thuộc chi nhánh của kho đó (hoặc Owner/Admin không có chi nhánh cụ thể)
                java.util.Set<String> approvers = new java.util.LinkedHashSet<>();
                for (StockTransfer s : subForStatus) {
                    if (s.getApprovedByName() != null && !s.getApprovedByName().trim().isEmpty()) {
                        if (warehouseId == 0 || s.getApprovedByBranchId() == null || s.getApprovedByBranchId() == userBranchId) {
                            approvers.add(s.getApprovedByName().trim());
                        }
                    }
                }
                if (!approvers.isEmpty()) {
                    t.setApprovedByName(String.join(", ", approvers));
                }
            } else if (sub != null && !sub.isEmpty()) {
                t.setCreatorBranchId(sub.get(0).getCreatorBranchId());
            }
        }

        return list;
    }

    public List<StockTransfer> findSubTransfersByCode(String transferCode) throws Exception {
        List<StockTransfer> list = new ArrayList<>();
        String sql = "SELECT st.*, " +
                "fw.warehouse_name as from_warehouse_name, tw.warehouse_name as to_warehouse_name, " +
                "fw.branch_id as from_branch_id, tw.branch_id as to_branch_id, " +
                "e.fullName as created_by_name, e.branch_id as creator_branch_id, e2.fullName as approved_by_name, " +
                "e2.branch_id as approved_by_branch_id " +
                "FROM stock_transfer st " +
                "LEFT JOIN warehouse fw ON st.from_warehouse_id = fw.warehouse_id " +
                "LEFT JOIN warehouse tw ON st.to_warehouse_id = tw.warehouse_id " +
                "LEFT JOIN Employee e ON st.created_by = e.emp_id " +
                "LEFT JOIN Employee e2 ON st.approved_by = e2.emp_id " +
                "WHERE st.transfer_code = ? " +
                "ORDER BY st.stock_transfer_id ASC";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, transferCode);
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
                    int ab = rs.getInt("approved_by"); if (!rs.wasNull()) t.setApprovedBy(ab);
                    t.setApprovedByName(rs.getString("approved_by_name"));
                    
                    int abb = rs.getInt("approved_by_branch_id");
                    if (!rs.wasNull()) t.setApprovedByBranchId(abb);
                    
                    t.setFromBranchId(rs.getInt("from_branch_id"));
                    t.setToBranchId(rs.getInt("to_branch_id"));
                    t.setCreatorBranchId(rs.getInt("creator_branch_id"));
                    
                    t.setDetails(getTransferDetails(t.getStockTransferId()));
                    list.add(t);
                }
            }
        }
        return list;
    }

    public boolean isFullyFinalized(String transferCode) throws Exception {
        List<StockTransfer> subList = findSubTransfersByCode(transferCode);
        for (StockTransfer sub : subList) {
            String s = sub.getStatus();
            if ("PENDING_OWNER".equals(s) || "PENDING_PARTNER".equals(s) || "APPROVED_DISPATCH".equals(s) || "IN_TRANSIT".equals(s)) {
                return false;
            }
        }
        return true;
    }

    public String calculateDisplayStatus(List<StockTransfer> subTransfers) {
        if (subTransfers == null || subTransfers.isEmpty()) return "Unknown";
        
        boolean anyPendingOwner = false;
        boolean anyPendingPartner = false;
        boolean anyApprovedDispatch = false;
        boolean anyInTransit = false;
        boolean anyCompleted = false;
        boolean anyCancelled = false;
        boolean anyRejected = false;
        
        for (StockTransfer st : subTransfers) {
            String s = st.getStatus();
            if (s == null) continue;
            switch (s) {
                case "PENDING_OWNER":
                case "PENDING_DISPATCH":
                    anyPendingOwner = true;
                    break;
                case "PENDING_PARTNER":
                    anyPendingPartner = true;
                    break;
                case "APPROVED_DISPATCH":
                case "APPROVED_PARTNER":
                    anyApprovedDispatch = true;
                    break;
                case "IN_TRANSIT":
                    anyInTransit = true;
                    break;
                case "COMPLETED":
                    anyCompleted = true;
                    break;
                case "CANCELLED":
                case "REJECTED":
                case "PARTNER_REJECTED":
                case "DISPATCH_REJECTED":
                case "RECEIVE_REJECTED":
                    anyRejected = true;
                    break;
            }
        }
        
        if (anyPendingOwner) return "PENDING_OWNER";
        if (anyPendingPartner) return "PENDING_PARTNER";
        
        boolean allRejectedOrCompleted = true;
        for (StockTransfer st : subTransfers) {
            String s = st.getStatus();
            if (s != null && !s.equals("COMPLETED") && !s.equals("CANCELLED") && !s.equals("REJECTED") 
                    && !s.equals("PARTNER_REJECTED") && !s.equals("DISPATCH_REJECTED") && !s.equals("RECEIVE_REJECTED")) {
                allRejectedOrCompleted = false;
            }
        }
        
        if (allRejectedOrCompleted) {
            if (anyCompleted && anyRejected) {
                return "PARTIAL_COMPLETE";
            } else if (anyCompleted) {
                return "COMPLETED";
            } else {
                return "CANCELLED";
            }
        }
        
        if (anyInTransit) return "IN_TRANSIT";
        if (anyApprovedDispatch) return "APPROVED_DISPATCH";
        
        return "IN_PROGRESS";
    }

    public boolean updateStatusByCode(String transferCode, String status, Integer approvedBy) throws Exception {
        String sql = "UPDATE stock_transfer SET status = ?, approved_by = ? WHERE transfer_code = ?";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            if (approvedBy != null) {
                ps.setInt(2, approvedBy);
            } else {
                ps.setNull(2, java.sql.Types.INTEGER);
            }
            ps.setString(3, transferCode);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateStatusForPartner(String transferCode, int partnerWarehouseId, String status) throws Exception {
        String sql = "UPDATE stock_transfer SET status = ? WHERE transfer_code = ? AND (from_warehouse_id = ? OR to_warehouse_id = ?)";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, transferCode);
            ps.setInt(3, partnerWarehouseId);
            ps.setInt(4, partnerWarehouseId);
            return ps.executeUpdate() > 0;
        }
    }
}
