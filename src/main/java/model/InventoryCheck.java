package model;

import java.time.LocalDateTime;
import java.util.List;

public class InventoryCheck {
    private int checkId;
    private String checkCode;
    private int warehouseId;
    private int createdBy;
    private Integer approvedBy;
    private String status; // PENDING, APPROVED, CANCELLED
    private int totalDiscrepancy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Transient fields
    private String warehouseName;
    private String createdByName;
    private String approvedByName;
    private String branchName;
    private List<InventoryCheckDetail> details;

    public InventoryCheck() {
        this.status = "PENDING";
    }

    public int getCheckId() {
        return checkId;
    }

    public void setCheckId(int checkId) {
        this.checkId = checkId;
    }

    public String getCheckCode() {
        return checkCode;
    }

    public void setCheckCode(String checkCode) {
        this.checkCode = checkCode;
    }

    public int getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(int warehouseId) {
        this.warehouseId = warehouseId;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public Integer getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(Integer approvedBy) {
        this.approvedBy = approvedBy;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getTotalDiscrepancy() {
        return totalDiscrepancy;
    }

    public void setTotalDiscrepancy(int totalDiscrepancy) {
        this.totalDiscrepancy = totalDiscrepancy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getWarehouseName() {
        return warehouseName;
    }

    public void setWarehouseName(String warehouseName) {
        this.warehouseName = warehouseName;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public String getApprovedByName() {
        return approvedByName;
    }

    public void setApprovedByName(String approvedByName) {
        this.approvedByName = approvedByName;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public List<InventoryCheckDetail> getDetails() {
        return details;
    }

    public void setDetails(List<InventoryCheckDetail> details) {
        this.details = details;
    }

    public String getFormattedCreatedAt() {
        if (createdAt == null) return "";
        return createdAt.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public String getFormattedUpdatedAt() {
        if (updatedAt == null) return "";
        return updatedAt.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
}
