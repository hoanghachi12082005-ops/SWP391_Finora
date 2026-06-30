package model;

import java.time.LocalDateTime;

public class Store {
    private int branchId;
    private String branchName;
    private String branchCode;
    private String address;
    private String district;
    private String city;
    private String phone;
    private String email;
    private String status;
    private LocalDateTime createdAt;

    public Store() { this.status = "ACTIVE"; }

    public int getBranchId() { return branchId; }
    public void setBranchId(int branchId) { this.branchId = branchId; }
    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }
    public String getBranchCode() { return branchCode; }
    public void setBranchCode(String branchCode) { this.branchCode = branchCode; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
