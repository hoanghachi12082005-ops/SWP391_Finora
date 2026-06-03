package customer.model;

/** Entity mapped to DBFinora.sql. TODO: Add validation/business helpers when implementing workflows. */
public class Customer {
    private int customerID;
    private String fullName;
    private String phone;
    private String email;
    private String address;
    private java.time.LocalDate doB;
    private String gender;
    private String membershipTier;
    private int points;
    private java.time.LocalDateTime createdAt;

    public Customer() {
    }
}
