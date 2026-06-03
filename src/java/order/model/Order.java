package order.model;

/** Entity mapped to DBFinora.sql. TODO: Add validation/business helpers when implementing workflows. */
public class Order {
    private int orderID;
    private int branchID;
    private int employeeID;
    private Integer customerID;
    private Integer supplierID;
    private String orderCode;
    private String orderType;
    private java.math.BigDecimal subtotal;
    private java.math.BigDecimal discountAmount;
    private java.math.BigDecimal totalAmount;
    private String status;
    private java.time.LocalDateTime createdAt;

    public Order() {
    }
}
