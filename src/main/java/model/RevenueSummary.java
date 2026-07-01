package model;

public class RevenueSummary {
    private double totalRevenue;
    private double cashRevenue;
    private double bankRevenue;
    private int totalOrders;
    private double aov;

    private double totalRevenueChange;
    private double cashRevenueChange;
    private double bankRevenueChange;
    private double totalOrdersChange;
    private double aovChange;

    public RevenueSummary() {}

    public RevenueSummary(double totalRevenue, double cashRevenue, double bankRevenue, int totalOrders, double aov,
                          double totalRevenueChange, double cashRevenueChange, double bankRevenueChange,
                          double totalOrdersChange, double aovChange) {
        this.totalRevenue = totalRevenue;
        this.cashRevenue = cashRevenue;
        this.bankRevenue = bankRevenue;
        this.totalOrders = totalOrders;
        this.aov = aov;
        this.totalRevenueChange = totalRevenueChange;
        this.cashRevenueChange = cashRevenueChange;
        this.bankRevenueChange = bankRevenueChange;
        this.totalOrdersChange = totalOrdersChange;
        this.aovChange = aovChange;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public double getCashRevenue() {
        return cashRevenue;
    }

    public void setCashRevenue(double cashRevenue) {
        this.cashRevenue = cashRevenue;
    }

    public double getBankRevenue() {
        return bankRevenue;
    }

    public void setBankRevenue(double bankRevenue) {
        this.bankRevenue = bankRevenue;
    }

    public int getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(int totalOrders) {
        this.totalOrders = totalOrders;
    }

    public double getAov() {
        return aov;
    }

    public void setAov(double aov) {
        this.aov = aov;
    }

    public double getTotalRevenueChange() {
        return totalRevenueChange;
    }

    public void setTotalRevenueChange(double totalRevenueChange) {
        this.totalRevenueChange = totalRevenueChange;
    }

    public double getCashRevenueChange() {
        return cashRevenueChange;
    }

    public void setCashRevenueChange(double cashRevenueChange) {
        this.cashRevenueChange = cashRevenueChange;
    }

    public double getBankRevenueChange() {
        return bankRevenueChange;
    }

    public void setBankRevenueChange(double bankRevenueChange) {
        this.bankRevenueChange = bankRevenueChange;
    }

    public double getTotalOrdersChange() {
        return totalOrdersChange;
    }

    public void setTotalOrdersChange(double totalOrdersChange) {
        this.totalOrdersChange = totalOrdersChange;
    }

    public double getAovChange() {
        return aovChange;
    }

    public void setAovChange(double aovChange) {
        this.aovChange = aovChange;
    }
}
