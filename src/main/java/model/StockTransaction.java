package model;

import java.time.LocalDateTime;

public class StockTransaction {
    private int stockTransactionId;
    private int warehouseId;
    private int productId;
    private String referenceType;
    private Integer referenceId;
    private String transactionType;
    private int quantity;
    private int beforeQuantity;
    private int afterQuantity;
    private String note;
    private int createdBy;
    private LocalDateTime createdAt;

    // Display fields
    private String productName;
    private String productCodebar;
    private String createdByName;
    private String warehouseName;

    public StockTransaction() {}

    public int getStockTransactionId() {
        return stockTransactionId;
    }

    public void setStockTransactionId(int stockTransactionId) {
        this.stockTransactionId = stockTransactionId;
    }

    public int getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(int warehouseId) {
        this.warehouseId = warehouseId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }

    public Integer getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Integer referenceId) {
        this.referenceId = referenceId;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getBeforeQuantity() {
        return beforeQuantity;
    }

    public void setBeforeQuantity(int beforeQuantity) {
        this.beforeQuantity = beforeQuantity;
    }

    public int getAfterQuantity() {
        return afterQuantity;
    }

    public void setAfterQuantity(int afterQuantity) {
        this.afterQuantity = afterQuantity;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductCodebar() {
        return productCodebar;
    }

    public void setProductCodebar(String productCodebar) {
        this.productCodebar = productCodebar;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public String getWarehouseName() {
        return warehouseName;
    }

    public void setWarehouseName(String warehouseName) {
        this.warehouseName = warehouseName;
    }
}
