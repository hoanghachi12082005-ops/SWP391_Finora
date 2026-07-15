package model;

public class Branch {

    public enum BranchStatus {
        ACTIVE("Hoạt động"),
        INACTIVE("Ngừng hoạt động");

        private final String displayName;

        BranchStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private int branchId;
    private String branchName;
    private String branchCode;
    private String address;
    private String phone;
    private String email;
    private String openingTime;   // Định dạng HH:mm
    private String closingTime;   // Định dạng HH:mm
    private String status;        // "active" hoặc "locked"
    private String createdAt;     // Định dạng yyyy-MM-dd HH:mm:ss
    private String updateAt;      // Định dạng yyyy-MM-dd HH:mm:ss
    private String city;
    private String district;
    private String imageUrl;

    // Các trường bổ sung phục vụ hiển thị trên view
    private String managerName;
    private int managerId;
    private int employeeCount;
    private double revenue;

    // =========================================================
    //  Constructors
    // =========================================================
    public Branch() {
        this.status = "ACTIVE";
    }

    public Branch(int branchId, String branchName, String branchCode,
            String address, String phone, String email,
            String openingTime, String closingTime,
            String status, String createdAt, String updateAt, String city, String district, String imageUrl) {
        this.branchId = branchId;
        this.branchName = branchName;
        this.branchCode = branchCode;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.openingTime = openingTime;
        this.closingTime = closingTime;
        setStatus(status);
        this.createdAt = createdAt;
        this.updateAt = updateAt;
        this.city = city;
        this.district = district;
        this.imageUrl = imageUrl;

    }

    // =========================================================
    //  Getters & Setters
    // =========================================================
    public int getBranchId() {
        return branchId;
    }

    public void setBranchId(int branchId) {
        this.branchId = branchId;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOpeningTime() {
        return openingTime;
    }

    public void setOpeningTime(String openingTime) {
        this.openingTime = openingTime;
    }

    public String getClosingTime() {
        return closingTime;
    }

    public void setClosingTime(String closingTime) {
        this.closingTime = closingTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        if (status == null || status.isBlank()) {
            this.status = "ACTIVE";
        } else if (status.equalsIgnoreCase("ACTIVE")) {
            this.status = "ACTIVE";
        } else {
            this.status = "INACTIVE";
        }
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(String updateAt) {
        this.updateAt = updateAt;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getFullAddress() {
        return address + ", " + district + ", " + city;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    public int getManagerId() {
        return managerId;
    }

    public void setManagerId(int managerId) {
        this.managerId = managerId;
    }

    public int getEmployeeCount() {
        return employeeCount;
    }

    public void setEmployeeCount(int employeeCount) {
        this.employeeCount = employeeCount;
    }

    public double getRevenue() {
        return revenue;
    }

    public void setRevenue(double revenue) {
        this.revenue = revenue;
    }

    // =========================================================
    //  Backward Compatibility & View Aliases
    // =========================================================
    public int getBranchID() {
        return getBranchId();
    }

    public void setBranchID(int branchID) {
        setBranchId(branchID);
    }

    public String getName() {
        return getBranchName();
    }

    public void setName(String name) {
        setBranchName(name);
    }

    public String getCreatedDate() {
        return getCreatedAt();
    }

    public void setCreatedDate(String createdDate) {
        setCreatedAt(createdDate);
    }

    @Override
    public String toString() {
        return "Branch{"
                + "branchId=" + branchId
                + ", branchName='" + branchName + '\''
                + ", branchCode='" + branchCode + '\''
                + ", status='" + status + '\''
                + '}';
    }
}
