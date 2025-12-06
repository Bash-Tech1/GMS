package com.gym.controller.member;

import com.gym.util.DatabaseConnection;
import com.gym.util.NavigationUtil;
import com.gym.util.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class MyStatisticsController implements Initializable {

    @FXML private Label classesAttendedLabel;
    @FXML private Label workoutPlansLabel;
    @FXML private Label totalBookingsLabel;
    @FXML private Label recentActivityLabel;
    
    private int memberId;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        memberId = SessionManager.getInstance().getCurrentUserId();
        loadStatistics();
    }

    @FXML
    private void handleBack(ActionEvent event) {
        NavigationUtil.switchScene(event, "/view/member/member-dashboard.fxml");
    }

    private void loadStatistics() {
        // Load classes attended
        String sql = "SELECT COUNT(*) as count FROM attendance WHERE user_id = ? AND status = 'present'";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, memberId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    classesAttendedLabel.setText(String.valueOf(rs.getInt("count")));
                }
            }
        } catch (SQLException e) {
            classesAttendedLabel.setText("Error");
        }

        // Load active workout plans
        sql = "SELECT COUNT(*) as count FROM member_workout_plans WHERE user_id = ? AND status = 'active'";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, memberId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    workoutPlansLabel.setText(String.valueOf(rs.getInt("count")));
                }
            }
        } catch (SQLException e) {
            workoutPlansLabel.setText("Error");
        }

        // Load total bookings
        sql = "SELECT COUNT(*) as count FROM bookings WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, memberId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    totalBookingsLabel.setText(String.valueOf(rs.getInt("count")));
                }
            }
        } catch (SQLException e) {
            totalBookingsLabel.setText("Error");
        }

        // Load recent activity
        StringBuilder activity = new StringBuilder();
        sql = "SELECT 'booking' as type, cs.title as description, b.booking_date as date " +
              "FROM bookings b JOIN class_sessions cs ON b.class_session_id = cs.id " +
              "WHERE b.user_id = ? " +
              "UNION ALL " +
              "SELECT 'attendance' as type, cs.title as description, a.check_in_time as date " +
              "FROM attendance a JOIN class_sessions cs ON a.class_session_id = cs.id " +
              "WHERE a.user_id = ? " +
              "ORDER BY date DESC LIMIT 5";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, memberId);
            stmt.setInt(2, memberId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    activity.append(String.format("• %s: %s (%s)\n", 
                        rs.getString("type").toUpperCase(),
                        rs.getString("description"),
                        rs.getTimestamp("date")));
                }
            }
            if (activity.length() == 0) {
                recentActivityLabel.setText("No recent activity.");
            } else {
                recentActivityLabel.setText(activity.toString());
            }
        } catch (SQLException e) {
            recentActivityLabel.setText("Error loading recent activity.");
        }
    }
}

