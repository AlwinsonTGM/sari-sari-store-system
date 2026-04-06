package model;

import java.sql.Timestamp;

/**
 * User Model Class
 * Represents a system user (Admin or Cashier)
 */
public class User {
    private int userId;
    private String username;
    private String passwordHash;
    private String fullName;
    private String role;  // "admin" or "cashier"
    private boolean isActive;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    
    // Default constructor
    public User() {
    }
    
    // Constructor without ID (for new users)
    public User(String username, String passwordHash, String fullName, String role, boolean isActive) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.role = role;
        this.isActive = isActive;
    }
    
    // Full constructor
    public User(int userId, String username, String passwordHash, String fullName, 
                String role, boolean isActive, Timestamp createdAt, Timestamp updatedAt) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.role = role;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // Getters and Setters
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPasswordHash() {
        return passwordHash;
    }
    
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }
    
    public Timestamp getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    
    public Timestamp getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    /**
     * Check if user is an admin
     * @return true if role is "admin"
     */
    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(role);
    }
    
    /**
     * Check if user is a cashier
     * @return true if role is "cashier"
     */
    public boolean isCashier() {
        return "cashier".equalsIgnoreCase(role);
    }
    
    @Override
    public String toString() {
        return fullName + " (" + role + ")";
    }
}
