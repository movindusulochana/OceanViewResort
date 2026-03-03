package com.oceanview.resort.controller;

import com.oceanview.resort.dao.GuestDAO;
import com.oceanview.resort.dao.RoomDAO;
import com.oceanview.resort.model.Guest;
import com.oceanview.resort.model.Reservation;
import com.oceanview.resort.model.Room;
import com.oceanview.resort.service.ReservationService;
import com.oceanview.resort.util.ValidationHelper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Task B.ii: Controller Layer (MVC Architecture).
 * Handles the logic for creating new reservations, guest registration, 
 * and real-time cost previews.
 */
public class NewReservationController implements Initializable {

    // FXML Form Fields
    @FXML private TextField guestNameField, addressField, contactNumberField, numberOfGuestsField, specialRequestsField, reservationNumberField;
    @FXML private ComboBox<String> roomTypeComboBox;
    @FXML private DatePicker checkInDatePicker, checkOutDatePicker;
    
    // Preview Labels
    @FXML private Label nightsPreviewLabel, ratePreviewLabel, estimatedTotalLabel, errorLabel, currentDateLabel;
    @FXML private Label loggedInUserLabel;

    // DAOs and Services
    private final GuestDAO guestDAO = new GuestDAO();
    private final RoomDAO roomDAO = new RoomDAO();
    private final ReservationService reservationService = new ReservationService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        currentDateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
        
        // Populate Room Types
        roomTypeComboBox.getItems().addAll("Single Standard", "Double Deluxe", "Family Suite", "Ocean View Suite");
        
        // Initialize Listeners for real-time cost calculation
        setupPreviewListeners();
    }

    /**
     * FIXED: Attaches proper JavaFX listeners to UI components.
     * This replaces the broken 'getSelectionListener' method.
     */
    private void setupPreviewListeners() {
        // Listen for Room Type selection changes
        roomTypeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) calculatePreview();
        });

        // Listen for Check-In date changes
        checkInDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> calculatePreview());

        // Listen for Check-Out date changes
        checkOutDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> calculatePreview());
    }

    /**
     * Calculates the duration and estimated cost of the stay.
     */
    private void calculatePreview() {
        LocalDate checkIn = checkInDatePicker.getValue();
        LocalDate checkOut = checkOutDatePicker.getValue();
        String type = roomTypeComboBox.getValue();

        if (checkIn != null && checkOut != null && type != null) {
            long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
            
            if (nights > 0) {
                double rate = getRate(type);
                double total = rate * nights;

                nightsPreviewLabel.setText(String.valueOf(nights));
                ratePreviewLabel.setText("Rs. " + String.format("%,.2f", rate));
                estimatedTotalLabel.setText("Rs. " + String.format("%,.2f", total));
                errorLabel.setText(""); 
            } else {
                errorLabel.setText("Check-out date must be after check-in date.");
                resetPreviewLabels();
            }
        }
    }

    /**
     * Handles the reservation submission process.
     */
    @FXML
    private void handleSubmitReservation(ActionEvent event) {
        errorLabel.setText(""); 

        if (!isInputValid()) return;

        try {
            // Register Guest
            Guest newGuest = new Guest(guestNameField.getText(), addressField.getText(), contactNumberField.getText());
            int guestId = guestDAO.addGuest(newGuest);

            // Find an Available Room
            List<Room> available = roomDAO.getAvailableRooms();
            Room selectedRoom = available.stream()
                    .filter(r -> r.getRoomType().equalsIgnoreCase(roomTypeComboBox.getValue()))
                    .findFirst()
                    .orElse(null);

            if (selectedRoom == null) {
                errorLabel.setText("Sorry, no " + roomTypeComboBox.getValue() + " rooms are currently available.");
                return;
            }

            // Create Reservation
            Reservation reservation = new Reservation(
                    guestId, 
                    selectedRoom.getRoomNumber(), 
                    checkInDatePicker.getValue(), 
                    checkOutDatePicker.getValue()
            );

            // Save through Service Layer
            boolean success = reservationService.createNewBooking(reservation, "guest@example.com");

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Reservation confirmed and Bill generated!");
                handleClearForm(null);
            }

        } catch (SQLException e) {
            errorLabel.setText("Database error: " + e.getMessage());
        }
    }

    private double getRate(String type) {
        return switch (type) {
            case "Double Deluxe" -> 15000.0;
            case "Family Suite" -> 25000.0;
            case "Ocean View Suite" -> 45000.0;
            default -> 8000.0;
        };
    }

    private boolean isInputValid() {
        if (ValidationHelper.isEmpty(guestNameField.getText()) || 
            ValidationHelper.isEmpty(contactNumberField.getText()) || 
            roomTypeComboBox.getValue() == null) {
            errorLabel.setText("Please fill in all required fields.");
            return false;
        }
        return ValidationHelper.isValidStayDates(checkInDatePicker.getValue(), checkOutDatePicker.getValue());
    }

    private void resetPreviewLabels() {
        nightsPreviewLabel.setText("—");
        ratePreviewLabel.setText("—");
        estimatedTotalLabel.setText("—");
    }

    @FXML 
    private void handleClearForm(ActionEvent event) {
        guestNameField.clear(); addressField.clear(); contactNumberField.clear();
        roomTypeComboBox.getSelectionModel().clearSelection();
        checkInDatePicker.setValue(null); checkOutDatePicker.setValue(null);
        resetPreviewLabels();
    }

    // Navigation Methods
    @FXML private void goToDashboard(ActionEvent event) { navigate(event, "/view/Dashboard.fxml"); }
    @FXML private void showViewReservations(ActionEvent event) { navigate(event, "/view/viewreservation.fxml"); }
    @FXML private void showBilling(ActionEvent event) { navigate(event, "/view/billing.fxml"); }
    @FXML private void showGuests(ActionEvent event) { navigate(event, "/view/guest_management.fxml"); }
    @FXML private void handleLogout(ActionEvent event) { navigate(event, "/view/Login.fxml"); }
    @FXML private void showNewReservation(ActionEvent event) {} // Refresh current page logic
    @FXML private void showHelp(ActionEvent event) { System.out.println("Displaying Help Guidelines..."); }

    private void navigate(ActionEvent event, String path) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(path));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}