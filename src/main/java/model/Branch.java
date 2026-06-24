package model;

import java.sql.Timestamp;

public class Branch {

    private int branchID;
    private String name;
    private String address;
    private String phone;
    private String status;
    private Timestamp createdAt;

    public Branch() {
    }

    public Branch(int branchID, String name, String address, String phone, String status) {
        this.branchID = branchID;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.status = status;
    }

    public int getBranchID() {
        return branchID;
    }

    public void setBranchID(int branchID) {
        this.branchID = branchID;
    }

    // Alias để nếu code gọi getBranchId/setBranchId vẫn chạy được
    public int getBranchId() {
        return branchID;
    }

    public void setBranchId(int branchID) {
        this.branchID = branchID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
