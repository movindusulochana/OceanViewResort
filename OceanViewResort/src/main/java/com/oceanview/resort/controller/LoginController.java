package com.oceanview.resort.controller;

import com.oceanview.resort.dao.UserDAO;
import com.oceanview.resort.util.ValidationHelper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Task B.ii: Controller Layer (MVC Architecture).
 * This class handles user interactions for the Login screen,
 * authenticating credentials against the database.
 */
public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    private Button loginButton;

    // Instance of DAO to handle database queries
    private final UserDAO userDAO = new UserDAO();

    /**
     * Task 1: User Authentication (Login).
     * Validates input fields and checks credentials in the MySQL database.
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Use ValidationHelper to restrict invalid entries (Task B)
        if (ValidationHelper.isEmpty(username) || ValidationHelper.isEmpty(password)) {
            errorLabel.setText("Username and password cannot be empty.");
            return;
        }

        // Authenticate using the Data Access Object
        boolean isAuthenticated = userDAO.validateLogin(username, password);

        if (isAuthenticated) {
            System.out.println("--> [LOGIN SUCCESS] Access granted for: " + username);
            navigateToDashboard(event);
        } else {
            errorLabel.setText("Invalid username or password.");
            System.err.println("--> [LOGIN FAILED] Unauthorized access attempt.");
        }
    }

    /**
     * Redirects the user to the Sign Up screen.
     */
    @FXML
    private void handleSignUp(ActionEvent event) {
        try {
            Parent signUpRoot = FXMLLoader.load(getClass().getResource("/view/SignUp.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(signUpRoot));
            stage.show();
        } catch (IOException e) {
            System.err.println("--> [ERROR] Could not load SignUp view: " + e.getMessage());
        }
    }

    /**
     * Transitions the application to the main Dashboard upon successful login.
     */
    private void navigateToDashboard(ActionEvent event) {
        try {
            Parent dashboardRoot = FXMLLoader.load(getClass().getResource("/view/Dashboard.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            
            // Set new scene with the dashboard layout
            Scene scene = new Scene(dashboardRoot);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.setTitle("Ocean View Resort - Dashboard");
            stage.show();
        } catch (IOException e) {
            errorLabel.setText("Error loading Dashboard. Please contact IT.");
            e.printStackTrace();
        }
    }
}