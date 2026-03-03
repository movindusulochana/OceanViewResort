package com.oceanview.resort.api;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ExchangeRateClient {

    // Using a free, public exchange rate API (No API key required for testing)
    private static final String API_URL = "https://open.er-api.com/v6/latest/USD";

    public String getExchangeRates() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                return response.body(); // Returns JSON string of exchange rates
            } else {
                return "Error fetching rates. Status Code: " + response.statusCode();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to connect to Exchange Rate API: " + e.getMessage();
        }
    }
}