package model;

import java.sql.Timestamp;

public class Role {

    private int roleID;
    private String roleName;
    private String discription;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Role() {
    }

    public Role(int roleID, String roleName, String discription, Timestamp createdAt, Timestamp updatedAt) {
        this.roleID = roleID;
        this.roleName = roleName;
        this.discription = discription;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getRoleID() {
        return roleID;
    }

    public void setRoleID(int roleID) {
        this.roleID = roleID;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getDiscription() {
        return discription;
    }

    public void setDiscription(String discription) {
        this.discription = discription;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Backward compat aliases
    public int getRoleId() {
        return roleID;
    }

    public void setRoleId(int roleID) {
        this.roleID = roleID;
    }

    public String getName() {
        return roleName;
    }

    public void setName(String name) {
        this.roleName = name;
    }

    public String getDescription() {
        return discription;
    }

    public void setDescription(String description) {
        this.discription = description;
    }
}
