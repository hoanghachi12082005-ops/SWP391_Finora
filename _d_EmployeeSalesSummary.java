package model;

import java.math.BigDecimal;

public class EmployeeSalesSummary {
    private int employeeId;
    private String fullName;
    private String branchName;
    private String roleName;
    private int totalOrders;
    private BigDecimal totalRevenue;
    private BigDecimal averageOrderValue;
    private int completedOrders;
    private int cancelledOrders;
    private int pointsEarned;

    public EmployeeSalesSummary() {
        this.totalRevenue = BigDecimal.ZERO;
        this.averageOrderValue = BigDecimal.ZERO;
    }

    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public int getTotalOrders() { return totalOrders; }
    public void setTotalOrders(int totalOrders) { this.totalOrders = totalOrders; }
    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }
    public BigDecimal getAverageOrderValue() { return averageOrderValue; }
    public void setAverageOrderValue(BigDecimal averageOrderValue) { this.averageOrderValue = averageOrderValue; }
    public int getCompletedOrders() { return completedOrders; }
    public void setCompletedOrders(int completedOrders) { this.completedOrders = completedOrders; }
    public int getCancelledOrders() { return cancelledOrders; }
    public void setCancelledOrders(int cancelledOrders) { this.cancelledOrders = cancelledOrders; }
    public int getPointsEarned() { return pointsEarned; }
    public void setPointsEarned(int pointsEarned) { this.pointsEarned = pointsEarned; }
}
