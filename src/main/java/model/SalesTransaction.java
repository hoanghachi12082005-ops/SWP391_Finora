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
