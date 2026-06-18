package model;

    public class User extends BaseModel {
    private String username;
private String passwordHash;
private String email;
private String phone;
private String role;

        public User() {}

    public String getUsername() { return username; }
public void setUsername(String username) { this.username = username; }
public String getPasswordHash() { return passwordHash; }
public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
public String getEmail() { return email; }
public void setEmail(String email) { this.email = email; }
public String getPhone() { return phone; }
public void setPhone(String phone) { this.phone = phone; }
public String getRole() { return role; }
public void setRole(String role) { this.role = role; }
    }
