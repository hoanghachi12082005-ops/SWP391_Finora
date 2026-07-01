package model;

/**
 * Model đại diện cho Khách hàng trong hệ thống Finora.
 * Khớp hoàn toàn với sơ đồ ERD.
 *
 * @author Finora Team
 */
public class Customer {

    /**
     * Phân loại khách hàng dựa trên tổng chi tiêu.
     */
    public enum CustomerType {
        REGULAR("Thường"),
        SILVER("Bạc"),
        GOLD("Vàng"),
        VIP("VIP");

        private final String displayName;
        CustomerType(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }

    /**
     * Trạng thái tài khoản khách hàng.
     */
    public enum CustomerStatus {
        ACTIVE("Hoạt động"),
        INACTIVE("Ngừng hoạt động"),
        BLOCKED("Đã khóa");

        private final String displayName;
        CustomerStatus(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }

    private int            cusId;
    private String         fullName;
    private String         gender;          // "Nam" / "Nữ" / "Khác"
    private String         bod;             // Date of Birth (yyyy-MM-dd)
    private String         address;
    private String         email;
    private String         phone;
    private String         passwordHash;    // Mật khẩu đã hash (tùy chọn)
    private CustomerType   cusType;
    private CustomerStatus status;
    private double         totalSpent;      // Tổng tiền đã chi tiêu
    private String         createdAt;       // yyyy-MM-dd HH:mm:ss
    private String         updatedAt;       // yyyy-MM-dd HH:mm:ss

    // ── Constructors ─────────────────────────────────────────

    public Customer() {}

    /** Constructor đầy đủ dùng khi map từ DB */
    public Customer(int cusId, String fullName, String gender, String bod,
                    String address, String email, String phone, String passwordHash,
                    CustomerType cusType, CustomerStatus status,
                    double totalSpent, String createdAt, String updatedAt) {
        this.cusId        = cusId;
        this.fullName     = fullName;
        this.gender       = gender;
        this.bod          = bod;
        this.address      = address;
        this.email        = email;
        this.phone        = phone;
        this.passwordHash = passwordHash;
        this.cusType      = cusType;
        this.status       = status;
        this.totalSpent   = totalSpent;
        this.createdAt    = createdAt;
        this.updatedAt    = updatedAt;
    }

    // ── Getters & Setters ─────────────────────────────────────

    public int getCusId()                { return cusId; }
    public void setCusId(int cusId)      { this.cusId = cusId; }

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

    public CustomerType getCusType()             { return cusType; }
    public void setCusType(CustomerType cusType) { this.cusType = cusType; }

    public CustomerStatus getStatus()            { return status; }
    public void setStatus(CustomerStatus status) { this.status = status; }

    public double getTotalSpent()                { return totalSpent; }
    public void setTotalSpent(double totalSpent) { this.totalSpent = totalSpent; }

    public String getCreatedAt()                 { return createdAt; }
    public void setCreatedAt(String createdAt)   { this.createdAt = createdAt; }

    public String getUpdatedAt()                 { return updatedAt; }
    public void setUpdatedAt(String updatedAt)   { this.updatedAt = updatedAt; }

    // ── Backward Compatibility Aliases ────────────────────────

    public int getCusID() { return getCusId(); }
    public void setCusID(int cusID) { setCusId(cusID); }

    public String getBoD() { return getBod(); }
    public void setBoD(String BoD) { setBod(BoD); }

    public String getCreatedDate() { return getCreatedAt(); }
    public void setCreatedDate(String createdDate) { setCreatedAt(createdDate); }

    @Override
    public String toString() {
        return "Customer{cusId=" + cusId + ", fullName='" + fullName + "', cusType=" + cusType + "}";
    }
}
