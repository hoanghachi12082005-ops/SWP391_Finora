package model;

/** Entity mapped to DBFinora.sql. TODO: Add validation/business helpers when implementing workflows. */
public class Product {
    private int productID;
    private int categoryID;
    private String name;
    private String sku;
    private java.math.BigDecimal price;
    private java.math.BigDecimal costPrice;
    private int stockAlertQty;
    private String status;
    private java.time.LocalDateTime createdAt;

    public Product() {
    }
}
