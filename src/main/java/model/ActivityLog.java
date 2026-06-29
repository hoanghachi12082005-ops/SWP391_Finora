package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Activity Log entity ánh xạ tới bảng audit_log trong DB.
 *  - id (auditLogId), empId, actionName, tableName, recordId, oldData, newData, createdAt
 *  - empName là thông tin join từ bảng employee để hiển thị (không lưu DB).
 */
public class ActivityLog {
    private int id;
    private int empId;
    private String empName;
    private String actionName;
    private String tableName;
    private Integer recordId;
    private String oldData;
    private String newData;
    private LocalDateTime createdAt;

    public ActivityLog() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEmpId() { return empId; }
    public void setEmpId(int empId) { this.empId = empId; }

    public String getEmpName() { return empName; }
    public void setEmpName(String empName) { this.empName = empName; }

    public String getActionName() { return actionName; }
    public void setActionName(String actionName) { this.actionName = actionName; }

    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }

    public Integer getRecordId() { return recordId; }
    public void setRecordId(Integer recordId) { this.recordId = recordId; }

    public String getOldData() { return oldData; }
    public void setOldData(String oldData) { this.oldData = oldData; }

    public String getNewData() { return newData; }
    public void setNewData(String newData) { this.newData = newData; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
