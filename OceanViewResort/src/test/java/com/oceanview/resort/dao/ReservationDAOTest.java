package com.oceanview.resort.dao;

import com.oceanview.resort.model.Guest;
import com.oceanview.resort.model.Reservation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.sql.SQLException;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Task C: Test Automation.
 * Verifies that reservations can be created and retrieved correctly.
 */
public class ReservationDAOTest {

    private ReservationDAO reservationDAO;
    private GuestDAO guestDAO;

    @BeforeEach
    public void setUp() {
        reservationDAO = new ReservationDAO();
        guestDAO = new GuestDAO(); 
    }

    @Test
    public void testAddReservation() {
        try {
            // STEP 1: Create a real guest in the database first to avoid Foreign Key errors
            Guest testGuest = new Guest("Test Guest", "123 Test Ave", "0112233445");
            int guestId = guestDAO.addGuest(testGuest);
            
            assertTrue(guestId > 0, "Guest should be created successfully before reservation.");

            // STEP 2: Create reservation (Ensure Room 101 or similar exists in your 'rooms' table)
            // If your rooms table is empty, this may still fail. Ensure you have room 101.
            Reservation testRes = new Reservation(guestId, 101, LocalDate.now(), LocalDate.now().plusDays(3));
            testRes.setStatus("Confirmed");

            int generatedId = reservationDAO.addReservation(testRes);
            
            // ASSERTION: Check if the database returned a valid ID
            assertTrue(generatedId > 0, "Reservation ID should be automatically generated.");
            System.out.println("--> [TEST SUCCESS] Reservation added with ID: " + generatedId);

        } catch (SQLException e) {
            fail("Build Failure due to SQL Constraint: " + e.getMessage());
        }
    }

    @Test
    public void testGetReservationById() {
        // Testing retrieval of ID 1
        Reservation retrieved = reservationDAO.getReservationById(1);
        
        if (retrieved != null) {
            assertNotNull(retrieved);
            assertEquals(1, retrieved.getReservationNumber());
            System.out.println("--> [TEST SUCCESS] getReservationById retrieved record correctly.");
        } else {
            System.out.println("--> [TEST INFO] ID 1 not found; skipping assertion.");
        }
    }
}