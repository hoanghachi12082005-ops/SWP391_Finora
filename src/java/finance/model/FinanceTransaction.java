package finance.model;

/** Entity mapped to DBFinora.sql. TODO: Add validation/business helpers when implementing workflows. */
public class FinanceTransaction {
    private int transactionID;
    private int branchID;
    private int employeeID;
    private String transactionCode;
    private java.time.LocalDateTime transactionDate;
    private String transactionType;
    private java.math.BigDecimal amount;
    private Integer referenceID;
    private String referenceType;
    private String note;
    private java.time.LocalDateTime createdAt;

    public FinanceTransaction() {
    }
}
