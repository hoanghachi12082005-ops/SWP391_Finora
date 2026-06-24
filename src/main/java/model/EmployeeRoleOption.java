/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author Dzung
 */
public class EmployeeRoleOption {

    private int roleId;
    private String roleName;

    public EmployeeRoleOption() {
    }

    public EmployeeRoleOption(int roleId, String roleName) {
        this.roleId = roleId;
        this.roleName = roleName;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleCode() {
        if (roleName == null) {
            return "";
        }

        return roleName.replaceAll("\\s+", "").toLowerCase();
    }
}