package model;

/**
 * Model đại diện cho Nhân viên trong hệ thống Finora.
 * Khớp hoàn toàn với sơ đồ ERD.
 *
 * @author Finora Team
 */
public class Employee {

    /**
     * Trạng thái làm việc của nhân viên.
     */
    public enum EmployeeStatus {
        ACTIVE("Đang làm việc"),
        INACTIVE("Đã nghỉ việc"),
        ON_LEAVE("Nghỉ phép");

        private final String displayName;
        EmployeeStatus(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }

    private int    empId;
    private int    branchId;        // FK → Branch.branchId
    private String fullName;
    private String gender;          // "Nam" / "Nữ" / "Khác"
    private String bod;             // Date of Birth (yyyy-MM-dd)
    private String address;
    private String email;
    private String phone;
    private String passwordHash;    // Mật khẩu đã hash
    private EmployeeStatus status;
    private String createdAt;       // yyyy-MM-dd HH:mm:ss
    private String updateAt;        // yyyy-MM-dd HH:mm:ss

    // ── Constructors ─────────────────────────────────────────

    public Employee() {}

    /** Constructor đầy đủ dùng khi map từ DB */
    public Employee(int empId, int branchId, String fullName, String gender, String bod,
                    String address, String email, String phone, String passwordHash,
                    EmployeeStatus status, String createdAt, String updateAt) {
        this.empId        = empId;
        this.branchId     = branchId;
        this.fullName     = fullName;
        this.gender       = gender;
        this.bod          = bod;
        this.address      = address;
        this.email        = email;
        this.phone        = phone;
        this.passwordHash = passwordHash;
        this.status       = status;
        this.createdAt    = createdAt;
        this.updateAt     = updateAt;
    }

    // ── Getters & Setters ─────────────────────────────────────

    public int getEmpId()               { return empId; }
    public void setEmpId(int empId)     { this.empId = empId; }

    public int getBranchId()            { return branchId; }
    public void setBranchId(int branchId) { this.branchId = branchId; }

    public String getFullName()                  { return fullName; }
    public void setFullName(String fullName)     { this.fullName = fullName; }

    public String getGender()                    { return gender; }
    public void setGender(String gender)         { this.gender = gender; }

    public String getBod()                       { return bod; }
    public void setBod(String bod)               { this.bod = bod; }

    public String getAddress()                   { return address; }
    public void setAddress(String address)       { this.address = address; }

    public String getEmail()                     { return email; }
    public void setEmail(String email)           { this.email = email; }

    public String getPhone()                     { return phone; }
    public void setPhone(String phone)           { this.phone = phone; }

    public String getPasswordHash()              { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public EmployeeStatus getStatus()   { return status; }
    public void setStatus(EmployeeStatus status) { this.status = status; }

    public String getCreatedAt()                 { return createdAt; }
    public void setCreatedAt(String createdAt)   { this.createdAt = createdAt; }

    public String getUpdateAt()                  { return updateAt; }
    public void setUpdateAt(String updateAt)     { this.updateAt = updateAt; }

    // ── Backward Compatibility Aliases ────────────────────────

    public int getEmpID() { return getEmpId(); }
    public void setEmpID(int empID) { setEmpId(empID); }

    public int getBranchID() { return getBranchId(); }
    public void setBranchID(int branchID) { setBranchId(branchID); }

    public String getDateOfBirth() { return getBod(); }
    public void setDateOfBirth(String dateOfBirth) { setBod(dateOfBirth); }

    @Override
    public String toString() {
        return "Employee{empId=" + empId + ", fullName='" + fullName
                + "', branchId=" + branchId + ", status=" + status + "}";
    }
}
