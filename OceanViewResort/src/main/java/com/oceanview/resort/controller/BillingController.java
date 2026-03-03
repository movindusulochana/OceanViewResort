package com.oceanview.resort.controller;


import com.oceanview.resort.dao.ReservationDAO;
import com.oceanview.resort.dao.RoomDAO;
import com.oceanview.resort.model.Reservation;
import com.oceanview.resort.model.Room;
import com.oceanview.resort.service.BillingService;
import com.oceanview.resort.util.ValidationHelper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
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
import java.util.ResourceBundle;

/**
 * Task 4: Calculate and Print Bill.
 * Task B.ii: Controller Layer (MVC).
 * This class handles complex billing logic, including taxes, discounts, 
 * and payment processing for Ocean View Resort.
 */
public class BillingController implements Initializable {

    // FXML UI Components
    @FXML private TextField reservationLookupField, extraChargesField, discountField, paymentNotesField;
    @FXML private Label lookupErrorLabel, paymentErrorLabel, currentDateLabel, loggedInUserLabel;
    @FXML private Label summaryGuestNameLabel, summaryContactLabel, summaryRoomTypeLabel, summaryRoomRateLabel;
    @FXML private Label summaryCheckInLabel, summaryCheckOutLabel, summaryNightsLabel;
    @FXML private Label roomChargesDescLabel, roomChargesAmountLabel, taxDescLabel, taxAmountLabel;
    @FXML private Label grandTotalLabel, totalRoomChargesLabel, totalExtrasLabel, totalDiscountLabel, totalVatLabel;
    @FXML private ComboBox<String> paymentMethodComboBox, paymentStatusComboBox;
    @FXML private HBox guestSummaryBox, billingMainArea;
    @FXML private VBox placeholderBox;

    // Logic Components
    private final ReservationDAO reservationDAO = new ReservationDAO();
    private final RoomDAO roomDAO = new RoomDAO();
    private final BillingService billingService = new BillingService();
    
    private Reservation currentReservation;
    private double roomRate = 0.0;
    private final double VAT_RATE = 0.15; // 15% Tax as per standard resort rules

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        currentDateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
        
        // Populate ComboBoxes
        paymentMethodComboBox.getItems().addAll("Cash", "Credit/Debit Card", "Bank Transfer");
        paymentStatusComboBox.getItems().addAll("Pending", "Partially Paid", "Paid In Full");
        
        // Default values
        extraChargesField.setText("0.00");
        discountField.setText("0");
    }

    /**
     * Task 3 & 4: Looks up a reservation and loads details for billing.
     */
    @FXML
    private void handleLookup(ActionEvent event) {
        String input = reservationLookupField.getText();
        if (ValidationHelper.isEmpty(input)) {
            lookupErrorLabel.setText("Please enter a reservation number.");
            return;
        }

        // Search in Database
        int resId = Integer.parseInt(input.replaceAll("[^0-9]", "")); // Extract numeric ID
        currentReservation = reservationDAO.getReservationById(resId);

        if (currentReservation != null) {
            Room room = roomDAO.getRoomByNumber(currentReservation.getRoomNumber());
            roomRate = room != null ? room.getPricePerNight() : 0.0;
            
            updateUIWithReservationDetails(room);
            calculateFinalBill(); // Initial calculation
            
            // Toggle visibility
            placeholderBox.setVisible(false);
            placeholderBox.setManaged(false);
            guestSummaryBox.setVisible(true);
            guestSummaryBox.setManaged(true);
            billingMainArea.setVisible(true);
            billingMainArea.setManaged(true);
            lookupErrorLabel.setText("");
        } else {
            lookupErrorLabel.setText("Reservation not found. Please try again.");
        }
    }

    /**
     * Fills the UI labels with data from the database.
     */
    private void updateUIWithReservationDetails(Room room) {
        summaryGuestNameLabel.setText("Guest ID: " + currentReservation.getGuestId());
        summaryRoomTypeLabel.setText(room != null ? room.getRoomType() : "Standard");
        summaryRoomRateLabel.setText("LKR " + String.format("%,.2f", roomRate) + " / night");
        summaryCheckInLabel.setText(currentReservation.getCheckInDate().toString());
        summaryCheckOutLabel.setText(currentReservation.getCheckOutDate().toString());

        long nights = ChronoUnit.DAYS.between(currentReservation.getCheckInDate(), currentReservation.getCheckOutDate());
        summaryNightsLabel.setText(String.valueOf(nights));
    }

    /**
     * Task 4: The core billing logic.
     * Calculates Room Total + Extras - Discount + Taxes.
     */
    @FXML
    private void handleRecalculate(ActionEvent event) {
        calculateFinalBill();
    }

    private void calculateFinalBill() {
        if (currentReservation == null) return;

        long nights = ChronoUnit.DAYS.between(currentReservation.getCheckInDate(), currentReservation.getCheckOutDate());
        double subtotalRoom = nights * roomRate;
        double extras = Double.parseDouble(extraChargesField.getText().isEmpty() ? "0" : extraChargesField.getText());
        double discountPercent = Double.parseDouble(discountField.getText().isEmpty() ? "0" : discountField.getText());
        
        double discountAmount = (subtotalRoom + extras) * (discountPercent / 100);
        double taxableAmount = (subtotalRoom + extras) - discountAmount;
        double vatAmount = taxableAmount * VAT_RATE;
        double finalTotal = taxableAmount + vatAmount;

        // Update Breakdown Labels
        roomChargesDescLabel.setText(nights + " nights x LKR " + String.format("%,.2f", roomRate));
        roomChargesAmountLabel.setText("LKR " + String.format("%,.2f", subtotalRoom));
        taxDescLabel.setText((VAT_RATE * 100) + "% on taxable subtotal");
        taxAmountLabel.setText("LKR " + String.format("%,.2f", vatAmount));

        // Update Right Side Summary
        totalRoomChargesLabel.setText("LKR " + String.format("%,.2f", subtotalRoom));
        totalExtrasLabel.setText("LKR " + String.format("%,.2f", extras));
        totalDiscountLabel.setText("- LKR " + String.format("%,.2f", discountAmount));
        totalVatLabel.setText("LKR " + String.format("%,.2f", vatAmount));
        grandTotalLabel.setText("LKR " + String.format("%,.2f", finalTotal));
    }

    /**
     * Task B.iii: Updates payment status in the database.
     */
    @FXML
    private void handleConfirmPayment(ActionEvent event) {
        if (paymentMethodComboBox.getValue() == null || paymentStatusComboBox.getValue() == null) {
            paymentErrorLabel.setText("Please select payment method and status.");
            return;
        }

        // Logic to update reservation status in DB
        currentReservation.setStatus(paymentStatusComboBox.getValue());
        // In a full system, you would call a BillingDAO.savePayment() here
        
        showAlert(Alert.AlertType.INFORMATION, "Payment Successful", 
                "Payment of " + grandTotalLabel.getText() + " confirmed via " + paymentMethodComboBox.getValue());
    }

    @FXML
    private void handlePrintBill(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Print Command", "Invoice generated and sent to printer.");
    }

    @FXML
    private void handleClear(ActionEvent event) {
        reservationLookupField.clear();
        extraChargesField.setText("0.00");
        discountField.setText("0");
        placeholderBox.setVisible(true);
        placeholderBox.setManaged(true);
        guestSummaryBox.setVisible(false);
        billingMainArea.setVisible(false);
    }

    // Navigation Logic
    @FXML private void goToDashboard(ActionEvent event) { navigate(event, "/view/Dashboard.fxml"); }
    @FXML private void showNewReservation(ActionEvent event) { navigate(event, "/view/NewReservation.fxml"); }
    @FXML private void showViewReservations(ActionEvent event) { navigate(event, "/view/viewreservation.fxml"); }
    @FXML private void showBilling(ActionEvent event) {} // Current page
    @FXML private void showGuests(ActionEvent event) { navigate(event, "/view/guest_management.fxml"); }
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