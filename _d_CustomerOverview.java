package model;

import java.math.BigDecimal;

public class CustomerOverview {
    private int totalCustomers;
    private int newCustomersThisMonth;
    private BigDecimal totalSpent;
    private String topCustomerName;
    private int topCustomerPoints;

    public CustomerOverview() {
        totalSpent = BigDecimal.ZERO;
        topCustomerPoints = 0;
    }

    public int getTotalCustomers() {
        return totalCustomers;
    }

    public void setTotalCustomers(int totalCustomers) {
        this.totalCustomers = totalCustomers;
    }

    public int getNewCustomersThisMonth() {
        return newCustomersThisMonth;
    }

    public void setNewCustomersThisMonth(int newCustomersThisMonth) {
        this.newCustomersThisMonth = newCustomersThisMonth;
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

    public int getTopCustomerPoints() {
        return topCustomerPoints;
    }

    public void setTopCustomerPoints(int topCustomerPoints) {
        this.topCustomerPoints = topCustomerPoints;
    }
}
