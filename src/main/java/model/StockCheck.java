package model;

import java.time.LocalDateTime;

public class StockCheck {
    private int stockCheckId;
    private int warehouseId;
    private String checkCode;
    private LocalDateTime checkDate;
    private String status;
    private String note;
    private int createdBy;
    private Integer approvedBy;
    private LocalDateTime approvedAt;

    // Display fields
    private String warehouseName;
    private String createdByName;
    private String approvedByName;
    private int totalDifference;

    public StockCheck() {}

    public int getStockCheckId() {
        return stockCheckId;
    }

    public void setStockCheckId(int stockCheckId) {
        this.stockCheckId = stockCheckId;
    }

    public int getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(int warehouseId) {
        this.warehouseId = warehouseId;
    }

    public String getCheckCode() {
        return checkCode;
    }

    public void setCheckCode(String checkCode) {
        this.checkCode = checkCode;
    }

    public LocalDateTime getCheckDate() {
        return checkDate;
    }

    public void setCheckDate(LocalDateTime checkDate) {
        this.checkDate = checkDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
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

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
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

    public int getTotalDifference() {
        return totalDifference;
    }

    public void setTotalDifference(int totalDifference) {
        this.totalDifference = totalDifference;
    }
}
