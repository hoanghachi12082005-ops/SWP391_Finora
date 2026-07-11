package dto.inventory;

public class StockCheckProductDTO {
    private int productId;
    private String productName;
    private int systemStock;
    private String categoryName;
    private String productCodebar;

    public StockCheckProductDTO() {}

    public StockCheckProductDTO(int productId, String productName, int systemStock, String categoryName, String productCodebar) {
        this.productId = productId;
        this.productName = productName;
        this.systemStock = systemStock;
        this.categoryName = categoryName;
        this.productCodebar = productCodebar;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getSystemStock() {
        return systemStock;
    }

    public void setSystemStock(int systemStock) {
        this.systemStock = systemStock;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getProductCodebar() {
        return productCodebar;
    }

    public void setProductCodebar(String productCodebar) {
        this.productCodebar = productCodebar;
    }
}
