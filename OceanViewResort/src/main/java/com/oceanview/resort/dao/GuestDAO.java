package com.oceanview.resort.dao;

import com.oceanview.resort.model.Guest;
import com.oceanview.resort.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Task B.iii: Data Access Object (DAO) Layer.
 * This class handles all CRUD operations for the 'guests' table in the MySQL database.
 * It ensures guest data is correctly stored and retrieved for reservations.
 */
public class GuestDAO {

    /**
     * Task 2: Add New Reservation (Guest Registration part).
     * Saves a new guest to the database and returns their unique generated ID.
     * @param guest The Guest object containing name, address, and contact info.
     * @return The auto-incremented guest_id from the database.
     * @throws SQLException if a database error occurs.
     */
    public int addGuest(Guest guest) throws SQLException {
        String query = "INSERT INTO guests (name, address, contact_number) VALUES (?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, guest.getName());
            stmt.setString(2, guest.getAddress());
            stmt.setString(3, guest.getContactNumber());
            
            stmt.executeUpdate();
            
            // Retrieve the ID to link this guest to a reservation immediately
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return -1;
    }

    /**
     * Retrieves a guest's information using their unique ID.
     * @param id The guest_id to search for.
     * @return A Guest object or null if not found.
     */
    public Guest getGuestById(int id) {
        String query = "SELECT * FROM guests WHERE guest_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return new Guest(
                    rs.getInt("guest_id"),
                    rs.getString("name"),
                    rs.getString("address"),
                    rs.getString("contact_number")
                );
            }
        } catch (SQLException e) {
            System.err.println("--> [DAO ERROR] Error finding guest: " + e.getMessage());
        }
        return null;
    }

    /**
     * Retrieves a list of all registered guests for the Guest Management UI.
     * @return A list of all Guest records.
     */
    public List<Guest> getAllGuests() {
        List<Guest> guestList = new ArrayList<>();
        String query = "SELECT * FROM guests";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                guestList.add(new Guest(
                    rs.getInt("guest_id"),
                    rs.getString("name"),
                    rs.getString("address"),
                    rs.getString("contact_number")
                ));
            }
        } catch (SQLException e) {
            System.err.println("--> [DAO ERROR] Error fetching guest list: " + e.getMessage());
        }
        return guestList;
    }

    /**
     * Updates an existing guest's details in the database.
     * @param guest The Guest object with updated information.
     * @return true if the update was successful.
     */
    public boolean updateGuest(Guest guest) {
        String query = "UPDATE guests SET name = ?, address = ?, contact_number = ? WHERE guest_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, guest.getName());
            stmt.setString(2, guest.getAddress());
            stmt.setString(3, guest.getContactNumber());
            stmt.setInt(4, guest.getGuestId());
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("--> [DAO ERROR] Error updating guest: " + e.getMessage());
            return false;
        }
    }
}