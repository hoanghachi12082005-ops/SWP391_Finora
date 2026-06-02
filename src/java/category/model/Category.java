package category.model;

/** Entity mapped to DBFinora.sql. TODO: Add validation/business helpers when implementing workflows. */
public class Category {
    private int categoryID;
    private String name;
    private String description;
    private Integer parentID;
    private String status;

    public Category() {
    }
}
