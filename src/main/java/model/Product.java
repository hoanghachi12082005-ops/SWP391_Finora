package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Entity mapped to DBFinoraV2 Product table with joins. */
public class Product {
    private int productID;
    private String name;
    private int categoryID;
    private int unitID;
    private BigDecimal sellingPrice;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Joint fields
    private String categoryName;
    private String unitName;

    // Transient: URL đến ảnh sản phẩm (không lưu trong DB, đọc từ /asset/product/)
    private String imageUrl;

    // Transient compatibility: quantity in stock for POS
    private int quantity;

    public Product() {
    }

    public Product(int productID, String name, int categoryID, int unitID, BigDecimal sellingPrice, String status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.productID = productID;
        this.name = name;
        this.categoryID = categoryID;
        this.unitID = unitID;
        this.sellingPrice = sellingPrice;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getProductID() { return productID; }
    public void setProductID(int productID) { this.productID = productID; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getCategoryID() { return categoryID; }
    public void setCategoryID(int categoryID) { this.categoryID = categoryID; }

    public int getUnitID() { return unitID; }
    public void setUnitID(int unitID) { this.unitID = unitID; }

    public BigDecimal getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(BigDecimal sellingPrice) { this.sellingPrice = sellingPrice; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getUnitName() { return unitName; }
    public void setUnitName(String unitName) { this.unitName = unitName; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    // =========================================================
    // Backward Compatibility Aliases for POS & Sales Modules
    // =========================================================

    public int getProductId() { return productID; }
    public void setProductId(int productId) { this.productID = productId; }

    public String getProductName() { return name; }
    public void setProductName(String productName) { this.name = productName; }

    private String productCodebar;
    public String getProductCode() { return productCodebar; }
    public void setProductCode(String productCode) { this.productCodebar = productCode; }
    public String getProductCodebar() { return productCodebar; }
    public void setProductCodebar(String productCodebar) { this.productCodebar = productCodebar; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public int getQuantityInStock() { return quantity; }
    public void setQuantityInStock(int quantityInStock) { this.quantity = quantityInStock; }

    public int getCategoryId() { return categoryID; }
    public void setCategoryId(int categoryId) { this.categoryID = categoryId; }

    public int getUnitId() { return unitID; }
    public void setUnitId(int unitId) { this.unitID = unitId; }

    @Override
    public String toString() {
        return "Product{" +
                "productID=" + productID +
                ", name='" + name + '\'' +
                ", sellingPrice=" + sellingPrice +
                '}';
    }
}
