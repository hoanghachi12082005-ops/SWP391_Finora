package model;

/** Entity mapped to DBFinora.sql. TODO: Add validation/business helpers when implementing workflows. */
public class Warehouse {
    private int warehouseID;
    private int branchID;
    private int employeeID;
    private int productID;
    private String name;
    private String address;
    private String status;
    private int quantity;
    private int availableQuantity;
    private int minQuantity;
    private int maxQuantity;
    private java.time.LocalDateTime updatedAt;
    private java.time.LocalDateTime createdAt;

    public Warehouse() {
    }
}
