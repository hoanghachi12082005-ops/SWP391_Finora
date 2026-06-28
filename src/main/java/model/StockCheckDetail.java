package model;

public class StockCheckDetail {
    private int stockCheckDetailId;
    private int stockCheckId;
    private int productId;
    private int systemQuantity;
    private int actualQuantity;
    private int difference;
    private String note;

    // Display fields
    private String productName;
    private String productCodebar;
    private String unitName;

    public StockCheckDetail() {}

    public int getStockCheckDetailId() {
        return stockCheckDetailId;
    }

    public void setStockCheckDetailId(int stockCheckDetailId) {
        this.stockCheckDetailId = stockCheckDetailId;
    }

    public int getStockCheckId() {
        return stockCheckId;
    }

    public void setStockCheckId(int stockCheckId) {
        this.stockCheckId = stockCheckId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getSystemQuantity() {
        return systemQuantity;
    }

    public void setSystemQuantity(int systemQuantity) {
        this.systemQuantity = systemQuantity;
    }

    public int getActualQuantity() {
        return actualQuantity;
    }

    public void setActualQuantity(int actualQuantity) {
        this.actualQuantity = actualQuantity;
    }

    public int getDifference() {
        return difference;
    }

    public void setDifference(int difference) {
        this.difference = difference;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
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
