package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

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

    /**
     * Ảnh sản phẩm — lưu dưới dạng JSON array string: ["url1","url2",...]
     * Có thể null hoặc JSON rỗng nếu không có ảnh.
     */
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

    // =========================================================
    //  ImageUrl — JSON array helpers
    // =========================================================

    /** Set raw JSON value (dùng cho DAO đọc từ DB) */
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    /** Trả về giá trị RAW từ DB (JSON array hoặc URL đơn) — dùng cho DAO ghi DB */
    public String getImageUrlRaw() { return this.imageUrl; }

    /**
     * Trả về URL ảnh đầu tiên để hiển thị.
     * Parse JSON array nếu cần, trả về null nếu không có ảnh.
     */
    public String getImageUrl() {
        List<String> urls = getImageUrlList();
        return urls.isEmpty() ? null : urls.get(0);
    }

    /** Parse JSON string -> List<String> */
    public List<String> getImageUrlList() {
        return parseJsonArray(this.imageUrl);
    }

    /** Gán danh sách ảnh -> lưu dạng JSON array */
    public void setImageUrlList(List<String> urls) {
        this.imageUrl = toJsonArray(urls);
    }

    /** Thêm 1 URL vào JSON array hiện tại */
    public void addImageUrl(String url) {
        List<String> urls = getImageUrlList();
        urls.add(url);
        this.imageUrl = toJsonArray(urls);
    }

    // =========================================================
    //  JSON array parsing helpers (không dùng thư viện ngoài)
    // =========================================================

    /** Parse ["a","b","c"] -> List["a","b","c"] */
    public static List<String> parseJsonArray(String json) {
        List<String> result = new ArrayList<>();
        if (json == null || json.isBlank()) return result;
        String trimmed = json.trim();
        if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) {
            // Không phải JSON, coi như URL đơn lẻ (backward compat)
            result.add(trimmed);
            return result;
        }
        String inner = trimmed.substring(1, trimmed.length() - 1).trim();
        if (inner.isEmpty()) return result;
        // Tách bằng dấu ","
        String[] parts = inner.split("\",\"");
        for (String part : parts) {
            String clean = part.trim();
            if (clean.startsWith("\"")) clean = clean.substring(1);
            if (clean.endsWith("\"")) clean = clean.substring(0, clean.length() - 1);
            if (!clean.isEmpty()) result.add(clean);
        }
        return result;
    }

    /** List["a","b","c"] -> ["a","b","c"] */
    public static String toJsonArray(List<String> urls) {
        if (urls == null || urls.isEmpty()) return null;
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < urls.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(urls.get(i)).append("\"");
        }
        sb.append("]");
        return sb.toString();
    }

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
                ", quantity=" + quantity +
                ", sellingPrice=" + sellingPrice +
                '}';
    }
}
