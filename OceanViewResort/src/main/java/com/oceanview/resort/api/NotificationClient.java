package com.oceanview.resort.api;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Task B.i: Web Services Integration.
 * This class handles external communication with a mock notification service.
 */
public class NotificationClient {

    // Using Postman Echo API to simulate a real REST API response
    private static final String MOCK_API_URL = "https://postman-echo.com/post";

    public boolean sendBookingConfirmation(String guestEmail, String reservationNum) {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            // JSON Payload
            String jsonBody = "{"
                    + "\"to\":\"" + guestEmail + "\","
                    + "\"subject\":\"Booking Confirmed\","
                    + "\"body\":\"Your reservation #" + reservationNum + " at Ocean View Resort is successful.\""
                    + "}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(MOCK_API_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            // Send request and wait for response
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Check if status is 200 OK
            if (response.statusCode() == 200) {
                System.out.println("--> [API SUCCESS] Notification sent to: " + guestEmail);
                return true;
            } else {
                System.err.println("--> [API ERROR] Server returned: " + response.statusCode());
                return false;
            }

        } catch (Exception e) {
            System.err.println("--> [API EXCEPTION] Connection failed: " + e.getMessage());
            return false;
        }
    }
}