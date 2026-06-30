package dao.purchase;

import model.PurchaseOrder;
import util.database.DBContext;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PurchaseOrderDAO {

    public List<PurchaseOrder> findAll() {
        List<PurchaseOrder> list = new ArrayList<>();
        String sql = "SELECT o.order_id, o.order_code, o.supplier_id, o.branch_id, o.emp_id, o.warehouse_id, "
                   + "o.subtotal, o.discount_amount, o.total_amount, o.status, o.created_at, "
                   + "s.supplier_name, b.branch_name, e.fullName AS emp_name "
                   + "FROM [order] o "
                   + "LEFT JOIN supplier s ON o.supplier_id = s.supplier_id "
                   + "LEFT JOIN Branch b ON o.branch_id = b.branch_id "
                   + "LEFT JOIN Employee e ON o.emp_id = e.emp_id "
                   + "WHERE o.order_type = 'PURCHASE' "
                   + "ORDER BY o.created_at DESC";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public PurchaseOrder findById(int id) {
        String sql = "SELECT o.order_id, o.order_code, o.supplier_id, o.branch_id, o.emp_id, o.warehouse_id, "
                   + "o.subtotal, o.discount_amount, o.total_amount, o.status, o.created_at, "
                   + "s.supplier_name, b.branch_name, e.fullName AS emp_name "
                   + "FROM [order] o "
                   + "LEFT JOIN supplier s ON o.supplier_id = s.supplier_id "
                   + "LEFT JOIN Branch b ON o.branch_id = b.branch_id "
                   + "LEFT JOIN Employee e ON o.emp_id = e.emp_id "
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
        if (rs.getTimestamp("created_at") != null) po.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        po.setSupplierName(rs.getString("supplier_name"));
        po.setBranchName(rs.getString("branch_name"));
        po.setEmpName(rs.getString("emp_name"));
        return po;
    }
}
