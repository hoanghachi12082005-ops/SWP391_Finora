package model;

import java.util.Date;

public class StockTransfer {
    private int stockTransferId;
    private int fromWarehouseId;
    private int toWarehouseId;
    private String transferCode;
    private Date transferDate;
    private String status;
    private String note;
    private int createdBy;

    // View-only fields
    private String fromWarehouseName;
    private String toWarehouseName;
    private String createdByName;
    private String transferProgress;
    private Integer approvedBy;
    private String approvedByName;

    public Integer getApprovedBy() { return approvedBy; }
    public void setApprovedBy(Integer approvedBy) { this.approvedBy = approvedBy; }
    public String getApprovedByName() { return approvedByName; }
    public void setApprovedByName(String approvedByName) { this.approvedByName = approvedByName; }

    public StockTransfer() {
    }

    public int getStockTransferId() {
        return stockTransferId;
    }

    public void setStockTransferId(int stockTransferId) {
        this.stockTransferId = stockTransferId;
    }

    public int getFromWarehouseId() {
        return fromWarehouseId;
    }

    public void setFromWarehouseId(int fromWarehouseId) {
        this.fromWarehouseId = fromWarehouseId;
    }

    public int getToWarehouseId() {
        return toWarehouseId;
    }

    public void setToWarehouseId(int toWarehouseId) {
        this.toWarehouseId = toWarehouseId;
    }

    public String getTransferCode() {
        return transferCode;
    }

    public void setTransferCode(String transferCode) {
        this.transferCode = transferCode;
    }

    public Date getTransferDate() {
        return transferDate;
    }

    public void setTransferDate(Date transferDate) {
        this.transferDate = transferDate;
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

    public String getFromWarehouseName() {
        return fromWarehouseName;
    }

    public void setFromWarehouseName(String fromWarehouseName) {
        this.fromWarehouseName = fromWarehouseName;
    }

    public String getToWarehouseName() {
        return toWarehouseName;
    }

    public void setToWarehouseName(String toWarehouseName) {
        this.toWarehouseName = toWarehouseName;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public String getTransferProgress() {
        return transferProgress;
    }

    public void setTransferProgress(String transferProgress) {
        this.transferProgress = transferProgress;
    }
}