package dao.inventory;

import model.InventoryCheck;
import model.InventoryCheckDetail;
import util.database.DBContext;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InventoryCheckDAO {

    public int createCheck(InventoryCheck check, List<InventoryCheckDetail> details) throws SQLException {
        String sqlCheck = "INSERT INTO inventory_check (check_code, warehouse_id, created_by, status, total_discrepancy, created_at, updated_at) VALUES (?, ?, ?, ?, ?, GETDATE(), GETDATE())";
        String sqlDetail = "INSERT INTO inventory_check_detail (check_id, product_id, system_qty, actual_qty, discrepancy, note) VALUES (?, ?, ?, ?, ?, ?)";
        
        int checkId = 0;
        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psCheck = conn.prepareStatement(sqlCheck, Statement.RETURN_GENERATED_KEYS)) {
                psCheck.setString(1, check.getCheckCode());
                psCheck.setInt(2, check.getWarehouseId());
                psCheck.setInt(3, check.getCreatedBy());
                psCheck.setString(4, check.getStatus());
                psCheck.setInt(5, check.getTotalDiscrepancy());
                
                int affected = psCheck.executeUpdate();
                if (affected == 0) {
                    throw new SQLException("Tạo phiếu kiểm kho thất bại.");
                }
                
                try (ResultSet rsKeys = psCheck.getGeneratedKeys()) {
                    if (rsKeys.next()) {
                        checkId = rsKeys.getInt(1);
                    } else {
                        throw new SQLException("Không lấy được ID phiếu kiểm kho vừa tạo.");
                    }
                }
                
                try (PreparedStatement psDetail = conn.prepareStatement(sqlDetail)) {
                    for (InventoryCheckDetail d : details) {
                        psDetail.setInt(1, checkId);
                        psDetail.setInt(2, d.getProductId());
                        psDetail.setInt(3, d.getSystemQty());
                        psDetail.setInt(4, d.getActualQty());
                        psDetail.setInt(5, d.getDiscrepancy());
                        psDetail.setString(6, d.getNote());
                        psDetail.addBatch();
                    }
                    psDetail.executeBatch();
                }
                
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
        return checkId;
    }

    public InventoryCheck getPendingCheckByWarehouse(int warehouseId) {
        if (warehouseId <= 0) return null;
        String sql = """
            SELECT TOP 1 ic.*, 
                   w.warehouse_name as warehouseName, 
                   e1.fullName as createdByName, 
                   e2.fullName as approvedByName
            FROM inventory_check ic
            JOIN warehouse w ON ic.warehouse_id = w.warehouse_id
            JOIN Employee e1 ON ic.created_by = e1.emp_id
            LEFT JOIN Employee e2 ON ic.approved_by = e2.emp_id
            WHERE ic.warehouse_id = ? AND ic.status = 'PENDING'
            ORDER BY ic.check_id DESC
            """;
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, warehouseId);
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

    public boolean hasPendingCheck(int warehouseId) {
        return getPendingCheckByWarehouse(warehouseId) != null;
    }

    public List<InventoryCheck> findAllByWarehouse(int warehouseId) {
        List<InventoryCheck> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
            SELECT ic.*, 
                   w.warehouse_name as warehouseName, 
                   e1.fullName as createdByName, 
                   e2.fullName as approvedByName
            FROM inventory_check ic
            JOIN warehouse w ON ic.warehouse_id = w.warehouse_id
            JOIN Employee e1 ON ic.created_by = e1.emp_id
            LEFT JOIN Employee e2 ON ic.approved_by = e2.emp_id
            """);
        
        boolean hasW = warehouseId > 0;
        if (hasW) {
            sql.append(" WHERE ic.warehouse_id = ?");
        }
        sql.append(" ORDER BY ic.check_id DESC");
        
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (hasW) {
                ps.setInt(1, warehouseId);
            }
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

    public List<InventoryCheck> findAllByWarehouseFiltered(int warehouseId, String checkCodeQuery, String statusQuery, String discrepancyQuery) {
        List<InventoryCheck> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
            SELECT ic.*, 
                   w.warehouse_name as warehouseName, 
                   e1.fullName as createdByName, 
                   e2.fullName as approvedByName
            FROM inventory_check ic
            JOIN warehouse w ON ic.warehouse_id = w.warehouse_id
            JOIN Employee e1 ON ic.created_by = e1.emp_id
            LEFT JOIN Employee e2 ON ic.approved_by = e2.emp_id
            WHERE 1=1
            """);
        
        if (warehouseId > 0) {
            sql.append(" AND ic.warehouse_id = ? ");
        }
        if (checkCodeQuery != null && !checkCodeQuery.trim().isEmpty()) {
            sql.append(" AND ic.check_code LIKE ? ");
        }
        if (statusQuery != null && !statusQuery.trim().isEmpty()) {
            if ("CANCELLED".equalsIgnoreCase(statusQuery.trim())) {
                sql.append(" AND (ic.status = 'CANCELLED' OR ic.status = 'REJECTED') ");
            } else {
                sql.append(" AND ic.status = ? ");
            }
        }
        if (discrepancyQuery != null && !discrepancyQuery.trim().isEmpty()) {
            if ("has_disc".equals(discrepancyQuery)) {
                sql.append(" AND ic.total_discrepancy > 0 ");
            } else if ("no_disc".equals(discrepancyQuery)) {
                sql.append(" AND ic.total_discrepancy = 0 ");
            }
        }
        
        sql.append(" ORDER BY ic.check_id DESC");
        
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int paramIndex = 1;
            if (warehouseId > 0) {
                ps.setInt(paramIndex++, warehouseId);
            }
            if (checkCodeQuery != null && !checkCodeQuery.trim().isEmpty()) {
                ps.setString(paramIndex++, "%" + checkCodeQuery.trim() + "%");
            }
            if (statusQuery != null && !statusQuery.trim().isEmpty() && !"CANCELLED".equalsIgnoreCase(statusQuery.trim())) {
                ps.setString(paramIndex++, statusQuery.trim());
            }
            
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

    public InventoryCheck findById(int checkId) {
        String sql = """
            SELECT ic.*, 
                   w.warehouse_name as warehouseName, 
                   e1.fullName as createdByName, 
                   e2.fullName as approvedByName
            FROM inventory_check ic
            JOIN warehouse w ON ic.warehouse_id = w.warehouse_id
            JOIN Employee e1 ON ic.created_by = e1.emp_id
            LEFT JOIN Employee e2 ON ic.approved_by = e2.emp_id
            WHERE ic.check_id = ?
            """;
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, checkId);
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

    public List<InventoryCheckDetail> getCheckDetails(int checkId) {
        List<InventoryCheckDetail> list = new ArrayList<>();
        String sql = """
            SELECT icd.*, 
                   p.product_name as productName, 
                   c.category_name as categoryName
            FROM inventory_check_detail icd
            JOIN [product] p ON icd.product_id = p.product_id
            LEFT JOIN category c ON p.category_id = c.category_id
            WHERE icd.check_id = ?
            """;
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, checkId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    InventoryCheckDetail d = new InventoryCheckDetail();
                    d.setDetailId(rs.getInt("detail_id"));
                    d.setCheckId(rs.getInt("check_id"));
                    d.setProductId(rs.getInt("product_id"));
                    d.setSystemQty(rs.getInt("system_qty"));
                    d.setActualQty(rs.getInt("actual_qty"));
                    d.setDiscrepancy(rs.getInt("discrepancy"));
                    d.setNote(rs.getString("note"));
                    d.setProductName(rs.getString("productName"));
                    d.setCategoryName(rs.getString("categoryName"));
                    list.add(d);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void updateStatus(Connection conn, int checkId, String status, Integer approvedBy) throws SQLException {
        String sql = "UPDATE inventory_check SET status = ?, approved_by = ?, updated_at = GETDATE() WHERE check_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            if (approvedBy != null) {
                ps.setInt(2, approvedBy);
            } else {
                ps.setNull(2, java.sql.Types.INTEGER);
            }
            ps.setInt(3, checkId);
            ps.executeUpdate();
        }
    }

    public boolean updateStatus(int checkId, String status, Integer approvedBy) {
        try (Connection conn = DBContext.getConnection()) {
            updateStatus(conn, checkId, status, approvedBy);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void updateCheck(int checkId, int totalDiscrepancy, List<InventoryCheckDetail> details) throws SQLException {
        String deleteDetailsSql = "DELETE FROM inventory_check_detail WHERE check_id = ?";
        String updateCheckSql = "UPDATE inventory_check SET total_discrepancy = ?, updated_at = GETDATE() WHERE check_id = ?";
        String insertDetailSql = "INSERT INTO inventory_check_detail (check_id, product_id, system_qty, actual_qty, discrepancy, note) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement psDelete = conn.prepareStatement(deleteDetailsSql)) {
                    psDelete.setInt(1, checkId);
                    psDelete.executeUpdate();
                }
                
                try (PreparedStatement psUpdate = conn.prepareStatement(updateCheckSql)) {
                    psUpdate.setInt(1, totalDiscrepancy);
                    psUpdate.setInt(2, checkId);
                    psUpdate.executeUpdate();
                }
                
                try (PreparedStatement psDetail = conn.prepareStatement(insertDetailSql)) {
                    for (InventoryCheckDetail d : details) {
                        psDetail.setInt(1, checkId);
                        psDetail.setInt(2, d.getProductId());
                        psDetail.setInt(3, d.getSystemQty());
                        psDetail.setInt(4, d.getActualQty());
                        psDetail.setInt(5, d.getDiscrepancy());
                        psDetail.setString(6, d.getNote());
                        psDetail.addBatch();
                    }
                    psDetail.executeBatch();
                }
                
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private InventoryCheck mapRow(ResultSet rs) throws SQLException {
        InventoryCheck ic = new InventoryCheck();
        ic.setCheckId(rs.getInt("check_id"));
        ic.setCheckCode(rs.getString("check_code"));
        ic.setWarehouseId(rs.getInt("warehouse_id"));
        ic.setCreatedBy(rs.getInt("created_by"));
        
        int approvedBy = rs.getInt("approved_by");
        ic.setApprovedBy(rs.wasNull() ? null : approvedBy);
        
        ic.setStatus(rs.getString("status"));
        ic.setTotalDiscrepancy(rs.getInt("total_discrepancy"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            ic.setCreatedAt(createdAt.toLocalDateTime());
        }
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            ic.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        ic.setWarehouseName(rs.getString("warehouseName"));
        ic.setCreatedByName(rs.getString("createdByName"));
        ic.setApprovedByName(rs.getString("approvedByName"));
        return ic;
    }
}
