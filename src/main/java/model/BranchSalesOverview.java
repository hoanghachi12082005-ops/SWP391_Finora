package model;

import java.math.BigDecimal;

public class BranchSalesOverview {
    private int totalBranches;
    private int totalOrders;
    private BigDecimal totalRevenue;
    private String topBranchName;
    private BigDecimal topBranchRevenue;

    public BranchSalesOverview() {
        this.totalRevenue = BigDecimal.ZERO;
        this.topBranchRevenue = BigDecimal.ZERO;
    }

    public int getTotalBranches() {
        return totalBranches;
    }

    public void setTotalBranches(int totalBranches) {
        this.totalBranches = totalBranches;
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

    public String getTopBranchName() {
        return topBranchName;
    }

    public void setTopBranchName(String topBranchName) {
        this.topBranchName = topBranchName;
    }

    public BigDecimal getTopBranchRevenue() {
        return topBranchRevenue;
    }

    public void setTopBranchRevenue(BigDecimal topBranchRevenue) {
        this.topBranchRevenue = topBranchRevenue;
    }
}
