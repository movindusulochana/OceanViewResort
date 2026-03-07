package com.oceanview.resort.servlet;

import com.google.gson.JsonObject;
import com.oceanview.resort.dao.RoomDAO;
import com.oceanview.resort.model.Room;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Read + Update REST endpoint for Room data.
 * Maps to `rooms` table: room_number, room_type, price_per_night, status.
 *
 * GET  /api/rooms                       -> list all rooms
 * GET  /api/rooms?available=true        -> list only 'Available' rooms
 * GET  /api/rooms?number={n}            -> get single room
 * GET  /api/rooms?stats=true            -> Quick Stats: {available, occupied, maintenance, total}
 * PUT  /api/rooms?number={n}&status={s} -> update room status ("Available" / "Occupied" / "Maintenance")
 *
 * Strictly uses javax.servlet.* (Tomcat 8.5 compatible).
 */
@WebServlet(name = "RoomServlet", urlPatterns = "/api/rooms")
public class RoomServlet extends BaseApiServlet {

    private final RoomDAO roomDAO = new RoomDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!requireAuth(req, resp)) return;

        String numberParam    = req.getParameter("number");
        String availableParam = req.getParameter("available");
        String statsParam     = req.getParameter("stats");

        if ("true".equalsIgnoreCase(statsParam)) {
            // â”€â”€ Quick Stats â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
            Map<String, Integer> stats = roomDAO.getRoomStats();
            JsonObject data = new JsonObject();
            data.addProperty("available",   stats.getOrDefault("Available",   0));
            data.addProperty("occupied",    stats.getOrDefault("Occupied",    0));
            data.addProperty("maintenance", stats.getOrDefault("Maintenance", 0));
            data.addProperty("total",       stats.getOrDefault("total",       0));
            writeSuccess(resp, data);

        } else if (numberParam != null) {
            try {
                int roomNum = Integer.parseInt(numberParam);
                Room room = roomDAO.getRoomByNumber(roomNum);
                if (room != null) {
                    writeSuccess(resp, room);
                } else {
                    writeError(resp, HttpServletResponse.SC_NOT_FOUND,
                            "Room " + roomNum + " not found.");
                }
            } catch (NumberFormatException e) {
                writeError(resp, HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid room number.");
            }

        } else if ("true".equalsIgnoreCase(availableParam)) {
            List<Room> rooms = roomDAO.getAvailableRooms();
            writeSuccess(resp, rooms);

        } else {
            List<Room> rooms = roomDAO.getAllRooms();
            writeSuccess(resp, rooms);
        }
    }

    /**
     * PUT /api/rooms?number={n}&status={s}
     * Allows dynamic room-status updates from the frontend (e.g. flag a room for Maintenance,
     * or restore it to Available without running a full checkout).
     */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!requireAuth(req, resp)) return;

        String numberParam = req.getParameter("number");
        String statusParam = req.getParameter("status");

        if (isBlank(numberParam) || isBlank(statusParam)) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "'number' and 'status' query parameters are required.");
            return;
        }

        // Whitelist of allowed status values to prevent arbitrary updates.
        String status = statusParam.trim();
        if (!status.equals("Available") && !status.equals("Occupied") && !status.equals("Maintenance")) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid status. Allowed: Available, Occupied, Maintenance.");
            return;
        }

        int roomNumber;
        try {
            roomNumber = Integer.parseInt(numberParam.trim());
        } catch (NumberFormatException e) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid room number.");
            return;
        }

        boolean updated = roomDAO.updateRoomStatus(roomNumber, status);
        if (updated) {
            JsonObject result = new JsonObject();
            result.addProperty("updated",     true);
            result.addProperty("room_number", roomNumber);
            result.addProperty("new_status",  status);
            writeSuccess(resp, result);
        } else {
            writeError(resp, HttpServletResponse.SC_NOT_FOUND,
                    "Room " + roomNumber + " not found or status unchanged.");
        }
    }
}

