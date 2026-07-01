package model;

/**
 * Model đại diện cho Sản phẩm (Product) trong hệ thống Finora.
 * Khớp hoàn toàn với sơ đồ ERD.
 *
 * @author Finora Team
 */
public class Product {

    public enum ProductStatus {
        ACTIVE("Đang kinh doanh"),
        OUT_OF_STOCK("Hết hàng"),
        DISCONTINUED("Ngừng kinh doanh");

        private final String displayName;
        ProductStatus(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }

    private int productId;
    private String productName;
    private String productCode;
    private int categoryId;       // FK -> Category.categoryId
    private int unitId;           // FK -> Unit.unitId
    private double sellingPrice;
    private ProductStatus status;
    private String createdAt;     // yyyy-MM-dd HH:mm:ss
    private String updateAt;      // yyyy-MM-dd HH:mm:ss

    // Transient fields (not persisted, populated by JOIN queries)
    private int quantityInStock;  // Tồn kho tại warehouse — dùng cho POS

    // ── Constructors ─────────────────────────────────────────

    public Product() {}

    public Product(int productId, String productName, String productCode, int categoryId,
                   int unitId, double sellingPrice, ProductStatus status, String createdAt, String updateAt) {
        this.productId = productId;
        this.productName = productName;
        this.productCode = productCode;
        this.categoryId = categoryId;
        this.unitId = unitId;
        this.sellingPrice = sellingPrice;
        this.status = status;
        this.createdAt = createdAt;
        this.updateAt = updateAt;
    }

    // ── Getters & Setters ─────────────────────────────────────

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

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public int getUnitId() {
        return unitId;
    }

    public void setUnitId(int unitId) {
        this.unitId = unitId;
    }

    public double getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(double sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public void setStatus(ProductStatus status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(String updateAt) {
        this.updateAt = updateAt;
    }

    // ── Transient Getters & Setters ─────────────────────────

    public int getQuantityInStock() { return quantityInStock; }
    public void setQuantityInStock(int quantityInStock) { this.quantityInStock = quantityInStock; }

    // ── Backward Compatibility Aliases ────────────────────────

    public int getProductID() { return getProductId(); }
    public void setProductID(int productID) { setProductId(productID); }

    public int getCategoryID() { return getCategoryId(); }
    public void setCategoryID(int categoryID) { setCategoryId(categoryID); }

    public double getPrice() { return getSellingPrice(); }
    public void setPrice(double price) { setSellingPrice(price); }

    /** Alias: productCodebar == productCode */
    public String getProductCodebar() { return getProductCode(); }
    public void setProductCodebar(String v) { setProductCode(v); }

    @Override
    public String toString() {
        return "Product{productId=" + productId + ", productName='" + productName + "', sellingPrice=" + sellingPrice + ", status=" + status + "}";
    }
}
