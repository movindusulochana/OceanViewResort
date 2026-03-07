package com.oceanview.resort.servlet;

import com.google.gson.JsonObject;
import com.oceanview.resort.dao.ReservationDAO;
import com.oceanview.resort.model.Reservation;
import com.oceanview.resort.service.ReservationService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * REST endpoint for Reservation management.
 * Maps to `reservations` table: reservation_number, guest_id, room_number,
 * check_in_date, check_out_date, total_bill, status.
 *
 * GET  /api/reservations         -> list all reservations
 * GET  /api/reservations?id={n}  -> get single reservation
 * POST /api/reservations         -> create new reservation (with overlap check)
 *
 * The DB trigger `after_reservation_insert` automatically updates room status.
 * Do NOT duplicate that logic here.
 *
 * Strictly uses javax.servlet.* (Tomcat 8.5 compatible).
 */
@WebServlet(name = "ReservationServlet", urlPatterns = "/api/reservations")
public class ReservationServlet extends BaseApiServlet {

    private final ReservationDAO reservationDAO = new ReservationDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!requireAuth(req, resp)) return;

        String idParam = req.getParameter("id");
        if (idParam != null) {
            try {
                int id = Integer.parseInt(idParam);
                Reservation res = reservationDAO.getReservationById(id);
                if (res != null) {
                    writeSuccess(resp, res);
                } else {
                    writeError(resp, HttpServletResponse.SC_NOT_FOUND,
                            "Reservation not found.");
                }
            } catch (NumberFormatException e) {
                writeError(resp, HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid reservation ID.");
            }
        } else {
            List<Reservation> list = reservationDAO.getAllReservations();
            writeSuccess(resp, list);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!requireAuth(req, resp)) return;

        String guestIdParam  = req.getParameter("guest_id");
        String roomNumParam  = req.getParameter("room_number");
        String checkInParam  = req.getParameter("check_in_date");
        String checkOutParam = req.getParameter("check_out_date");

        if (isBlank(guestIdParam) || isBlank(roomNumParam)
                || isBlank(checkInParam) || isBlank(checkOutParam)) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "guest_id, room_number, check_in_date, and check_out_date are required.");
            return;
        }

        int guestId, roomNumber;
        LocalDate checkIn, checkOut;

        try {
            guestId    = Integer.parseInt(guestIdParam.trim());
            roomNumber = Integer.parseInt(roomNumParam.trim());
            checkIn    = LocalDate.parse(checkInParam.trim());
            checkOut   = LocalDate.parse(checkOutParam.trim());
        } catch (NumberFormatException e) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "guest_id and room_number must be integers.");
            return;
        } catch (DateTimeParseException e) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "Dates must be in YYYY-MM-DD format.");
            return;
        }

        if (!checkOut.isAfter(checkIn)) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "Check-out date must be after check-in date.");
            return;
        }

        // Check for overlapping reservations BEFORE inserting
        try {
            boolean overlapping = reservationDAO.hasOverlappingReservation(
                    roomNumber, checkIn, checkOut);
            if (overlapping) {
                writeError(resp, HttpServletResponse.SC_CONFLICT,
                        "Room " + roomNumber
                                + " is already booked for the selected dates. "
                                + "Please choose different dates or another room.");
                return;
            }

            Reservation reservation = new Reservation(guestId, roomNumber, checkIn, checkOut);
            int newId = reservationDAO.addReservation(reservation);

            if (newId > 0) {
                resp.setStatus(HttpServletResponse.SC_CREATED);
                JsonObject result = new JsonObject();
                result.addProperty("success", true);
                result.addProperty("reservation_id", newId);
                result.addProperty("message",
                        "Reservation #" + newId + " created successfully. "
                                + "Room status updated automatically by the DB trigger.");
                writeSuccess(resp, result);
            } else {
                writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Failed to create reservation.");
            }

        } catch (SQLException e) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Database error: " + e.getMessage());
        }
    }

}

