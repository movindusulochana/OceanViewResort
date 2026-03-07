package com.oceanview.resort.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDate;

/**
 * Base class for all authenticated API servlets.
 * Provides:
 *   - Global exception guard: any unhandled runtime exception is caught here
 *     and returned as a JSON error instead of Tomcat's HTML 500 page.
 *     This prevents the front-end fetch from receiving HTML and throwing
 *     "Unexpected token '<'" during JSON.parse().
 *   - Session validation helper
 *   - Standardised JSON response helpers (writeSuccess / writeError)
 *
 * All child servlets strictly use javax.servlet.* (Tomcat 8.5 compatible).
 */
public abstract class BaseApiServlet extends HttpServlet {

    // HARD CONSTRAINT: Java 17's module system blocks reflective access to java.time
    // internals, so the default Gson throws InaccessibleObjectException on LocalDate.
    // A named TypeAdapter (not an inline lambda cast to JsonSerializer) is required
    // because TypeAdapter registers for BOTH serialization AND deserialization,
    // satisfying the GsonBuilder contract completely.
    private static final class LocalDateAdapter extends TypeAdapter<LocalDate> {
        @Override
        public void write(JsonWriter out, LocalDate value) throws java.io.IOException {
            if (value == null) { out.nullValue(); return; }
            out.value(value.toString());   // ISO-8601: "2024-11-15"
        }
        @Override
        public LocalDate read(JsonReader in) throws java.io.IOException {
            if (in.peek() == com.google.gson.stream.JsonToken.NULL) {
                in.nextNull(); return null;
            }
            return LocalDate.parse(in.nextString());
        }
    }

    protected static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();

    // ------------------------------------------------------------------
    // Global Exception Guard
    // Override service() so ANY uncaught exception from doGet / doPost
    // etc. is converted to a clean JSON 500 instead of Tomcat's HTML page.
    // ------------------------------------------------------------------
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            super.service(req, resp);
        } catch (Exception ex) {
            // Only write a response if nothing has been committed yet.
            if (!resp.isCommitted()) {
                writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Internal server error: " + ex.getMessage());
            }
            // Log the full stack trace so it appears in catalina.out.
            ex.printStackTrace();
        }
    }

    // ------------------------------------------------------------------
    // Authentication
    // ------------------------------------------------------------------

    /**
     * Checks whether the current request has a valid server-side session.
     * Writes a 401 JSON response automatically if not authenticated.
     * @return true if authenticated; false (and response already written) if not.
     */
    protected boolean requireAuth(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        if ("/api/login".equals(req.getServletPath())) {
            return true;
        }
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            writeError(resp, HttpServletResponse.SC_UNAUTHORIZED,
                    "Unauthorized");
            return false;
        }
        return true;
    }

    protected boolean isAuthenticated(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        return requireAuth(req, resp);
    }

    // ------------------------------------------------------------------
    // JSON Response Helpers
    // ------------------------------------------------------------------

    /**
     * Write a 200 OK JSON response using the consistent envelope:
     *   {"ok": true, "data": <payload>}
     * Every successful API response is wrapped this way so the frontend
     * can always check body.ok and read body.data without special-casing.
     */
    protected void writeSuccess(HttpServletResponse resp, Object data) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        JsonObject wrapper = new JsonObject();
        wrapper.addProperty("ok", true);
        wrapper.add("data", GSON.toJsonTree(data));
        resp.getWriter().print(GSON.toJson(wrapper));
    }

    /**
     * Write an error JSON response using the consistent envelope:
     *   {"ok": false, "message": "<message>"}
     */
    protected void writeError(HttpServletResponse resp, int status, String message)
            throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json;charset=UTF-8");
        JsonObject err = new JsonObject();
        err.addProperty("ok", false);
        err.addProperty("message", message);
        resp.getWriter().print(GSON.toJson(err));
    }

    // ------------------------------------------------------------------
    // String Utility
    // ------------------------------------------------------------------

    /** Returns true if the string is null or contains only whitespace. */
    protected boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
