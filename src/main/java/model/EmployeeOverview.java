package model;

import java.math.BigDecimal;

public class EmployeeOverview {
    private int totalEmployees;
    private int activeEmployees;
    private int totalOrders;
    private BigDecimal totalRevenue;
    private String topEmployeeName;
    private BigDecimal topEmployeeRevenue;
    private String lowestEmployeeName;
    private BigDecimal lowestEmployeeRevenue;
    private BigDecimal avgRevenuePerEmployee;

    public EmployeeOverview() {
        totalRevenue = BigDecimal.ZERO;
        topEmployeeRevenue = BigDecimal.ZERO;
        lowestEmployeeRevenue = BigDecimal.ZERO;
        avgRevenuePerEmployee = BigDecimal.ZERO;
    }

    public int getTotalEmployees() {
        return totalEmployees;
    }

    public void setTotalEmployees(int totalEmployees) {
        this.totalEmployees = totalEmployees;
    }

    public int getActiveEmployees() {
        return activeEmployees;
    }

    public void setActiveEmployees(int activeEmployees) {
        this.activeEmployees = activeEmployees;
    }

    public int getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(int totalOrders) {
        this.totalOrders = totalOrders;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public String getTopEmployeeName() {
        return topEmployeeName;
    }

    public void setTopEmployeeName(String topEmployeeName) {
        this.topEmployeeName = topEmployeeName;
    }

    public BigDecimal getTopEmployeeRevenue() {
        return topEmployeeRevenue;
    }

    public void setTopEmployeeRevenue(BigDecimal topEmployeeRevenue) {
        this.topEmployeeRevenue = topEmployeeRevenue;
    }

    public String getLowestEmployeeName() { return lowestEmployeeName; }
    public void setLowestEmployeeName(String lowestEmployeeName) { this.lowestEmployeeName = lowestEmployeeName; }

    public BigDecimal getLowestEmployeeRevenue() { return lowestEmployeeRevenue; }
    public void setLowestEmployeeRevenue(BigDecimal lowestEmployeeRevenue) { this.lowestEmployeeRevenue = lowestEmployeeRevenue; }

    public BigDecimal getAvgRevenuePerEmployee() { return avgRevenuePerEmployee; }
    public void setAvgRevenuePerEmployee(BigDecimal avgRevenuePerEmployee) { this.avgRevenuePerEmployee = avgRevenuePerEmployee; }
}