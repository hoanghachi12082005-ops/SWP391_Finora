package model;

import java.sql.Timestamp;

public class Employee {

    // ─── Hằng số ─────────────────────────────────────────────
    public static final String STATUS_ACTIVE   = "ACTIVE";
    public static final String STATUS_INACTIVE = "INACTIVE";
    public static final int    MAX_FAILED_LOGIN = 5;

    // ─── Trường dữ liệu ──────────────────────────────────────
    private int       employeeID;
    private int       roleID;
    private Integer   branchID;

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
    public Employee(int employeeID, int roleID, Integer branchID, String fullName, String gender,
            Timestamp dob, String address, String email, String phone, String passwordHash,
            String status, Timestamp createdAt, Timestamp updatedAt) {
        this.employeeID  = employeeID;
        this.roleID      = roleID;
        this.branchID    = branchID;
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

    // =========================
    // Employee ID
    // =========================
    public int getEmployeeID() { return employeeID; }
    public void setEmployeeID(int employeeID) { this.employeeID = employeeID; }
    public int getEmployeeId() { return employeeID; }
    public void setEmployeeId(int employeeID) { this.employeeID = employeeID; }

    // =========================
    // Role ID
    // =========================
    public int getRoleID() { return roleID; }
    public void setRoleID(int roleID) { this.roleID = roleID; }
    public int getRoleId() { return roleID; }
    public void setRoleId(int roleID) { this.roleID = roleID; }

    // =========================
    // Branch ID
    // =========================
    public Integer getBranchID() { return branchID; }
    public void setBranchID(Integer branchID) { this.branchID = branchID; }
    public void setBranchID(int branchID) { this.branchID = branchID; }
    public Integer getBranchId() { return branchID; }
    public void setBranchId(Integer branchID) { this.branchID = branchID; }
    public void setBranchId(int branchID) { this.branchID = branchID; }

    // =========================
    // Basic information
    // =========================
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

    // Alias: some old code uses username as email
    public String getUsername() { return email; }
    public void setUsername(String username) { this.email = username; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    // Alias: some old code uses password
    public String getPassword() { return passwordHash; }
    public void setPassword(String password) { this.passwordHash = password; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // =========================
    // Failed Login Count
    // =========================
    public int getFailedLoginCount() { return failedLoginCount; }
    public void setFailedLoginCount(int failedLoginCount) { this.failedLoginCount = failedLoginCount; }

    /** Số lần thử còn lại trước khi bị khoá. */
    public int getRemainingAttempts() {
        int remaining = MAX_FAILED_LOGIN - failedLoginCount;
        return Math.max(0, remaining);
    }

    // =========================
    // Timestamps
    // =========================
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    // =========================
    // Role & Branch display fields
    // =========================
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }

    public String getRoleNames() { return roleNames; }
    public void setRoleNames(String roleNames) { this.roleNames = roleNames; }

    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }

    // =========================
    // Helper status checks
    // =========================
    public boolean isActive() {
        return STATUS_ACTIVE.equalsIgnoreCase(status);
    }

    // =========================
    // ToString (Debug)
    // =========================
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

    public void setAvatarUrl(String string) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
