package com.oceanview.resort.dao;

import com.oceanview.resort.util.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Task B.iii: Data Access Object (DAO) Layer.
 * This class handles database operations related to User Authentication (Task 1).
 * It interacts with the 'users' table in the 'resort' database.
 */
public class UserDAO {

    /**
     * Task 1: User Authentication (Login).
     * Validates provided credentials against the encrypted or plain-text 
     * passwords stored in the database.
     * * @param username The username entered in the Login UI.
     * @param password The password entered in the Login UI.
     * @return true if a matching record is found, false otherwise.
     */
    public boolean validateLogin(String username, String password) {
        // SQL query to check for matching username and password
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            // Set parameters safely to prevent SQL Injection (Ethical/Security requirement)
            stmt.setString(1, username);
            stmt.setString(2, password);
            
            // Execute the query
            try (ResultSet rs = stmt.executeQuery()) {
                // If the ResultSet has at least one row, credentials are valid
                if (rs.next()) {
                    System.out.println("--> [DAO SUCCESS] User authenticated: " + username);
                    return true;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("--> [DAO ERROR] Database authentication failed: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Return false if no match is found or an error occurs
        return false;
    }
}