package com.oceanview.resort.dao;

import com.oceanview.resort.model.UserLogin;
import com.oceanview.resort.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the 'users' table.
 *
 * Supported operations:
 *   - validateLogin  (authentication)
 *   - getAllUsers     (list all users — password excluded from display)
 *   - getUserById    (single user lookup)
 *   - addUser        (INSERT)
 *   - updateUser     (UPDATE username / password / role)
 *   - deleteUser     (DELETE by user_id)
 *
 * Password is stored as plain-text to match the existing schema convention.
 * Column names assumed: user_id, username, password, role.
 */
public class UserDAO {

    // ------------------------------------------------------------------
    // Authentication
    // ------------------------------------------------------------------

    /**
     * Validates credentials against the users table.
     * Returns the authenticated UserLogin (with role) or null if invalid.
     */
    public UserLogin validateLogin(String username, String password) {
        String sql = "SELECT user_id, username, role FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("--> [DAO] User authenticated: " + username);
                    return new UserLogin(rs.getInt("user_id"), rs.getString("username"), "", rs.getString("role"));
                }
            }
        } catch (SQLException e) {
            System.err.println("--> [DAO ERROR] validateLogin: " + e.getMessage());
        }
        return null;
    }

    // ------------------------------------------------------------------
    // READ
    // ------------------------------------------------------------------

    /** Returns all users (password field is intentionally left empty for safety). */
    public List<UserLogin> getAllUsers() {
        List<UserLogin> list = new ArrayList<>();
        String sql = "SELECT user_id, username, role FROM users ORDER BY user_id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(new UserLogin(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        "",                         // never expose password
                        rs.getString("role")));
            }
        } catch (SQLException e) {
            System.err.println("--> [DAO ERROR] getAllUsers: " + e.getMessage());
        }
        return list;
    }

    /** Returns a single user by ID (password excluded). */
    public UserLogin getUserById(int userId) {
        String sql = "SELECT user_id, username, role FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new UserLogin(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            "",
                            rs.getString("role"));
                }
            }
        } catch (SQLException e) {
            System.err.println("--> [DAO ERROR] getUserById: " + e.getMessage());
        }
        return null;
    }

    // ------------------------------------------------------------------
    // CREATE
    // ------------------------------------------------------------------

    /**
     * Inserts a new user.  Returns the new auto-generated user_id, or -1 on failure.
     * Duplicate username will cause an SQL constraint violation and return -1.
     */
    public int addUser(UserLogin user) {
        String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getRole() != null ? user.getRole() : "Staff");

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        return keys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("--> [DAO ERROR] addUser: " + e.getMessage());
        }
        return -1;
    }

    // ------------------------------------------------------------------
    // UPDATE
    // ------------------------------------------------------------------

    /**
     * Updates username, role.  If the password field in the UserLogin is non-blank
     * it is also updated; if blank the existing password is preserved.
     */
    public boolean updateUser(UserLogin user) {
        boolean changePassword = user.getPassword() != null && !user.getPassword().trim().isEmpty();

        String sql = changePassword
                ? "UPDATE users SET username = ?, password = ?, role = ? WHERE user_id = ?"
                : "UPDATE users SET username = ?, role = ? WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (changePassword) {
                stmt.setString(1, user.getUsername());
                stmt.setString(2, user.getPassword());
                stmt.setString(3, user.getRole());
                stmt.setInt(4, user.getUserId());
            } else {
                stmt.setString(1, user.getUsername());
                stmt.setString(2, user.getRole());
                stmt.setInt(3, user.getUserId());
            }

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("--> [DAO ERROR] updateUser: " + e.getMessage());
        }
        return false;
    }

    // ------------------------------------------------------------------
    // DELETE
    // ------------------------------------------------------------------

    /** Permanently removes a user by ID. Returns true if a row was deleted. */
    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("--> [DAO ERROR] deleteUser: " + e.getMessage());
        }
        return false;
    }
}