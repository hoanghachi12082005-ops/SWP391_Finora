package dto;

/** Role option shown by the shared development Role Selector. */
public class RoleOptionDTO {
    private final String name;
    private final String description;
    public RoleOptionDTO(String name, String description) { this.name = name; this.description = description; }
    public String getName() { return name; }
    public String getDescription() { return description; }
}
