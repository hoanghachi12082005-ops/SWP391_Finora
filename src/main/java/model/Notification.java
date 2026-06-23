package model;

    public class Notification {
    private int userId;
private String message;
private boolean read;

        public Notification() {}

    public int getUserId() { return userId; }
public void setUserId(int userId) { this.userId = userId; }
public String getMessage() { return message; }
public void setMessage(String message) { this.message = message; }
public boolean isRead() { return read; }
public void setRead(boolean read) { this.read = read; }
    }
