package dao.purchase;

import model.PurchaseOrder;
import util.database.DBContext;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PurchaseOrderDAO {

    public List<PurchaseOrder> findAllByWarehouseAndType(int warehouseId, String orderType, String status) {
        List<PurchaseOrder> list = new ArrayList<>();
        String sql = "SELECT o.order_id, o.order_code, o.supplier_id, o.branch_id, o.emp_id, o.approved_by, o.warehouse_id, o.order_type, "
                   + "o.subtotal, o.discount_amount, o.total_amount, o.status, o.created_at, "
                   + "COALESCE(s.supplier_name, (SELECT STRING_AGG(sup.supplier_name, ', ') FROM (SELECT DISTINCT s2.supplier_name FROM order_detail od2 JOIN supplier s2 ON od2.supplier_id = s2.supplier_id WHERE od2.order_id = o.order_id) sup)) AS supplier_name, "
                   + "b.branch_name, e.fullName AS emp_name, e2.fullName AS approved_by_name "
                   + "FROM [order] o "
                   + "LEFT JOIN supplier s ON o.supplier_id = s.supplier_id "
                   + "LEFT JOIN Branch b ON o.branch_id = b.branch_id "
                   + "LEFT JOIN Employee e ON o.emp_id = e.emp_id "
                   + "LEFT JOIN Employee e2 ON o.approved_by = e2.emp_id "
                   + "WHERE o.order_type = ? ";
        if (warehouseId > 0) {
            sql += "AND o.warehouse_id = ? ";
        }
        if (status != null && !status.isEmpty()) {
            sql += "AND o.status = ? ";
        }
        sql += "ORDER BY o.created_at DESC";
        
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            int paramIdx = 1;
            ps.setString(paramIdx++, orderType);
            if (warehouseId > 0) {
                ps.setInt(paramIdx++, warehouseId);
            }
            if (status != null && !status.isEmpty()) {
                ps.setString(paramIdx++, status);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<PurchaseOrder> findAll() {
        return findAllByWarehouseAndType(0, "PURCHASE", null);
    }

    public PurchaseOrder findById(int id) {
        String sql = "SELECT o.order_id, o.order_code, o.supplier_id, o.branch_id, o.emp_id, o.approved_by, o.warehouse_id, o.order_type, "
                   + "o.subtotal, o.discount_amount, o.total_amount, o.status, o.created_at, "
                   + "COALESCE(s.supplier_name, (SELECT STRING_AGG(sup.supplier_name, ', ') FROM (SELECT DISTINCT s2.supplier_name FROM order_detail od2 JOIN supplier s2 ON od2.supplier_id = s2.supplier_id WHERE od2.order_id = o.order_id) sup)) AS supplier_name, "
                   + "b.branch_name, e.fullName AS emp_name, e2.fullName AS approved_by_name "
                   + "FROM [order] o "
                   + "LEFT JOIN supplier s ON o.supplier_id = s.supplier_id "
                   + "LEFT JOIN Branch b ON o.branch_id = b.branch_id "
                   + "LEFT JOIN Employee e ON o.emp_id = e.emp_id "
                   + "LEFT JOIN Employee e2 ON o.approved_by = e2.emp_id "
                   + "WHERE o.order_id = ?";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return map(rs); }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    private PurchaseOrder map(ResultSet rs) throws SQLException {
        PurchaseOrder po = new PurchaseOrder();
        po.setOrderId(rs.getInt("order_id"));
        po.setOrderCode(rs.getString("order_code"));
        int sid = rs.getInt("supplier_id"); if (!rs.wasNull()) po.setSupplierId(sid);
        int bid = rs.getInt("branch_id"); if (!rs.wasNull()) po.setBranchId(bid);
        int eid = rs.getInt("emp_id"); if (!rs.wasNull()) po.setEmpId(eid);
        int wid = rs.getInt("warehouse_id"); if (!rs.wasNull()) po.setWarehouseId(wid);
        po.setSubtotal(rs.getBigDecimal("subtotal"));
        po.setDiscountAmount(rs.getBigDecimal("discount_amount"));
        po.setTotalAmount(rs.getBigDecimal("total_amount"));
        po.setStatus(rs.getString("status"));
        po.setOrderType(rs.getString("order_type"));
        if (rs.getTimestamp("created_at") != null) po.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        po.setSupplierName(rs.getString("supplier_name"));
        po.setBranchName(rs.getString("branch_name"));
        po.setEmpName(rs.getString("emp_name"));
        int ab = rs.getInt("approved_by"); if (!rs.wasNull()) po.setApprovedBy(ab);
        po.setApprovedByName(rs.getString("approved_by_name"));
        return po;
    }
}
