package com.oceanview.resort.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.oceanview.resort.dao.UserDAO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Handles user authentication (Login / Logout).
 * Maps POST /login   -> validate credentials against `users` table.
 * Maps POST /logout  -> invalidate the HTTP session.
 * Strictly uses javax.servlet.* (Tomcat 8.5 compatible).
 */
@WebServlet(name = "LoginServlet", urlPatterns = {"/login", "/logout"})
public class LoginServlet extends HttpServlet {

    private static final Gson GSON = new Gson();
    private final UserDAO userDAO = new UserDAO();

    /** POST /login – authenticate user */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        JsonObject result = new JsonObject();

        String servletPath = req.getServletPath();

        if ("/logout".equals(servletPath)) {
            HttpSession session = req.getSession(false);
            if (session != null) session.invalidate();
            result.addProperty("success", true);
            result.addProperty("message", "Logged out successfully");
            out.print(GSON.toJson(result));
            return;
        }

        // /login path
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        if (username == null || username.trim().isEmpty()
                || password == null || password.trim().isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            result.addProperty("success", false);
            result.addProperty("message", "Username and password are required.");
            out.print(GSON.toJson(result));
            return;
        }

        com.oceanview.resort.model.UserLogin user =
                userDAO.validateLogin(username.trim(), password.trim());

        if (user != null) {
            HttpSession session = req.getSession(true);
            session.setAttribute("username", user.getUsername());
            session.setAttribute("role",     user.getRole());
            session.setMaxInactiveInterval(1800); // 30 minutes
            result.addProperty("success",  true);
            result.addProperty("message",  "Login successful");
            result.addProperty("username", user.getUsername());
            result.addProperty("role",     user.getRole() != null ? user.getRole() : "Staff");
        } else {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            result.addProperty("success", false);
            result.addProperty("message", "Invalid username or password.");
        }

        out.print(GSON.toJson(result));
    }

    /** GET /logout – redirect backwards compatibility */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if ("/logout".equals(req.getServletPath())) {
            HttpSession session = req.getSession(false);
            if (session != null) session.invalidate();
        }
        resp.sendRedirect(req.getContextPath() + "/index.html");
    }
}
