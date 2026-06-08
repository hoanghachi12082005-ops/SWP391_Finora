package product.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Entity mapped to DBFinora.sql. */
public class Product {
    private int productID;
    private int categoryID;
    private String name;
    private String sku;
    private BigDecimal price;
    private BigDecimal costPrice;
    private int stockAlertQty;
    private String status;
    private LocalDateTime createdAt;

    public Product() {
    }

    public Product(int productID, int categoryID, String name, String sku, BigDecimal price, BigDecimal costPrice, int stockAlertQty, String status, LocalDateTime createdAt) {
        this.productID = productID;
        this.categoryID = categoryID;
        this.name = name;
        this.sku = sku;
        this.price = price;
        this.costPrice = costPrice;
        this.stockAlertQty = stockAlertQty;
        this.status = status;
        this.createdAt = createdAt;
    }

    public int getProductID() { return productID; }
    public void setProductID(int productID) { this.productID = productID; }

    public int getCategoryID() { return categoryID; }
    public void setCategoryID(int categoryID) { this.categoryID = categoryID; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public BigDecimal getCostPrice() { return costPrice; }
    public void setCostPrice(BigDecimal costPrice) { this.costPrice = costPrice; }

    public int getStockAlertQty() { return stockAlertQty; }
    public void setStockAlertQty(int stockAlertQty) { this.stockAlertQty = stockAlertQty; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Product{" +
                "productID=" + productID +
                ", name='" + name + '\'' +
                ", sku='" + sku + '\'' +
                ", price=" + price +
                '}';
    }
}
