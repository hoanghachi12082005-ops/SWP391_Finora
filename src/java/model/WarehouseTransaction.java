package model;

/** Entity mapped to DBFinora.sql. TODO: Add validation/business helpers when implementing workflows. */
public class WarehouseTransaction {
    private int warehouseTransactionID;
    private int warehouseID;
    private int productID;
    private int beforeQuantity;
    private int quantity;
    private String transactionType;
    private int afterQuantity;
    private java.math.BigDecimal unitCost;
    private String referenceType;
    private Integer referenceID;
    private int createdBy;
    private java.time.LocalDateTime createdAt;

    public WarehouseTransaction() {
    }
}
