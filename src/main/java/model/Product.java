package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Entity mapped to DBFinoraV2 Product table with joins. */
public class Product {
    private int productID;
    private String name;
    private int quantity;
    private int categoryID;
    private int unitID;
    private String supplierIDs;
    private BigDecimal sellingPrice;
    private BigDecimal importPrice;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Joint fields
    private String categoryName;
    private String unitName;

    // Transient: URL đến ảnh sản phẩm (không lưu trong DB, đọc từ /asset/product/)
    private String imageUrl;

    public Product() {
    }

    public Product(int productID, String name, int quantity, int categoryID, int unitID, String supplierIDs, BigDecimal sellingPrice, BigDecimal importPrice, String status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.productID = productID;
        this.name = name;
        this.quantity = quantity;
        this.categoryID = categoryID;
        this.unitID = unitID;
        this.supplierIDs = supplierIDs;
        this.sellingPrice = sellingPrice;
        this.importPrice = importPrice;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getProductID() { return productID; }
    public void setProductID(int productID) { this.productID = productID; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public int getCategoryID() { return categoryID; }
    public void setCategoryID(int categoryID) { this.categoryID = categoryID; }

    public int getUnitID() { return unitID; }
    public void setUnitID(int unitID) { this.unitID = unitID; }

    public String getSupplierIDs() { return supplierIDs; }
    public void setSupplierIDs(String supplierIDs) { this.supplierIDs = supplierIDs; }

    public BigDecimal getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(BigDecimal sellingPrice) { this.sellingPrice = sellingPrice; }

    public BigDecimal getImportPrice() { return importPrice; }
    public void setImportPrice(BigDecimal importPrice) { this.importPrice = importPrice; }

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

    @Override
    public String toString() {
        return "Product{" +
                "productID=" + productID +
                ", name='" + name + '\'' +
                ", quantity=" + quantity +
                ", sellingPrice=" + sellingPrice +
                '}';
    }
}
