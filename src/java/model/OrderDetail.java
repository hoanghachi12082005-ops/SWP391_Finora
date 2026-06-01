package model;

/** Entity mapped to DBFinora.sql. TODO: Add validation/business helpers when implementing workflows. */
public class OrderDetail {
    private int orderDetailID;
    private int orderID;
    private int productID;
    private int quantity;
    private java.math.BigDecimal unitPrice;
    private java.math.BigDecimal subtotal;

    public OrderDetail() {
    }
}
