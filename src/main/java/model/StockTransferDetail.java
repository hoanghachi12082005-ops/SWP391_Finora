package model;

public class StockTransferDetail {
    private int stockTransferDetailId;
    private int stockTransferId;
    private int productId;
    private int quantity;

    // Display fields
    private String productName;
    private String productCodebar;
    private String unitName;

    public StockTransferDetail() {}

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

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }
}
