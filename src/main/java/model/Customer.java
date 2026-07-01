package model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Customer {

    private int customerId;
    private String fullName;
    private String gender;
    private LocalDate dateOfBirth;
    private String address;
    private String email;
    private String phone;
    private String passwordHash;
    private String cusType;
    private String status;
    private BigDecimal totalSpent;
    private int loyaltyPoint;
    private int lifetimePoints;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Customer() {}

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getCusType() { return cusType; }
    public void setCusType(String cusType) { this.cusType = cusType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getTotalSpent() { return totalSpent; }
    public void setTotalSpent(BigDecimal totalSpent) { this.totalSpent = totalSpent; }

    public int getLoyaltyPoint() { return loyaltyPoint; }
    public void setLoyaltyPoint(int loyaltyPoint) { this.loyaltyPoint = loyaltyPoint; }

    public int getLifetimePoints() { return lifetimePoints; }
    public void setLifetimePoints(int lifetimePoints) { this.lifetimePoints = lifetimePoints; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Backward compatibility aliases for sales DAO

    public int getCusId() { return customerId; }
    public void setCusId(int cusId) { this.customerId = cusId; }

    public String getBod() { return dateOfBirth != null ? dateOfBirth.toString() : null; }
    public void setBod(String bod) { this.dateOfBirth = bod != null && !bod.isBlank() ? LocalDate.parse(bod) : null; }

    public double getTotalSpentAsDouble() { return totalSpent != null ? totalSpent.doubleValue() : 0.0; }
    public void setTotalSpent(double totalSpent) { this.totalSpent = BigDecimal.valueOf(totalSpent); }

    public String getCreatedAtAsString() { return createdAt != null ? createdAt.toString() : null; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt != null && !createdAt.isBlank() ? LocalDateTime.parse(createdAt.replace(" ", "T")) : null; }

    public String getUpdatedAtAsString() { return updatedAt != null ? updatedAt.toString() : null; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt != null && !updatedAt.isBlank() ? LocalDateTime.parse(updatedAt.replace(" ", "T")) : null; }

    public void setStatus(Enum<?> status) { this.status = status != null ? status.name().toLowerCase() : null; }

    @Override
    public String toString() {
        return "Customer{customerId=" + customerId + ", fullName='" + fullName + "'}";
    }
}
