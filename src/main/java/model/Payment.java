package model;

import java.sql.Timestamp;

public class Payment {
    private int id;
    private String name;
    private String status;
    private Integer orderId;
    private double amount;
    private String method; // CASH, BANKING, CARD, etc.
    private String paymentType; // INCOME, EXPENSE
    private String description;
    private Integer employeeId;
    private String creatorName; // Display name
    private Integer branchId;
    private String branchName; // Display name
    private Timestamp paymentDate;

    public Payment() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public Integer getBranchId() {
        return branchId;
    }

    public void setBranchId(Integer branchId) {
        this.branchId = branchId;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public Timestamp getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(Timestamp paymentDate) {
        this.paymentDate = paymentDate;
    }

    // =========================================================
    // Backward Compatibility Getters & Setters for Sales
    // =========================================================

    public double getPaymentAmount() {
        return getAmount();
    }

    public void setPaymentAmount(double amount) {
        setAmount(amount);
    }

    public String getPaymentStatus() {
        return getStatus();
    }

    public void setPaymentStatus(String status) {
        setStatus(status);
    }

    public String getTransactionCode() {
        return getName();
    }

    public void setTransactionCode(String transactionCode) {
        setName(transactionCode);
    }
}
