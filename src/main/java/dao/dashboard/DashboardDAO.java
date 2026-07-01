package dao.dashboard;

import model.DashboardOverview;
import model.DashboardOverview.BranchRevenue;
import model.DashboardOverview.TopProduct;
import util.database.DBContext;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO phục vụ Dashboard Owner Overview.
 * Truy vấn tổng hợp từ các bảng: [order], order_detail, customer,
 * inventory, product, Branch, Employee.
 * Tất cả phương thức chỉ READ – không thay đổi dữ liệu.
 */
public class DashboardDAO {

    // ──────────────────────── REVENUE ────────────────────────

    /** Tổng doanh thu hôm nay (đơn COMPLETED, loại SALE). */
    public BigDecimal getRevenueToday() throws SQLException {
        String sql = "SELECT ISNULL(SUM(total_amount), 0) "
                   + "FROM [order] "
                   + "WHERE status = 'COMPLETED' AND order_type = 'SALE' "
                   + "  AND CAST(created_at AS DATE) = CAST(GETDATE() AS DATE)";
        return queryBigDecimal(sql);
    }

    /** Tổng doanh thu hôm qua. */
    public BigDecimal getRevenueYesterday() throws SQLException {
        String sql = "SELECT ISNULL(SUM(total_amount), 0) "
                   + "FROM [order] "
                   + "WHERE status = 'COMPLETED' AND order_type = 'SALE' "
                   + "  AND CAST(created_at AS DATE) = CAST(DATEADD(DAY, -1, GETDATE()) AS DATE)";
        return queryBigDecimal(sql);
    }

    /** Tổng doanh thu tháng này. */
    public BigDecimal getRevenueThisMonth() throws SQLException {
        String sql = "SELECT ISNULL(SUM(total_amount), 0) "
                   + "FROM [order] "
                   + "WHERE status = 'COMPLETED' AND order_type = 'SALE' "
                   + "  AND YEAR(created_at) = YEAR(GETDATE()) AND MONTH(created_at) = MONTH(GETDATE())";
        return queryBigDecimal(sql);
    }

    /** Tổng doanh thu tháng trước. */
    public BigDecimal getRevenueLastMonth() throws SQLException {
        String sql = "SELECT ISNULL(SUM(total_amount), 0) "
                   + "FROM [order] "
                   + "WHERE status = 'COMPLETED' AND order_type = 'SALE' "
                   + "  AND YEAR(created_at) = YEAR(DATEADD(MONTH, -1, GETDATE())) "
                   + "  AND MONTH(created_at) = MONTH(DATEADD(MONTH, -1, GETDATE()))";
        return queryBigDecimal(sql);
    }

    /** Tổng doanh thu năm nay. */
    public BigDecimal getRevenueThisYear() throws SQLException {
        String sql = "SELECT ISNULL(SUM(total_amount), 0) "
                   + "FROM [order] "
                   + "WHERE status = 'COMPLETED' AND order_type = 'SALE' "
                   + "  AND YEAR(created_at) = YEAR(GETDATE())";
        return queryBigDecimal(sql);
    }

    // ──────────────────────── ORDERS ────────────────────────

    /** Số đơn hàng bán hôm nay (COMPLETED). */
    public int getOrdersToday() throws SQLException {
        String sql = "SELECT COUNT(*) FROM [order] "
                   + "WHERE order_type = 'SALE' AND status = 'COMPLETED' "
                   + "  AND CAST(created_at AS DATE) = CAST(GETDATE() AS DATE)";
        return queryInt(sql);
    }

    /** Số đơn hàng bán tháng này (COMPLETED). */
    public int getOrdersThisMonth() throws SQLException {
        String sql = "SELECT COUNT(*) FROM [order] "
                   + "WHERE order_type = 'SALE' AND status = 'COMPLETED' "
                   + "  AND YEAR(created_at) = YEAR(GETDATE()) AND MONTH(created_at) = MONTH(GETDATE())";
        return queryInt(sql);
    }

    /** Tổng số đơn hàng bán (tất cả trạng thái). */
    public int getTotalOrders() throws SQLException {
        String sql = "SELECT COUNT(*) FROM [order] WHERE order_type = 'SALE'";
        return queryInt(sql);
    }

    /** Số đơn đang chờ xử lý. */
    public int getPendingOrders() throws SQLException {
        String sql = "SELECT COUNT(*) FROM [order] WHERE order_type = 'SALE' AND status = 'PENDING'";
        return queryInt(sql);
    }

    /** Giá trị trung bình mỗi đơn hàng (COMPLETED, SALE). */
    public BigDecimal getAverageOrderValue() throws SQLException {
        String sql = "SELECT ISNULL(AVG(total_amount), 0) FROM [order] "
                   + "WHERE order_type = 'SALE' AND status = 'COMPLETED'";
        return queryBigDecimal(sql);
    }

    // ──────────────────────── CUSTOMERS ────────────────────────

    /** Tổng số khách hàng. */
    public int getTotalCustomers() throws SQLException {
        String sql = "SELECT COUNT(*) FROM customer";
        return queryInt(sql);
    }

    /** Số khách mới tháng này. */
    public int getNewCustomersThisMonth() throws SQLException {
        String sql = "SELECT COUNT(*) FROM customer "
                   + "WHERE YEAR(created_at) = YEAR(GETDATE()) AND MONTH(created_at) = MONTH(GETDATE())";
        return queryInt(sql);
    }

    /** Khách hàng đang hoạt động. */
    public int getActiveCustomers() throws SQLException {
        String sql = "SELECT COUNT(*) FROM customer WHERE status = 'ACTIVE'";
        return queryInt(sql);
    }

    // ──────────────────────── INVENTORY ────────────────────────

    /** Tổng số sản phẩm trong hệ thống. */
    public int getTotalProducts() throws SQLException {
        String sql = "SELECT COUNT(*) FROM [product]";
        return queryInt(sql);
    }

    /** Số sản phẩm hết hàng (quantity_in_stock = 0 hoặc status OUT_OF_STOCK). */
    public int getOutOfStockItems() throws SQLException {
        String sql = "SELECT COUNT(*) FROM inventory WHERE quantity_in_stock = 0 OR status = 'OUT_OF_STOCK'";
        return queryInt(sql);
    }

    /** Số sản phẩm tồn kho thấp (<=10 và > 0). */
    public int getLowStockItems() throws SQLException {
        String sql = "SELECT COUNT(*) FROM inventory WHERE quantity_in_stock > 0 AND quantity_in_stock <= 10";
        return queryInt(sql);
    }

    /** Tổng giá trị hàng tồn kho (quantity * selling_price). */
    public BigDecimal getTotalStockValue() throws SQLException {
        String sql = "SELECT ISNULL(SUM(i.quantity_in_stock * p.selling_price), 0) "
                   + "FROM inventory i JOIN [product] p ON i.product_id = p.product_id";
        return queryBigDecimal(sql);
    }

    // ──────────────────────── STORES / EMPLOYEES ────────────────────────

    /** Tổng số chi nhánh đang hoạt động. */
    public int getTotalStores() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Branch WHERE status = 'ACTIVE'";
        return queryInt(sql);
    }

    /** Tổng số nhân viên đang hoạt động. */
    public int getTotalEmployees() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Employee WHERE status = 'ACTIVE'";
        return queryInt(sql);
    }

    // ──────────────────────── CHARTS ────────────────────────

    /** Doanh thu theo chi nhánh (tháng này, đơn COMPLETED SALE). */
    public List<BranchRevenue> getBranchRevenues() throws SQLException {
        String sql = "SELECT b.branch_id, b.branch_name, b.branch_code, "
                   + "       ISNULL(SUM(o.total_amount), 0) AS revenue, COUNT(o.order_id) AS order_count "
                   + "FROM Branch b "
                   + "LEFT JOIN [order] o ON o.branch_id = b.branch_id "
                   + "     AND o.status = 'COMPLETED' AND o.order_type = 'SALE' "
                   + "     AND YEAR(o.created_at) = YEAR(GETDATE()) "
                   + "     AND MONTH(o.created_at) = MONTH(GETDATE()) "
                   + "WHERE b.status = 'ACTIVE' "
                   + "GROUP BY b.branch_id, b.branch_name, b.branch_code "
                   + "ORDER BY revenue DESC";
        List<BranchRevenue> list = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                BranchRevenue br = new BranchRevenue();
                br.setBranchId(rs.getInt("branch_id"));
                br.setBranchName(rs.getString("branch_name"));
                br.setBranchCode(rs.getString("branch_code"));
                br.setRevenue(rs.getBigDecimal("revenue"));
                br.setOrderCount(rs.getInt("order_count"));
                list.add(br);
            }
        }
        return list;
    }

    /** Top 5 sản phẩm bán chạy nhất tháng này. */
    public List<TopProduct> getTopProducts() throws SQLException {
        String sql = "SELECT TOP 5 p.product_id, p.product_name, "
                   + "       SUM(od.quantity) AS quantity_sold, "
                   + "       SUM(od.total_price) AS revenue "
                   + "FROM order_detail od "
                   + "JOIN [order] o ON od.order_id = o.order_id "
                   + "JOIN [product] p ON od.product_id = p.product_id "
                   + "WHERE o.status = 'COMPLETED' AND o.order_type = 'SALE' "
                   + "  AND YEAR(o.created_at) = YEAR(GETDATE()) AND MONTH(o.created_at) = MONTH(GETDATE()) "
                   + "GROUP BY p.product_id, p.product_name "
                   + "ORDER BY quantity_sold DESC";
        List<TopProduct> list = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                TopProduct tp = new TopProduct();
                tp.setProductId(rs.getInt("product_id"));
                tp.setProductName(rs.getString("product_name"));
                tp.setQuantitySold(rs.getInt("quantity_sold"));
                tp.setRevenue(rs.getBigDecimal("revenue"));
                list.add(tp);
            }
        }
        return list;
    }

    // ──────────────────────── AGGREGATE ────────────────────────

    /** Gọi tất cả query và trả về model DashboardOverview hoàn chỉnh. */
    public DashboardOverview getOwnerOverview() throws SQLException {
        DashboardOverview ov = new DashboardOverview();

        ov.setRevenueToday(getRevenueToday());
        ov.setRevenueYesterday(getRevenueYesterday());
        ov.setRevenueThisMonth(getRevenueThisMonth());
        ov.setRevenueLastMonth(getRevenueLastMonth());
        ov.setRevenueThisYear(getRevenueThisYear());

        ov.setOrdersToday(getOrdersToday());
        ov.setOrdersThisMonth(getOrdersThisMonth());
        ov.setTotalOrders(getTotalOrders());
        ov.setPendingOrders(getPendingOrders());
        ov.setAverageOrderValue(getAverageOrderValue());

        ov.setTotalCustomers(getTotalCustomers());
        ov.setNewCustomersThisMonth(getNewCustomersThisMonth());
        ov.setActiveCustomers(getActiveCustomers());

        ov.setTotalProducts(getTotalProducts());
        ov.setOutOfStockItems(getOutOfStockItems());
        ov.setLowStockItems(getLowStockItems());
        ov.setTotalStockValue(getTotalStockValue());

        ov.setTotalStores(getTotalStores());
        ov.setTotalEmployees(getTotalEmployees());

        List<BranchRevenue> branches = getBranchRevenues();
        ov.setBranchRevenues(branches);
        if (!branches.isEmpty()) {
            ov.setTopStoreName(branches.get(0).getBranchName());
            ov.setTopStoreRevenue(branches.get(0).getRevenue());
        }

        ov.setTopProducts(getTopProducts());

        return ov;
    }

    // ──────────────────────── UTIL ────────────────────────

    private BigDecimal queryBigDecimal(String sql) throws SQLException {
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
        }
    }

    private int queryInt(String sql) throws SQLException {
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}
