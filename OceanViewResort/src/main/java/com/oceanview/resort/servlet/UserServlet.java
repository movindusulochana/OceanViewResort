package com.oceanview.resort.servlet;

import com.google.gson.JsonObject;
import com.oceanview.resort.dao.UserDAO;
import com.oceanview.resort.model.UserLogin;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Full CRUD REST endpoint for User management.
 * Maps to the `users` table (user_id, username, password, role).
 *
 * GET    /api/users          -> list all users (password omitted)
 * GET    /api/users?id={n}   -> get user by ID  (password omitted)
 * POST   /api/users          -> create a new user
 * PUT    /api/users          -> update an existing user
 * DELETE /api/users?id={n}   -> delete a user  (cannot delete self)
 *
 * Admin-only: only sessions whose role attribute equals "Admin" may access this endpoint.
 * Strictly uses javax.servlet.* (Tomcat 8.5 compatible).
 */
@WebServlet(name = "UserServlet", urlPatterns = "/api/users")
public class UserServlet extends BaseApiServlet {

    private final UserDAO userDAO = new UserDAO();

    // ------------------------------------------------------------------
    // GET â€“ list all users or get one by ?id=
    // ------------------------------------------------------------------
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!requireAuth(req, resp)) return;

        String idParam = req.getParameter("id");
        if (idParam != null) {
            try {
                int id = Integer.parseInt(idParam.trim());
                UserLogin user = userDAO.getUserById(id);
                if (user != null) {
                    writeSuccess(resp, user);
                } else {
                    writeError(resp, HttpServletResponse.SC_NOT_FOUND,
                            "User ID " + id + " not found.");
                }
            } catch (NumberFormatException e) {
                writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid user ID.");
            }
        } else {
            List<UserLogin> users = userDAO.getAllUsers();
            writeSuccess(resp, users);
        }
    }

    // ------------------------------------------------------------------
    // POST â€“ create a new user
    // ------------------------------------------------------------------
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!requireAuth(req, resp)) return;

        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String role     = req.getParameter("role");

        if (isBlank(username) || isBlank(password)) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "username and password are required.");
            return;
        }

        UserLogin newUser = new UserLogin();
        newUser.setUsername(username.trim());
        newUser.setPassword(password.trim());
        newUser.setRole(isBlank(role) ? "Staff" : role.trim());

        int newId = userDAO.addUser(newUser);
        if (newId > 0) {
            newUser.setUserId(newId);
            newUser.setPassword("");          // do not echo password back
            JsonObject body = new JsonObject();
            body.addProperty("success",  true);
            body.addProperty("message",  "User created successfully.");
            body.addProperty("user_id",  newId);
            body.addProperty("username", newUser.getUsername());
            body.addProperty("role",     newUser.getRole());
            resp.setStatus(HttpServletResponse.SC_CREATED);
            writeSuccess(resp, body);
        } else {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Failed to create user. The username may already be taken.");
        }
    }

    // ------------------------------------------------------------------
    // PUT â€“ update an existing user
    // ------------------------------------------------------------------
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!requireAuth(req, resp)) return;

        String idParam  = req.getParameter("user_id");
        String username = req.getParameter("username");
        String password = req.getParameter("password"); // optional â€” leave blank to keep existing
        String role     = req.getParameter("role");

        if (isBlank(idParam) || isBlank(username)) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "user_id and username are required.");
            return;
        }

        int id;
        try {
            id = Integer.parseInt(idParam.trim());
        } catch (NumberFormatException e) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid user_id.");
            return;
        }

        UserLogin updated = new UserLogin();
        updated.setUserId(id);
        updated.setUsername(username.trim());
        updated.setPassword(password != null ? password.trim() : "");  // blank = keep existing
        updated.setRole(isBlank(role) ? "Staff" : role.trim());

        boolean success = userDAO.updateUser(updated);
        if (success) {
            JsonObject body = new JsonObject();
            body.addProperty("success", true);
            body.addProperty("message", "User updated successfully.");
            writeSuccess(resp, body);
        } else {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Failed to update user ID " + id + ".");
        }
    }

    // ------------------------------------------------------------------
    // DELETE â€“ remove a user
    // ------------------------------------------------------------------
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!requireAuth(req, resp)) return;

        String idParam = req.getParameter("id");
        if (isBlank(idParam)) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "id parameter required.");
            return;
        }

        int id;
        try {
            id = Integer.parseInt(idParam.trim());
        } catch (NumberFormatException e) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid user ID.");
            return;
        }

        // Prevent self-deletion: compare with logged-in username in session
        String sessionUser = (String) req.getSession(false).getAttribute("username");
        UserLogin target = userDAO.getUserById(id);
        if (target != null && target.getUsername().equalsIgnoreCase(sessionUser)) {
            writeError(resp, HttpServletResponse.SC_FORBIDDEN,
                    "Cannot delete your own account while logged in.");
            return;
        }

        boolean deleted = userDAO.deleteUser(id);
        if (deleted) {
            JsonObject body = new JsonObject();
            body.addProperty("success", true);
            body.addProperty("message", "User ID " + id + " deleted.");
            writeSuccess(resp, body);
        } else {
            writeError(resp, HttpServletResponse.SC_NOT_FOUND,
                    "User ID " + id + " not found or could not be deleted.");
        }
    }
}

