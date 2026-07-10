package model;

public class StockTransferDetail {
    private int stockTransferDetailId;
    private int stockTransferId;
    private int productId;
    private int quantity;

    // View-only fields
    private String productCodebar;
    private String productName;
    private String unitName;
    private String categoryName;
    private Integer actualQuantity; // In case they need a check quantity later, but in the schema it doesn't exist. Let's stick to DB.

    public StockTransferDetail() {
    }

    public int getStockTransferDetailId() {
        return stockTransferDetailId;
    }

    public void setStockTransferDetailId(int stockTransferDetailId) {
        this.stockTransferDetailId = stockTransferDetailId;
    }

    public int getStockTransferId() {
        return stockTransferId;
    }

    public void setStockTransferId(int stockTransferId) {
        this.stockTransferId = stockTransferId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getProductCodebar() {
        return productCodebar;
    }

    public void setProductCodebar(String productCodebar) {
        this.productCodebar = productCodebar;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}
