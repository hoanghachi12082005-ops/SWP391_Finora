package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Invoice {
    private int invoiceId;
    private int orderId;
    private String invoiceCode;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;

    public Invoice() { this.totalAmount = BigDecimal.ZERO; }

    public int getInvoiceId() { return invoiceId; }
    public void setInvoiceId(int invoiceId) { this.invoiceId = invoiceId; }
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public String getInvoiceCode() { return invoiceCode; }
    public void setInvoiceCode(String invoiceCode) { this.invoiceCode = invoiceCode; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
