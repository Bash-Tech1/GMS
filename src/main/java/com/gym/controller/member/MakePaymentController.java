package com.gym.controller.member;

import com.gym.util.DatabaseConnection;
import com.gym.util.NavigationUtil;
import com.gym.util.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class MakePaymentController implements Initializable {

    @FXML private ComboBox<String> planCombo;
    @FXML private ComboBox<String> paymentMethodCombo;
    @FXML private Label planDetailsLabel;
    @FXML private Label amountLabel;
    
    private int memberId;
    private double selectedAmount = 0.0;
    private int selectedPlanId = -1;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        memberId = SessionManager.getInstance().getCurrentUserId();
        loadPlans();
        loadPaymentMethods();
        
        planCombo.setOnAction(e -> updatePlanDetails());
    }

    @FXML
    private void handleBack(ActionEvent event) {
        NavigationUtil.switchScene(event, "/view/member/member-dashboard.fxml");
    }

    @FXML
    private void handlePayment(ActionEvent event) {
        if (selectedPlanId == -1) {
            showError("Please select a membership plan.");
            return;
        }

        String paymentMethod = paymentMethodCombo.getValue();
        if (paymentMethod == null) {
            showError("Please select a payment method.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Payment");
        confirm.setHeaderText("Process Payment");
        confirm.setContentText(String.format("Amount: $%.2f\nPayment Method: %s\n\nProceed with payment?", selectedAmount, paymentMethod));
        
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            String sql = "INSERT INTO payments (user_id, plan_id, amount, payment_method, status, paid_at, transaction_reference) " +
                         "VALUES (?, ?, ?, ?, 'completed', NOW(), ?)";
            
            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, memberId);
                stmt.setInt(2, selectedPlanId);
                stmt.setDouble(3, selectedAmount);
                stmt.setString(4, paymentMethod);
                stmt.setString(5, "TXN-" + System.currentTimeMillis());
                stmt.executeUpdate();
                
                showSuccess("Payment processed successfully!");
                NavigationUtil.switchScene(event, "/view/member/member-dashboard.fxml");
            } catch (SQLException e) {
                showError("Error processing payment: " + e.getMessage());
            }
        }
    }

    private void loadPlans() {
        planCombo.getItems().clear();
        String sql = "SELECT id, name, price, duration, description FROM plans WHERE is_active = TRUE ORDER BY price";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                planCombo.getItems().add(rs.getInt("id") + " - " + rs.getString("name") + " ($" + rs.getDouble("price") + ")");
            }
        } catch (SQLException e) {
            showError("Error loading plans: " + e.getMessage());
        }
    }

    private void loadPaymentMethods() {
        paymentMethodCombo.getItems().addAll("credit_card", "debit_card", "paypal", "cash", "bank_transfer");
    }

    private void updatePlanDetails() {
        String selection = planCombo.getValue();
        if (selection == null) {
            selectedPlanId = -1;
            selectedAmount = 0.0;
            planDetailsLabel.setText("");
            amountLabel.setText("$0.00");
            return;
        }

        selectedPlanId = Integer.parseInt(selection.split(" - ")[0]);
        String sql = "SELECT name, price, duration, description FROM plans WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, selectedPlanId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    selectedAmount = rs.getDouble("price");
                    String details = String.format("Duration: %d days\n%s", 
                        rs.getInt("duration"),
                        rs.getString("description") != null ? rs.getString("description") : "");
                    planDetailsLabel.setText(details);
                    amountLabel.setText(String.format("$%.2f", selectedAmount));
                }
            }
        } catch (SQLException e) {
            showError("Error loading plan details: " + e.getMessage());
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

