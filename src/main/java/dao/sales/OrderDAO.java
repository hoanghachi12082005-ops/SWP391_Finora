package dao.sales;

import model.Order;
import model.OrderDetail;
import util.database.DBContext;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    public List<Order> getAllSaleOrders(String keyword, int branchId) {
        List<Order> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
            SELECT o.*, 
                   c.full_name AS customerName, 
                   e.fullName AS employeeName, 
                   b.branch_name AS branchName
            FROM [order] o
            LEFT JOIN Customer c ON o.customer_id = c.cus_id
            JOIN Employee e ON o.emp_id = e.emp_id
            JOIN Branch b ON o.branch_id = b.branch_id
            WHERE o.order_type = 'SALE'
            """);
        
        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        boolean hasBranch = branchId > 0;
        
        if (hasKeyword) {
            sql.append(" AND (o.order_code LIKE ? OR c.full_name LIKE ?)");
        }
        if (hasBranch) {
            sql.append(" AND o.branch_id = ?");
        }
        sql.append(" ORDER BY o.order_id DESC");
        
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int paramIndex = 1;
            if (hasKeyword) {
                String searchStr = "%" + keyword.trim() + "%";
                ps.setString(paramIndex++, searchStr);
                ps.setString(paramIndex++, searchStr);
            }
            if (hasBranch) {
                ps.setInt(paramIndex++, branchId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order o = mapRow(rs);
                    o.setCustomerName(rs.getString("customerName"));
                    o.setEmployeeName(rs.getString("employeeName"));
                    o.setBranchName(rs.getString("branchName"));
                    list.add(o);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<OrderDetail> getOrderDetailById(int orderId) {
        return findDetailsByOrderId(orderId);
    }

    public List<OrderDetail> findDetailsByOrderId(int orderId) {
        List<OrderDetail> list = new ArrayList<>();
        String sql = """
            SELECT od.*, p.product_name, p.product_codebar 
            FROM order_detail od 
            JOIN Product p ON od.product_id = p.product_id 
            WHERE od.order_id = ?
            """;
        
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderDetail od = new OrderDetail();
                    od.setOrderDetailId(rs.getInt("order_detail_id"));
                    od.setOrderId(rs.getInt("order_id"));
                    od.setProductId(rs.getInt("product_id"));
                    od.setQuantity(rs.getInt("quantity"));
                    od.setUnitPrice(rs.getDouble("unit_price"));
                    od.setTotalPrice(rs.getDouble("total_price"));
                    od.setProductName(rs.getString("product_name"));
                    od.setProductCode(rs.getString("product_codebar"));
                    list.add(od);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Order findById(int orderId) {
        String sql = """
            SELECT o.*, 
                   c.full_name AS customerName, 
                   c.phone AS customerPhone,
                   cp.current_points AS customerPoints,
                   e.fullName AS employeeName, 
                   b.branch_name AS branchName
            FROM [order] o
            LEFT JOIN Customer c ON o.customer_id = c.cus_id
            LEFT JOIN customer_point cp ON c.cus_id = cp.cus_id
            JOIN Employee e ON o.emp_id = e.emp_id
            JOIN Branch b ON o.branch_id = b.branch_id
            WHERE o.order_id = ?
            """;
        
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Order o = mapRow(rs);
                    o.setCustomerName(rs.getString("customerName"));
                    o.setEmployeeName(rs.getString("employeeName"));
                    o.setBranchName(rs.getString("branchName"));
                    // We can return details if needed, but this is fine.
                    return o;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateStatus(int orderId, String status) {
        String sql = "UPDATE [order] SET status = ? WHERE order_id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, orderId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void updateStatus(Connection conn, int orderId, String status) throws SQLException {
        String sql = "UPDATE [order] SET status = ? WHERE order_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, orderId);
            ps.executeUpdate();
        }
    }

    public int createOrderInTransaction(Connection conn, Order order) throws SQLException {
        String sql = """
            INSERT INTO [order] 
            (order_code, order_type, customer_id, branch_id, supplier_id, emp_id, 
             voucher_id, warehouse_id, subtotal, discount_amount, total_amount, 
             payment_method, status, created_at) 
            VALUES (?, ?, ?, ?, NULL, ?, ?, ?, ?, ?, ?, ?, ?, GETDATE())
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, order.getOrderCode());
            ps.setString(2, order.getOrderType());
            if (order.getCustomerId() != null && order.getCustomerId() > 0) {
                ps.setInt(3, order.getCustomerId());
            } else {
                ps.setNull(3, java.sql.Types.INTEGER);
            }
            ps.setInt(4, order.getBranchId());
            ps.setInt(5, order.getEmpId());
            if (order.getVoucherId() != null && order.getVoucherId() > 0) {
                ps.setInt(6, order.getVoucherId());
            } else {
                ps.setNull(6, java.sql.Types.INTEGER);
            }
            ps.setInt(7, order.getWarehouseId());
            ps.setDouble(8, order.getSubtotal());
            ps.setDouble(9, order.getDiscountAmount());
            ps.setDouble(10, order.getTotalAmount());
            ps.setString(11, order.getPaymentMethod());
            ps.setString(12, order.getStatus() != null ? order.getStatus().name() : "PENDING");

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Tạo đơn hàng thất bại.");
            }
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
            throw new SQLException("Không lấy được ID đơn hàng vừa tạo.");
        }
    }

    private Order mapRow(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setOrderId(rs.getInt("order_id"));
        o.setOrderCode(rs.getString("order_code"));
        o.setOrderType(rs.getString("order_type"));
        
        int customerId = rs.getInt("customer_id");
        o.setCustomerId(rs.wasNull() ? null : customerId);
        
        o.setBranchId(rs.getInt("branch_id"));
        
        int supplierId = rs.getInt("supplier_id");
        o.setSupplierId(rs.wasNull() ? null : supplierId);
        
        o.setEmpId(rs.getInt("emp_id"));
        
        int voucherId = rs.getInt("voucher_id");
        o.setVoucherId(rs.wasNull() ? null : voucherId);
        
        o.setWarehouseId(rs.getInt("warehouse_id"));
        o.setSubtotal(rs.getDouble("subtotal"));
        o.setDiscountAmount(rs.getDouble("discount_amount"));
        o.setTotalAmount(rs.getDouble("total_amount"));
        o.setPaymentMethod(rs.getString("payment_method"));
        
        String statusStr = rs.getString("status");
        if (statusStr != null) {
            try {
                o.setStatus(Order.OrderStatus.valueOf(statusStr.toUpperCase()));
            } catch (IllegalArgumentException e) {
                o.setStatus(Order.OrderStatus.PENDING);
            }
        }
        
        // Formating created_at standard
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            o.setCreatedAt(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(ts));
        }
        return o;
    }

    public int countSaleOrders(String keyword, int branchId) {
        StringBuilder sql = new StringBuilder("""
            SELECT COUNT(*) 
            FROM [order] o
            LEFT JOIN Customer c ON o.customer_id = c.cus_id
            JOIN Employee e ON o.emp_id = e.emp_id
            JOIN Branch b ON o.branch_id = b.branch_id
            WHERE o.order_type = 'SALE'
            """);
        
        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        boolean hasBranch = branchId > 0;
        
        if (hasKeyword) {
            sql.append(" AND (o.order_code LIKE ? OR c.full_name LIKE ?)");
        }
        if (hasBranch) {
            sql.append(" AND o.branch_id = ?");
        }
        
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int paramIndex = 1;
            if (hasKeyword) {
                String searchStr = "%" + keyword.trim() + "%";
                ps.setString(paramIndex++, searchStr);
                ps.setString(paramIndex++, searchStr);
            }
            if (hasBranch) {
                ps.setInt(paramIndex++, branchId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<Order> getAllSaleOrdersPaginated(String keyword, int branchId, int offset, int pageSize) {
        List<Order> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
            SELECT o.*, 
                   c.full_name AS customerName, 
                   e.fullName AS employeeName, 
                   b.branch_name AS branchName
            FROM [order] o
            LEFT JOIN Customer c ON o.customer_id = c.cus_id
            JOIN Employee e ON o.emp_id = e.emp_id
            JOIN Branch b ON o.branch_id = b.branch_id
            WHERE o.order_type = 'SALE'
            """);
        
        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        boolean hasBranch = branchId > 0;
        
        if (hasKeyword) {
            sql.append(" AND (o.order_code LIKE ? OR c.full_name LIKE ?)");
        }
        if (hasBranch) {
            sql.append(" AND o.branch_id = ?");
        }
        sql.append(" ORDER BY o.order_id DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int paramIndex = 1;
            if (hasKeyword) {
                String searchStr = "%" + keyword.trim() + "%";
                ps.setString(paramIndex++, searchStr);
                ps.setString(paramIndex++, searchStr);
            }
            if (hasBranch) {
                ps.setInt(paramIndex++, branchId);
            }
            ps.setInt(paramIndex++, offset);
            ps.setInt(paramIndex++, pageSize);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order o = mapRow(rs);
                    o.setCustomerName(rs.getString("customerName"));
                    o.setEmployeeName(rs.getString("employeeName"));
                    o.setBranchName(rs.getString("branchName"));
                    list.add(o);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
