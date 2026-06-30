package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Payment {
    private int paymentId;
    private int orderId;
    private BigDecimal paymentAmount;
    private LocalDateTime paymentDate;
    private String paymentStatus;
    private String transactionCode;

    public Payment() {
        this.paymentAmount = BigDecimal.ZERO;
        this.paymentStatus = "PENDING";
    }

    public int getPaymentId() { return paymentId; }
    public void setPaymentId(int paymentId) { this.paymentId = paymentId; }
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public BigDecimal getPaymentAmount() { return paymentAmount; }
    public void setPaymentAmount(BigDecimal paymentAmount) { this.paymentAmount = paymentAmount; }
    public LocalDateTime getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public String getTransactionCode() { return transactionCode; }
    public void setTransactionCode(String transactionCode) { this.transactionCode = transactionCode; }
}
