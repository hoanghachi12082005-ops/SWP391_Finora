# Task 1: Create Model Classes

**Files to create:**
- `src/main/java/model/SalesTransactionFilter.java`
- `src/main/java/model/SalesTransactionKpi.java`
- `src/main/java/model/SalesTransaction.java`

**Package:** `model`

These are plain POJOs used by the DAO, Controller, Service, JSP, and export utilities. No annotations, no inheritance.

## SalesTransactionFilter.java

```java
package model;

import java.time.LocalDate;

public class SalesTransactionFilter {
    private String datePreset;
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private String transactionCode;
    private String transactionType;
    private String paymentMethod;
    private Double amountFrom;
    private Double amountTo;
    private Integer branchId;
    private Integer empId;
    private String keyword;
    private String sortBy;
    private String sortDir;

    public SalesTransactionFilter() {}

    // Getters and setters for all fields
    public String getDatePreset() { return datePreset; }
    public void setDatePreset(String datePreset) { this.datePreset = datePreset; }
    public LocalDate getDateFrom() { return dateFrom; }
    public void setDateFrom(LocalDate dateFrom) { this.dateFrom = dateFrom; }
    public LocalDate getDateTo() { return dateTo; }
    public void setDateTo(LocalDate dateTo) { this.dateTo = dateTo; }
    public String getTransactionCode() { return transactionCode; }
    public void setTransactionCode(String transactionCode) { this.transactionCode = transactionCode; }
    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public Double getAmountFrom() { return amountFrom; }
    public void setAmountFrom(Double amountFrom) { this.amountFrom = amountFrom; }
    public Double getAmountTo() { return amountTo; }
    public void setAmountTo(Double amountTo) { this.amountTo = amountTo; }
    public Integer getBranchId() { return branchId; }
    public void setBranchId(Integer branchId) { this.branchId = branchId; }
    public Integer getEmpId() { return empId; }
    public void setEmpId(Integer empId) { this.empId = empId; }
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public String getSortBy() { return sortBy; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy; }
    public String getSortDir() { return sortDir; }
    public void setSortDir(String sortDir) { this.sortDir = sortDir; }
}
```

## SalesTransactionKpi.java

```java
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
```

## SalesTransaction.java

```java
package model;

import java.sql.Timestamp;

public class SalesTransaction {
    private int id;
    private String transactionCode;
    private Timestamp paymentDate;
    private String transactionType;
    private String paymentMethod;
    private double amount;
    private String description;
    private String branchName;
    private String employeeName;
    private String status;
    private Integer orderId;

    public int getId() { return id; }
    public void setId(int v) { this.id = v; }
    public String getTransactionCode() { return transactionCode; }
    public void setTransactionCode(String v) { this.transactionCode = v; }
    public Timestamp getPaymentDate() { return paymentDate; }
    public void setPaymentDate(Timestamp v) { this.paymentDate = v; }
    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String v) { this.transactionType = v; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String v) { this.paymentMethod = v; }
    public double getAmount() { return amount; }
    public void setAmount(double v) { this.amount = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }
    public String getBranchName() { return branchName; }
    public void setBranchName(String v) { this.branchName = v; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String v) { this.employeeName = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer v) { this.orderId = v; }
}
```
