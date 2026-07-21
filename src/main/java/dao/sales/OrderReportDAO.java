package dao.sales;

import model.BranchKpi;
import model.EmployeeKpi;
import model.Order;
import model.OrderReportFilter;
import model.OrderReportKpi;
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

    void appendFilters(StringBuilder sql, List<Object> params, OrderReportFilter f) {
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

    public OrderReportKpi calculateKpiSummary(OrderReportFilter f) {
        OrderReportKpi kpi = new OrderReportKpi();
        StringBuilder sql = new StringBuilder(
            "SELECT COUNT(*) AS total_orders, " +
            "SUM(CASE WHEN o.status = 'COMPLETED' THEN 1 ELSE 0 END) AS completed_orders, " +
            "SUM(CASE WHEN o.status = 'CANCELLED' THEN 1 ELSE 0 END) AS cancelled_orders, " +
            "COALESCE(SUM(CASE WHEN o.status = 'COMPLETED' THEN o.total_amount ELSE 0 END), 0) AS total_revenue "
        ).append(FROM);
        List<Object> params = new ArrayList<>();
        appendFilters(sql, params, f);

        String debugSql = sql.toString();
        System.out.println("[KPI-DEBUG] KpiSummary SQL: " + debugSql);
        System.out.println("[KPI-DEBUG] KpiSummary params: " + params);
        System.out.println("[KPI-DEBUG] KpiSummary empId: " + f.getEmpId());
        System.out.println("[KPI-DEBUG] KpiSummary branchId: " + f.getBranchId());
        System.out.println("[KPI-DEBUG] KpiSummary dateFrom: " + f.getDateFrom());
        System.out.println("[KPI-DEBUG] KpiSummary dateTo: " + f.getDateTo());
        System.out.println("[KPI-DEBUG] KpiSummary orderStatus: " + f.getOrderStatus());

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(debugSql)) {
            int idx = 1;
            for (Object p : params) setParam(ps, idx++, p);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int total = rs.getInt("total_orders");
                    int completed = rs.getInt("completed_orders");
                    int cancelled = rs.getInt("cancelled_orders");
                    double revenue = rs.getDouble("total_revenue");

                    kpi.setTotalOrders(total);
                    kpi.setCompletedOrders(completed);
                    kpi.setCancelledOrders(cancelled);
                    kpi.setTotalRevenue(revenue);
                    kpi.setAov(completed > 0 ? revenue / completed : 0);
                    kpi.setCompletionRate(total > 0 ? (double) completed / total * 100 : 0);

                    System.out.println("[KPI-DEBUG] KpiSummary result: total=" + total + " completed=" + completed + " cancelled=" + cancelled + " revenue=" + revenue + " aov=" + kpi.getAov() + " rate=" + kpi.getCompletionRate());
                }
            }
        } catch (SQLException e) {
            System.err.println("[KPI-DEBUG] KpiSummary SQL error: " + e.getMessage());
            e.printStackTrace();
        }
        return kpi;
    }

    public EmployeeKpi calculateEmployeeKpi(OrderReportFilter f) {
        if (f.getEmpId() == null) return null;
        EmployeeKpi kpi = new EmployeeKpi();
        StringBuilder sql = new StringBuilder(
            "SELECT e.fullName AS employee_name, " +
            "COUNT(*) AS total_orders, " +
            "SUM(CASE WHEN o.status = 'COMPLETED' THEN 1 ELSE 0 END) AS completed_orders, " +
            "SUM(CASE WHEN o.status = 'CANCELLED' THEN 1 ELSE 0 END) AS cancelled_orders, " +
            "COALESCE(SUM(CASE WHEN o.status = 'COMPLETED' THEN o.total_amount ELSE 0 END), 0) AS revenue " +
            "FROM [Order] o " +
            "JOIN Employee e ON o.emp_id = e.emp_id " +
            "JOIN Branch b ON o.branch_id = b.branch_id " +
            "LEFT JOIN Customer c ON o.customer_id = c.cus_id " +
            "WHERE o.order_type = 'SALE' AND o.emp_id = ?"
        );
        List<Object> params = new ArrayList<>();
        params.add(f.getEmpId());

        OrderReportFilter tmp = copyFilterWithoutEmpId(f);
        appendFilters(sql, params, tmp);
        sql.append(" GROUP BY e.fullName, e.emp_id");

        String debugSql = sql.toString();
        System.out.println("[KPI-DEBUG] EmployeeKpi SQL: " + debugSql);
        System.out.println("[KPI-DEBUG] EmployeeKpi params: " + params);
        System.out.println("[KPI-DEBUG] EmployeeKpi empId: " + f.getEmpId());

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(debugSql)) {
            int idx = 1;
            for (Object p : params) setParam(ps, idx++, p);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int total = rs.getInt("total_orders");
                    int completed = rs.getInt("completed_orders");
                    int cancelled = rs.getInt("cancelled_orders");
                    double revenue = rs.getDouble("revenue");

                    kpi.setEmployeeName(rs.getString("employee_name"));
                    kpi.setCompletedOrders(completed);
                    kpi.setCancelledOrders(cancelled);
                    kpi.setRevenue(revenue);
                    kpi.setAov(completed > 0 ? revenue / completed : 0);
                    kpi.setCompletionRate(total > 0 ? (double) completed / total * 100 : 0);

                    System.out.println("[KPI-DEBUG] EmployeeKpi result: total=" + total + " completed=" + completed + " cancelled=" + cancelled + " revenue=" + revenue);
                } else {
                    System.out.println("[KPI-DEBUG] EmployeeKpi: no rows returned");
                }
            }
        } catch (SQLException e) {
            System.err.println("[KPI-DEBUG] EmployeeKpi SQL error: " + e.getMessage());
            e.printStackTrace();
        }
        return kpi;
    }

    public List<BranchKpi> calculateBranchKpi(OrderReportFilter f) {
        List<BranchKpi> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT b.branch_id, b.branch_name, " +
            "COUNT(*) AS total_orders, " +
            "COALESCE(SUM(CASE WHEN o.status = 'COMPLETED' THEN o.total_amount ELSE 0 END), 0) AS revenue " +
            "FROM [Order] o " +
            "JOIN Branch b ON o.branch_id = b.branch_id " +
            "JOIN Employee e ON o.emp_id = e.emp_id " +
            "LEFT JOIN Customer c ON o.customer_id = c.cus_id " +
            "WHERE o.order_type = 'SALE'"
        );
        List<Object> params = new ArrayList<>();
        appendFilters(sql, params, f);
        sql.append(" GROUP BY b.branch_id, b.branch_name ORDER BY revenue DESC");

        String debugSql = sql.toString();
        System.out.println("[KPI-DEBUG] BranchKpi SQL: " + debugSql);
        System.out.println("[KPI-DEBUG] BranchKpi params: " + params);

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(debugSql)) {
            int idx = 1;
            for (Object p : params) setParam(ps, idx++, p);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BranchKpi bk = new BranchKpi();
                    bk.setBranchId(rs.getInt("branch_id"));
                    bk.setBranchName(rs.getString("branch_name"));
                    bk.setOrders(rs.getInt("total_orders"));
                    bk.setRevenue(rs.getDouble("revenue"));
                    list.add(bk);
                }
            }
            System.out.println("[KPI-DEBUG] BranchKpi rows returned: " + list.size());
        } catch (SQLException e) {
            System.err.println("[KPI-DEBUG] BranchKpi SQL error: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    private OrderReportFilter copyFilterWithoutEmpId(OrderReportFilter f) {
        OrderReportFilter c = new OrderReportFilter();
        c.setDateFrom(f.getDateFrom());
        c.setDateTo(f.getDateTo());
        c.setBranchId(f.getBranchId());
        c.setCustomerId(f.getCustomerId());
        c.setOrderId(f.getOrderId());
        c.setOrderStatus(f.getOrderStatus());
        c.setPaymentMethod(f.getPaymentMethod());
        c.setKeyword(f.getKeyword());
        c.setSortBy(f.getSortBy());
        c.setSortDir(f.getSortDir());
        return c;
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
