package payment.model;

/** Entity mapped to DBFinora.sql. TODO: Add validation/business helpers when implementing workflows. */
public class Payment {
    private int paymentsID;
    private int orderID;
    private String paymentMethod;
    private java.math.BigDecimal amount;
    private java.time.LocalDateTime paidAt;
    private String reference;
    private String status;
    private java.time.LocalDateTime createdAt;

    public Payment() {
    }
}
