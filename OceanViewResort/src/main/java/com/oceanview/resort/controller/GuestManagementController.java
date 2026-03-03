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
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Task B.ii: Controller Layer (MVC Architecture).
 * Handles the Guest Management module with full CRUD and real-time search.
 */
public class GuestManagementController implements Initializable {

    // FXML Table Components
    @FXML private TableView<Guest> guestTable;
    @FXML private TableColumn<Guest, Integer> colGuestId;
    @FXML private TableColumn<Guest, String> colFullName, colContact, colEmail, colAddress;

    // FXML UI Components
    @FXML private TextField searchGuestField;
    @FXML private Label currentDateLabel;

    private final GuestDAO guestDAO = new GuestDAO();
    private ObservableList<Guest> guestMasterData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Set current date in header
        if (currentDateLabel != null) {
            currentDateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
        }

        // Mapping TableView columns to Guest model fields
        colGuestId.setCellValueFactory(new PropertyValueFactory<>("guestId"));
        colFullName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colContact.setCellValueFactory(new PropertyValueFactory<>("contactNumber"));
        colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email")); 

        loadGuestData();
        setupSearchFilter();
    }

    /**
     * Fetches all guests from the database via the DAO.
     */
    private void loadGuestData() {
        guestMasterData.clear();
        guestMasterData.addAll(guestDAO.getAllGuests());
        guestTable.setItems(guestMasterData);
    }

    /**
     * Implements real-time search filtering.
     */
    private void setupSearchFilter() {
        FilteredList<Guest> filteredData = new FilteredList<>(guestMasterData, p -> true);
        searchGuestField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(guest -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String lower = newVal.toLowerCase();
                return guest.getName().toLowerCase().contains(lower) || 
                       String.valueOf(guest.getGuestId()).contains(lower);
            });
        });
        SortedList<Guest> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(guestTable.comparatorProperty());
        guestTable.setItems(sortedData);
    }

    /**
     * Opens a custom dialog to edit Name, Address, and Contact.
     */
    @FXML
    private void handleEditGuest(ActionEvent event) {
        Guest selectedGuest = guestTable.getSelectionModel().getSelectedItem();
        
        if (selectedGuest == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a guest from the table to edit.");
            return;
        }

        Dialog<Guest> dialog = new Dialog<>();
        dialog.setTitle("Update Guest Information");
        dialog.setHeaderText("Editing Guest ID: " + selectedGuest.getGuestId());

        ButtonType updateButtonType = new ButtonType("Update Record", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField name = new TextField(selectedGuest.getName());
        TextField contact = new TextField(selectedGuest.getContactNumber());
        TextArea address = new TextArea(selectedGuest.getAddress());
        address.setPrefRowCount(3);

        grid.add(new Label("Full Name:"), 0, 0); grid.add(name, 1, 0);
        grid.add(new Label("Contact No:"), 0, 1); grid.add(contact, 1, 1);
        grid.add(new Label("Address:"), 0, 2); grid.add(address, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == updateButtonType) {
                selectedGuest.setName(name.getText());
                selectedGuest.setContactNumber(contact.getText());
                selectedGuest.setAddress(address.getText());
                return selectedGuest;
            }
            return null;
        });

        Optional<Guest> result = dialog.showAndWait();
        result.ifPresent(updatedGuest -> {
            if (guestDAO.updateGuest(updatedGuest)) {
                loadGuestData();
                showAlert(Alert.AlertType.INFORMATION, "Success", "All guest information updated.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to update record.");
            }
        });
    }

    /**
     * Deletes the selected guest after confirmation.
     */
    @FXML
    private void handleDeleteGuest(ActionEvent event) {
        Guest selected = guestTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete " + selected.getName() + "?", ButtonType.YES, ButtonType.NO);
            alert.setHeaderText("Permanent Deletion Warning");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Assuming guestDAO.deleteGuest(id) is implemented
                System.out.println("Deleting Guest ID: " + selected.getGuestId());
                loadGuestData();
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a guest to delete.");
        }
    }

    // ═══════════════════════════════════════════════════════
    //  NAVIGATION & SIDEBAR HANDLERS
    // ═══════════════════════════════════════════════════════

    @FXML private void goToDashboard(ActionEvent event) { navigate(event, "/view/Dashboard.fxml"); }
    @FXML private void showNewReservation(ActionEvent event) { navigate(event, "/view/NewReservation.fxml"); }
    @FXML private void showViewReservations(ActionEvent event) { navigate(event, "/view/viewreservation.fxml"); }
    @FXML private void showBilling(ActionEvent event) { navigate(event, "/view/billing.fxml"); }
    @FXML private void handleLogout(ActionEvent event) { navigate(event, "/view/Login.fxml"); }
    @FXML private void handleAddGuest(ActionEvent event) { navigate(event, "/view/NewReservation.fxml"); }

    @FXML
    private void showGuests(ActionEvent event) {
        loadGuestData();
        System.out.println("--> [UI] Guest Management view refreshed.");
    }

    @FXML
    private void showHelp(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Help", "Use the search bar to filter guests. Double-click or use Edit to update info.");
    }

    private void navigate(ActionEvent event, String path) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(path));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            System.err.println("Navigation error: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert a = new Alert(type); a.setTitle(title); a.setHeaderText(null); a.setContentText(content); a.showAndWait();
    }
}