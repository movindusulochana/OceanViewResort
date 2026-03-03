package com.oceanview.resort.model;

/**
 * Task B: The "Model" layer for User Authentication.
 * Represents the credentials and role of a system user.
 */
public class UserLogin {
    private int userId;
    private String username;
    private String password;
    private String role; // e.g., 'Admin' or 'Staff'

    // Default Constructor
    public UserLogin() {}

    // Constructor for login attempts
    public UserLogin(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Full Constructor (used when retrieving user data from DB)
    public UserLogin(int userId, String username, String password, String role) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // Getters and Setters
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    @Override
    public String toString() {
        return "User: " + username + " (" + role + ")";
    }
}