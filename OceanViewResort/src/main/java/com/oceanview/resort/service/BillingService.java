package com.oceanview.resort.service;

import com.oceanview.resort.util.DatabaseConnection;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;

/**
 * Task 4: Calculate and Print Bill.
 * Task B.iii: Business Logic Layer.
 */
public class BillingService {

    /**
     * දත්ත සමුදායේ ඇති Stored Procedure එක භාවිතා කර බිල ගණනය කිරීම.
     * @param reservationId වෙන්කිරීමේ අංකය
     * @return සාර්ථක නම් true ලබා දෙයි
     */
    public boolean processFinalBill(int reservationId) {
        String query = "{CALL CalculateReservationBill(?)}";

        // The stored procedure does only SELECT..INTO (no client result set) + UPDATE.
        // We use try-with-resources for both connection and statement — the driver
        // cleans up any pending protocol state when the statement is closed.
        // No explicit transaction wrapper needed: MariaDB auto-commits each DML
        // statement inside the SP, and wrapping a DDL-free SP in setAutoCommit(false)
        // has caused protocol-level failures with MySQL Connector/J 9.x + MariaDB.
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement stmt = conn.prepareCall(query)) {

            stmt.setInt(1, reservationId);
            stmt.execute();   // runs the SP; try-with-resources closes stmt & conn cleanly

            System.out.println("--> [SERVICE] Bill calculated for Reservation #" + reservationId);
            return true;

        } catch (SQLException e) {
            System.err.println("--> [SERVICE ERROR] Billing failed for Reservation #"
                    + reservationId + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * නිශ්චිත වෙන්කිරීමකට අදාළ බිල්පතේ මුළු අගය ලබා ගැනීම.
     */
    public double getTotalAmount(int reservationId) {
        String query = "SELECT total_bill FROM reservations WHERE reservation_number = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, reservationId);
            // BUG FIX: ResultSet in try-with-resources; bare assignment leaks the
            // cursor on any exception path inside the block.
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total_bill");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }
}