package com.oceanview.resort.dao;

import com.oceanview.resort.model.Reservation;
import com.oceanview.resort.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Task B.iii: Data Access Object (DAO) Layer.
 * Handles CRUD operations for the 'reservations' table.
 */
public class ReservationDAO {

    /**
     * Task 2: Add New Reservation.
     * Inserts a record and returns the auto-generated Reservation ID.
     */
    public int addReservation(Reservation res) throws SQLException {
        // We include total_bill as 0.0 initially; Task 4 logic updates this later.
        String query = "INSERT INTO reservations (guest_id, room_number, check_in_date, check_out_date, total_bill, status) VALUES (?, ?, ?, ?, 0.0, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, res.getGuestId());
            stmt.setInt(2, res.getRoomNumber());
            stmt.setDate(3, Date.valueOf(res.getCheckInDate()));
            stmt.setDate(4, Date.valueOf(res.getCheckOutDate()));
            stmt.setString(5, res.getStatus());

            stmt.executeUpdate();

            // BUG FIX: generated-keys ResultSet must be closed; omitting try-with-resources
            // leaks the cursor when the caller's try-with-resources closes the statement.
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    /**
     * Task 3: Display Reservation Details.
     */
    public Reservation getReservationById(int id) {
        String query = "SELECT * FROM reservations WHERE reservation_number = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Reservation(
                        rs.getInt("reservation_number"),
                        rs.getInt("guest_id"),
                        rs.getInt("room_number"),
                        rs.getDate("check_in_date").toLocalDate(),
                        rs.getDate("check_out_date").toLocalDate(),
                        rs.getDouble("total_bill"),
                        rs.getString("status")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Reservation> getAllReservations() {
        List<Reservation> list = new ArrayList<>();
        String query = "SELECT * FROM reservations";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                list.add(new Reservation(
                    rs.getInt("reservation_number"),
                    rs.getInt("guest_id"),
                    rs.getInt("room_number"),
                    rs.getDate("check_in_date").toLocalDate(),
                    rs.getDate("check_out_date").toLocalDate(),
                    rs.getDouble("total_bill"),
                    rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Checks whether a room already has a CONFIRMED reservation that overlaps
     * with the proposed [checkIn, checkOut) window.
     * Overlap condition: existing.check_in < proposed.checkOut
     *                AND existing.check_out > proposed.checkIn
     *
     * Added for the Web layer (ReservationServlet overlap validation).
     *
     * @param roomNumber The room to check.
     * @param checkIn    The proposed check-in date.
     * @param checkOut   The proposed check-out date.
     * @return true if an overlapping confirmed reservation exists.
     */
    public boolean hasOverlappingReservation(int roomNumber,
                                              java.time.LocalDate checkIn,
                                              java.time.LocalDate checkOut)
            throws SQLException {
        String query = "SELECT COUNT(*) FROM reservations "
                + "WHERE room_number = ? AND status = 'Confirmed' "
                + "AND check_in_date < ? AND check_out_date > ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, roomNumber);
            stmt.setDate(2, Date.valueOf(checkOut));
            stmt.setDate(3, Date.valueOf(checkIn));

            // BUG FIX: ResultSet wrapped in try-with-resources so the cursor is released
            // immediately after the count is read, regardless of exception path.
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Updates the status of a reservation.
     * Used by BillingServlet after checkout to set the reservation to "Checked-Out".
     *
     * @param reservationId The reservation to update.
     * @param status        New status — must match DB enum: 'Confirmed', 'Checked-In',
     *                      'Checked-Out', 'Cancelled'.
     * @return true if at least one row was updated.
     */
    public boolean updateStatus(int reservationId, String status) {
        String query = "UPDATE reservations SET status = ? WHERE reservation_number = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, status);
            stmt.setInt(2, reservationId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("--> [DAO ERROR] updateStatus failed for res #"
                    + reservationId + ": " + e.getMessage());
            return false;
        }
    }
}