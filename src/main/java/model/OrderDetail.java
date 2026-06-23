package model;

    public class OrderDetail{
    private int orderId;
private int productId;
private int quantity;
private double unitPrice;

        public OrderDetail() {}

    public int getOrderId() { return orderId; }
public void setOrderId(int orderId) { this.orderId = orderId; }
public int getProductId() { return productId; }
public void setProductId(int productId) { this.productId = productId; }
public int getQuantity() { return quantity; }
public void setQuantity(int quantity) { this.quantity = quantity; }
public double getUnitPrice() { return unitPrice; }
public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
    }
