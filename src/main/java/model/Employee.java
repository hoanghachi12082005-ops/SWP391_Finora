package model;

import java.sql.Timestamp;

public class Employee {
    // ─── Hằng số ─────────────────────────────────────────────
    public static final String STATUS_ACTIVE   = "ACTIVE";
    public static final String STATUS_INACTIVE = "INACTIVE";
    public static final int    MAX_FAILED_LOGIN = 5;

    public enum EmployeeStatus {
        ACTIVE("Đang làm việc"),
        INACTIVE("Đã nghỉ việc"),
        ON_LEAVE("Nghỉ phép");

        private final String displayName;
        EmployeeStatus(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }

    // ─── Trường dữ liệu ──────────────────────────────────────
    private int       employeeID;
    private int       roleID;
    private Integer   branchID;
    private String    avatarUrl;
    private String    fullName;
    private String    gender;
    private Timestamp dob;
    private String    address;
    private String    email;
    private String    phone;
    private String    passwordHash;
    private String    status;
    private int       failedLoginCount;

    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Extra fields for displaying JOIN data
    private String roleName;
    private String roleNames;
    private String branchName;

    // Default Constructor
    public Employee() {
    }

    // Full Constructor
    public Employee(int employeeID, int roleID, Integer branchID, String avatarUrl, String fullName, String gender,
            Timestamp dob, String address, String email, String phone, String passwordHash,
            String status, Timestamp createdAt, Timestamp updatedAt) {
        this.employeeID  = employeeID;
        this.roleID      = roleID;
        this.branchID    = branchID;
        this.avatarUrl   = avatarUrl;
        this.fullName    = fullName;
        this.gender      = gender;
        this.dob         = dob;
        this.address     = address;
        this.email       = email;
        this.phone       = phone;
        this.passwordHash = passwordHash;
        this.status      = status;
        this.createdAt   = createdAt;
        this.updatedAt   = updatedAt;
    }

    // =========================================================
    // Getters & Setters
    // =========================================================

    public int getEmployeeID() { return employeeID; }
    public void setEmployeeID(int employeeID) { this.employeeID = employeeID; }
    public int getEmployeeId() { return employeeID; }
    public void setEmployeeId(int employeeID) { this.employeeID = employeeID; }
    public int getEmpId() { return employeeID; }
    public void setEmpId(int empId) { this.employeeID = empId; }
    public int getEmpID() { return employeeID; }
    public void setEmpID(int empID) { this.employeeID = empID; }

    public int getRoleID() { return roleID; }
    public void setRoleID(int roleID) { this.roleID = roleID; }
    public int getRoleId() { return roleID; }
    public void setRoleId(int roleID) { this.roleID = roleID; }

    public Integer getBranchID() { return branchID; }
    public void setBranchID(Integer branchID) { this.branchID = branchID; }
    public void setBranchID(int branchID) { this.branchID = branchID; }
    public Integer getBranchId() { return branchID; }
    public void setBranchId(Integer branchID) { this.branchID = branchID; }
    public void setBranchId(int branchID) { this.branchID = branchID; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public Timestamp getDob() { return dob; }
    public void setDob(Timestamp dob) { this.dob = dob; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return email; }
    public void setUsername(String username) { this.email = username; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getPassword() { return passwordHash; }
    public void setPassword(String password) { this.passwordHash = password; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getFailedLoginCount() { return failedLoginCount; }
    public void setFailedLoginCount(int failedLoginCount) { this.failedLoginCount = failedLoginCount; }

    public int getRemainingAttempts() {
        int remaining = MAX_FAILED_LOGIN - failedLoginCount;
        return Math.max(0, remaining);
    }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }

    public String getRoleNames() { return roleNames; }
    public void setRoleNames(String roleNames) { this.roleNames = roleNames; }

    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }

    public boolean isActive() {
        return STATUS_ACTIVE.equalsIgnoreCase(status) || "active".equalsIgnoreCase(status);
    }

    // =========================================================
    // Backward Compatibility String-date & Enum properties
    // =========================================================

    public String getBod() {
        return dob != null ? dob.toString().substring(0, 10) : null;
    }

    public void setBod(String bod) {
        if (bod != null && !bod.isBlank()) {
            try {
                this.dob = Timestamp.valueOf(bod + " 00:00:00");
            } catch (Exception e) {
                // Ignore parsing errors
            }
        }
    }

    public void setCreatedAt(String createdAtStr) {
        if (createdAtStr != null && !createdAtStr.isBlank()) {
            try {
                this.createdAt = Timestamp.valueOf(createdAtStr);
            } catch (Exception e) {
                try {
                    this.createdAt = Timestamp.valueOf(createdAtStr + " 00:00:00");
                } catch (Exception ex) {}
            }
        }
    }

    public String getUpdateAt() {
        return updatedAt != null ? updatedAt.toString() : null;
    }

    public void setUpdateAt(String updatedAtStr) {
        if (updatedAtStr != null && !updatedAtStr.isBlank()) {
            try {
                this.updatedAt = Timestamp.valueOf(updatedAtStr);
            } catch (Exception e) {
                try {
                    this.updatedAt = Timestamp.valueOf(updatedAtStr + " 00:00:00");
                } catch (Exception ex) {}
            }
        }
    }

    public void setStatus(EmployeeStatus employeeStatus) {
        this.status = employeeStatus != null ? employeeStatus.name() : null;
    }

    @Override
    public String toString() {
        return "Employee{"
                + "employeeID=" + employeeID
                + ", roleID=" + roleID
                + ", branchID=" + branchID
                + ", fullName='" + fullName + '\''
                + ", email='" + email + '\''
                + ", status='" + status + '\''
                + ", failedLoginCount=" + failedLoginCount
                + '}';
    }
}
