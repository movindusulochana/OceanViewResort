package com.oceanview.resort.servlet;

import com.google.gson.JsonObject;
import com.oceanview.resort.dao.GuestDAO;
import com.oceanview.resort.model.Guest;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Full CRUD REST endpoint for Guest management.
 * Maps to `guests` table: guest_id, name, address, contact_number.
 *
 * GET    /api/guests          -> list all guests
 * GET    /api/guests?id={n}   -> get guest by ID
 * POST   /api/guests          -> add a new guest
 * PUT    /api/guests          -> update an existing guest
 * DELETE /api/guests?id={n}   -> delete a guest
 *
 * Strictly uses javax.servlet.* (Tomcat 8.5 compatible).
 */
@WebServlet(name = "GuestServlet", urlPatterns = "/api/guests")
public class GuestServlet extends BaseApiServlet {

    private final GuestDAO guestDAO = new GuestDAO();

    /** GET: list all guests, or get single guest by ?id= */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!requireAuth(req, resp)) return;

        String idParam = req.getParameter("id");
        if (idParam != null) {
            try {
                int id = Integer.parseInt(idParam);
                Guest guest = guestDAO.getGuestById(id);
                if (guest != null) {
                    writeSuccess(resp, guest);
                } else {
                    writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Guest not found.");
                }
            } catch (NumberFormatException e) {
                writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid guest ID.");
            }
        } else {
            List<Guest> guests = guestDAO.getAllGuests();
            writeSuccess(resp, guests);
        }
    }

    /** POST: add a new guest */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!requireAuth(req, resp)) return;

        String name    = req.getParameter("name");
        String address = req.getParameter("address");
        String contact = req.getParameter("contact_number");

        if (isBlank(name) || isBlank(address) || isBlank(contact)) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "name, address, and contact_number are required.");
            return;
        }

        Guest guest = new Guest(name.trim(), address.trim(), contact.trim());
        try {
            int newId = guestDAO.addGuest(guest);
            if (newId > 0) {
                guest.setGuestId(newId);
                resp.setStatus(HttpServletResponse.SC_CREATED);
                JsonObject result = new JsonObject();
                result.addProperty("success", true);
                result.addProperty("guest_id", newId);
                result.addProperty("message", "Guest added successfully.");
                writeSuccess(resp, result);
            } else {
                writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Failed to add guest.");
            }
        } catch (SQLException e) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Database error: " + e.getMessage());
        }
    }

    /** PUT: update an existing guest */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!requireAuth(req, resp)) return;

        String idParam = req.getParameter("guest_id");
        String name    = req.getParameter("name");
        String address = req.getParameter("address");
        String contact = req.getParameter("contact_number");

        if (isBlank(idParam) || isBlank(name) || isBlank(address) || isBlank(contact)) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "guest_id, name, address, and contact_number are required.");
            return;
        }

        try {
            int id = Integer.parseInt(idParam);
            Guest guest = new Guest(id, name.trim(), address.trim(), contact.trim());
            boolean updated = guestDAO.updateGuest(guest);
            JsonObject result = new JsonObject();
            result.addProperty("success", updated);
            result.addProperty("message", updated ? "Guest updated." : "Guest not found or no change.");
            writeSuccess(resp, result);
        } catch (NumberFormatException e) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid guest_id.");
        }
    }

    /** DELETE: remove a guest by ?id= */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!requireAuth(req, resp)) return;

        String idParam = req.getParameter("id");
        if (isBlank(idParam)) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "id parameter is required.");
            return;
        }

        try {
            int id = Integer.parseInt(idParam);
            boolean deleted = guestDAO.deleteGuest(id);
            JsonObject result = new JsonObject();
            result.addProperty("success", deleted);
            result.addProperty("message", deleted ? "Guest deleted." : "Guest not found.");
            writeSuccess(resp, result);
        } catch (NumberFormatException e) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid guest ID.");
        }
    }

}

