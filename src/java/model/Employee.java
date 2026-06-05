package model;

/** Entity mapped to DBFinora.sql. TODO: Add validation/business helpers when implementing workflows. */
public class Employee {
    private int employeeID;
    private int roleID;
    private int branchID;
    private String fullName;
    private String email;
    private String phone;
    private String passwordHash;
    private String status;
    private java.time.LocalDateTime createdAt;

    public Employee() {
    }
}
