package model;

/**
 * Model đại diện cho Nhật ký hoạt động (AuditLog) trong hệ thống Finora.
 * Khớp hoàn toàn với sơ đồ ERD.
 *
 * @author Finora Team
 */
public class AuditLog {

    private int auditLogId;
    private int empId;          // FK -> Employee.empId
    private String actionName;
    private String tableName;
    private int recordId;
    private String oldData;     // Nội dung dữ liệu cũ dạng JSON hoặc TEXT
    private String newData;     // Nội dung dữ liệu mới dạng JSON hoặc TEXT
    private String createdAt;   // yyyy-MM-dd HH:mm:ss

    // ── Constructors ─────────────────────────────────────────

    public AuditLog() {}

    public AuditLog(int auditLogId, int empId, String actionName, String tableName, int recordId, String oldData, String newData, String createdAt) {
        this.auditLogId = auditLogId;
        this.empId = empId;
        this.actionName = actionName;
        this.tableName = tableName;
        this.recordId = recordId;
        this.oldData = oldData;
        this.newData = newData;
        this.createdAt = createdAt;
    }

    // ── Getters & Setters ─────────────────────────────────────

    public int getAuditLogId() {
        return auditLogId;
    }

    public void setAuditLogId(int auditLogId) {
        this.auditLogId = auditLogId;
    }

    public int getEmpId() {
        return empId;
    }

    public void setEmpId(int empId) {
        this.empId = empId;
    }

    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public int getRecordId() {
        return recordId;
    }

    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }

    public String getOldData() {
        return oldData;
    }

    public void setOldData(String oldData) {
        this.oldData = oldData;
    }

    public String getNewData() {
        return newData;
    }

    public void setNewData(String newData) {
        this.newData = newData;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "AuditLog{auditLogId=" + auditLogId + ", empId=" + empId + ", actionName='" + actionName + "', tableName='" + tableName + "'}";
    }
}
