package com.oceanview.resort.controller;

import com.oceanview.resort.util.DatabaseConnection;
import com.oceanview.resort.util.ValidationHelper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Task B: Controller Layer (MVC Architecture).
 * This class manages the registration of new staff members into the system.
 */
public class SignUpController {

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label errorLabel;

    /**
     * Handles the 'Create Account' button action.
     * Implements validation and database insertion for new users.
     */
    @FXML
    private void handleSignUpSubmit(ActionEvent event) {
        String fullName = fullNameField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // 1. Check for empty fields using ValidationHelper
        if (ValidationHelper.isEmpty(fullName) || ValidationHelper.isEmpty(username) || 
            ValidationHelper.isEmpty(password)) {
            errorLabel.setText("All fields are required.");
            return;
        }

        // 2. Validate password match
        if (!password.equals(confirmPassword)) {
            errorLabel.setText("Passwords do not match.");
            return;
        }

        // 3. Insert new user into the database
        if (registerUser(username, password)) {
            System.out.println("--> [SIGNUP SUCCESS] Account created for: " + username);
            switchToLogin(event);
        } else {
            errorLabel.setText("Registration failed. Username might already exist.");
        }
    }

    /**
     * Logic to insert the user into the MySQL 'users' table.
     */
    private boolean registerUser(String username, String password) {
        String query = "INSERT INTO users (username, password, role) VALUES (?, ?, 'Staff')";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, username);
            stmt.setString(2, password);
            
            int result = stmt.executeUpdate();
            return result > 0;
            
        } catch (SQLException e) {
            System.err.println("--> [DB ERROR] Sign up failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Navigates back to the Login screen.
     */
    @FXML
    private void switchToLogin(ActionEvent event) {
        try {
            Parent loginRoot = FXMLLoader.load(getClass().getResource("/view/Login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(loginRoot));
            stage.setTitle("Ocean View Resort - Login");
            stage.show();
        } catch (IOException e) {
            System.err.println("--> [ERROR] Could not load Login view: " + e.getMessage());
        }
    }
}