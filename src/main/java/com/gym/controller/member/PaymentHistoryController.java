package com.gym.controller.member;

import com.gym.util.DatabaseConnection;
import com.gym.util.NavigationUtil;
import com.gym.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class PaymentHistoryController implements Initializable {

    @FXML private TableView<PaymentData> paymentsTable;
    private ObservableList<PaymentData> payments = FXCollections.observableArrayList();
    private int memberId;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        memberId = SessionManager.getInstance().getCurrentUserId();
        setupTable();
        loadPayments();
    }

    private void setupTable() {
        paymentsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    @FXML
    private void handleBack(ActionEvent event) {
        NavigationUtil.switchScene(event, "/view/member/member-dashboard.fxml");
    }

    private void loadPayments() {
        payments.clear();
        String sql = "SELECT p.id, pl.name as plan_name, p.amount, p.payment_method, p.status, " +
                     "p.paid_at, p.transaction_reference " +
                     "FROM payments p " +
                     "LEFT JOIN plans pl ON p.plan_id = pl.id " +
                     "WHERE p.user_id = ? " +
                     "ORDER BY p.created_at DESC";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, memberId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    PaymentData payment = new PaymentData(
                        rs.getInt("id"),
                        rs.getString("plan_name"),
                        rs.getDouble("amount"),
                        rs.getString("payment_method"),
                        rs.getString("status"),
                        rs.getTimestamp("paid_at"),
                        rs.getString("transaction_reference")
                    );
                    payments.add(payment);
                }
            }
            paymentsTable.setItems(payments);
        } catch (SQLException e) {
            showError("Error loading payments: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class PaymentData {
        private int id;
        private String planName;
        private double amount;
        private String method;
        private String status;
        private Timestamp paidDate;
        private String reference;

        public PaymentData(int id, String planName, double amount, String method, String status, Timestamp paidDate, String reference) {
            this.id = id;
            this.planName = planName != null ? planName : "N/A";
            this.amount = amount;
            this.method = method;
            this.status = status;
            this.paidDate = paidDate;
            this.reference = reference != null ? reference : "";
        }

        public int getId() { return id; }
        public String getPlanName() { return planName; }
        public double getAmount() { return amount; }
        public String getAmountStr() { return String.format("$%.2f", amount); }
        public String getMethod() { return method; }
        public String getStatus() { return status; }
        public Timestamp getPaidDate() { return paidDate; }
        public String getReference() { return reference; }
        public String getPaidDateStr() { return paidDate != null ? paidDate.toString() : ""; }
    }
}

