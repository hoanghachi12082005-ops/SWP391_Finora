package model;

/** Entity mapped to DBFinora.sql. TODO: Add validation/business helpers when implementing workflows. */
public class AuditLog {
    private int auditLogID;
    private int employeeID;
    private String action;
    private String entityName;
    private int entityID;
    private String oldData;
    private String newData;
    private java.time.LocalDateTime createdAt;

    public AuditLog() {
    }
}
