package model;

    public class ActivityLog extends BaseModel {
    private int userId;
private String action;
private String description;

        public ActivityLog() {}

    public int getUserId() { return userId; }
public void setUserId(int userId) { this.userId = userId; }
public String getAction() { return action; }
public void setAction(String action) { this.action = action; }
public String getDescription() { return description; }
public void setDescription(String description) { this.description = description; }
    }
