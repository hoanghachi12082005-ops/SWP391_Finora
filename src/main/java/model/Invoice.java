package model;

    public class Invoice extends BaseModel {
    private int orderId;
private String invoiceCode;
private double totalAmount;

        public Invoice() {}

    public int getOrderId() { return orderId; }
public void setOrderId(int orderId) { this.orderId = orderId; }
public String getInvoiceCode() { return invoiceCode; }
public void setInvoiceCode(String invoiceCode) { this.invoiceCode = invoiceCode; }
public double getTotalAmount() { return totalAmount; }
public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    }
