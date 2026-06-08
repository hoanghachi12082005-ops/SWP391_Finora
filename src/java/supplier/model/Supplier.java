package supplier.model;

/** Entity mapped to DBFinora.sql. TODO: Add validation/business helpers when implementing workflows. */
public class Supplier {
    private int supplierID;
    private String name;
    private String phone;
    private String email;
    private String address;
    private String status;
    private java.time.LocalDateTime createdAt;

    public Supplier() {
    }
}
