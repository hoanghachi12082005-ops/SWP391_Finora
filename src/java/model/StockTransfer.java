package model;

/** Entity mapped to DBFinora.sql. TODO: Add validation/business helpers when implementing workflows. */
public class StockTransfer {
    private int stockTransferID;
    private int branchID;
    private int employeeID;
    private int productID;
    private int fromWarehouseID;
    private int toWarehouseID;
    private String transferCode;
    private java.time.LocalDateTime transferDate;
    private int quantity;
    private String status;
    private String note;
    private java.time.LocalDateTime createdAt;

    public StockTransfer() {
    }
}
