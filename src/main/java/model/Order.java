package model;

/**
 * Model đại diện cho Đơn hàng (Order) trong hệ thống Finora.
 * Khớp hoàn toàn với sơ đồ ERD.
 *
 * @author Finora Team
 */
public class Order {

    public enum OrderStatus {
        PENDING("Chờ thanh toán"),
        PAID("Đã thanh toán"),
        CANCELLED("Đã hủy"), COMPLETED("");

        private final String displayName;
        OrderStatus(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }

    private int orderId;
    private String orderCode;
    private String orderType;       // "SALES" / "PURCHASE" / v.v.
    private Integer customerId;     // FK -> Customer.cusId (Có thể null nếu khách vãng lai)
    private int branchId;           // FK -> Branch.branchId
    private Integer supplierId;     // FK -> Supplier.supplierId (Có thể null nếu bán hàng)
    private int empId;              // FK -> Employee.empId
    private Integer voucherId;      // FK -> Voucher.voucherId (Có thể null)
    private int warehouseId;        // FK -> Warehouse.warehouseId
    private double subtotal;
    private double discountAmount;
    private double totalAmount;
    private String paymentMethod;   // "CASH", "CARD", "TRANSFER", v.v.
    private OrderStatus status;
    private String createdAt;       // yyyy-MM-dd HH:mm:ss

    // Transient fields for join queries
    private String customerName;
    private String employeeName;
    private String branchName;

    // ── Constructors ─────────────────────────────────────────

    public Order() {}

    public Order(int orderId, String orderCode, String orderType, Integer customerId, int branchId,
                 Integer supplierId, int empId, Integer voucherId, int warehouseId,
                 double subtotal, double discountAmount, double totalAmount,
                 String paymentMethod, OrderStatus status, String createdAt) {
        this.orderId = orderId;
        this.orderCode = orderCode;
        this.orderType = orderType;
        this.customerId = customerId;
        this.branchId = branchId;
        this.supplierId = supplierId;
        this.empId = empId;
        this.voucherId = voucherId;
        this.warehouseId = warehouseId;
        this.subtotal = subtotal;
        this.discountAmount = discountAmount;
        this.totalAmount = totalAmount;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.createdAt = createdAt;
    }

    // ── Getters & Setters ─────────────────────────────────────

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public int getBranchId() {
        return branchId;
    }

    public void setBranchId(int branchId) {
        this.branchId = branchId;
    }

    public Integer getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Integer supplierId) {
        this.supplierId = supplierId;
    }

    public int getEmpId() {
        return empId;
    }

    public void setEmpId(int empId) {
        this.empId = empId;
    }

    public Integer getVoucherId() {
        return voucherId;
    }

    public void setVoucherId(Integer voucherId) {
        this.voucherId = voucherId;
    }

    public int getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(int warehouseId) {
        this.warehouseId = warehouseId;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    // ── Backward Compatibility Aliases ────────────────────────

    public int getOrderID() { return getOrderId(); }
    public void setOrderID(int orderID) { setOrderId(orderID); }

    public int getCusID() { return getCustomerId() != null ? getCustomerId() : 0; }
    public void setCusID(int cusID) { setCustomerId(cusID > 0 ? cusID : null); }

    public int getEmpID() { return getEmpId(); }
    public void setEmpID(int empID) { setEmpId(empID); }

    public String getOrderDate() { return getCreatedAt(); }
    public void setOrderDate(String orderDate) { setCreatedAt(orderDate); }

    @Override
    public String toString() {
        return "Order{orderId=" + orderId + ", orderCode='" + orderCode + "', totalAmount=" + totalAmount + ", status=" + status + "}";
    }
}
