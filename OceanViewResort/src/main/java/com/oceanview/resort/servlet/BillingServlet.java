package com.oceanview.resort.servlet;

import com.google.gson.JsonObject;
import com.oceanview.resort.dao.ReservationDAO;
import com.oceanview.resort.dao.RoomDAO;
import com.oceanview.resort.model.Reservation;
import com.oceanview.resort.service.BillingService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Billing & Checkout servlet.
 *
 * GET  /api/billing?id={n}      -> retrieve current bill details for a reservation
 * POST /api/billing/checkout    -> trigger CalculateReservationBill(?) stored procedure
 *                                  and mark the reservation as 'CheckedOut'
 *
 * CRITICAL: The stored procedure `CALL CalculateReservationBill(?)` is invoked via
 * BillingService.processFinalBill(reservationId).  Do NOT re-implement that logic here.
 *
 * Strictly uses javax.servlet.* (Tomcat 8.5 compatible).
 */
@WebServlet(name = "BillingServlet", urlPatterns = {"/api/billing", "/api/billing/checkout"})
public class BillingServlet extends BaseApiServlet {

    private final BillingService    billingService    = new BillingService();
    private final ReservationDAO    reservationDAO    = new ReservationDAO();
    private final RoomDAO           roomDAO           = new RoomDAO();

    /** GET /api/billing?id={n} â€“ fetch bill info for a reservation */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!requireAuth(req, resp)) return;

        String idParam = req.getParameter("id");
        if (idParam == null || idParam.trim().isEmpty()) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "Reservation id parameter is required.");
            return;
        }

        try {
            int reservationId = Integer.parseInt(idParam.trim());
            Reservation res = reservationDAO.getReservationById(reservationId);

            if (res == null) {
                writeError(resp, HttpServletResponse.SC_NOT_FOUND,
                        "Reservation #" + reservationId + " not found.");
                return;
            }

            // If bill hasn't been calculated yet, show 0.0
            double totalBill = res.getTotalBill();
            if (totalBill == 0.0) {
                // Attempt to get the freshly-calculated bill from DB
                totalBill = billingService.getTotalAmount(reservationId);
            }

            JsonObject data = new JsonObject();
            data.addProperty("reservation_id",  res.getReservationNumber());
            data.addProperty("guest_id",         res.getGuestId());
            data.addProperty("room_number",      res.getRoomNumber());
            data.addProperty("check_in_date",    res.getCheckInDate().toString());
            data.addProperty("check_out_date",   res.getCheckOutDate().toString());
            data.addProperty("total_bill",       totalBill);
            data.addProperty("status",           res.getStatus());
            writeSuccess(resp, data);

        } catch (NumberFormatException e) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid reservation ID.");
        }
    }

    /** POST /api/billing/checkout â€“ execute stored procedure and checkout the guest */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!requireAuth(req, resp)) return;

        String idParam = req.getParameter("reservation_id");
        if (idParam == null || idParam.trim().isEmpty()) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "reservation_id is required.");
            return;
        }

        int reservationId;
        try {
            reservationId = Integer.parseInt(idParam.trim());
        } catch (NumberFormatException e) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid reservation_id.");
            return;
        }

        // Verify reservation exists
        Reservation res = reservationDAO.getReservationById(reservationId);
        if (res == null) {
            writeError(resp, HttpServletResponse.SC_NOT_FOUND,
                    "Reservation #" + reservationId + " not found.");
            return;
        }

        // DB enum value for checked-out is 'Checked-Out' (with a hyphen).
        if ("Checked-Out".equalsIgnoreCase(res.getStatus())) {
            writeError(resp, HttpServletResponse.SC_CONFLICT,
                    "Reservation #" + reservationId + " has already been checked out.");
            return;
        }

        // ---------------------------------------------------------------
        // CRITICAL: Call the MySQL Stored Procedure: CALL CalculateReservationBill(?)
        // This calculates the total_bill and updates the reservations table.
        // ---------------------------------------------------------------
        boolean billed = billingService.processFinalBill(reservationId);

        if (!billed) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Billing procedure failed for Reservation #" + reservationId);
            return;
        }

        // Retrieve the calculated total bill
        double totalBill = billingService.getTotalAmount(reservationId);

        // Update reservation status to 'Checked-Out' (DB enum value with hyphen).
        // The stored procedure only recalculates total_bill; it does NOT change status.
        boolean statusUpdated = reservationDAO.updateStatus(reservationId, "Checked-Out");
        System.out.println("--> [BILLING] Reservation #" + reservationId
                + (statusUpdated ? " marked Checked-Out." : " status update skipped."));

        // Release the room back to 'Available' so it can be booked again.
        boolean roomReleased = roomDAO.updateRoomStatus(res.getRoomNumber(), "Available");
        System.out.println("--> [BILLING] Room " + res.getRoomNumber()
                + (roomReleased ? " marked Available." : " room-release skipped (not found)."));

        JsonObject result = new JsonObject();
        result.addProperty("success",        true);
        result.addProperty("reservation_id", reservationId);
        result.addProperty("room_number",    res.getRoomNumber());
        result.addProperty("total_bill",     totalBill);
        result.addProperty("message",
                "Checkout complete for Reservation #" + reservationId
                        + ". Total bill: " + totalBill);
        writeSuccess(resp, result);
    }
}

