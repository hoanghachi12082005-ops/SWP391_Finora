package model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Tổng hợp các chỉ số dùng cho card thống kê trên Dashboard Owner Overview.
 * Bao gồm: doanh thu, số đơn, khách hàng, tồn kho và các dữ liệu phụ trợ.
 */
public class DashboardOverview {

    // ===== Revenue =====
    private BigDecimal revenueToday = BigDecimal.ZERO;
    private BigDecimal revenueYesterday = BigDecimal.ZERO;
    private BigDecimal revenueThisMonth = BigDecimal.ZERO;
    private BigDecimal revenueLastMonth = BigDecimal.ZERO;
    private BigDecimal revenueThisYear = BigDecimal.ZERO;

    // ===== Orders =====
    private int ordersToday;
    private int ordersThisMonth;
    private int totalOrders;
    private int pendingOrders;
    private BigDecimal averageOrderValue = BigDecimal.ZERO;

    // ===== Customers =====
    private int totalCustomers;
    private int newCustomersThisMonth;
    private int activeCustomers;

    // ===== Inventory =====
    private int totalProducts;
    private int outOfStockItems;
    private int lowStockItems;
    private BigDecimal totalStockValue = BigDecimal.ZERO;

    // ===== Stores / Employees (bổ sung cho card chuỗi cửa hàng) =====
    private int totalStores;
    private int totalEmployees;
    private String topStoreName;
    private BigDecimal topStoreRevenue = BigDecimal.ZERO;

    // ===== Charts =====
    private List<BranchRevenue> branchRevenues = new ArrayList<>();
    private List<TopProduct> topProducts = new ArrayList<>();

    // ===== Getters / Setters =====
    public BigDecimal getRevenueToday() { return revenueToday; }
    public void setRevenueToday(BigDecimal v) { this.revenueToday = v == null ? BigDecimal.ZERO : v; }

    public BigDecimal getRevenueYesterday() { return revenueYesterday; }
    public void setRevenueYesterday(BigDecimal v) { this.revenueYesterday = v == null ? BigDecimal.ZERO : v; }

    public BigDecimal getRevenueThisMonth() { return revenueThisMonth; }
    public void setRevenueThisMonth(BigDecimal v) { this.revenueThisMonth = v == null ? BigDecimal.ZERO : v; }

    public BigDecimal getRevenueLastMonth() { return revenueLastMonth; }
    public void setRevenueLastMonth(BigDecimal v) { this.revenueLastMonth = v == null ? BigDecimal.ZERO : v; }

    public BigDecimal getRevenueThisYear() { return revenueThisYear; }
    public void setRevenueThisYear(BigDecimal v) { this.revenueThisYear = v == null ? BigDecimal.ZERO : v; }

    public int getOrdersToday() { return ordersToday; }
    public void setOrdersToday(int v) { this.ordersToday = v; }

    public int getOrdersThisMonth() { return ordersThisMonth; }
    public void setOrdersThisMonth(int v) { this.ordersThisMonth = v; }

    public int getTotalOrders() { return totalOrders; }
    public void setTotalOrders(int v) { this.totalOrders = v; }

    public int getPendingOrders() { return pendingOrders; }
    public void setPendingOrders(int v) { this.pendingOrders = v; }

    public BigDecimal getAverageOrderValue() { return averageOrderValue; }
    public void setAverageOrderValue(BigDecimal v) { this.averageOrderValue = v == null ? BigDecimal.ZERO : v; }

    public int getTotalCustomers() { return totalCustomers; }
    public void setTotalCustomers(int v) { this.totalCustomers = v; }

    public int getNewCustomersThisMonth() { return newCustomersThisMonth; }
    public void setNewCustomersThisMonth(int v) { this.newCustomersThisMonth = v; }

    public int getActiveCustomers() { return activeCustomers; }
    public void setActiveCustomers(int v) { this.activeCustomers = v; }

    public int getTotalProducts() { return totalProducts; }
    public void setTotalProducts(int v) { this.totalProducts = v; }

    public int getOutOfStockItems() { return outOfStockItems; }
    public void setOutOfStockItems(int v) { this.outOfStockItems = v; }

    public int getLowStockItems() { return lowStockItems; }
    public void setLowStockItems(int v) { this.lowStockItems = v; }

    public BigDecimal getTotalStockValue() { return totalStockValue; }
    public void setTotalStockValue(BigDecimal v) { this.totalStockValue = v == null ? BigDecimal.ZERO : v; }

    public int getTotalStores() { return totalStores; }
    public void setTotalStores(int v) { this.totalStores = v; }

    public int getTotalEmployees() { return totalEmployees; }
    public void setTotalEmployees(int v) { this.totalEmployees = v; }

    public String getTopStoreName() { return topStoreName; }
    public void setTopStoreName(String v) { this.topStoreName = v; }

    public BigDecimal getTopStoreRevenue() { return topStoreRevenue; }
    public void setTopStoreRevenue(BigDecimal v) { this.topStoreRevenue = v == null ? BigDecimal.ZERO : v; }

    public List<BranchRevenue> getBranchRevenues() { return branchRevenues; }
    public void setBranchRevenues(List<BranchRevenue> v) { this.branchRevenues = v == null ? new ArrayList<>() : v; }

    public List<TopProduct> getTopProducts() { return topProducts; }
    public void setTopProducts(List<TopProduct> v) { this.topProducts = v == null ? new ArrayList<>() : v; }

    /** % thay đổi doanh thu hôm nay so với hôm qua. Trả null khi không tính được. */
    public Double getRevenueChangeVsYesterday() {
        if (revenueYesterday == null || revenueYesterday.signum() == 0) return null;
        return revenueToday.subtract(revenueYesterday)
                .multiply(new BigDecimal("100"))
                .divide(revenueYesterday, 2, java.math.RoundingMode.HALF_UP)
                .doubleValue();
    }

    /** % thay đổi doanh thu tháng này so với tháng trước. */
    public Double getRevenueChangeVsLastMonth() {
        if (revenueLastMonth == null || revenueLastMonth.signum() == 0) return null;
        return revenueThisMonth.subtract(revenueLastMonth)
                .multiply(new BigDecimal("100"))
                .divide(revenueLastMonth, 2, java.math.RoundingMode.HALF_UP)
                .doubleValue();
    }

    // ===== Nested DTOs =====
    public static class BranchRevenue {
        private int branchId;
        private String branchName;
        private String branchCode;
        private BigDecimal revenue = BigDecimal.ZERO;
        private int orderCount;
        private String status;

        public int getBranchId() { return branchId; }
        public void setBranchId(int branchId) { this.branchId = branchId; }
        public String getBranchName() { return branchName; }
        public void setBranchName(String branchName) { this.branchName = branchName; }
        public String getBranchCode() { return branchCode; }
        public void setBranchCode(String branchCode) { this.branchCode = branchCode; }
        public BigDecimal getRevenue() { return revenue; }
        public void setRevenue(BigDecimal revenue) { this.revenue = revenue == null ? BigDecimal.ZERO : revenue; }
        public int getOrderCount() { return orderCount; }
        public void setOrderCount(int orderCount) { this.orderCount = orderCount; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class TopProduct {
        private int productId;
        private String productName;
        private int quantitySold;
        private BigDecimal revenue = BigDecimal.ZERO;

        public int getProductId() { return productId; }
        public void setProductId(int productId) { this.productId = productId; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public int getQuantitySold() { return quantitySold; }
        public void setQuantitySold(int quantitySold) { this.quantitySold = quantitySold; }
        public BigDecimal getRevenue() { return revenue; }
        public void setRevenue(BigDecimal revenue) { this.revenue = revenue == null ? BigDecimal.ZERO : revenue; }
    }
}
