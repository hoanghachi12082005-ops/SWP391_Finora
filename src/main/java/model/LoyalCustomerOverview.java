package model;

import java.math.BigDecimal;

public class LoyalCustomerOverview {
    private int totalCustomers;
    private BigDecimal totalSpent;
    private String topCustomerName;
    private BigDecimal topCustomerSpent;
    private int totalPoints;

    public LoyalCustomerOverview() {
        this.totalSpent = BigDecimal.ZERO;
        this.topCustomerSpent = BigDecimal.ZERO;
    }

    public int getTotalCustomers() {
        return totalCustomers;
    }

    public void setTotalCustomers(int totalCustomers) {
        this.totalCustomers = totalCustomers;
    }

    public BigDecimal getTotalSpent() {
        return totalSpent;
    }

    public void setTotalSpent(BigDecimal totalSpent) {
        this.totalSpent = totalSpent;
    }

    public String getTopCustomerName() {
        return topCustomerName;
    }

    public void setTopCustomerName(String topCustomerName) {
        this.topCustomerName = topCustomerName;
    }

    public BigDecimal getTopCustomerSpent() {
        return topCustomerSpent;
    }

    public void setTopCustomerSpent(BigDecimal topCustomerSpent) {
        this.topCustomerSpent = topCustomerSpent;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(int totalPoints) {
        this.totalPoints = totalPoints;
    }
}
