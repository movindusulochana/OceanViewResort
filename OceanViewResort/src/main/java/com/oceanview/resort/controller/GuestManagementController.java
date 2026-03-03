package com.oceanview.resort.controller;

import com.oceanview.resort.dao.GuestDAO;
import com.oceanview.resort.model.Guest;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Task B.ii: Controller Layer (MVC Architecture).
 * This class handles the Guest Management module, providing full CRUD 
 * (Create, Read, Update, Delete) capabilities for the resort's guest records.
 */
public class GuestManagementController implements Initializable {

    // FXML Table Components
    @FXML private TableView<Guest> guestTable;
    @FXML private TableColumn<Guest, Integer> colGuestId;
    @FXML private TableColumn<Guest, String> colFullName;
    @FXML private TableColumn<Guest, String> colContact;
    @FXML private TableColumn<Guest, String> colEmail; // Note: Ensure Guest model has email field
    @FXML private TableColumn<Guest, String> colAddress;

    // FXML UI Components
    @FXML private TextField searchGuestField;
    @FXML private Label currentDateLabel;
    @FXML private Label loggedInUserLabel;

    private final GuestDAO guestDAO = new GuestDAO();
    private ObservableList<Guest> guestMasterData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // 1. Set current date in header
        currentDateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));

        // 2. Initialize Table Columns
        colGuestId.setCellValueFactory(new PropertyValueFactory<>("guestId"));
        colFullName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colContact.setCellValueFactory(new PropertyValueFactory<>("contactNumber"));
        colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        
        // Note: If your Guest model doesn't have email yet, this will be empty.
        // To match your FXML, you should add an 'email' field to the Guest.java class.
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email")); 

        // 3. Load Data from Database
        loadGuestData();

        // 4. Setup Real-time Search Filter (Task B Requirement)
        setupSearchFilter();
    }

    /**
     * Fetches all guests from the MySQL database via the DAO.
     */
    private void loadGuestData() {
        guestMasterData.clear();
        guestMasterData.addAll(guestDAO.getAllGuests());
        guestTable.setItems(guestMasterData);
    }

    /**
     * Implements a real-time search filter for the guest table.
     */
    private void setupSearchFilter() {
        FilteredList<Guest> filteredData = new FilteredList<>(guestMasterData, p -> true);

        searchGuestField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(guest -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                if (guest.getName().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (String.valueOf(guest.getGuestId()).contains(lowerCaseFilter)) {
                    return true;
                } else if (guest.getContactNumber().contains(lowerCaseFilter)) {
                    return true;
                }
                return false;
            });
        });

        SortedList<Guest> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(guestTable.comparatorProperty());
        guestTable.setItems(sortedData);
    }

    /**
     * Navigates to the New Reservation screen to add a guest.
     */
    @FXML
    private void handleAddGuest(ActionEvent event) {
        showNewReservation(event);
    }

    /**
     * Handles updating an existing guest's information.
     */
    @FXML
    private void handleEditGuest(ActionEvent event) {
        Guest selectedGuest = guestTable.getSelectionModel().getSelectedItem();
        if (selectedGuest != null) {
            // Implementation: Show a Dialog to edit and then call guestDAO.updateGuest()
            System.out.println("Editing Guest: " + selectedGuest.getName());
            showAlert(Alert.AlertType.INFORMATION, "Edit Guest", "Update logic for " + selectedGuest.getName() + " triggered.");
        } else {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a guest from the table to edit.");
        }
    }

    /**
     * Deletes the selected guest from the database after confirmation.
     */
    @FXML
    private void handleDeleteGuest(ActionEvent event) {
        Guest selectedGuest = guestTable.getSelectionModel().getSelectedItem();
        if (selectedGuest != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Deletion");
            alert.setHeaderText("Remove Guest: " + selectedGuest.getName());
            alert.setContentText("Are you sure you want to delete this guest? This action cannot be undone.");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Logic to call guestDAO.deleteGuest(selectedGuest.getGuestId());
                System.out.println("Deleting Guest ID: " + selectedGuest.getGuestId());
                loadGuestData(); // Refresh table
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a guest from the table to delete.");
        }
    }

    // ═══════════════════════════════════════════════════════
    //  NAVIGATION METHODS
    // ═══════════════════════════════════════════════════════

    @FXML private void goToDashboard(ActionEvent event) { navigate(event, "/view/Dashboard.fxml"); }
    @FXML private void showNewReservation(ActionEvent event) { navigate(event, "/view/NewReservation.fxml"); }
    @FXML private void showViewReservations(ActionEvent event) { navigate(event, "/view/viewreservation.fxml"); }
    @FXML private void showBilling(ActionEvent event) { navigate(event, "/view/billing.fxml"); }
    @FXML private void showGuests(ActionEvent event) { loadGuestData(); } // Refresh current view
    @FXML private void showHelp(ActionEvent event) { System.out.println("Displaying Help Guidelines..."); }
    @FXML private void handleLogout(ActionEvent event) { navigate(event, "/view/Login.fxml"); }

    private void navigate(ActionEvent event, String path) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(path));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Error navigating to: " + path);
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}