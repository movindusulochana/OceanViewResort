package com.oceanview.resort.dao;

import com.oceanview.resort.model.Room;
import com.oceanview.resort.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Task B.iii: Data Access Object (DAO) Layer.
 * This class manages all CRUD operations for the 'rooms' table.
 * It ensures that the UI can retrieve room pricing and availability.
 */
public class RoomDAO {

    /**
     * Retrieves all rooms currently stored in the database.
     * @return A list of all Room objects.
     */
    public List<Room> getAllRooms() {
        List<Room> rooms = new ArrayList<>();
        String query = "SELECT * FROM rooms";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                rooms.add(new Room(
                    rs.getInt("room_number"),
                    rs.getString("room_type"),
                    rs.getDouble("price_per_night"),
                    rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            System.err.println("--> [DAO ERROR] Could not fetch all rooms: " + e.getMessage());
        }
        return rooms;
    }

    /**
     * Task 2: Add New Reservation (Helper).
     * Filters and returns only rooms that are currently marked as 'Available'.
     * @return A list of available rooms for the booking UI.
     */
    public List<Room> getAvailableRooms() {
        List<Room> availableRooms = new ArrayList<>();
        String query = "SELECT * FROM rooms WHERE status = 'Available'";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                availableRooms.add(new Room(
                    rs.getInt("room_number"),
                    rs.getString("room_type"),
                    rs.getDouble("price_per_night"),
                    rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            System.err.println("--> [DAO ERROR] Could not fetch available rooms: " + e.getMessage());
        }
        return availableRooms;
    }

    /**
     * Task 4: Calculate Bill (Helper).
     * Retrieves the details of a specific room, mainly to get its 'price_per_night'.
     * @param roomNumber The unique room number.
     * @return A Room object or null if not found.
     */
    public Room getRoomByNumber(int roomNumber) {
        String query = "SELECT * FROM rooms WHERE room_number = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, roomNumber);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Room(
                    rs.getInt("room_number"),
                    rs.getString("room_type"),
                    rs.getDouble("price_per_night"),
                    rs.getString("status")
                );
            }
        } catch (SQLException e) {
            System.err.println("--> [DAO ERROR] Could not fetch room details: " + e.getMessage());
        }
        return null;
    }

    /**
     * Updates the status of a room (e.g., from 'Available' to 'Occupied').
     * Note: Most updates are handled by the SQL Trigger we created earlier.
     * @param roomNumber The room to update.
     * @param newStatus The new status (Available, Occupied, Maintenance).
     */
    public void updateRoomStatus(int roomNumber, String newStatus) {
        String query = "UPDATE rooms SET status = ? WHERE room_number = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, newStatus);
            stmt.setInt(2, roomNumber);
            stmt.executeUpdate();
            System.out.println("--> [DAO SUCCESS] Room " + roomNumber + " status updated to " + newStatus);

        } catch (SQLException e) {
            System.err.println("--> [DAO ERROR] Room status update failed: " + e.getMessage());
        }
    }
}