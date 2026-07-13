package model;

/**
 * Model đại diện cho Chi tiết đơn hàng (OrderDetail) trong hệ thống Finora.
 * Khớp hoàn toàn với sơ đồ ERD.
 *
 * @author Finora Team
 */
public class OrderDetail {

    private int orderDetailId;
    private int orderId;        // FK -> Order.orderId
    private int productId;      // FK -> Product.productId
    private int quantity;
    private double unitPrice;
    private double totalPrice;
    private double importPrice; // Added import_price column field
    private Integer supplierId;    // FK -> Supplier (for multi-supplier purchase orders)
    private String supplierStatus; // PENDING, APPROVED, SHIPPED, DELIVERED

    // Transient fields for join queries
    private String productName;
    private String productCode;
    private String supplierName;

    // ── Constructors ─────────────────────────────────────────

    public OrderDetail() {}

    public OrderDetail(int orderDetailId, int orderId, int productId, int quantity, double unitPrice, double totalPrice) {
        this.orderDetailId = orderDetailId;
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
        this.importPrice = 0.0;
    }

    public OrderDetail(int orderDetailId, int orderId, int productId, int quantity, double unitPrice, double totalPrice, double importPrice) {
        this.orderDetailId = orderDetailId;
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
        this.importPrice = importPrice;
    }

    // ── Getters & Setters ─────────────────────────────────────

    public int getOrderDetailId() {
        return orderDetailId;
    }

    public void setOrderDetailId(int orderDetailId) {
        this.orderDetailId = orderDetailId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
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

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public double getImportPrice() {
        return importPrice;
    }

    public void setImportPrice(double importPrice) {
        this.importPrice = importPrice;
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

    public Integer getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Integer supplierId) {
        this.supplierId = supplierId;
    }

    public String getSupplierStatus() {
        return supplierStatus;
    }

    public void setSupplierStatus(String supplierStatus) {
        this.supplierStatus = supplierStatus;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    @Override
    public String toString() {
        return "OrderDetail{orderDetailId=" + orderDetailId + ", orderId=" + orderId + ", productId=" + productId + ", quantity=" + quantity + ", totalPrice=" + totalPrice + ", importPrice=" + importPrice + ", productName='" + productName + "'}";
    }
}
