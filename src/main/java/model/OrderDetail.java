package model;

import java.math.BigDecimal;

public class OrderDetail {
    private int orderDetailId;
    private int orderId;
    private int productId;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;

    private String productName;
    private String productCodebar;

    public OrderDetail() {
        this.unitPrice = BigDecimal.ZERO;
        this.totalPrice = BigDecimal.ZERO;
    }

    public int getOrderDetailId() { return orderDetailId; }
    public void setOrderDetailId(int orderDetailId) { this.orderDetailId = orderDetailId; }
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getProductCodebar() { return productCodebar; }
    public void setProductCodebar(String productCodebar) { this.productCodebar = productCodebar; }
}
