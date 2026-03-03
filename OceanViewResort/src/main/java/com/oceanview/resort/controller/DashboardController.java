package com.oceanview.resort.controller;

import com.oceanview.resort.dao.ReservationDAO;
import com.oceanview.resort.dao.RoomDAO;
import com.oceanview.resort.model.Reservation;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Task B.ii: Controller Layer (MVC Architecture).
 * This class serves as the main hub of the application, displaying 
 * real-time statistics and handling navigation between modules.
 */
public class DashboardController implements Initializable {

    @FXML private Label totalReservationsLabel;
    @FXML private Label checkedInLabel;
    @FXML private Label availableRoomsLabel;
    @FXML private Label pendingBillsLabel;
    @FXML private Label currentDateLabel;
    @FXML private Label loggedInUserLabel;
    @FXML private VBox reservationsTableContainer;
    @FXML private Label noReservationsLabel;

    private final ReservationDAO reservationDAO = new ReservationDAO();
    private final RoomDAO roomDAO = new RoomDAO();

    /**
     * Called automatically when the dashboard loads.
     * Fulfills Task 3: Displaying system overview.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Set the current date in the header
        currentDateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
        
        // Load real-time data from the database
        refreshStatistics();
        loadRecentReservations();
    }

    /**
     * Fetches counts from DAOs to update the dashboard stat cards.
     */
    private void refreshStatistics() {
        List<Reservation> allRes = reservationDAO.getAllReservations();
        
        long total = allRes.size();
        long checkedIn = allRes.stream().filter(r -> "Checked-In".equals(r.getStatus())).count();
        long availableRooms = roomDAO.getAvailableRooms().size();
        long pending = allRes.stream().filter(r -> r.getTotalBill() == 0).count();

        totalReservationsLabel.setText(String.valueOf(total));
        checkedInLabel.setText(String.valueOf(checkedIn));
        availableRoomsLabel.setText(String.valueOf(availableRooms));
        pendingBillsLabel.setText(String.valueOf(pending));
    }

    /**
     * Dynamically populates the 'Recent Reservations' section.
     */
    private void loadRecentReservations() {
        List<Reservation> recent = reservationDAO.getAllReservations();
        
        if (recent.isEmpty()) {
            noReservationsLabel.setVisible(true);
        } else {
            noReservationsLabel.setVisible(false);
            reservationsTableContainer.getChildren().clear();

            // Display the last 5 reservations
            int count = 0;
            for (int i = recent.size() - 1; i >= 0 && count < 5; i--) {
                Reservation res = recent.get(i);
                addRowToTable(res);
                count++;
            }
        }
    }

    /**
     * Helper to inject a row into the FXML VBox container.
     */
    private void addRowToTable(Reservation res) {
        HBox row = new HBox();
        row.setSpacing(0);
        row.setPrefHeight(40);
        row.setStyle("-fx-padding: 0 12 0 12; -fx-alignment: CENTER_LEFT; -fx-border-color: #f0f4f8; -fx-border-width: 0 0 1 0;");

        row.getChildren().addAll(
            createTableCell("#" + res.getReservationNumber(), 90),
            createTableCell("Guest ID: " + res.getGuestId(), 160),
            createTableCell("Room " + res.getRoomNumber(), 110),
            createTableCell(res.getCheckInDate().toString(), 110),
            createTableCell(res.getCheckOutDate().toString(), 110),
            createTableCell(res.getStatus(), 90)
        );

        reservationsTableContainer.getChildren().add(row);
    }

    private Label createTableCell(String text, double width) {
        Label label = new Label(text);
        label.setPrefWidth(width);
        label.setStyle("-fx-font-size: 12; -fx-text-fill: #555555;");
        return label;
    }

    // ═══════════════════════════════════════════════════════
    //  NAVIGATION METHODS
    // ═══════════════════════════════════════════════════════

    @FXML private void showDashboard(ActionEvent event) { refreshStatistics(); loadRecentReservations(); }

    @FXML private void showNewReservation(ActionEvent event) { changeScene(event, "/view/NewReservation.fxml"); }

    @FXML private void showViewReservations(ActionEvent event) { changeScene(event, "/view/viewreservation.fxml"); }

    @FXML private void showBilling(ActionEvent event) { changeScene(event, "/view/billing.fxml"); }

    @FXML private void showGuests(ActionEvent event) { changeScene(event, "/view/guest_management.fxml"); }

    @FXML private void showHelp(ActionEvent event) { 
        // Logic for Task 5: Help Section
        System.out.println("Displaying Help Guidelines...");
    }

    @FXML private void handleLogout(ActionEvent event) { 
        // Task 6: Exit/Logout
        changeScene(event, "/view/Login.fxml"); 
    }

    private void changeScene(ActionEvent event, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading view: " + fxmlPath);
            e.printStackTrace();
        }
    }
}