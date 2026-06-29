package model;

import java.time.LocalDateTime;

public class InventoryTicket {
    private int ticketId;
    private String ticketCode;
    private String ticketType; // IMPORT, EXPORT, TRANSFER, CHECK
    private Integer fromWarehouseId;
    private Integer toWarehouseId;
    private String status; // PENDING, PROCESSING, COMPLETED, CANCELLED
    private String note;
    private int createdBy;
    private LocalDateTime createdAt;
    
    // Virtual fields
    private String fromWarehouseName;
    private String toWarehouseName;
    private String createdByName;
    
    // New fields for Transit Workflow
    private boolean isExportedBySender;
    private boolean isImportedByReceiver;

    public InventoryTicket() {
    }

    public int getTicketId() {
        return ticketId;
    }

    public void setTicketId(int ticketId) {
        this.ticketId = ticketId;
    }

    public String getTicketCode() {
        return ticketCode;
    }

    public void setTicketCode(String ticketCode) {
        this.ticketCode = ticketCode;
    }

    public String getTicketType() {
        return ticketType;
    }

    public void setTicketType(String ticketType) {
        this.ticketType = ticketType;
    }

    public Integer getFromWarehouseId() {
        return fromWarehouseId;
    }

    public void setFromWarehouseId(Integer fromWarehouseId) {
        this.fromWarehouseId = fromWarehouseId;
    }

    public Integer getToWarehouseId() {
        return toWarehouseId;
    }

    public void setToWarehouseId(Integer toWarehouseId) {
        this.toWarehouseId = toWarehouseId;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
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

    public boolean isExportedBySender() {
        return isExportedBySender;
    }

    public void setExportedBySender(boolean exportedBySender) {
        isExportedBySender = exportedBySender;
    }

    public boolean isImportedByReceiver() {
        return isImportedByReceiver;
    }

    public void setImportedByReceiver(boolean importedByReceiver) {
        isImportedByReceiver = importedByReceiver;
    }
}
