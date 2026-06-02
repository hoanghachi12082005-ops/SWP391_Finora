package common.dto;

import java.util.List;

/** Dashboard card and workspace model for one grouped RDS module. */
public class ModuleDTO {
    private final String name;
    private final String route;
    private final String description;
    private final String databaseMapping;
    private final String ownerSuggestion;
    private final List<ModuleActionDTO> actions;
    private final boolean allowed;

    public ModuleDTO(String name, String route, String description, String databaseMapping,
                     String ownerSuggestion, List<ModuleActionDTO> actions, boolean allowed) {
        this.name = name;
        this.route = route;
        this.description = description;
        this.databaseMapping = databaseMapping;
        this.ownerSuggestion = ownerSuggestion;
        this.actions = actions;
        this.allowed = allowed;
    }

    public String getName() { return name; }
    public String getRoute() { return route; }
    public String getDescription() { return description; }
    public String getDatabaseMapping() { return databaseMapping; }
    public String getOwnerSuggestion() { return ownerSuggestion; }
    public List<ModuleActionDTO> getActions() { return actions; }
    public boolean isAllowed() { return allowed; }

    public long getAllowedActionCount() {
        return actions.stream().filter(ModuleActionDTO::isAllowed).count();
    }
}
