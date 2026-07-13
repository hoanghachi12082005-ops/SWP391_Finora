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

    public List<Order> getPendingInventoryOrders(int branchId) {
        List<Order> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
            SELECT o.*, 
                   s.supplier_name AS supplierName, 
                   e.fullName AS employeeName, 
                   w.warehouse_name AS warehouseName
            FROM [order] o
            LEFT JOIN Supplier s ON o.supplier_id = s.supplier_id
            JOIN Employee e ON o.emp_id = e.emp_id
            JOIN Warehouse w ON o.warehouse_id = w.warehouse_id
            WHERE o.order_type IN ('PURCHASE', 'EXPORT') AND o.status = 'PENDING'
            """);
        
        if (branchId > 0) {
            sql.append(" AND o.branch_id = ?");
        }
        sql.append(" ORDER BY o.created_at ASC");
        
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
             
            if (branchId > 0) {
                ps.setInt(1, branchId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order o = mapRow(rs);
                    o.setSupplierName(rs.getString("supplierName"));
                    o.setEmployeeName(rs.getString("employeeName"));
                    // We can reuse getCustomerName to store warehouseName or add warehouseName field.
                    // For simplicity, let's reuse setCustomerName for warehouse name in the approval screen.
                    o.setCustomerName(rs.getString("warehouseName")); 
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
            SELECT od.*, p.product_name, p.product_codebar, s.supplier_name 
            FROM order_detail od 
            JOIN Product p ON od.product_id = p.product_id 
            LEFT JOIN supplier s ON od.supplier_id = s.supplier_id
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
                    od.setImportPrice(rs.getDouble("import_price"));
                    od.setProductName(rs.getString("product_name"));
                    od.setProductCode(rs.getString("product_codebar"));
                    od.setSupplierName(rs.getString("supplier_name"));
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

    public List<Order> findByEmployeeId(int empId) {
        List<Order> list = new ArrayList<>();
        String sql = """
            SELECT o.*,
                   c.full_name AS customerName,
                   c.phone AS customerPhone,
                   e.fullName AS employeeName,
                   b.branch_name AS branchName
            FROM [order] o
            LEFT JOIN Customer c ON o.customer_id = c.cus_id
            JOIN Employee e ON o.emp_id = e.emp_id
            JOIN Branch b ON o.branch_id = b.branch_id
            WHERE o.emp_id = ?
            ORDER BY o.created_at DESC
            """;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, empId);
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

    public boolean updateStatus(int orderId, String status, Integer approvedBy) {
        String sql = "UPDATE [order] SET status = ?, approved_by = ? WHERE order_id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            if (approvedBy != null) {
                ps.setInt(2, approvedBy);
            } else {
                ps.setNull(2, java.sql.Types.INTEGER);
            }
            ps.setInt(3, orderId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void updateStatus(Connection conn, int orderId, String status, Integer approvedBy) throws SQLException {
        String sql = "UPDATE [order] SET status = ?, approved_by = ? WHERE order_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            if (approvedBy != null) {
                ps.setInt(2, approvedBy);
            } else {
                ps.setNull(2, java.sql.Types.INTEGER);
            }
            ps.setInt(3, orderId);
            ps.executeUpdate();
        }
    }

    /**
     * Tìm ID đơn hàng theo mã đơn hàng (dùng cho VNPay IPN)
     */
    public int findIdByCode(Connection conn, String orderCode) throws SQLException {
        String sql = "SELECT order_id FROM [order] WHERE order_code = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, orderCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("order_id");
            }
        }
        return 0;
    }

    /**
     * Tìm đơn hàng theo mã đơn hàng (dùng cho VNPay)
     */
    public Order findByCode(Connection conn, String orderCode) throws SQLException {
        String sql = "SELECT * FROM [order] WHERE order_code = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, orderCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    /**
     * Lấy trạng thái đơn hàng theo ID (dùng cho VNPay IPN)
     */
    public String getStatus(Connection conn, int orderId) throws SQLException {
        String sql = "SELECT status FROM [order] WHERE order_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("status");
            }
        }
        return null;
    }

    /**
     * Lấy trạng thái đơn hàng theo mã đơn hàng (dùng cho polling VNPAY)
     */
    public String getStatusByCode(Connection conn, String orderCode) throws SQLException {
        String sql = "SELECT status FROM [order] WHERE order_code = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, orderCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("status");
            }
        }
        return null;
    }

    public int createOrderInTransaction(Connection conn, Order order) throws SQLException {
        String sql = """
            INSERT INTO [order] 
            (order_code, order_type, customer_id, branch_id, supplier_id, emp_id, 
             voucher_id, warehouse_id, subtotal, discount_amount, total_amount, 
             payment_method, status, created_at) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, GETDATE())
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
            if (order.getSupplierId() != null && order.getSupplierId() > 0) {
                ps.setInt(5, order.getSupplierId());
            } else {
                ps.setNull(5, java.sql.Types.INTEGER);
            }
            ps.setInt(6, order.getEmpId());
            if (order.getVoucherId() != null && order.getVoucherId() > 0) {
                ps.setInt(7, order.getVoucherId());
            } else {
                ps.setNull(7, java.sql.Types.INTEGER);
            }
            ps.setInt(8, order.getWarehouseId());
            ps.setDouble(9, order.getSubtotal());
            ps.setDouble(10, order.getDiscountAmount());
            ps.setDouble(11, order.getTotalAmount());
            ps.setString(12, order.getPaymentMethod());
            ps.setString(13, order.getStatus() != null ? order.getStatus().name() : "PENDING");

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
