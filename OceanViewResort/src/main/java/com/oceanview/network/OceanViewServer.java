package com.oceanview.network;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OceanViewServer {

    private static final Logger LOGGER = Logger.getLogger(OceanViewServer.class.getName());
    private static final int PORT = 8080;
    private static final ObjectMapper MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.setExecutor(Executors.newFixedThreadPool(10));

        registerHandlers(server);

        server.start();
        System.out.println("==================================================");
        System.out.println("🚀 OCEAN VIEW WEB SERVICE STARTED ON PORT 8080");
        System.out.println("👉 FULL UI WEBSITE : http://localhost:8080/");
        System.out.println("==================================================");
    }

    private static void registerHandlers(HttpServer server) {
        // 1. Beautiful Web UIs
        server.createContext("/", new LandingPageHandler());          // මුල් පිටුව (Animated)
        server.createContext("/portal", new DashboardHandler());      // Reservations UI
        server.createContext("/guests", new GuestsUIHandler());       // Guests UI (Fixed for your DB!)
        server.createContext("/rooms", new RoomsUIHandler());         // Rooms UI (Fixed for your DB!)

        // 2. JSON APIs
        server.createContext("/api/rooms", new ApiRoomsHandler());
        server.createContext("/api/auth/login", new ApiLoginHandler());
        server.createContext("/health", exchange -> {
            addCorsHeaders(exchange);
            sendJson(exchange, 200, Map.of("status", "UP", "service", "OceanView"));
        });
    }

    // ==================================================================================
    //  1. LANDING PAGE
    // ==================================================================================
    static class LandingPageHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String html = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
              <meta charset="UTF-8"/>
              <title>Ocean View Resort</title>
              <style>
                body { margin: 0; font-family: sans-serif; background: #0d2b3e; color: #f5f0e8; overflow: hidden; display: flex; flex-direction: column; align-items: center; justify-content: center; height: 100vh; text-align: center; }
                .bg { position: fixed; inset: 0; z-index: -1; background: linear-gradient(160deg, #041c2c 0%, #0d2b3e 45%, #134b6e 100%); }
                .wave { position: absolute; bottom: 0; left: 0; width: 200%; height: 180px; background: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 1440 180'%3E%3Cpath fill='%231a5276' fill-opacity='0.4' d='M0,96L60,106.7C120,117,240,139,360,133.3C480,128,600,96,720,85.3C840,75,960,85,1080,96C1200,107,1320,117,1380,122.7L1440,128L1440,180L1380,180C1320,180,1200,180,1080,180C960,180,840,180,720,180C600,180,480,180,360,180C240,180,120,180,60,180L0,180Z'/%3E%3C/svg%3E") repeat-x bottom; animation: waveMove 8s linear infinite; }
                .wave2 { position: absolute; bottom: 0; left: 0; width: 200%; height: 140px; background: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 1440 140'%3E%3Cpath fill='%230d2b3e' fill-opacity='0.6' d='M0,64L48,74.7C96,85,192,107,288,101.3C384,96,480,64,576,58.7C672,53,768,75,864,80C960,85,1056,75,1152,64C1248,53,1344,43,1392,37.3L1440,32L1440,140L1392,140C1344,140,1248,140,1152,140C1056,140,960,140,864,140C768,140,672,140,576,140C480,140,384,140,288,140C192,140,96,140,48,140L0,140Z'/%3E%3C/svg%3E") repeat-x bottom; animation: waveMove 12s linear infinite reverse; }
                @keyframes waveMove { from { transform: translateX(0); } to { transform: translateX(-50%); } }
                .star { position: absolute; border-radius: 50%; background: white; animation: twinkle infinite; }
                @keyframes twinkle { 0%,100% { opacity: 0; } 50% { opacity: 0.8; } }
                h1 { font-size: 5rem; margin: 0; font-family: serif; font-weight: normal; }
                .subtitle { color: #a8c5da; letter-spacing: 2px; font-size: 1.2rem; margin-bottom: 30px; }
                .btn { display: inline-block; padding: 15px 35px; background: #c9a84c; color: #0d2b3e; text-decoration: none; border-radius: 5px; font-weight: bold; letter-spacing: 1px; transition: 0.3s; }
                .btn:hover { background: #d4af5a; transform: translateY(-2px); box-shadow: 0 5px 15px rgba(201,168,76,0.4); }
                .pill { display: inline-block; padding: 5px 15px; background: rgba(39, 174, 96, 0.2); border: 1px solid #27ae60; color: #27ae60; border-radius: 20px; font-size: 12px; margin-bottom: 20px; font-weight: bold; }
              </style>
            </head>
            <body>
              <div class="bg">
                <div class="wave"></div><div class="wave2"></div>
                <script>
                  for(let i=0; i<80; i++){
                    let s = document.createElement('div'); s.className='star';
                    s.style.width=s.style.height=Math.random()*3+'px';
                    s.style.left=Math.random()*100+'%'; s.style.top=Math.random()*70+'%';
                    s.style.animationDuration=(Math.random()*3+2)+'s';
                    document.querySelector('.bg').appendChild(s);
                  }
                </script>
              </div>
              <div class="pill">● ALL SYSTEMS OPERATIONAL</div>
              <h1>Ocean View</h1>
              <p class="subtitle">LUXURY HOSPITALITY DISTRIBUTED SYSTEM</p>
              <a href="/portal" class="btn">OPEN ADMIN DASHBOARD</a>
            </body>
            </html>
            """;
            sendResponse(t, html, "text/html; charset=UTF-8");
        }
    }

    // ==================================================================================
    //  2. RESERVATIONS DASHBOARD
    // ==================================================================================
    static class DashboardHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            StringBuilder rows = new StringBuilder();
            int totalReservations = 0;
            double totalRevenue = 0;

            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/resort", "root", "")) {
                String sql = "SELECT r.reservation_number, g.name, r.room_number, r.check_in_date, r.total_bill FROM reservations r JOIN guests g ON r.guest_id = g.guest_id ORDER BY r.reservation_number DESC";
                ResultSet rs = conn.createStatement().executeQuery(sql);
                while (rs.next()) {
                    totalReservations++;
                    totalRevenue += rs.getDouble("total_bill");
                    String initials = getInitials(rs.getString("name"));
                    rows.append("<tr>")
                        .append("<td><span style='color:#7a8b98; font-weight:bold;'>#").append(rs.getInt("reservation_number")).append("</span></td>")
                        .append("<td><div style='display:flex; align-items:center;'><span style='background:linear-gradient(135deg, #1a5276, #4a90b8); color:white; width:30px; height:30px; border-radius:50%; display:inline-flex; justify-content:center; align-items:center; font-size:12px; margin-right:10px; font-weight:bold;'>").append(initials).append("</span><b>").append(rs.getString("name")).append("</b></div></td>")
                        .append("<td><span style='background:#e3f2fd; color:#1a5276; padding:5px 10px; border-radius:20px; font-weight:bold; font-size:12px;'>").append(rs.getInt("room_number")).append("</span></td>")
                        .append("<td style='color:#666;'>").append(rs.getDate("check_in_date")).append("</td>")
                        .append("<td style='color:#0d2b3e; font-weight:bold; font-size:16px;'>Rs. ").append(String.format("%,.2f", rs.getDouble("total_bill"))).append("</td>")
                        .append("</tr>");
                }
            } catch (Exception e) { rows.append("<tr><td colspan='5' style='color:red;'>DB Error: ").append(e.getMessage()).append("</td></tr>"); }

            String stats = "<div class='stats-bar'>" +
                           "<div class='stat-card'><span class='stat-label'>Total Reservations</span><span class='stat-value'>" + totalReservations + "</span></div>" +
                           "<div class='stat-card'><span class='stat-label'>Total Revenue</span><span class='stat-value'>Rs. " + String.format("%,.0f", totalRevenue) + "</span></div>" +
                           "<div class='stat-card'><span class='stat-label'>System Status</span><span class='stat-value' style='color:#27ae60;'><span class='dot'></span>Online</span></div>" +
                           "</div>";
                           
            String tableHtml = "<table><tr><th>Reservation</th><th>Guest</th><th>Room</th><th>Check-In</th><th>Bill</th></tr>" + rows.toString() + "</table>";
            sendResponse(t, getSidebarLayout("Active Reservations", "portal", stats + "<div class='table-card'><div class='table-header'>Recent Bookings</div>" + tableHtml + "</div>"), "text/html; charset=UTF-8");
        }
        private String getInitials(String name) {
            if (name == null || name.isBlank()) return "?";
            String[] p = name.trim().split("\\s+");
            return p.length >= 2 ? (p[0].substring(0,1) + p[1].substring(0,1)).toUpperCase() : name.substring(0,1).toUpperCase();
        }
    }

    // ==================================================================================
    //  3. GUESTS UI (අකුරටම ඔයාගේ DB එකට ගැලපෙනවා)
    // ==================================================================================
    static class GuestsUIHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            StringBuilder rows = new StringBuilder();
            
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/resort", "root", "")) {
                // ඔයාගේ Database එකේ තියෙන columns විතරයි මෙතන ගන්නේ
                ResultSet rs = conn.createStatement().executeQuery("SELECT guest_id, name, address, contact_number FROM guests");
                
                while (rs.next()) {
                    rows.append("<tr>")
                        .append("<td><span style='color:#7a8b98; font-weight:bold;'>#G").append(rs.getInt("guest_id")).append("</span></td>")
                        .append("<td><b>").append(rs.getString("name")).append("</b></td>")
                        .append("<td>").append(rs.getString("address") != null ? rs.getString("address") : "-").append("</td>")
                        .append("<td>").append(rs.getString("contact_number")).append("</td>")
                        .append("</tr>");
                }
            } catch (Exception e) { rows.append("<tr><td colspan='4' style='color:red;'>DB Error: ").append(e.getMessage()).append("</td></tr>"); }

            String tableHtml = "<div class='table-card'><div class='table-header'>Registered Guests Database</div><div style='overflow-x:auto;'><table>" +
                               "<thead><tr><th>Guest ID</th><th>Full Name</th><th>Address</th><th>Contact Number</th></tr></thead>" +
                               "<tbody>" + rows.toString() + "</tbody></table></div></div>";
            sendResponse(t, getSidebarLayout("Guests Management", "guests", tableHtml), "text/html; charset=UTF-8");
        }
    }

    // ==================================================================================
    //  4. ROOMS UI (අකුරටම ඔයාගේ DB එකට ගැලපෙනවා)
    // ==================================================================================
    static class RoomsUIHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            StringBuilder rows = new StringBuilder();
            
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/resort", "root", "")) {
                // ඔයාගේ Database එකේ තියෙන columns විතරයි මෙතන ගන්නේ
                ResultSet rs = conn.createStatement().executeQuery("SELECT room_number, room_type, price_per_night, status FROM rooms");

                while (rs.next()) {
                    String status = rs.getString("status");
                    String badgeColor = "Available".equalsIgnoreCase(status) ? "#27ae60" : "#e74c3c";
                    String badgeBg = "Available".equalsIgnoreCase(status) ? "#eafaf1" : "#fdedec";
                    
                    String roomType = rs.getString("room_type");
                    if (roomType == null || roomType.trim().isEmpty()) { roomType = "Standard"; } // හිස් නම් Standard විදිහට පෙන්වනවා

                    rows.append("<tr>")
                        .append("<td><span style='background:#e3f2fd; color:#1a5276; padding:5px 10px; border-radius:20px; font-weight:bold; font-size:12px;'>").append(rs.getInt("room_number")).append("</span></td>")
                        .append("<td><b>").append(roomType).append("</b></td>")
                        .append("<td style='color:#0d2b3e; font-weight:bold;'>Rs. ").append(String.format("%,.2f", rs.getDouble("price_per_night"))).append("</td>")
                        .append("<td><span style='background:").append(badgeBg).append("; color:").append(badgeColor).append("; padding:5px 12px; border-radius:20px; font-weight:bold; font-size:12px;'>").append(status).append("</span></td>")
                        .append("</tr>");
                }
            } catch (Exception e) { rows.append("<tr><td colspan='4' style='color:red;'>DB Error: ").append(e.getMessage()).append("</td></tr>"); }

            String tableHtml = "<div class='table-card'><div class='table-header'>Room Status & Details</div><div style='overflow-x:auto;'><table>" +
                               "<thead><tr><th>Room No</th><th>Type</th><th>Price per Night</th><th>Status</th></tr></thead>" +
                               "<tbody>" + rows.toString() + "</tbody></table></div></div>";
            sendResponse(t, getSidebarLayout("Rooms Management", "rooms", tableHtml), "text/html; charset=UTF-8");
        }
    }

    // ==================================================================================
    //  SIDEBAR LAYOUT GENERATOR (පොදු රාමුව)
    // ==================================================================================
    private static String getSidebarLayout(String title, String activeMenu, String content) {
        String pAct = activeMenu.equals("portal") ? "active" : "";
        String gAct = activeMenu.equals("guests") ? "active" : "";
        String rAct = activeMenu.equals("rooms") ? "active" : "";

        return """
        <!DOCTYPE html>
        <html>
        <head>
          <meta charset="UTF-8"/>
          <title>%s</title>
          <style>
            *, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }
            body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: #f0f2f5; color: #333; display: flex; height: 100vh; overflow: hidden;}
            .sidebar { width: 260px; background: #0d2b3e; color: white; display: flex; flex-direction: column; flex-shrink: 0;}
            .logo { padding: 25px 20px; font-size: 1.5rem; font-family: serif; color: #c9a84c; border-bottom: 1px solid rgba(255,255,255,0.1); }
            .nav { padding: 20px 10px; flex: 1; }
            .nav-label { font-size: 0.65rem; letter-spacing: 2px; text-transform: uppercase; color: rgba(255,255,255,0.3); margin: 15px 0 5px 15px; }
            .nav a { display: block; color: rgba(255,255,255,0.7); text-decoration: none; padding: 12px 15px; margin-bottom: 5px; border-radius: 6px; font-size: 0.9rem; transition: 0.2s;}
            .nav a:hover { background: rgba(255,255,255,0.1); color: white; }
            .nav a.active { background: rgba(201,168,76,0.15); color: #c9a84c; font-weight: bold; border-left: 4px solid #c9a84c; }
            .main { flex: 1; display: flex; flex-direction: column; overflow: hidden; }
            .header { height: 70px; background: white; padding: 0 30px; display: flex; align-items: center; font-size: 1.2rem; font-weight: bold; color: #0d2b3e; border-bottom: 1px solid #e1e4e8; flex-shrink: 0;}
            .content { padding: 30px; overflow-y: auto; flex: 1; }
            
            /* Stats Bar */
            .stats-bar { display: flex; gap: 20px; margin-bottom: 30px; }
            .stat-card { background: white; border: 1px solid #e1e4e8; border-radius: 12px; padding: 20px; flex: 1; box-shadow: 0 2px 10px rgba(0,0,0,0.02);}
            .stat-label { font-size: 0.75rem; color: #7a8b98; text-transform: uppercase; display: block; margin-bottom: 8px; letter-spacing: 1px;}
            .stat-value { font-size: 2rem; font-weight: bold; color: #0d2b3e; font-family: serif;}
            .dot { display: inline-block; width: 10px; height: 10px; background: #27ae60; border-radius: 50%%; margin-right: 8px; animation: pulse 2s infinite;}
            @keyframes pulse { 0%% { box-shadow: 0 0 0 0 rgba(39, 174, 96, 0.4); } 70%% { box-shadow: 0 0 0 10px rgba(39, 174, 96, 0); } 100%% { box-shadow: 0 0 0 0 rgba(39, 174, 96, 0); } }
            
            /* Table */
            .table-card { background: white; border: 1px solid #e1e4e8; border-radius: 12px; overflow: hidden; box-shadow: 0 2px 10px rgba(0,0,0,0.02);}
            .table-header { padding: 20px; border-bottom: 1px solid #e1e4e8; font-weight: bold; font-size: 1.1rem; color: #0d2b3e; background: #fafbfc;}
            table { width: 100%%; border-collapse: collapse; }
            th { text-align: left; padding: 15px 20px; background: #fafbfc; color: #7a8b98; text-transform: uppercase; font-size: 0.75rem; border-bottom: 2px solid #e1e4e8; letter-spacing: 1px;}
            td { padding: 15px 20px; border-bottom: 1px solid #f0f2f5; font-size: 0.9rem; color: #333;}
            tr:hover { background: #f8f9fa; }
            tr:last-child td { border-bottom: none; }
          </style>
        </head>
        <body>
          <div class="sidebar">
            <div class="logo">🌊 Ocean View</div>
            <div class="nav">
              <div class="nav-label">Management</div>
              <a href="/portal" class="%s">📅 Reservations</a>
              <a href="/guests" class="%s">👥 Guests</a>
              <a href="/rooms" class="%s">🏨 Rooms</a>
              <div class="nav-label" style="margin-top: 30px;">System</div>
              <a href="/">🏠 Back to Home</a>
            </div>
          </div>
          <div class="main">
            <div class="header">%s</div>
            <div class="content">%s</div>
          </div>
        </body>
        </html>
        """.formatted(title, pAct, gAct, rAct, title, content);
    }

    // ==================================================================================
    //  5. JSON APIs (ඔයාගේ පරණ API ටික එහෙම්මමයි)
    // ==================================================================================
    static class ApiRoomsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            addCorsHeaders(t);
            try {
                List<Map<String, Object>> rooms = new ArrayList<>();
                try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/resort", "root", "")) {
                    ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM rooms");
                    while (rs.next()) {
                        Map<String, Object> r = new HashMap<>();
                        r.put("room_number", rs.getInt("room_number"));
                        r.put("type", rs.getString("room_type"));
                        r.put("status", rs.getString("status"));
                        rooms.add(r);
                    }
                }
                sendJson(t, 200, rooms);
            } catch (Exception e) { sendError(t, 500, e.getMessage()); }
        }
    }

    static class ApiLoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            addCorsHeaders(t);
            if ("POST".equalsIgnoreCase(t.getRequestMethod())) {
                try {
                    Map<String, String> creds = MAPPER.readValue(t.getRequestBody(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>(){});
                    boolean isValid = false;
                    try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/resort", "root", "")) {
                        PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE username=? AND password=?");
                        ps.setString(1, creds.get("username"));
                        ps.setString(2, creds.get("password"));
                        if (ps.executeQuery().next()) isValid = true;
                    }
                    if (isValid) sendJson(t, 200, Map.of("message", "Login successful"));
                    else sendError(t, 401, "Invalid credentials");
                } catch (Exception e) { sendError(t, 500, "Server Error"); }
            }
        }
    }

    // ==================================================================================
    //  HELPERS
    // ==================================================================================
    private static void sendResponse(HttpExchange t, String response, String contentType) throws IOException {
        byte[] bytes = response.getBytes("UTF-8");
        t.getResponseHeaders().set("Content-Type", contentType);
        t.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = t.getResponseBody()) { os.write(bytes); }
    }
    private static void sendJson(HttpExchange exchange, int statusCode, Object payload) {
        try {
            byte[] responseBytes = MAPPER.writeValueAsBytes(payload);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(statusCode, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) { os.write(responseBytes); }
        } catch (IOException e) { }
    }
    private static void sendError(HttpExchange exchange, int statusCode, String message) { sendJson(exchange, statusCode, Map.of("error", message)); }
    private static void addCorsHeaders(HttpExchange exchange) { exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*"); }
}