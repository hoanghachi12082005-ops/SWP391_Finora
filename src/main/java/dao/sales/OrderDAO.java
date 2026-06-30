package dao.sales;

import model.Order;
import util.database.DBContext;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    public List<Order> findAll() {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT o.order_id, o.order_code, o.order_type, o.customer_id, o.branch_id, o.supplier_id, o.emp_id, o.voucher_id, o.warehouse_id, "
                   + "o.subtotal, o.discount_amount, o.total_amount, o.payment_method, o.status, o.created_at, "
                   + "c.full_name AS customer_name, b.branch_name, e.fullName AS emp_name "
                   + "FROM [order] o "
                   + "LEFT JOIN customer c ON o.customer_id = c.cus_id "
                   + "LEFT JOIN Branch b ON o.branch_id = b.branch_id "
                   + "LEFT JOIN Employee e ON o.emp_id = e.emp_id "
                   + "ORDER BY o.created_at DESC";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public Order findById(int id) {
        String sql = "SELECT o.order_id, o.order_code, o.order_type, o.customer_id, o.branch_id, o.supplier_id, o.emp_id, o.voucher_id, o.warehouse_id, "
                   + "o.subtotal, o.discount_amount, o.total_amount, o.payment_method, o.status, o.created_at, "
                   + "c.full_name AS customer_name, b.branch_name, e.fullName AS emp_name "
                   + "FROM [order] o "
                   + "LEFT JOIN customer c ON o.customer_id = c.cus_id "
                   + "LEFT JOIN Branch b ON o.branch_id = b.branch_id "
                   + "LEFT JOIN Employee e ON o.emp_id = e.emp_id "
                   + "WHERE o.order_id = ?";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return map(rs); }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }
    
    public int insert(Order order, Connection conn) throws SQLException {
        String sql = "INSERT INTO [order] (order_code, order_type, customer_id, branch_id, emp_id, subtotal, discount_amount, total_amount, payment_method, status, created_at) "
                   + "VALUES (?, 'SALE', ?, ?, ?, ?, ?, ?, ?, 'COMPLETED', GETDATE())";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, order.getOrderCode());
            if (order.getCustomerId() != null && order.getCustomerId() > 0) ps.setInt(2, order.getCustomerId()); else ps.setNull(2, Types.INTEGER);
            if (order.getBranchId() != null) ps.setInt(3, order.getBranchId()); else ps.setNull(3, Types.INTEGER);
            if (order.getEmpId() != null) ps.setInt(4, order.getEmpId()); else ps.setNull(4, Types.INTEGER);
            ps.setBigDecimal(5, order.getSubtotal() != null ? order.getSubtotal() : java.math.BigDecimal.ZERO);
            ps.setBigDecimal(6, order.getDiscountAmount() != null ? order.getDiscountAmount() : java.math.BigDecimal.ZERO);
            ps.setBigDecimal(7, order.getTotalAmount() != null ? order.getTotalAmount() : java.math.BigDecimal.ZERO);
            ps.setString(8, order.getPaymentMethod() != null ? order.getPaymentMethod() : "CASH");
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
            throw new SQLException("No order ID generated");
        }
    }

    public List<Order> findByEmployeeId(int empId) {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT o.order_id, o.order_code, o.order_type, o.customer_id, o.branch_id, o.supplier_id, o.emp_id, o.voucher_id, o.warehouse_id, "
                   + "o.subtotal, o.discount_amount, o.total_amount, o.payment_method, o.status, o.created_at, "
                   + "c.full_name AS customer_name, b.branch_name, e.fullName AS emp_name "
                   + "FROM [order] o "
                   + "LEFT JOIN customer c ON o.customer_id = c.cus_id "
                   + "LEFT JOIN Branch b ON o.branch_id = b.branch_id "
                   + "LEFT JOIN Employee e ON o.emp_id = e.emp_id "
                   + "WHERE o.emp_id = ? "
                   + "ORDER BY o.created_at DESC";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, empId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    private Order map(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setOrderId(rs.getInt("order_id"));
        o.setOrderCode(rs.getString("order_code"));
        o.setOrderType(rs.getString("order_type"));
        int cid = rs.getInt("customer_id"); if (!rs.wasNull()) o.setCustomerId(cid);
        int bid = rs.getInt("branch_id"); if (!rs.wasNull()) o.setBranchId(bid);
        int sid = rs.getInt("supplier_id"); if (!rs.wasNull()) o.setSupplierId(sid);
        int eid = rs.getInt("emp_id"); if (!rs.wasNull()) o.setEmpId(eid);
        int vid = rs.getInt("voucher_id"); if (!rs.wasNull()) o.setVoucherId(vid);
        int wid = rs.getInt("warehouse_id"); if (!rs.wasNull()) o.setWarehouseId(wid);
        o.setSubtotal(rs.getBigDecimal("subtotal"));
        o.setDiscountAmount(rs.getBigDecimal("discount_amount"));
        o.setTotalAmount(rs.getBigDecimal("total_amount"));
        o.setPaymentMethod(rs.getString("payment_method"));
        o.setStatus(rs.getString("status"));
        o.setCreatedAt(rs.getTimestamp("created_at"));
        o.setCustomerName(rs.getString("customer_name"));
        o.setBranchName(rs.getString("branch_name"));
        o.setEmpName(rs.getString("emp_name"));
        return o;
    }
}
