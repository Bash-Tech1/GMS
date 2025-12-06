package com.gym.controller.auth;

import com.gym.util.DatabaseConnection;
import com.gym.util.NavigationUtil;
import com.gym.util.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    private void handleLogin(ActionEvent event) {
        String emailOrUsername = emailField.getText() != null ? emailField.getText().trim() : "";
        String password = passwordField.getText() != null ? passwordField.getText().trim() : "";

        if (emailOrUsername.isBlank() || password.isBlank()) {
            errorLabel.setText("Please enter email and password.");
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
            return;
        }
        
        // Hide error label when starting login
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        // DB-backed authentication using the `users` table with plain text password comparison.
        String sql = "SELECT id, role, is_active, password, full_name FROM users WHERE email = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, emailOrUsername);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    errorLabel.setText("Invalid email or password.");
                    errorLabel.setVisible(true);
                    errorLabel.setManaged(true);
                    return;
                }

                // Get the stored password from the database
                String storedPassword = rs.getString("password");
                
                // Simple plain text password comparison
                if (!password.equals(storedPassword)) {
                    errorLabel.setText("Invalid email or password.");
                    errorLabel.setVisible(true);
                    errorLabel.setManaged(true);
                    return;
                }

                // Password is correct, now check if account is active
                boolean active = rs.getBoolean("is_active");
                if (!active) {
                    errorLabel.setText("This account is disabled. Please contact the admin.");
                    errorLabel.setVisible(true);
                    errorLabel.setManaged(true);
                    return;
                }

                // Store user session
                int userId = rs.getInt("id");
                String role = rs.getString("role");
                String fullName = rs.getString("full_name");
                SessionManager.getInstance().setCurrentUser(userId, emailOrUsername, role, fullName);

                // Get the user's role and navigate to appropriate dashboard
                if ("admin".equalsIgnoreCase(role)) {
                    NavigationUtil.switchScene(event, "/view/admin/admin-dashboard.fxml");
                } else if ("trainer".equalsIgnoreCase(role)) {
                    NavigationUtil.switchScene(event, "/view/trainer/trainer-dashboard.fxml");
                } else if ("member".equalsIgnoreCase(role)) {
                    NavigationUtil.switchScene(event, "/view/member/member-dashboard.fxml");
                } else {
                    // Fallback if role is something unexpected
                    errorLabel.setText("Unknown user role: " + role);
                    errorLabel.setVisible(true);
                    errorLabel.setManaged(true);
                }
            }

        } catch (SQLException e) {
            System.err.println("Database error during login: " + e.getMessage());
            e.printStackTrace();
            errorLabel.setText("Login failed due to a database error: " + e.getMessage());
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        }
    }

}


