package model;

import java.time.LocalDateTime;

public class InventoryItem {
    private int inventoryId;
    private int warehouseId;
    private int productId;
    private int quantityInStock;
    private String status;
    private LocalDateTime updatedAt;

    private String warehouseName;
    private String productName;
    private String productCodebar;

    public InventoryItem() { this.status = "ACTIVE"; }

    public int getInventoryId() { return inventoryId; }
    public void setInventoryId(int inventoryId) { this.inventoryId = inventoryId; }
    public int getWarehouseId() { return warehouseId; }
    public void setWarehouseId(int warehouseId) { this.warehouseId = warehouseId; }
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    public int getQuantityInStock() { return quantityInStock; }
    public void setQuantityInStock(int quantityInStock) { this.quantityInStock = quantityInStock; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getProductCodebar() { return productCodebar; }
    public void setProductCodebar(String productCodebar) { this.productCodebar = productCodebar; }
}
