package model;

    public class Product extends BaseModel {
    private String sku;
private int categoryId;
private double price;
private int quantity;

        public Product() {}

    public String getSku() { return sku; }
public void setSku(String sku) { this.sku = sku; }
public int getCategoryId() { return categoryId; }
public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
public double getPrice() { return price; }
public void setPrice(double price) { this.price = price; }
public int getQuantity() { return quantity; }
public void setQuantity(int quantity) { this.quantity = quantity; }
    }
