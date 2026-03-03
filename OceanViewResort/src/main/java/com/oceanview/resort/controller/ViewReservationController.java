package com.oceanview.resort.controller;

import com.oceanview.resort.dao.ReservationDAO;
import com.oceanview.resort.dao.GuestDAO;
import com.oceanview.resort.dao.RoomDAO;
import com.oceanview.resort.model.Reservation;
import com.oceanview.resort.model.Guest;
import com.oceanview.resort.model.Room;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Task 3: Display Reservation Details.
 * This controller manages the searchable list of bookings and 
 * the interactive right-side detail panel.
 */
public class ViewReservationController implements Initializable {

    // Table Container and Search
    @FXML private VBox reservationsTableContainer;
    @FXML private VBox emptyStateBox;
    @FXML private TextField searchField;
    @FXML private Label resultsCountLabel;

    // Detail Panel Components
    @FXML private VBox detailPanel;
    @FXML private Label detailResNoLabel, detailGuestNameLabel, detailAddressLabel, detailContactLabel;
    @FXML private Label detailRoomTypeLabel, detailCheckInLabel, detailCheckOutLabel;
    @FXML private Label detailNightsLabel, detailStatusLabel, detailTotalLabel, detailSpecialRequestsLabel;

    // Header Components
    @FXML private Label currentDateLabel;

    private final ReservationDAO reservationDAO = new ReservationDAO();
    private final GuestDAO guestDAO = new GuestDAO();
    private final RoomDAO roomDAO = new RoomDAO();
    
    private List<Reservation> allReservations;
    private Reservation selectedReservation;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        currentDateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
        refreshData();
    }

    /**
     * Fetches fresh data from the DB and renders the custom table rows.
     */
    private void refreshData() {
        allReservations = reservationDAO.getAllReservations();
        renderRows(allReservations);
    }

    /**
     * Dynamically generates HBox rows for the VBox table container.
     */
    private void renderRows(List<Reservation> list) {
        reservationsTableContainer.getChildren().clear();
        
        if (list.isEmpty()) {
            emptyStateBox.setVisible(true);
            emptyStateBox.setManaged(true);
            resultsCountLabel.setText("Showing 0 reservations");
            return;
        }

        emptyStateBox.setVisible(false);
        emptyStateBox.setManaged(false);

        for (Reservation res : list) {
            HBox row = createDataRow(res);
            reservationsTableContainer.getChildren().add(row);
        }
        
        resultsCountLabel.setText("Showing " + list.size() + " reservations");
    }

    private HBox createDataRow(Reservation res) {
        HBox row = new HBox();
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.setPrefHeight(50);
        row.setStyle("-fx-background-color: white; -fx-border-color: #f0f4f8; -fx-border-width: 0 0 1 0; -fx-cursor: hand;");
        row.setPadding(new javafx.geometry.Insets(0, 12, 0, 12));

        // Column Data
        Label resNo = createCell("#" + res.getReservationNumber(), 90);
        Label guest = createCell("Guest ID: " + res.getGuestId(), 160);
        Label contact = createCell("Room " + res.getRoomNumber(), 130);
        Label roomType = createCell("Check-In:", 110); // Simple placeholder
        Label checkIn = createCell(res.getCheckInDate().toString(), 100);
        Label checkOut = createCell(res.getCheckOutDate().toString(), 100);
        Label status = createCell(res.getStatus(), 90);
        
        Button viewBtn = new Button("View Details");
        viewBtn.setStyle("-fx-background-color: #e8f0fe; -fx-text-fill: #005b96; -fx-background-radius: 15; -fx-font-size: 10px; -fx-cursor: hand;");
        viewBtn.setOnAction(e -> showDetailPanel(res));

        row.getChildren().addAll(resNo, guest, contact, roomType, checkIn, checkOut, status, viewBtn);
        
        // Hover Effect
        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #f8fbff; -fx-border-color: #f0f4f8; -fx-border-width: 0 0 1 0; -fx-cursor: hand;"));
        row.setOnMouseExited(e -> row.setStyle("-fx-background-color: white; -fx-border-color: #f0f4f8; -fx-border-width: 0 0 1 0; -fx-cursor: hand;"));
        row.setOnMouseClicked(e -> showDetailPanel(res));

        return row;
    }

    private Label createCell(String text, double width) {
        Label l = new Label(text);
        l.setPrefWidth(width);
        l.setStyle("-fx-text-fill: #555555; -fx-font-size: 12px;");
        return l;
    }

    /**
     * Fills the right panel with complete data when a row is clicked.
     */
    private void showDetailPanel(Reservation res) {
        this.selectedReservation = res;
        Guest guest = guestDAO.getGuestById(res.getGuestId());
        Room room = roomDAO.getRoomByNumber(res.getRoomNumber());

        detailResNoLabel.setText("RES-" + String.format("%04d", res.getReservationNumber()));
        detailGuestNameLabel.setText(guest != null ? guest.getName() : "Unknown");
        detailAddressLabel.setText(guest != null ? guest.getAddress() : "--");
        detailContactLabel.setText(guest != null ? guest.getContactNumber() : "--");
        
        detailRoomTypeLabel.setText(room != null ? room.getRoomType() : "Standard");
        detailCheckInLabel.setText(res.getCheckInDate().toString());
        detailCheckOutLabel.setText(res.getCheckOutDate().toString());
        
        long nights = ChronoUnit.DAYS.between(res.getCheckInDate(), res.getCheckOutDate());
        detailNightsLabel.setText(String.valueOf(nights));
        detailStatusLabel.setText(res.getStatus());
        detailTotalLabel.setText("LKR " + String.format("%,.2f", res.getTotalBill()));
        
        detailPanel.setVisible(true);
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        String query = searchField.getText().toLowerCase();
        List<Reservation> filtered = allReservations.stream()
                .filter(r -> String.valueOf(r.getReservationNumber()).contains(query) || 
                             String.valueOf(r.getGuestId()).contains(query))
                .collect(Collectors.toList());
        renderRows(filtered);
    }

    @FXML
    private void handleClearSearch(ActionEvent event) {
        searchField.clear();
        renderRows(allReservations);
    }

    @FXML
    private void handleCloseDetailPanel(ActionEvent event) {
        detailPanel.setVisible(false);
    }

    @FXML
    private void handleDetailCalculateBill(ActionEvent event) {
        // Task 4: Jump to Billing with this reservation loaded
        navigate(event, "/view/billing.fxml");
    }

    @FXML
    private void handleDetailDelete(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete this reservation?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                // Logic: reservationDAO.delete(selectedReservation.getId());
                refreshData();
                detailPanel.setVisible(false);
            }
        });
    }

    @FXML private void handleDetailEdit(ActionEvent event) { System.out.println("Edit clicked"); }

    // Navigation Methods
    @FXML private void goToDashboard(ActionEvent event) { navigate(event, "/view/Dashboard.fxml"); }
    @FXML private void showNewReservation(ActionEvent event) { navigate(event, "/view/NewReservation.fxml"); }
    @FXML private void showViewReservations(ActionEvent event) { refreshData(); }
    @FXML private void showBilling(ActionEvent event) { navigate(event, "/view/billing.fxml"); }
    @FXML private void showGuests(ActionEvent event) { navigate(event, "/view/guest_management.fxml"); }
    @FXML private void showHelp(ActionEvent event) { System.out.println("Help..."); }
    @FXML private void handleLogout(ActionEvent event) { navigate(event, "/view/Login.fxml"); }

    private void navigate(ActionEvent event, String path) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(path));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) { e.printStackTrace(); }
    }

    // Pagination Stubs (For UI buttons)
    @FXML private void handlePrevPage(ActionEvent event) {}
    @FXML private void handleNextPage(ActionEvent event) {}
}