package model;

/**
 * Đối tượng giỏ hàng — chỉ dùng trong session, KHÔNG ánh xạ bảng DB.
 * Chứa thông tin sản phẩm + số lượng đang chọn.
 */
public class CartItem implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private int productId;
    private String productName;
    private String productCodebar;
    private double sellingPrice;
    private int quantity;
    private int stockAvailable; // tồn kho hiện tại — dùng để validate phía client

    // ── Constructors ─────────────────────────────────────────

    public CartItem() {}

    public CartItem(int productId, String productName, String productCodebar,
                    double sellingPrice, int quantity, int stockAvailable) {
        this.productId      = productId;
        this.productName    = productName;
        this.productCodebar = productCodebar;
        this.sellingPrice   = sellingPrice;
        this.quantity        = quantity;
        this.stockAvailable  = stockAvailable;
    }

    // ── Computed ──────────────────────────────────────────────

    /** Thành tiền = đơn giá × số lượng */
    public double getLineTotal() {
        return sellingPrice * quantity;
    }

    // ── Getters & Setters ────────────────────────────────────

    public int getProductId()                     { return productId; }
    public void setProductId(int productId)       { this.productId = productId; }

    public String getProductName()                { return productName; }
    public void setProductName(String productName){ this.productName = productName; }

    public String getProductCodebar()             { return productCodebar; }
    public void setProductCodebar(String v)       { this.productCodebar = v; }

    public double getSellingPrice()               { return sellingPrice; }
    public void setSellingPrice(double v)         { this.sellingPrice = v; }

    public int getQuantity()                      { return quantity; }
    public void setQuantity(int quantity)         { this.quantity = quantity; }

    public int getStockAvailable()                { return stockAvailable; }
    public void setStockAvailable(int v)          { this.stockAvailable = v; }

    @Override
    public String toString() {
        return "CartItem{productId=" + productId + ", name='" + productName
                + "', qty=" + quantity + ", lineTotal=" + getLineTotal() + "}";
    }
}
