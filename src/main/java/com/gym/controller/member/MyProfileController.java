package com.gym.controller.member;

import com.gym.util.DatabaseConnection;
import com.gym.util.NavigationUtil;
import com.gym.util.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class MyProfileController implements Initializable {

    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private Label planLabel;
    
    private int memberId;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        memberId = SessionManager.getInstance().getCurrentUserId();
        loadProfile();
    }

    @FXML
    private void handleBack(ActionEvent event) {
        NavigationUtil.switchScene(event, "/view/member/member-dashboard.fxml");
    }

    @FXML
    private void handleUpdate(ActionEvent event) {
        String fullName = fullNameField.getText().trim();
        String phone = phoneField.getText().trim();

        if (fullName.isEmpty()) {
            showError("Full name cannot be empty.");
            return;
        }

        String sql = "UPDATE users SET full_name = ?, phone = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, fullName);
            stmt.setString(2, phone.isEmpty() ? null : phone);
            stmt.setInt(3, memberId);
            stmt.executeUpdate();
            showSuccess("Profile updated successfully!");
            loadProfile();
        } catch (SQLException e) {
            showError("Error updating profile: " + e.getMessage());
        }
    }

    @FXML
    private void handleChangePassword(ActionEvent event) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Change Password");
        dialog.setHeaderText(null);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        PasswordField currentPasswordField = new PasswordField();
        PasswordField newPasswordField = new PasswordField();
        PasswordField confirmPasswordField = new PasswordField();

        grid.add(new Label("Current Password:"), 0, 0);
        grid.add(currentPasswordField, 1, 0);
        grid.add(new Label("New Password:"), 0, 1);
        grid.add(newPasswordField, 1, 1);
        grid.add(new Label("Confirm Password:"), 0, 2);
        grid.add(confirmPasswordField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        if (dialog.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            String currentPassword = currentPasswordField.getText();
            String newPassword = newPasswordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            if (newPassword.isEmpty()) {
                showError("New password cannot be empty.");
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                showError("New passwords do not match.");
                return;
            }

            // Verify current password
            String verifySql = "SELECT password FROM users WHERE id = ?";
            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement verifyStmt = conn.prepareStatement(verifySql)) {
                verifyStmt.setInt(1, memberId);
                try (ResultSet rs = verifyStmt.executeQuery()) {
                    if (rs.next()) {
                        String storedPassword = rs.getString("password");
                        if (!currentPassword.equals(storedPassword)) {
                            showError("Current password is incorrect.");
                            return;
                        }
                    }
                }
            } catch (SQLException e) {
                showError("Error verifying password: " + e.getMessage());
                return;
            }

            // Update password
            String updateSql = "UPDATE users SET password = ? WHERE id = ?";
            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setString(1, newPassword);
                updateStmt.setInt(2, memberId);
                updateStmt.executeUpdate();
                showSuccess("Password changed successfully!");
            } catch (SQLException e) {
                showError("Error changing password: " + e.getMessage());
            }
        }
    }

    private void loadProfile() {
        String sql = "SELECT u.full_name, u.email, u.phone, pl.name as plan_name " +
                     "FROM users u " +
                     "LEFT JOIN plans pl ON u.plan_id = pl.id " +
                     "WHERE u.id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, memberId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    fullNameField.setText(rs.getString("full_name"));
                    emailField.setText(rs.getString("email"));
                    phoneField.setText(rs.getString("phone") != null ? rs.getString("phone") : "");
                    planLabel.setText(rs.getString("plan_name") != null ? rs.getString("plan_name") : "No plan");
                }
            }
        } catch (SQLException e) {
            showError("Error loading profile: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

