package com.storemanagement.model;

    public class PurchaseDetail extends BaseModel {
    private int purchaseOrderId;
private int productId;
private int quantity;
private double unitPrice;

        public PurchaseDetail() {}

    public int getPurchaseOrderId() { return purchaseOrderId; }
public void setPurchaseOrderId(int purchaseOrderId) { this.purchaseOrderId = purchaseOrderId; }
public int getProductId() { return productId; }
public void setProductId(int productId) { this.productId = productId; }
public int getQuantity() { return quantity; }
public void setQuantity(int quantity) { this.quantity = quantity; }
public double getUnitPrice() { return unitPrice; }
public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
    }
