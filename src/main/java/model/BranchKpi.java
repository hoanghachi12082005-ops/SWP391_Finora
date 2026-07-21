package model;

public class BranchKpi {
    private int branchId;
    private String branchName;
    private int orders;
    private double revenue;
    private double revenuePercent;

    public int getBranchId() { return branchId; }
    public void setBranchId(int branchId) { this.branchId = branchId; }
    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }
    public int getOrders() { return orders; }
    public void setOrders(int orders) { this.orders = orders; }
    public double getRevenue() { return revenue; }
    public void setRevenue(double revenue) { this.revenue = revenue; }
    public double getRevenuePercent() { return revenuePercent; }
    public void setRevenuePercent(double revenuePercent) { this.revenuePercent = revenuePercent; }
}
