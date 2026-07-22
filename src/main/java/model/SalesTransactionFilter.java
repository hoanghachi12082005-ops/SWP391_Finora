package model;

import java.time.LocalDate;

public class SalesTransactionFilter {
    private String datePreset;
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private String transactionCode;
    private String transactionType;
    private String orderType;
    private String paymentMethod;
    private Double amountFrom;
    private Double amountTo;
    private Integer branchId;
    private Integer empId;
    private String keyword;
    private String sortBy;
    private String sortDir;

    public SalesTransactionFilter() {}

    public String getOrderType() { return orderType; }
    public void setOrderType(String orderType) { this.orderType = orderType; }

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
