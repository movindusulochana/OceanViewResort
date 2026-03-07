package com.oceanview.resort.dao;

import com.oceanview.resort.model.Room;
import com.oceanview.resort.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Task B.iii: Data Access Object (DAO) Layer.
 * This class manages all CRUD operations for the 'rooms' table.
 * It ensures that the UI can retrieve room pricing and availability.
 */
public class RoomDAO {

    private String normalizeRoomType(String roomType) {
        return (roomType == null || roomType.trim().isEmpty()) ? "Standard" : roomType.trim();
    }

    /**
     * Retrieves all rooms currently stored in the database.
     * @return A list of all Room objects.
     */
    public List<Room> getAllRooms() {
        List<Room> rooms = new ArrayList<>();
        // COALESCE at SQL level: defense-in-depth so NULL room_type never reaches Java.
        // PreparedStatement used for consistency even though there are no parameters.
        String query = "SELECT room_number, COALESCE(NULLIF(TRIM(room_type), ''), 'Standard') AS room_type, "
                     + "price_per_night, status FROM rooms";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                rooms.add(new Room(
                    rs.getInt("room_number"),
                    normalizeRoomType(rs.getString("room_type")),
                    rs.getDouble("price_per_night"),
                    rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            System.err.println("--> [DAO ERROR] Could not fetch all rooms: " + e.getMessage());
            e.printStackTrace();
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
        String query = "SELECT room_number, COALESCE(NULLIF(TRIM(room_type), ''), 'Standard') AS room_type, "
                     + "price_per_night, status FROM rooms WHERE status = 'Available'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                availableRooms.add(new Room(
                    rs.getInt("room_number"),
                    normalizeRoomType(rs.getString("room_type")),
                    rs.getDouble("price_per_night"),
                    rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            System.err.println("--> [DAO ERROR] Could not fetch available rooms: " + e.getMessage());
            e.printStackTrace();
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
        String query = "SELECT room_number, COALESCE(NULLIF(TRIM(room_type), ''), 'Standard') AS room_type, "
                     + "price_per_night, status FROM rooms WHERE room_number = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
        ) {
            stmt.setInt(1, roomNumber);
            // BUG FIX: use try-with-resources on ResultSet to prevent resource leak
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Room(
                        rs.getInt("room_number"),
                        normalizeRoomType(rs.getString("room_type")),
                        rs.getDouble("price_per_night"),
                        rs.getString("status")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("--> [DAO ERROR] Could not fetch room details: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Updates the status of a room (e.g., from 'Occupied' back to 'Available' after checkout).
     * Note: insert/delete triggers handle automatic direction; this method handles
     * the explicit checkout direction that the trigger does not cover.
     *
     * @param roomNumber The room to update.
     * @param newStatus  The new status: "Available", "Occupied", or "Maintenance".
     * @return true if exactly one row was updated; false otherwise.
     */
    public boolean updateRoomStatus(int roomNumber, String newStatus) {
        String query = "UPDATE rooms SET status = ? WHERE room_number = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, newStatus);
            stmt.setInt(2, roomNumber);
            int rows = stmt.executeUpdate();
            System.out.println("--> [DAO SUCCESS] Room " + roomNumber + " status → " + newStatus);
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("--> [DAO ERROR] Room status update failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Quick Stats: counts rooms grouped by status for the Dashboard.
     * Returns a Map like: {"Available": 8, "Occupied": 4, "Maintenance": 1, "total": 13}
     */
    public Map<String, Integer> getRoomStats() {
        Map<String, Integer> stats = new HashMap<>();
        String query = "SELECT status, COUNT(*) AS cnt FROM rooms GROUP BY status";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            int total = 0;
            while (rs.next()) {
                int cnt = rs.getInt("cnt");
                stats.put(rs.getString("status"), cnt);
                total += cnt;
            }
            stats.put("total", total);

        } catch (SQLException e) {
            System.err.println("--> [DAO ERROR] getRoomStats failed: " + e.getMessage());
            e.printStackTrace();
        }
        return stats;
    }
}