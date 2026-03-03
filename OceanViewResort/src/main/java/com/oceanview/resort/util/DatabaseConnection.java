package com.oceanview.resort.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Task B.ii: Singleton Design Pattern implementation.
 * Ensures a single database connection instance is shared across the app.
 */
public class DatabaseConnection {

    // Database credentials based on your 'resort' DB
    private static final String URL = "jdbc:mysql://localhost:3306/resort";
    private static final String USER = "root"; 
    private static final String PASSWORD = ""; // Add your MySQL password here if you have one

    private static Connection connection = null;

    // Private constructor prevents instantiation from other classes
    private DatabaseConnection() {}

    /**
     * Returns the active database connection. 
     * If null or closed, it creates a new one.
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                // Load MySQL JDBC Driver
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("--> [DB SUCCESS] Connected to 'resort' database.");
            } catch (ClassNotFoundException e) {
                System.err.println("--> [DB ERROR] JDBC Driver not found: " + e.getMessage());
                throw new SQLException(e);
            } catch (SQLException e) {
                System.err.println("--> [DB ERROR] Connection failed: " + e.getMessage());
                throw e;
            }
        }
        return connection;
    }
}