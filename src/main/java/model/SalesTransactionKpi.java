package model;

public class SalesTransactionKpi {
    private int totalTransactions;
    private double totalRevenue;
    private double totalExpense;
    private double netCashFlow;
    private double avgTransactionValue;
    private int totalSalesOrders;

    public int getTotalTransactions() { return totalTransactions; }
    public void setTotalTransactions(int v) { this.totalTransactions = v; }
    public double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(double v) { this.totalRevenue = v; }
    public double getTotalExpense() { return totalExpense; }
    public void setTotalExpense(double v) { this.totalExpense = v; }
    public double getNetCashFlow() { return netCashFlow; }
    public void setNetCashFlow(double v) { this.netCashFlow = v; }
    public double getAvgTransactionValue() { return avgTransactionValue; }
    public void setAvgTransactionValue(double v) { this.avgTransactionValue = v; }
    public int getTotalSalesOrders() { return totalSalesOrders; }
    public void setTotalSalesOrders(int v) { this.totalSalesOrders = v; }
}
