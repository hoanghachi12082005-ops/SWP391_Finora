package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Product {
    private int productId;
    private String productCodebar;
    private String productName;
    private Integer categoryId;
    private Integer unitId;
    private BigDecimal sellingPrice;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String categoryName;
    private String unitName;

    public Product() { this.sellingPrice = BigDecimal.ZERO; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    public String getProductCodebar() { return productCodebar; }
    public void setProductCodebar(String productCodebar) { this.productCodebar = productCodebar; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
    public Integer getUnitId() { return unitId; }
    public void setUnitId(Integer unitId) { this.unitId = unitId; }
    public BigDecimal getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(BigDecimal sellingPrice) { this.sellingPrice = sellingPrice; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getUnitName() { return unitName; }
    public void setUnitName(String unitName) { this.unitName = unitName; }
}
