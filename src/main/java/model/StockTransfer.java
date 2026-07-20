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

    // Grouping transient fields
    private java.util.List<StockTransferDetail> details;
    private java.util.List<StockTransfer> subTransfers;
    private String displayStatus;

    public java.util.List<StockTransferDetail> getDetails() { return details; }
    public void setDetails(java.util.List<StockTransferDetail> details) { this.details = details; }
    public java.util.List<StockTransfer> getSubTransfers() { return subTransfers; }
    public void setSubTransfers(java.util.List<StockTransfer> subTransfers) { this.subTransfers = subTransfers; }
    public String getDisplayStatus() { return displayStatus; }
    public void setDisplayStatus(String displayStatus) { this.displayStatus = displayStatus; }

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

    private int fromBranchId;
    private int toBranchId;
    private int creatorBranchId;

    public int getFromBranchId() { return fromBranchId; }
    public void setFromBranchId(int fromBranchId) { this.fromBranchId = fromBranchId; }
    public int getToBranchId() { return toBranchId; }
    public void setToBranchId(int toBranchId) { this.toBranchId = toBranchId; }
    public int getCreatorBranchId() { return creatorBranchId; }
    public void setCreatorBranchId(int creatorBranchId) { this.creatorBranchId = creatorBranchId; }

    private Integer approvedByBranchId;
    public Integer getApprovedByBranchId() { return approvedByBranchId; }
    public void setApprovedByBranchId(Integer approvedByBranchId) { this.approvedByBranchId = approvedByBranchId; }
}