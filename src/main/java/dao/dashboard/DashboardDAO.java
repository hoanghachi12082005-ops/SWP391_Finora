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

    // ──────────────────────── REVENUE ────────────────────────

    /** Tổng doanh thu hôm nay (đơn COMPLETED, loại SALE). */
    public BigDecimal getRevenueToday() throws SQLException {
        String sql = "SELECT ISNULL(SUM(total_amount), 0) "
                   + "FROM [order] WITH (NOLOCK) "
                   + "WHERE status = 'COMPLETED' AND order_type = 'SALE' "
                   + "  AND CAST(created_at AS DATE) = CAST(GETDATE() AS DATE)";
        return queryBigDecimal(sql);
    }

    /** Tổng doanh thu hôm qua. */
    public BigDecimal getRevenueYesterday() throws SQLException {
        String sql = "SELECT ISNULL(SUM(total_amount), 0) "
                   + "FROM [order] WITH (NOLOCK) "
                   + "WHERE status = 'COMPLETED' AND order_type = 'SALE' "
                   + "  AND CAST(created_at AS DATE) = CAST(DATEADD(DAY, -1, GETDATE()) AS DATE)";
        return queryBigDecimal(sql);
    }

    /** Tổng doanh thu tháng này. */
    public BigDecimal getRevenueThisMonth() throws SQLException {
        String sql = "SELECT ISNULL(SUM(total_amount), 0) "
                   + "FROM [order] WITH (NOLOCK) "
                   + "WHERE status = 'COMPLETED' AND order_type = 'SALE' "
                   + "  AND YEAR(created_at) = YEAR(GETDATE()) AND MONTH(created_at) = MONTH(GETDATE())";
        return queryBigDecimal(sql);
    }

    /** Tổng doanh thu tháng trước. */
    public BigDecimal getRevenueLastMonth() throws SQLException {
        String sql = "SELECT ISNULL(SUM(total_amount), 0) "
                   + "FROM [order] WITH (NOLOCK) "
                   + "WHERE status = 'COMPLETED' AND order_type = 'SALE' "
                   + "  AND YEAR(created_at) = YEAR(DATEADD(MONTH, -1, GETDATE())) "
                   + "  AND MONTH(created_at) = MONTH(DATEADD(MONTH, -1, GETDATE()))";
        return queryBigDecimal(sql);
    }

    /** Tổng doanh thu năm nay. */
    public BigDecimal getRevenueThisYear() throws SQLException {
        String sql = "SELECT ISNULL(SUM(total_amount), 0) "
                   + "FROM [order] WITH (NOLOCK) "
                   + "WHERE status = 'COMPLETED' AND order_type = 'SALE' "
                   + "  AND YEAR(created_at) = YEAR(GETDATE())";
        return queryBigDecimal(sql);
    }

    // ──────────────────────── ORDERS ────────────────────────

    /** Số đơn hàng bán hôm nay (COMPLETED). */
    public int getOrdersToday() throws SQLException {
        String sql = "SELECT COUNT(*) FROM [order] WITH (NOLOCK) "
                   + "WHERE order_type = 'SALE' AND status = 'COMPLETED' "
                   + "  AND CAST(created_at AS DATE) = CAST(GETDATE() AS DATE)";
        return queryInt(sql);
    }

    /** Số đơn hàng bán tháng này (COMPLETED). */
    public int getOrdersThisMonth() throws SQLException {
        String sql = "SELECT COUNT(*) FROM [order] WITH (NOLOCK) "
                   + "WHERE order_type = 'SALE' AND status = 'COMPLETED' "
                   + "  AND YEAR(created_at) = YEAR(GETDATE()) AND MONTH(created_at) = MONTH(GETDATE())";
        return queryInt(sql);
    }

    /** Tổng số đơn hàng bán (tất cả trạng thái). */
    public int getTotalOrders() throws SQLException {
        String sql = "SELECT COUNT(*) FROM [order] WITH (NOLOCK) WHERE order_type = 'SALE'";
        return queryInt(sql);
    }

    /** Số đơn đang chờ xử lý. */
    public int getPendingOrders() throws SQLException {
        String sql = "SELECT COUNT(*) FROM [order] WITH (NOLOCK) WHERE order_type = 'SALE' AND status = 'PENDING'";
        return queryInt(sql);
    }

    /** Giá trị trung bình mỗi đơn hàng (COMPLETED, SALE). */
    public BigDecimal getAverageOrderValue() throws SQLException {
        String sql = "SELECT ISNULL(AVG(total_amount), 0) FROM [order] WITH (NOLOCK) "
                   + "WHERE order_type = 'SALE' AND status = 'COMPLETED'";
        return queryBigDecimal(sql);
    }

    // ──────────────────────── CUSTOMERS ────────────────────────

    /** Tổng số khách hàng. */
    public int getTotalCustomers() throws SQLException {
        String sql = "SELECT COUNT(*) FROM customer WITH (NOLOCK)";
        return queryInt(sql);
    }

    /** Số khách mới tháng này. */
    public int getNewCustomersThisMonth() throws SQLException {
        String sql = "SELECT COUNT(*) FROM customer WITH (NOLOCK) "
                   + "WHERE YEAR(created_at) = YEAR(GETDATE()) AND MONTH(created_at) = MONTH(GETDATE())";
        return queryInt(sql);
    }

    /** Khách hàng đang hoạt động. */
    public int getActiveCustomers() throws SQLException {
        String sql = "SELECT COUNT(*) FROM customer WITH (NOLOCK) WHERE status = 'ACTIVE'";
        return queryInt(sql);
    }

    // ──────────────────────── INVENTORY ────────────────────────

    /** Tổng số sản phẩm trong hệ thống. */
    public int getTotalProducts() throws SQLException {
        String sql = "SELECT COUNT(*) FROM [product] WITH (NOLOCK)";
        return queryInt(sql);
    }

    /** Số sản phẩm hết hàng (quantity_in_stock = 0 hoặc status OUT_OF_STOCK). */
    public int getOutOfStockItems() throws SQLException {
        String sql = "SELECT COUNT(*) FROM inventory WITH (NOLOCK) WHERE quantity_in_stock = 0 OR status = 'OUT_OF_STOCK'";
        return queryInt(sql);
    }

    /** Số sản phẩm tồn kho thấp (<=10 và > 0). */
    public int getLowStockItems() throws SQLException {
        String sql = "SELECT COUNT(*) FROM inventory WITH (NOLOCK) WHERE quantity_in_stock > 0 AND quantity_in_stock <= 10";
        return queryInt(sql);
    }

    /** Tổng giá trị hàng tồn kho (quantity * selling_price). */
    public BigDecimal getTotalStockValue() throws SQLException {
        String sql = "SELECT ISNULL(SUM(i.quantity_in_stock * p.selling_price), 0) "
                   + "FROM inventory i WITH (NOLOCK) JOIN [product] p WITH (NOLOCK) ON i.product_id = p.product_id";
        return queryBigDecimal(sql);
    }

    // ──────────────────────── STORES / EMPLOYEES ────────────────────────

    /** Tổng số chi nhánh đang hoạt động. */
    public int getTotalStores() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Branch WITH (NOLOCK) WHERE status = 'ACTIVE'";
        return queryInt(sql);
    }

    /** Tổng số nhân viên đang hoạt động. */
    public int getTotalEmployees() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Employee WITH (NOLOCK) WHERE status = 'ACTIVE'";
        return queryInt(sql);
    }

    // ──────────────────────── CHARTS ────────────────────────

    /** Doanh thu theo chi nhánh (tháng này, đơn COMPLETED SALE). */
    public List<BranchRevenue> getBranchRevenues() throws SQLException {
        String sql = "SELECT b.branch_id, b.branch_name, b.branch_code, "
                   + "       ISNULL(SUM(o.total_amount), 0) AS revenue, COUNT(o.order_id) AS order_count "
                   + "FROM Branch b WITH (NOLOCK) "
                   + "LEFT JOIN [order] o WITH (NOLOCK) ON o.branch_id = b.branch_id "
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
                   + "FROM order_detail od WITH (NOLOCK) "
                   + "JOIN [order] o WITH (NOLOCK) ON od.order_id = o.order_id "
                   + "JOIN [product] p WITH (NOLOCK) ON od.product_id = p.product_id "
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
        String sql = "SELECT "
                + "(SELECT ISNULL(SUM(total_amount),0) FROM [order] WITH (NOLOCK) WHERE status='COMPLETED' AND order_type='SALE' AND CAST(created_at AS DATE)=CAST(GETDATE() AS DATE)) AS revenue_today, "
                + "(SELECT ISNULL(SUM(total_amount),0) FROM [order] WITH (NOLOCK) WHERE status='COMPLETED' AND order_type='SALE' AND CAST(created_at AS DATE)=CAST(DATEADD(DAY,-1,GETDATE()) AS DATE)) AS revenue_yesterday, "
                + "(SELECT ISNULL(SUM(total_amount),0) FROM [order] WITH (NOLOCK) WHERE status='COMPLETED' AND order_type='SALE' AND YEAR(created_at)=YEAR(GETDATE()) AND MONTH(created_at)=MONTH(GETDATE())) AS revenue_this_month, "
                + "(SELECT ISNULL(SUM(total_amount),0) FROM [order] WITH (NOLOCK) WHERE status='COMPLETED' AND order_type='SALE' AND YEAR(created_at)=YEAR(DATEADD(MONTH,-1,GETDATE())) AND MONTH(created_at)=MONTH(DATEADD(MONTH,-1,GETDATE()))) AS revenue_last_month, "
                + "(SELECT ISNULL(SUM(total_amount),0) FROM [order] WITH (NOLOCK) WHERE status='COMPLETED' AND order_type='SALE' AND YEAR(created_at)=YEAR(GETDATE())) AS revenue_this_year, "
                + "(SELECT COUNT(*) FROM [order] WITH (NOLOCK) WHERE order_type='SALE' AND status='COMPLETED' AND CAST(created_at AS DATE)=CAST(GETDATE() AS DATE)) AS orders_today, "
                + "(SELECT COUNT(*) FROM [order] WITH (NOLOCK) WHERE order_type='SALE' AND status='COMPLETED' AND YEAR(created_at)=YEAR(GETDATE()) AND MONTH(created_at)=MONTH(GETDATE())) AS orders_this_month, "
                + "(SELECT COUNT(*) FROM [order] WITH (NOLOCK) WHERE order_type='SALE') AS total_orders, "
                + "(SELECT COUNT(*) FROM [order] WITH (NOLOCK) WHERE order_type='SALE' AND status='PENDING') AS pending_orders, "
                + "(SELECT ISNULL(AVG(total_amount),0) FROM [order] WITH (NOLOCK) WHERE order_type='SALE' AND status='COMPLETED') AS avg_order_value, "
                + "(SELECT COUNT(*) FROM customer WITH (NOLOCK)) AS total_customers, "
                + "(SELECT COUNT(*) FROM customer WITH (NOLOCK) WHERE YEAR(created_at)=YEAR(GETDATE()) AND MONTH(created_at)=MONTH(GETDATE())) AS new_customers, "
                + "(SELECT COUNT(*) FROM customer WITH (NOLOCK) WHERE status='ACTIVE') AS active_customers, "
                + "(SELECT COUNT(*) FROM product WITH (NOLOCK)) AS total_products, "
                + "(SELECT COUNT(*) FROM inventory WITH (NOLOCK) WHERE quantity_in_stock=0 OR status='OUT_OF_STOCK') AS out_of_stock, "
                + "(SELECT COUNT(*) FROM inventory WITH (NOLOCK) WHERE quantity_in_stock>0 AND quantity_in_stock<=10) AS low_stock, "
                + "(SELECT ISNULL(SUM(i.quantity_in_stock*p.selling_price),0) FROM inventory i WITH (NOLOCK) JOIN product p WITH (NOLOCK) ON i.product_id=p.product_id) AS total_stock_value, "
                + "(SELECT COUNT(*) FROM Branch WITH (NOLOCK) WHERE status='ACTIVE') AS total_stores, "
                + "(SELECT COUNT(*) FROM Employee WITH (NOLOCK) WHERE status='ACTIVE') AS total_employees";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            DashboardOverview ov = new DashboardOverview();
            if (rs.next()) {
                ov.setRevenueToday(rs.getBigDecimal("revenue_today"));
                ov.setRevenueYesterday(rs.getBigDecimal("revenue_yesterday"));
                ov.setRevenueThisMonth(rs.getBigDecimal("revenue_this_month"));
                ov.setRevenueLastMonth(rs.getBigDecimal("revenue_last_month"));
                ov.setRevenueThisYear(rs.getBigDecimal("revenue_this_year"));
                ov.setOrdersToday(rs.getInt("orders_today"));
                ov.setOrdersThisMonth(rs.getInt("orders_this_month"));
                ov.setTotalOrders(rs.getInt("total_orders"));
                ov.setPendingOrders(rs.getInt("pending_orders"));
                ov.setAverageOrderValue(rs.getBigDecimal("avg_order_value"));
                ov.setTotalCustomers(rs.getInt("total_customers"));
                ov.setNewCustomersThisMonth(rs.getInt("new_customers"));
                ov.setActiveCustomers(rs.getInt("active_customers"));
                ov.setTotalProducts(rs.getInt("total_products"));
                ov.setOutOfStockItems(rs.getInt("out_of_stock"));
                ov.setLowStockItems(rs.getInt("low_stock"));
                ov.setTotalStockValue(rs.getBigDecimal("total_stock_value"));
                ov.setTotalStores(rs.getInt("total_stores"));
                ov.setTotalEmployees(rs.getInt("total_employees"));
            }
            List<BranchRevenue> branches = getBranchRevenues();
            ov.setBranchRevenues(branches);
            if (!branches.isEmpty()) {
                ov.setTopStoreName(branches.get(0).getBranchName());
                ov.setTopStoreRevenue(branches.get(0).getRevenue());
            }
            ov.setTopProducts(getTopProducts());
            return ov;
        }
    }

    /** Lấy dữ liệu Tổng quan cho Dashboard của Store Manager (lọc theo branch_id). */
    public DashboardOverview getBranchOverview(int branchId) throws SQLException {
        String sql = "SELECT "
                + "(SELECT ISNULL(SUM(total_amount),0) FROM [order] WITH (NOLOCK) WHERE branch_id=? AND status='COMPLETED' AND order_type='SALE' AND CAST(created_at AS DATE)=CAST(GETDATE() AS DATE)) AS revenue_today, "
                + "(SELECT ISNULL(SUM(total_amount),0) FROM [order] WITH (NOLOCK) WHERE branch_id=? AND status='COMPLETED' AND order_type='SALE' AND CAST(created_at AS DATE)=CAST(DATEADD(DAY,-1,GETDATE()) AS DATE)) AS revenue_yesterday, "
                + "(SELECT ISNULL(SUM(total_amount),0) FROM [order] WITH (NOLOCK) WHERE branch_id=? AND status='COMPLETED' AND order_type='SALE' AND YEAR(created_at)=YEAR(GETDATE()) AND MONTH(created_at)=MONTH(GETDATE())) AS revenue_this_month, "
                + "(SELECT ISNULL(SUM(total_amount),0) FROM [order] WITH (NOLOCK) WHERE branch_id=? AND status='COMPLETED' AND order_type='SALE' AND YEAR(created_at)=YEAR(DATEADD(MONTH,-1,GETDATE())) AND MONTH(created_at)=MONTH(DATEADD(MONTH,-1,GETDATE()))) AS revenue_last_month, "
                + "(SELECT ISNULL(SUM(total_amount),0) FROM [order] WITH (NOLOCK) WHERE branch_id=? AND status='COMPLETED' AND order_type='SALE' AND YEAR(created_at)=YEAR(GETDATE())) AS revenue_this_year, "
                + "(SELECT COUNT(*) FROM [order] WITH (NOLOCK) WHERE branch_id=? AND order_type='SALE' AND status='COMPLETED' AND CAST(created_at AS DATE)=CAST(GETDATE() AS DATE)) AS orders_today, "
                + "(SELECT COUNT(*) FROM [order] WITH (NOLOCK) WHERE branch_id=? AND order_type='SALE' AND status='COMPLETED' AND YEAR(created_at)=YEAR(GETDATE()) AND MONTH(created_at)=MONTH(GETDATE())) AS orders_this_month, "
                + "(SELECT COUNT(*) FROM [order] WITH (NOLOCK) WHERE branch_id=? AND order_type='SALE') AS total_orders, "
                + "(SELECT COUNT(*) FROM [order] WITH (NOLOCK) WHERE branch_id=? AND order_type='SALE' AND status='PENDING') AS pending_orders, "
                + "(SELECT ISNULL(AVG(total_amount),0) FROM [order] WITH (NOLOCK) WHERE branch_id=? AND order_type='SALE' AND status='COMPLETED') AS avg_order_value, "
                + "(SELECT COUNT(DISTINCT customer_id) FROM [order] WITH (NOLOCK) WHERE branch_id=? AND customer_id IS NOT NULL) AS total_customers, "
                + "(SELECT COUNT(DISTINCT customer_id) FROM [order] WITH (NOLOCK) WHERE branch_id=? AND customer_id IS NOT NULL AND YEAR(created_at)=YEAR(GETDATE()) AND MONTH(created_at)=MONTH(GETDATE())) AS new_customers, "
                + "(SELECT COUNT(DISTINCT i.product_id) FROM inventory i WITH (NOLOCK) JOIN warehouse w WITH (NOLOCK) ON i.warehouse_id=w.warehouse_id WHERE w.branch_id=?) AS total_products, "
                + "(SELECT COUNT(DISTINCT i.product_id) FROM inventory i WITH (NOLOCK) JOIN warehouse w WITH (NOLOCK) ON i.warehouse_id=w.warehouse_id WHERE w.branch_id=? AND (i.quantity_in_stock=0 OR i.status='OUT_OF_STOCK')) AS out_of_stock, "
                + "(SELECT COUNT(DISTINCT i.product_id) FROM inventory i WITH (NOLOCK) JOIN warehouse w WITH (NOLOCK) ON i.warehouse_id=w.warehouse_id WHERE w.branch_id=? AND i.quantity_in_stock>0 AND i.quantity_in_stock<=10) AS low_stock, "
                + "(SELECT ISNULL(SUM(i.quantity_in_stock*p.selling_price),0) FROM inventory i WITH (NOLOCK) JOIN product p WITH (NOLOCK) ON i.product_id=p.product_id JOIN warehouse w WITH (NOLOCK) ON i.warehouse_id=w.warehouse_id WHERE w.branch_id=?) AS total_stock_value, "
                + "(SELECT COUNT(*) FROM Employee WITH (NOLOCK) WHERE branch_id=? AND status='ACTIVE') AS total_employees";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 1; i <= 17; i++) {
                ps.setInt(i, branchId);
            }
            DashboardOverview ov = new DashboardOverview();
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ov.setRevenueToday(rs.getBigDecimal("revenue_today"));
                    ov.setRevenueYesterday(rs.getBigDecimal("revenue_yesterday"));
                    ov.setRevenueThisMonth(rs.getBigDecimal("revenue_this_month"));
                    ov.setRevenueLastMonth(rs.getBigDecimal("revenue_last_month"));
                    ov.setRevenueThisYear(rs.getBigDecimal("revenue_this_year"));
                    ov.setOrdersToday(rs.getInt("orders_today"));
                    ov.setOrdersThisMonth(rs.getInt("orders_this_month"));
                    ov.setTotalOrders(rs.getInt("total_orders"));
                    ov.setPendingOrders(rs.getInt("pending_orders"));
                    ov.setAverageOrderValue(rs.getBigDecimal("avg_order_value"));
                    ov.setTotalCustomers(rs.getInt("total_customers"));
                    ov.setNewCustomersThisMonth(rs.getInt("new_customers"));
                    ov.setActiveCustomers(rs.getInt("total_customers"));
                    ov.setTotalProducts(rs.getInt("total_products"));
                    ov.setOutOfStockItems(rs.getInt("out_of_stock"));
                    ov.setLowStockItems(rs.getInt("low_stock"));
                    ov.setTotalStockValue(rs.getBigDecimal("total_stock_value"));
                    ov.setTotalStores(1);
                    ov.setTotalEmployees(rs.getInt("total_employees"));
                }
            }
            List<BranchRevenue> dailyRevenues = getBranchDailyRevenues(branchId);
            ov.setBranchRevenues(dailyRevenues);
            ov.setTopProducts(getTopProductsByBranch(branchId));
            return ov;
        }
    }

    /** Lấy doanh thu 7 ngày gần đây của 1 chi nhánh để dựng biểu đồ. */
    public List<BranchRevenue> getBranchDailyRevenues(int branchId) throws SQLException {
        String sql = "WITH Last7Days AS ("
                   + "  SELECT CAST(DATEADD(DAY, -i, GETDATE()) AS DATE) AS d_date "
                   + "  FROM (VALUES (6),(5),(4),(3),(2),(1),(0)) AS T(i)"
                   + ")"
                   + "SELECT d.d_date, "
                   + "       ISNULL(SUM(o.total_amount), 0) AS revenue, "
                   + "       COUNT(o.order_id) AS order_count "
                   + "FROM Last7Days d "
                   + "LEFT JOIN [order] o WITH (NOLOCK) ON CAST(o.created_at AS DATE) = d.d_date "
                   + "     AND o.branch_id = ? AND o.status = 'COMPLETED' AND o.order_type = 'SALE' "
                   + "GROUP BY d.d_date "
                   + "ORDER BY d.d_date ASC";
        List<BranchRevenue> list = new ArrayList<>();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM");
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, branchId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BranchRevenue br = new BranchRevenue();
                    java.sql.Date dDate = rs.getDate("d_date");
                    br.setBranchName(dDate != null ? sdf.format(dDate) : "");
                    br.setRevenue(rs.getBigDecimal("revenue"));
                    br.setOrderCount(rs.getInt("order_count"));
                    list.add(br);
                }
            }
        }
        return list;
    }

    /** Top 5 sản phẩm bán chạy nhất tháng này của 1 chi nhánh. */
    public List<TopProduct> getTopProductsByBranch(int branchId) throws SQLException {
        String sql = "SELECT TOP 5 p.product_id, p.product_name, "
                   + "       SUM(od.quantity) AS quantity_sold, "
                   + "       SUM(od.total_price) AS revenue "
                   + "FROM order_detail od WITH (NOLOCK) "
                   + "JOIN [order] o WITH (NOLOCK) ON od.order_id = o.order_id "
                   + "JOIN [product] p WITH (NOLOCK) ON od.product_id = p.product_id "
                   + "WHERE o.branch_id = ? AND o.status = 'COMPLETED' AND o.order_type = 'SALE' "
                   + "  AND YEAR(o.created_at) = YEAR(GETDATE()) AND MONTH(o.created_at) = MONTH(GETDATE()) "
                   + "GROUP BY p.product_id, p.product_name "
                   + "ORDER BY quantity_sold DESC";
        List<TopProduct> list = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, branchId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TopProduct tp = new TopProduct();
                    tp.setProductId(rs.getInt("product_id"));
                    tp.setProductName(rs.getString("product_name"));
                    tp.setQuantitySold(rs.getInt("quantity_sold"));
                    tp.setRevenue(rs.getBigDecimal("revenue"));
                    list.add(tp);
                }
            }
        }
        return list;
    }

    // ──────────────────────── FINANCIAL DASHBOARD ────────────────────────
    public static class FinancialData {
        public BigDecimal totalRevenue = BigDecimal.ZERO;
        public BigDecimal totalExpenses = BigDecimal.ZERO;
        public BigDecimal netProfit = BigDecimal.ZERO;
        public int totalInvoices;
        public List<model.DashboardOverview.BranchRevenue> branchRevenues = new ArrayList<>();
    }

    private String sanitizeDate(String date) {
        if (date == null || date.trim().isEmpty()) return null;
        String clean = date.trim();
        if (clean.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            return clean;
        }
        return null;
    }

    public FinancialData getFinancialData(String range) throws SQLException {
        return getFinancialData(range, null, null, null);
    }

    public FinancialData getFinancialData(String range, Integer branchId) throws SQLException {
        return getFinancialData(range, null, null, branchId);
    }

    public FinancialData getFinancialData(String range, String fromDate, String toDate, Integer branchId) throws SQLException {
        FinancialData fd = new FinancialData();
        
        String orderTimeFilter = "";
        String cleanFrom = sanitizeDate(fromDate);
        String cleanTo = sanitizeDate(toDate);

        if (cleanFrom != null) {
            orderTimeFilter += " AND CAST(o.created_at AS DATE) >= '" + cleanFrom + "' ";
        }
        if (cleanTo != null) {
            orderTimeFilter += " AND CAST(o.created_at AS DATE) <= '" + cleanTo + "' ";
        }

        if (cleanFrom == null && cleanTo == null) {
            if ("day".equalsIgnoreCase(range) || "today".equalsIgnoreCase(range)) {
                orderTimeFilter = " AND CAST(o.created_at AS DATE) = CAST(GETDATE() AS DATE) ";
            } else if ("week".equalsIgnoreCase(range) || "this_week".equalsIgnoreCase(range)) {
                orderTimeFilter = " AND DATEPART(WEEK, o.created_at) = DATEPART(WEEK, GETDATE()) AND YEAR(o.created_at) = YEAR(GETDATE()) ";
            } else { // default is month
                orderTimeFilter = " AND YEAR(o.created_at) = YEAR(GETDATE()) AND MONTH(o.created_at) = MONTH(GETDATE()) ";
            }
        }

        if (branchId != null) {
            orderTimeFilter += " AND o.branch_id = " + branchId + " ";
        }

        // 1. Total Revenue
        String revSql = "SELECT ISNULL(SUM(o.total_amount), 0) FROM [order] o WITH (NOLOCK) WHERE o.status IN ('COMPLETED', 'PAID') AND o.order_type IN ('SALE', 'RECEIPT') " + orderTimeFilter;
        fd.totalRevenue = queryBigDecimal(revSql);

        // 2. Total Expenses
        String expSql = "SELECT ISNULL(SUM(o.total_amount), 0) FROM [order] o WITH (NOLOCK) WHERE o.status IN ('COMPLETED', 'PAID') AND o.order_type IN ('PURCHASE', 'IMPORT', 'EXPENSE') " + orderTimeFilter;
        fd.totalExpenses = queryBigDecimal(expSql);

        // 3. Net Profit
        fd.netProfit = fd.totalRevenue.subtract(fd.totalExpenses);

        // 4. Total Invoices
        String invSql = "SELECT COUNT(*) FROM [order] o WITH (NOLOCK) WHERE o.status = 'COMPLETED' AND o.order_type = 'SALE' " + orderTimeFilter;
        fd.totalInvoices = queryInt(invSql);

        // 5. Branch Revenues / Performance
        String branchSql = "SELECT b.branch_id, b.branch_name, b.branch_code, b.status, "
                         + "       ISNULL(SUM(o.total_amount), 0) AS revenue, COUNT(o.order_id) AS order_count "
                         + "FROM Branch b WITH (NOLOCK) "
                         + "LEFT JOIN [order] o WITH (NOLOCK) ON o.branch_id = b.branch_id "
                         + "     AND o.status = 'COMPLETED' AND o.order_type = 'SALE' "
                         + orderTimeFilter
                         + "GROUP BY b.branch_id, b.branch_name, b.branch_code, b.status "
                         + "ORDER BY revenue DESC";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(branchSql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                model.DashboardOverview.BranchRevenue br = new model.DashboardOverview.BranchRevenue();
                br.setBranchId(rs.getInt("branch_id"));
                br.setBranchName(rs.getString("branch_name"));
                br.setBranchCode(rs.getString("branch_code"));
                br.setRevenue(rs.getBigDecimal("revenue"));
                br.setOrderCount(rs.getInt("order_count"));
                br.setStatus(rs.getString("status"));
                fd.branchRevenues.add(br);
            }
        }

        return fd;
    }

    public List<model.Payment> getBranchPayments(String range, Integer branchId) throws SQLException {
        return getBranchPayments(range, null, null, branchId, 1, 999999);
    }

    public List<model.Payment> getBranchPayments(String range, String fromDate, String toDate, Integer branchId) throws SQLException {
        return getBranchPayments(range, fromDate, toDate, branchId, 1, 999999);
    }

    public int getBranchPaymentsCount(String range, String fromDate, String toDate, Integer branchId) throws SQLException {
        String orderTimeFilter = "";
        String cleanFrom = sanitizeDate(fromDate);
        String cleanTo = sanitizeDate(toDate);

        if (cleanFrom != null) {
            orderTimeFilter += " AND CAST(o.created_at AS DATE) >= '" + cleanFrom + "' ";
        }
        if (cleanTo != null) {
            orderTimeFilter += " AND CAST(o.created_at AS DATE) <= '" + cleanTo + "' ";
        }

        if (cleanFrom == null && cleanTo == null) {
            if ("day".equalsIgnoreCase(range) || "today".equalsIgnoreCase(range)) {
                orderTimeFilter = " AND CAST(o.created_at AS DATE) = CAST(GETDATE() AS DATE) ";
            } else if ("week".equalsIgnoreCase(range) || "this_week".equalsIgnoreCase(range)) {
                orderTimeFilter = " AND DATEPART(WEEK, o.created_at) = DATEPART(WEEK, GETDATE()) AND YEAR(o.created_at) = YEAR(GETDATE()) ";
            } else { // month
                orderTimeFilter = " AND YEAR(o.created_at) = YEAR(GETDATE()) AND MONTH(o.created_at) = MONTH(GETDATE()) ";
            }
        }
        
        String orderBranchFilter = (branchId != null) ? " AND o.branch_id = " + branchId + " " : "";
        
        String sql = "SELECT COUNT(*) FROM ("
                   + "  SELECT o.order_id "
                   + "  FROM [order] o WITH (NOLOCK) "
                   + "  WHERE o.status IN ('COMPLETED', 'PAID') AND o.order_type IN ('SALE', 'RECEIPT') " + orderBranchFilter + orderTimeFilter
                   + "  UNION ALL "
                   + "  SELECT o.order_id "
                   + "  FROM [order] o WITH (NOLOCK) "
                   + "  WHERE o.status IN ('COMPLETED', 'PAID') AND o.order_type IN ('PURCHASE', 'IMPORT', 'EXPENSE') " + orderBranchFilter + orderTimeFilter
                   + ") t";
                   
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public List<model.Payment> getBranchPayments(String range, String fromDate, String toDate, Integer branchId, int page, int pageSize) throws SQLException {
        List<model.Payment> list = new ArrayList<>();
        
        String orderTimeFilter = "";
        String cleanFrom = sanitizeDate(fromDate);
        String cleanTo = sanitizeDate(toDate);

        if (cleanFrom != null) {
            orderTimeFilter += " AND CAST(o.created_at AS DATE) >= '" + cleanFrom + "' ";
        }
        if (cleanTo != null) {
            orderTimeFilter += " AND CAST(o.created_at AS DATE) <= '" + cleanTo + "' ";
        }

        if (cleanFrom == null && cleanTo == null) {
            if ("day".equalsIgnoreCase(range) || "today".equalsIgnoreCase(range)) {
                orderTimeFilter = " AND CAST(o.created_at AS DATE) = CAST(GETDATE() AS DATE) ";
            } else if ("week".equalsIgnoreCase(range) || "this_week".equalsIgnoreCase(range)) {
                orderTimeFilter = " AND DATEPART(WEEK, o.created_at) = DATEPART(WEEK, GETDATE()) AND YEAR(o.created_at) = YEAR(GETDATE()) ";
            } else { // month
                orderTimeFilter = " AND YEAR(o.created_at) = YEAR(GETDATE()) AND MONTH(o.created_at) = MONTH(GETDATE()) ";
            }
        }
        
        String orderBranchFilter = (branchId != null) ? " AND o.branch_id = " + branchId + " " : "";
        
        String sql = "SELECT TransactionCode, PaymentDate, PaymentType, PaymentMethod, PaymentAmount, Description FROM ("
                   + "  SELECT o.order_code AS TransactionCode, o.created_at AS PaymentDate, 'INCOME' AS PaymentType, "
                   + "         o.payment_method AS PaymentMethod, o.total_amount AS PaymentAmount, "
                   + "         ISNULL(o.description, N'Bán hàng - Hóa đơn ' + o.order_code) AS Description "
                   + "  FROM [order] o WITH (NOLOCK) "
                   + "  WHERE o.status IN ('COMPLETED', 'PAID') AND o.order_type IN ('SALE', 'RECEIPT') " + orderBranchFilter + orderTimeFilter
                   + "  UNION ALL "
                   + "  SELECT o.order_code AS TransactionCode, o.created_at AS PaymentDate, 'EXPENSE' AS PaymentType, "
                   + "         o.payment_method AS PaymentMethod, o.total_amount AS PaymentAmount, "
                   + "         ISNULL(o.description, N'Chi phí phát sinh - Hóa đơn ' + o.order_code) AS Description "
                   + "  FROM [order] o WITH (NOLOCK) "
                   + "  WHERE o.status IN ('COMPLETED', 'PAID') AND o.order_type IN ('PURCHASE', 'IMPORT', 'EXPENSE') " + orderBranchFilter + orderTimeFilter
                   + ") t "
                   + "ORDER BY PaymentDate DESC "
                   + "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
                   
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, (page - 1) * pageSize);
            ps.setInt(2, pageSize);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.Payment p = new model.Payment();
                    p.setName(rs.getString("TransactionCode"));
                    p.setPaymentDate(rs.getTimestamp("PaymentDate"));
                    p.setPaymentType(rs.getString("PaymentType"));
                    p.setMethod(rs.getString("PaymentMethod"));
                    p.setAmount(rs.getDouble("PaymentAmount"));
                    p.setDescription(rs.getString("Description"));
                    list.add(p);
                }
            }
        }
        return list;
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
