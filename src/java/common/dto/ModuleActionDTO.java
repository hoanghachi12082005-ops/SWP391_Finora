package common.dto;

/** One action/function inside a grouped RDS module workspace. */
public class ModuleActionDTO {
    private final String name;
    private final String description;
    private final String databaseMapping;
    private final String todo;
    private final boolean allowed;

    public ModuleActionDTO(String name, String description, String databaseMapping, String todo, boolean allowed) {
        this.name = name;
        this.description = description;
        this.databaseMapping = databaseMapping;
        this.todo = todo;
        this.allowed = allowed;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getDatabaseMapping() { return databaseMapping; }
    public String getTodo() { return todo; }
    public boolean isAllowed() { return allowed; }
}
