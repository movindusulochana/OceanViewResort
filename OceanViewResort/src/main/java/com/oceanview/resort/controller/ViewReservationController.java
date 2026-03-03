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
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
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

public class ViewReservationController implements Initializable {

    // FXML Table Elements
    @FXML private VBox reservationsTableContainer;
    @FXML private VBox emptyStateBox;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterRoomTypeComboBox;
    @FXML private ComboBox<String> filterStatusComboBox;
    @FXML private Label resultsCountLabel;

    // FXML Detail Panel Elements
    @FXML private VBox detailPanel;
    @FXML private Label detailResNoLabel, detailGuestNameLabel, detailAddressLabel, detailContactLabel;
    @FXML private Label detailRoomTypeLabel, detailCheckInLabel, detailCheckOutLabel;
    @FXML private Label detailNightsLabel, detailStatusLabel, detailTotalLabel;

    // FXML Header Elements
    @FXML private Label currentDateLabel;

    private final ReservationDAO reservationDAO = new ReservationDAO();
    private final GuestDAO guestDAO = new GuestDAO();
    private final RoomDAO roomDAO = new RoomDAO();
    private List<Reservation> allReservations;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Set header date
        if (currentDateLabel != null) {
            currentDateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
        }

        // Setup Dropdowns
        filterRoomTypeComboBox.getItems().addAll("All Types", "Single", "Double", "Suite", "Deluxe");
        filterStatusComboBox.getItems().addAll("All Statuses", "Confirmed", "Checked-In", "Checked-Out");

        refreshTable();
    }

    /**
     * Loads data from DB and renders rows
     */
    private void refreshTable() {
        allReservations = reservationDAO.getAllReservations();
        renderReservationRows(allReservations);
    }

    private void renderReservationRows(List<Reservation> list) {
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
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPrefHeight(48);
        row.setStyle("-fx-background-color: white; -fx-border-color: #f0f4f8; -fx-border-width: 0 0 1 0; -fx-cursor: hand;");
        row.setPadding(new javafx.geometry.Insets(0, 12, 0, 12));

        row.getChildren().addAll(
            createLabel("#" + res.getReservationNumber(), 90),
            createLabel("ID: " + res.getGuestId(), 160),
            createLabel("Room " + res.getRoomNumber(), 130),
            createLabel(res.getCheckInDate().toString(), 110),
            createLabel(res.getCheckOutDate().toString(), 110),
            createLabel(res.getStatus(), 110)
        );

        Button viewBtn = new Button("View Detail");
        viewBtn.setOnAction(e -> showReservationDetails(res));
        row.getChildren().add(viewBtn);

        return row;
    }

    private Label createLabel(String text, double width) {
        Label l = new Label(text);
        l.setPrefWidth(width);
        l.setStyle("-fx-font-size: 12px; -fx-text-fill: #555555;");
        return l;
    }

    /**
     * Task 3: Shows full details when a row is clicked
     */
    private void showReservationDetails(Reservation res) {
        detailPanel.setVisible(true);
        detailPanel.setManaged(true);

        Guest g = guestDAO.getGuestById(res.getGuestId());
        Room rm = roomDAO.getRoomByNumber(res.getRoomNumber());

        detailResNoLabel.setText("RES-" + String.format("%04d", res.getReservationNumber()));
        detailGuestNameLabel.setText(g != null ? g.getName() : "Unknown");
        detailAddressLabel.setText(g != null ? g.getAddress() : "--");
        detailContactLabel.setText(g != null ? g.getContactNumber() : "--");

        detailRoomTypeLabel.setText(rm != null ? rm.getRoomType() : "Standard");
        detailCheckInLabel.setText(res.getCheckInDate().toString());
        detailCheckOutLabel.setText(res.getCheckOutDate().toString());

        long nights = ChronoUnit.DAYS.between(res.getCheckInDate(), res.getCheckOutDate());
        detailNightsLabel.setText(String.valueOf(nights));
        detailStatusLabel.setText(res.getStatus());
        detailTotalLabel.setText("Rs. " + String.format("%,.2f", res.getTotalBill()));
    }

    @FXML private void handleSearch(ActionEvent event) {
        String q = searchField.getText().toLowerCase();
        List<Reservation> filtered = allReservations.stream()
                .filter(r -> String.valueOf(r.getReservationNumber()).contains(q) || 
                             String.valueOf(r.getGuestId()).contains(q))
                .collect(Collectors.toList());
        renderReservationRows(filtered);
    }

    @FXML private void handleClearSearch(ActionEvent event) {
        searchField.clear();
        refreshTable();
    }

    @FXML private void handleCloseDetailPanel() { detailPanel.setVisible(false); detailPanel.setManaged(false); }

    // Navigation Methods
    @FXML private void goToDashboard(ActionEvent event) { navigate(event, "/view/Dashboard.fxml"); }
    @FXML private void showNewReservation(ActionEvent event) { navigate(event, "/view/NewReservation.fxml"); }
    @FXML private void showViewReservations(ActionEvent event) { refreshTable(); }
    @FXML private void showBilling(ActionEvent event) { navigate(event, "/view/billing.fxml"); }
    @FXML private void showGuests(ActionEvent event) { navigate(event, "/view/guest_management.fxml"); }
    @FXML private void showHelp() { System.out.println("Guidelines opened."); }
    @FXML private void handleLogout(ActionEvent event) { navigate(event, "/view/Login.fxml"); }
    @FXML private void handleDetailCalculateBill(ActionEvent event) { navigate(event, "/view/billing.fxml"); }
    
    // Unused in current logic but present in FXML
    @FXML private void handlePrevPage() {}
    @FXML private void handleNextPage() {}
    @FXML private void handleDetailEdit() {}
    @FXML private void handleDetailDelete() {}

    private void navigate(ActionEvent event, String path) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(path));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            System.err.println("Error loading: " + path);
            e.printStackTrace();
        }
    }
}