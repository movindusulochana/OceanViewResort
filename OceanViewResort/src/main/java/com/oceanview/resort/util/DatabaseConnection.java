package com.oceanview.resort.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Task B.ii: Database connection factory.
 * Each call to getConnection() returns a FRESH connection so that concurrent
 * Tomcat threads never share the same JDBC connection — sharing a single
 * connection across threads causes race conditions, result-set corruption, and
 * "connection in use" errors under load.
 *
 * Callers MUST close the returned Connection (use try-with-resources).
 */
public class DatabaseConnection {

    // Database credentials based on your 'resort' DB
    private static final String URL  = "jdbc:mysql://localhost:3306/resort"
            + "?useSSL=false"
            + "&serverTimezone=UTC"
            + "&noAccessToProcedureBodies=true"   // skip information_schema lookup for SP params (MariaDB compat)
            + "&useServerPrepStmts=false"          // client-side prepared stmts — more compatible with MariaDB
            + "&allowMultiQueries=true";           // allow multiple statements / SP result draining
    private static final String USER     = "root";
    private static final String PASSWORD = ""; // Set your MySQL password here if needed

    // Private constructor prevents instantiation
    private DatabaseConnection() {}

    /**
     * Opens and returns a brand-new database connection.
     * The caller is responsible for closing it (try-with-resources is strongly
     * recommended to guarantee the connection is always returned to the server).
     */
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("--> [DB] New connection opened.");
            return conn;
        } catch (ClassNotFoundException e) {
            System.err.println("--> [DB ERROR] JDBC Driver not found: " + e.getMessage());
            throw new SQLException("JDBC driver not found", e);
        } catch (SQLException e) {
            System.err.println("--> [DB ERROR] Connection failed: " + e.getMessage());
            throw e;
        }
    }
}