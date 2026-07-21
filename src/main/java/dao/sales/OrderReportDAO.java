package dao.sales;

import model.Order;
import model.OrderReportFilter;
import util.database.DBContext;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OrderReportDAO {

    private static final String SELECT =
            "SELECT o.*, c.full_name AS customerName, e.fullName AS employeeName, b.branch_name AS branchName";

    private static final String FROM =
            "FROM [Order] o " +
            "LEFT JOIN Customer c ON o.customer_id = c.cus_id " +
            "JOIN Employee e ON o.emp_id = e.emp_id " +
            "JOIN Branch b ON o.branch_id = b.branch_id " +
            "WHERE o.order_type = 'SALE'";

    public List<Order> searchOrders(OrderReportFilter f, int page, int pageSize) {
        List<Order> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(SELECT).append(" ").append(FROM);
        List<Object> params = new ArrayList<>();
        appendFilters(sql, params, f);
        sql.append(" ORDER BY ").append(f.getSortColumn()).append(" ").append(f.getSortDirection());
        sql.append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            for (Object p : params) {
                setParam(ps, idx++, p);
            }
            ps.setInt(idx++, (page - 1) * pageSize);
            ps.setInt(idx, pageSize);

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

    public int countOrders(OrderReportFilter f) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) AS Total ").append(FROM);
        List<Object> params = new ArrayList<>();
        appendFilters(sql, params, f);

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            for (Object p : params) {
                setParam(ps, idx++, p);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("Total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void appendFilters(StringBuilder sql, List<Object> params, OrderReportFilter f) {
        if (f.getDateFrom() != null) {
            sql.append(" AND CAST(o.created_at AS DATE) >= ?");
            params.add(Date.valueOf(f.getDateFrom()));
        }
        if (f.getDateTo() != null) {
            sql.append(" AND CAST(o.created_at AS DATE) <= ?");
            params.add(Date.valueOf(f.getDateTo()));
        }
        if (f.getEmpId() != null) {
            sql.append(" AND o.emp_id = ?");
            params.add(f.getEmpId());
        }
        if (f.getBranchId() != null) {
            sql.append(" AND o.branch_id = ?");
            params.add(f.getBranchId());
        }
        if (f.getCustomerId() != null) {
            sql.append(" AND o.customer_id = ?");
            params.add(f.getCustomerId());
        }
        if (f.getOrderId() != null) {
            sql.append(" AND o.order_id = ?");
            params.add(f.getOrderId());
        }
        if (f.getOrderStatus() != null) {
            sql.append(" AND o.status = ?");
            params.add(f.getOrderStatus());
        }
        if (f.getPaymentMethod() != null) {
            sql.append(" AND o.payment_method = ?");
            params.add(f.getPaymentMethod());
        }
        if (f.getKeyword() != null && !f.getKeyword().trim().isEmpty()) {
            String kw = "%" + f.getKeyword().trim() + "%";
            sql.append(" AND (o.order_code LIKE ? OR c.full_name LIKE ? OR e.fullName LIKE ? OR b.branch_name LIKE ?)");
            params.add(kw); params.add(kw); params.add(kw); params.add(kw);
        }
    }

    private void setParam(PreparedStatement ps, int idx, Object value) throws SQLException {
        if (value == null) {
            ps.setNull(idx, Types.NULL);
        } else if (value instanceof Date) {
            ps.setDate(idx, (Date) value);
        } else if (value instanceof Integer) {
            ps.setInt(idx, (Integer) value);
        } else {
            ps.setString(idx, value.toString());
        }
    }

    private Order mapRow(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setOrderId(rs.getInt("order_id"));
        o.setOrderCode(rs.getString("order_code"));
        o.setOrderType(rs.getString("order_type"));
        o.setCustomerId(rs.getObject("customer_id") != null ? rs.getInt("customer_id") : null);
        o.setBranchId(rs.getInt("branch_id"));
        o.setEmpId(rs.getInt("emp_id"));
        o.setSubtotal(rs.getDouble("subtotal"));
        o.setDiscountAmount(rs.getDouble("discount_amount"));
        o.setTotalAmount(rs.getDouble("total_amount"));
        o.setPaymentMethod(rs.getString("payment_method"));
        String status = rs.getString("status");
        if (status != null) {
            o.setStatus(Order.OrderStatus.valueOf(status));
        }
        o.setCreatedAt(rs.getString("created_at"));
        o.setCustomerName(rs.getString("customerName"));
        o.setEmployeeName(rs.getString("employeeName"));
        o.setBranchName(rs.getString("branchName"));
        return o;
    }
}
