package com.oceanview.resort.service;

import com.oceanview.resort.util.DatabaseConnection;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

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
        // කලින් අපි SQL එකේ ලියපු CalculateReservationBill procedure එක මෙතනදී call කරනවා
        String query = "{CALL CalculateReservationBill(?)}";

        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement stmt = conn.prepareCall(query)) {

            stmt.setInt(1, reservationId);
            stmt.execute();
            System.out.println("--> [SERVICE] Bill calculated for Reservation #" + reservationId);
            return true;

        } catch (SQLException e) {
            System.err.println("--> [SERVICE ERROR] Billing failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * නිශ්චිත වෙන්කිරීමකට අදාළ බිල්පතේ මුළු අගය ලබා ගැනීම.
     */
    public double getTotalAmount(int reservationId) {
        String query = "SELECT total_bill FROM reservations WHERE reservation_number = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             var stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, reservationId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble("total_bill");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }
}