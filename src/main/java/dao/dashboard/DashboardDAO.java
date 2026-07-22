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
        String sql = "SELECT "
                + "(SELECT ISNULL(SUM(total_amount),0) FROM [order] WHERE status='COMPLETED' AND order_type='SALE' AND CAST(created_at AS DATE)=CAST(GETDATE() AS DATE)) AS revenue_today, "
                + "(SELECT ISNULL(SUM(total_amount),0) FROM [order] WHERE status='COMPLETED' AND order_type='SALE' AND CAST(created_at AS DATE)=CAST(DATEADD(DAY,-1,GETDATE()) AS DATE)) AS revenue_yesterday, "
                + "(SELECT ISNULL(SUM(total_amount),0) FROM [order] WHERE status='COMPLETED' AND order_type='SALE' AND YEAR(created_at)=YEAR(GETDATE()) AND MONTH(created_at)=MONTH(GETDATE())) AS revenue_this_month, "
                + "(SELECT ISNULL(SUM(total_amount),0) FROM [order] WHERE status='COMPLETED' AND order_type='SALE' AND YEAR(created_at)=YEAR(DATEADD(MONTH,-1,GETDATE())) AND MONTH(created_at)=MONTH(DATEADD(MONTH,-1,GETDATE()))) AS revenue_last_month, "
                + "(SELECT ISNULL(SUM(total_amount),0) FROM [order] WHERE status='COMPLETED' AND order_type='SALE' AND YEAR(created_at)=YEAR(GETDATE())) AS revenue_this_year, "
                + "(SELECT COUNT(*) FROM [order] WHERE order_type='SALE' AND status='COMPLETED' AND CAST(created_at AS DATE)=CAST(GETDATE() AS DATE)) AS orders_today, "
                + "(SELECT COUNT(*) FROM [order] WHERE order_type='SALE' AND status='COMPLETED' AND YEAR(created_at)=YEAR(GETDATE()) AND MONTH(created_at)=MONTH(GETDATE())) AS orders_this_month, "
                + "(SELECT COUNT(*) FROM [order] WHERE order_type='SALE') AS total_orders, "
                + "(SELECT COUNT(*) FROM [order] WHERE order_type='SALE' AND status='PENDING') AS pending_orders, "
                + "(SELECT ISNULL(AVG(total_amount),0) FROM [order] WHERE order_type='SALE' AND status='COMPLETED') AS avg_order_value, "
                + "(SELECT COUNT(*) FROM customer) AS total_customers, "
                + "(SELECT COUNT(*) FROM customer WHERE YEAR(created_at)=YEAR(GETDATE()) AND MONTH(created_at)=MONTH(GETDATE())) AS new_customers, "
                + "(SELECT COUNT(*) FROM customer WHERE status='ACTIVE') AS active_customers, "
                + "(SELECT COUNT(*) FROM product) AS total_products, "
                + "(SELECT COUNT(*) FROM inventory WHERE quantity_in_stock=0 OR status='OUT_OF_STOCK') AS out_of_stock, "
                + "(SELECT COUNT(*) FROM inventory WHERE quantity_in_stock>0 AND quantity_in_stock<=10) AS low_stock, "
                + "(SELECT ISNULL(SUM(i.quantity_in_stock*p.selling_price),0) FROM inventory i JOIN product p ON i.product_id=p.product_id) AS total_stock_value, "
                + "(SELECT COUNT(*) FROM Branch WHERE status='ACTIVE') AS total_stores, "
                + "(SELECT COUNT(*) FROM Employee WHERE status='ACTIVE') AS total_employees";
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

    // ──────────────────────── FINANCIAL DASHBOARD ────────────────────────
    public static class FinancialData {
        public BigDecimal totalRevenue = BigDecimal.ZERO;
        public BigDecimal totalExpenses = BigDecimal.ZERO;
        public BigDecimal netProfit = BigDecimal.ZERO;
        public int totalInvoices;
        public List<model.DashboardOverview.BranchRevenue> branchRevenues = new ArrayList<>();
    }

    public FinancialData getFinancialData(String range) throws SQLException {
        return getFinancialData(range, null);
    }

    public FinancialData getFinancialData(String range, Integer branchId) throws SQLException {
        FinancialData fd = new FinancialData();
        
        String orderTimeFilter = "";
        String paymentTimeFilter = "";
        
        if ("day".equalsIgnoreCase(range) || "today".equalsIgnoreCase(range)) {
            orderTimeFilter = " AND CAST(o.created_at AS DATE) = CAST(GETDATE() AS DATE) ";
            paymentTimeFilter = " AND CAST(p.payment_date AS DATE) = CAST(GETDATE() AS DATE) ";
        } else if ("week".equalsIgnoreCase(range) || "this_week".equalsIgnoreCase(range)) {
            orderTimeFilter = " AND DATEPART(WEEK, o.created_at) = DATEPART(WEEK, GETDATE()) AND YEAR(o.created_at) = YEAR(GETDATE()) ";
            paymentTimeFilter = " AND DATEPART(WEEK, p.payment_date) = DATEPART(WEEK, GETDATE()) AND YEAR(p.payment_date) = YEAR(GETDATE()) ";
        } else { // default is month
            orderTimeFilter = " AND YEAR(o.created_at) = YEAR(GETDATE()) AND MONTH(o.created_at) = MONTH(GETDATE()) ";
            paymentTimeFilter = " AND YEAR(p.payment_date) = YEAR(GETDATE()) AND MONTH(p.payment_date) = MONTH(GETDATE()) ";
        }

        if (branchId != null) {
            orderTimeFilter += " AND o.branch_id = " + branchId + " ";
            paymentTimeFilter += " AND p.BranchID = " + branchId + " ";
        }

        // 1. Total Revenue
        String revSql = "SELECT ISNULL(SUM(o.total_amount), 0) FROM [order] o WHERE o.status = 'COMPLETED' AND o.order_type = 'SALE' " + orderTimeFilter;
        fd.totalRevenue = queryBigDecimal(revSql);

        // 2. Total Expenses
        String expSql = "SELECT ISNULL(SUM(p.payment_amount), 0) FROM payment p WHERE p.payment_status = 'PAID' AND p.PaymentType = 'EXPENSE' " + paymentTimeFilter;
        fd.totalExpenses = queryBigDecimal(expSql);

        // 3. Net Profit
        fd.netProfit = fd.totalRevenue.subtract(fd.totalExpenses);

        // 4. Total Invoices
        String invSql = "SELECT COUNT(*) FROM [order] o WHERE o.status = 'COMPLETED' AND o.order_type = 'SALE' " + orderTimeFilter;
        fd.totalInvoices = queryInt(invSql);

        // 5. Branch Revenues / Performance
        String branchSql = "SELECT b.branch_id, b.branch_name, b.branch_code, b.status, "
                         + "       ISNULL(SUM(o.total_amount), 0) AS revenue, COUNT(o.order_id) AS order_count "
                         + "FROM Branch b "
                         + "LEFT JOIN [order] o ON o.branch_id = b.branch_id "
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
        List<model.Payment> list = new ArrayList<>();
        
        String orderTimeFilter = "";
        String paymentTimeFilter = "";
        
        if ("day".equalsIgnoreCase(range) || "today".equalsIgnoreCase(range)) {
            orderTimeFilter = " AND CAST(o.created_at AS DATE) = CAST(GETDATE() AS DATE) ";
            paymentTimeFilter = " AND CAST(p.payment_date AS DATE) = CAST(GETDATE() AS DATE) ";
        } else if ("week".equalsIgnoreCase(range) || "this_week".equalsIgnoreCase(range)) {
            orderTimeFilter = " AND DATEPART(WEEK, o.created_at) = DATEPART(WEEK, GETDATE()) AND YEAR(o.created_at) = YEAR(GETDATE()) ";
            paymentTimeFilter = " AND DATEPART(WEEK, p.payment_date) = DATEPART(WEEK, GETDATE()) AND YEAR(p.payment_date) = YEAR(GETDATE()) ";
        } else { // month
            orderTimeFilter = " AND YEAR(o.created_at) = YEAR(GETDATE()) AND MONTH(o.created_at) = MONTH(GETDATE()) ";
            paymentTimeFilter = " AND YEAR(p.payment_date) = YEAR(GETDATE()) AND MONTH(p.payment_date) = MONTH(GETDATE()) ";
        }
        
        String orderBranchFilter = (branchId != null) ? " AND o.branch_id = " + branchId + " " : "";
        String paymentBranchFilter = (branchId != null) ? " AND p.BranchID = " + branchId + " " : "";
        
        String sql = "SELECT TransactionCode, PaymentDate, PaymentType, PaymentMethod, PaymentAmount, Description FROM ("
                   + "  SELECT o.order_code AS TransactionCode, o.created_at AS PaymentDate, 'INCOME' AS PaymentType, "
                   + "         o.payment_method AS PaymentMethod, o.total_amount AS PaymentAmount, "
                   + "         N'Bán hàng - Hóa đơn ' + o.order_code AS Description "
                   + "  FROM [order] o "
                   + "  WHERE o.status = 'COMPLETED' AND o.order_type = 'SALE' " + orderBranchFilter + orderTimeFilter
                   + "  UNION ALL "
                   + "  SELECT p.transaction_code AS TransactionCode, p.payment_date AS PaymentDate, p.PaymentType AS PaymentType, "
                   + "         p.payment_method AS PaymentMethod, p.payment_amount AS PaymentAmount, p.Description AS Description "
                   + "  FROM payment p "
                   + "  WHERE p.PaymentType = 'EXPENSE' " + paymentBranchFilter + paymentTimeFilter
                   + ") t "
                   + "ORDER BY PaymentDate DESC";
                   
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
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
