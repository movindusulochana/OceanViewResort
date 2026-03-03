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
 * This class handles the logic for Task 2: Add New Reservation.
 * It integrates Guest registration, Room assignment, and Billing triggers.
 */
public class NewReservationController implements Initializable {

    // FXML Form Fields
    @FXML private TextField guestNameField, addressField, contactNumberField, numberOfGuestsField, specialRequestsField, reservationNumberField;
    @FXML private ComboBox<String> roomTypeComboBox;
    @FXML private DatePicker checkInDatePicker, checkOutDatePicker;
    
    // Preview Labels
    @FXML private Label nightsPreviewLabel, ratePreviewLabel, estimatedTotalLabel, errorLabel, currentDateLabel;

    // DAOs and Services
    private final GuestDAO guestDAO = new GuestDAO();
    private final RoomDAO roomDAO = new RoomDAO();
    private final ReservationService reservationService = new ReservationService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        currentDateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
        
        // Populate Room Types (Requirement: Display room types and rates)
        roomTypeComboBox.getItems().addAll("Single Standard", "Double Deluxe", "Family Suite", "Ocean View Suite");
        
        // Listeners for Real-time Cost Preview
        setupPreviewListeners();
    }

    /**
     * Task 2: Add New Reservation logic.
     * Step 1: Validate. Step 2: Register Guest. Step 3: Find Room. Step 4: Save Booking.
     */
    @FXML
    private void handleSubmitReservation(ActionEvent event) {
        errorLabel.setText(""); // Reset errors

        // 1. Validation (Task B)
        if (!isInputValid()) return;

        try {
            // 2. Register/Find Guest
            Guest newGuest = new Guest(guestNameField.getText(), addressField.getText(), contactNumberField.getText());
            int guestId = guestDAO.addGuest(newGuest);

            // 3. Find an Available Room of selected type
            List<Room> available = roomDAO.getAvailableRooms();
            Room selectedRoom = available.stream()
                    .filter(r -> r.getRoomType().equalsIgnoreCase(roomTypeComboBox.getValue()))
                    .findFirst()
                    .orElse(null);

            if (selectedRoom == null) {
                errorLabel.setText("Sorry, no " + roomTypeComboBox.getValue() + " rooms are currently available.");
                return;
            }

            // 4. Create Reservation Object
            Reservation reservation = new Reservation(
                    guestId, 
                    selectedRoom.getRoomNumber(), 
                    checkInDatePicker.getValue(), 
                    checkOutDatePicker.getValue()
            );

            // 5. Save through Service (Triggers Billing and Web Service Notifications - Task B.i)
            boolean success = reservationService.createNewBooking(reservation, "guest@example.com");

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Reservation confirmed and Bill generated!");
                handleClearForm(null);
            }

        } catch (SQLException e) {
            errorLabel.setText("Database error: " + e.getMessage());
        }
    }

  private void setupPreviewListeners() {
        // FIXED: Replaced invalid method with standard JavaFX property listeners
        roomTypeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> calculatePreview());
        checkInDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> calculatePreview());
        checkOutDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> calculatePreview());
    }
    private void calculatePreview() {
        if (checkInDatePicker.getValue() != null && checkOutDatePicker.getValue() != null) {
            long nights = ChronoUnit.DAYS.between(checkInDatePicker.getValue(), checkOutDatePicker.getValue());
            if (nights > 0) {
                nightsPreviewLabel.setText(String.valueOf(nights));
                // Simulated rates based on room type
                double rate = getRate(roomTypeComboBox.getValue());
                ratePreviewLabel.setText("Rs. " + rate);
                estimatedTotalLabel.setText("Rs. " + (rate * nights));
            }
        }
    }

    private double getRate(String type) {
        if (type == null) return 0.0;
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
            errorLabel.setText("Please fill in all mandatory fields (*)");
            return false;
        }
        if (!ValidationHelper.isValidStayDates(checkInDatePicker.getValue(), checkOutDatePicker.getValue())) {
            errorLabel.setText("Check-out date must be after check-in date.");
            return false;
        }
        return true;
    }

    @FXML private void handleClearForm(ActionEvent event) {
        guestNameField.clear(); addressField.clear(); contactNumberField.clear();
        roomTypeComboBox.getSelectionModel().clearSelection();
        checkInDatePicker.setValue(null); checkOutDatePicker.setValue(null);
        nightsPreviewLabel.setText("—"); estimatedTotalLabel.setText("—");
    }

    // Navigation Methods
    @FXML private void goToDashboard(ActionEvent event) { navigate(event, "/view/Dashboard.fxml"); }
    @FXML private void showViewReservations(ActionEvent event) { navigate(event, "/view/viewreservation.fxml"); }
    @FXML private void showBilling(ActionEvent event) { navigate(event, "/view/billing.fxml"); }
    @FXML private void showGuests(ActionEvent event) { navigate(event, "/view/guest_management.fxml"); }
    @FXML private void showNewReservation(ActionEvent event) {} // Already here
    @FXML private void showHelp(ActionEvent event) { System.out.println("Displaying Help..."); }
    @FXML private void handleLogout(ActionEvent event) { navigate(event, "/view/Login.fxml"); }

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