package com.gym.controller.admin;

import com.gym.util.DatabaseConnection;
import com.gym.util.NavigationUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class ReportsController implements Initializable {

    @FXML private Label totalRevenueLabel;
    @FXML private Label monthRevenueLabel;
    @FXML private Label totalMembersLabel;
    @FXML private Label totalUsersLabel;
    @FXML private Label activeTrainersLabel;
    @FXML private Label totalClassesLabel;
    @FXML private Label totalBookingsLabel;
    @FXML private Label totalEquipmentLabel;
    @FXML private Label activePlansLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            loadReports();
        } catch (Exception e) {
            System.err.println("Error initializing ReportsController: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        NavigationUtil.switchScene(event, "/view/admin/admin-dashboard.fxml");
    }

    private void loadReports() {
        loadRevenueStats();
        loadUserStats();
        loadClassStats();
        loadEquipmentStats();
        loadPlanStats();
    }

    private void loadRevenueStats() {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            // Total Revenue
            String sql = "SELECT COALESCE(SUM(amount), 0) as total FROM payments WHERE status = 'completed'";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                if (rs.next() && totalRevenueLabel != null) {
                    totalRevenueLabel.setText(String.format("$%.2f", rs.getDouble("total")));
                }
            }

            // This Month Revenue
            sql = "SELECT COALESCE(SUM(amount), 0) as total FROM payments WHERE status = 'completed' AND MONTH(paid_at) = MONTH(CURRENT_DATE()) AND YEAR(paid_at) = YEAR(CURRENT_DATE())";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                if (rs.next() && monthRevenueLabel != null) {
                    monthRevenueLabel.setText(String.format("$%.2f", rs.getDouble("total")));
                }
            }

            // Total Members
            sql = "SELECT COUNT(*) as count FROM users WHERE role = 'member'";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                if (rs.next() && totalMembersLabel != null) {
                    totalMembersLabel.setText(String.valueOf(rs.getInt("count")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadUserStats() {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            String sql = "SELECT COUNT(*) as count FROM users";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                if (rs.next() && totalUsersLabel != null) {
                    totalUsersLabel.setText(String.valueOf(rs.getInt("count")));
                }
            }

            sql = "SELECT COUNT(*) as count FROM users WHERE role = 'trainer' AND is_active = TRUE";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                if (rs.next() && activeTrainersLabel != null) {
                    activeTrainersLabel.setText(String.valueOf(rs.getInt("count")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadClassStats() {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            String sql = "SELECT COUNT(*) as count FROM class_sessions";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                if (rs.next() && totalClassesLabel != null) {
                    totalClassesLabel.setText(String.valueOf(rs.getInt("count")));
                }
            }

            sql = "SELECT COUNT(*) as count FROM bookings";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                if (rs.next() && totalBookingsLabel != null) {
                    totalBookingsLabel.setText(String.valueOf(rs.getInt("count")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadEquipmentStats() {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            String sql = "SELECT COUNT(*) as count FROM equipment";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                if (rs.next() && totalEquipmentLabel != null) {
                    totalEquipmentLabel.setText(String.valueOf(rs.getInt("count")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadPlanStats() {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            String sql = "SELECT COUNT(*) as count FROM plans WHERE is_active = TRUE";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                if (rs.next() && activePlansLabel != null) {
                    activePlansLabel.setText(String.valueOf(rs.getInt("count")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

