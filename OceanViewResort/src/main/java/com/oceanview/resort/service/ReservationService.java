package com.oceanview.resort.service;

import com.oceanview.resort.dao.ReservationDAO;
import com.oceanview.resort.model.Reservation;
import com.oceanview.resort.api.NotificationClient;
import java.sql.SQLException;
import java.util.List;

/**
 * Task B.iii: Business Logic Layer (Service Layer).
 * This class coordinates the core processes for managing hotel bookings,
 * ensuring that data flows correctly between the UI controllers and the Database.
 */
public class ReservationService {

    private final ReservationDAO reservationDAO;
    private final BillingService billingService;
    private final NotificationClient notificationClient;

    /**
     * Constructor initializes required DAO and API components.
     */
    public ReservationService() {
        this.reservationDAO = new ReservationDAO();
        this.billingService = new BillingService();
        this.notificationClient = new NotificationClient();
    }

    /**
     * Handles the complete workflow for creating a new reservation.
     * Fulfills Task 2 (Add New Reservation) and Task B.i (Web Services).
     * * @param reservation The reservation data object from the UI.
     * @param guestEmail The guest's email address for sending confirmations.
     * @return true if the booking, billing, and notification are successful.
     */
    public boolean createNewBooking(Reservation reservation, String guestEmail) {
        try {
            // STEP 1: Save the reservation to the database using the DAO
            int resId = reservationDAO.addReservation(reservation);
            
            if (resId > 0) {
                // STEP 2: Automatically calculate the total bill (Task 4)
                // This calls the stored procedure via BillingService
                billingService.processFinalBill(resId);
                
                // STEP 3: Trigger the external Web Service (Task B.i)
                // This sends a simulated email/SMS confirmation to the guest
                notificationClient.sendBookingConfirmation(guestEmail, String.valueOf(resId));
                
                System.out.println("--> [SERVICE SUCCESS] Reservation #" + resId + " finalized.");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("--> [SERVICE ERROR] Database operation failed: " + e.getMessage());
        }
        return false;
    }

    /**
     * Retrieves full booking information for a specific reservation.
     * Fulfills Task 3: Display Reservation Details.
     * * @param reservationId The unique ID of the reservation.
     * @return A Reservation model object populated with data.
     */
    public Reservation getReservationDetails(int reservationId) {
        return reservationDAO.getReservationById(reservationId);
    }

    /**
     * Fetches a list of all current reservations in the system.
     * Useful for the Dashboard and reporting.
     * * @return A List of Reservation objects.
     */
    public List<Reservation> getAllBookings() {
        return reservationDAO.getAllReservations();
    }
}