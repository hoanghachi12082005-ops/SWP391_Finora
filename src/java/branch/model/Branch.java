package branch.model;

/** Entity mapped to DBFinora.sql. TODO: Add validation/business helpers when implementing workflows. */
public class Branch {
    private int branchID;
    private String name;
    private String address;
    private String phone;
    private String status;
    private java.time.LocalDateTime createdAt;

    public Branch() {
    }
}
