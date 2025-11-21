package models;

import java.sql.Timestamp;

public class User {
    private int id;
    private String username;
    private String email;
    private String password;
    private int roleId; // Ganti dari String role ke int roleId
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public User() {}

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.roleId = 2; // Default ke cashier (sesuaikan ID default jika perlu)
    }

    public User(int id, String username, String email, int roleId) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.roleId = roleId;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    // Ganti getRole() dan setRole() menjadi getRoleId() dan setRoleId()
    public int getRoleId() { return roleId; }
    public void setRoleId(int roleId) { this.roleId = roleId; }
    
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
    
    public String getRoleName() {
        switch (this.roleId) {
            case 1:
                return "admin";
            case 2:
                return "cashier";
            default:
                return "unknown"; // Atau bisa juga throw exception jika diperlukan
        }
    }
}