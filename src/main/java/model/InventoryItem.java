package com.storemanagement.model;

    public class InventoryItem extends BaseModel {
    private int productId;
private int storeId;
private int quantity;

        public InventoryItem() {}

    public int getProductId() { return productId; }
public void setProductId(int productId) { this.productId = productId; }
public int getStoreId() { return storeId; }
public void setStoreId(int storeId) { this.storeId = storeId; }
public int getQuantity() { return quantity; }
public void setQuantity(int quantity) { this.quantity = quantity; }
    }
