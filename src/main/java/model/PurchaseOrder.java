package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PurchaseOrder {
    private int orderId;
    private String orderCode;
    private Integer supplierId;
    private Integer branchId;
    private Integer empId;
    private Integer warehouseId;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime createdAt;
    private String orderType; // PURCHASE or EXPORT

    private String supplierName;
    private String branchName;
    private String empName;
    private Integer approvedBy;
    private String approvedByName;

    public Integer getApprovedBy() { return approvedBy; }
    public void setApprovedBy(Integer approvedBy) { this.approvedBy = approvedBy; }
    public String getApprovedByName() { return approvedByName; }
    public void setApprovedByName(String approvedByName) { this.approvedByName = approvedByName; }

    public PurchaseOrder() {
        this.subtotal = BigDecimal.ZERO;
        this.discountAmount = BigDecimal.ZERO;
        this.totalAmount = BigDecimal.ZERO;
        this.status = "PENDING";
    }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public String getOrderCode() { return orderCode; }
    public void setOrderCode(String orderCode) { this.orderCode = orderCode; }
    public Integer getSupplierId() { return supplierId; }
    public void setSupplierId(Integer supplierId) { this.supplierId = supplierId; }
    public Integer getBranchId() { return branchId; }
    public void setBranchId(Integer branchId) { this.branchId = branchId; }
    public Integer getEmpId() { return empId; }
    public void setEmpId(Integer empId) { this.empId = empId; }
    public Integer getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Integer warehouseId) { this.warehouseId = warehouseId; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getCreatedAtFormatted() {
        if (createdAt == null) return "";
        return createdAt.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
    public String getOrderType() { return orderType; }
    public void setOrderType(String orderType) { this.orderType = orderType; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }
    public String getEmpName() { return empName; }
    public void setEmpName(String empName) { this.empName = empName; }
}
