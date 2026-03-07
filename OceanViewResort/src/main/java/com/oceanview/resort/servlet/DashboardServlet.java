package com.oceanview.resort.servlet;

import com.google.gson.JsonObject;
import com.oceanview.resort.util.DatabaseConnection;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Returns dashboard metrics as JSON.
 * GET /api/dashboard -> { availableRooms, activeGuests, todayCheckins, totalRooms }
 * Strictly uses javax.servlet.* (Tomcat 8.5 compatible).
 */
@WebServlet(name = "DashboardServlet", urlPatterns = "/api/dashboard")
public class DashboardServlet extends BaseApiServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!requireAuth(req, resp)) return;

        int availableRooms = 0;
        int occupiedRooms  = 0;
        int activeGuests   = 0;
        int todayCheckins  = 0;
        int totalRooms     = 0;

        String sql = "SELECT "
                + "(SELECT COUNT(*) FROM rooms WHERE status = 'Available')        AS available_rooms, "
                + "(SELECT COUNT(*) FROM rooms WHERE status = 'Occupied')         AS occupied_rooms, "
                + "(SELECT COUNT(*) FROM reservations WHERE status = 'Confirmed') AS active_guests, "
                + "(SELECT COUNT(*) FROM reservations WHERE check_in_date = CURDATE()) AS today_checkins, "
                + "(SELECT COUNT(*) FROM rooms)                                    AS total_rooms";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                availableRooms = rs.getInt("available_rooms");
                occupiedRooms  = rs.getInt("occupied_rooms");
                activeGuests   = rs.getInt("active_guests");
                todayCheckins  = rs.getInt("today_checkins");
                totalRooms     = rs.getInt("total_rooms");
            }

        } catch (Exception e) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Failed to load dashboard metrics: " + e.getMessage());
            return;
        }

        JsonObject data = new JsonObject();
        data.addProperty("availableRooms", availableRooms);
        data.addProperty("occupiedRooms",  occupiedRooms);
        data.addProperty("activeGuests",   activeGuests);
        data.addProperty("todayCheckins",  todayCheckins);
        data.addProperty("totalRooms",     totalRooms);
        writeSuccess(resp, data);
    }
}

