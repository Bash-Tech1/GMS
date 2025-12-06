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
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ResourceBundle;

public class WorkoutProgressController implements Initializable {

    @FXML private Label activePlansLabel;
    @FXML private Label completedPlansLabel;
    @FXML private TableView<ProgressData> progressTable;
    
    private ObservableList<ProgressData> progress = FXCollections.observableArrayList();
    private int memberId;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        memberId = SessionManager.getInstance().getCurrentUserId();
        setupTable();
        loadProgress();
    }

    private void setupTable() {
        progressTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    @FXML
    private void handleBack(ActionEvent event) {
        NavigationUtil.switchScene(event, "/view/member/member-dashboard.fxml");
    }

    private void loadProgress() {
        progress.clear();
        
        // Count active and completed plans
        String countSql = "SELECT status, COUNT(*) as count FROM member_workout_plans WHERE user_id = ? GROUP BY status";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(countSql)) {
            stmt.setInt(1, memberId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String status = rs.getString("status");
                    int count = rs.getInt("count");
                    if ("active".equals(status)) {
                        activePlansLabel.setText(String.valueOf(count));
                    } else if ("completed".equals(status)) {
                        completedPlansLabel.setText(String.valueOf(count));
                    }
                }
            }
        } catch (SQLException e) {
            activePlansLabel.setText("Error");
            completedPlansLabel.setText("Error");
        }

        // Load detailed progress
        String sql = "SELECT wp.name as plan_name, mwp.status, mwp.started_at " +
                     "FROM member_workout_plans mwp " +
                     "JOIN workout_plans wp ON mwp.workout_plan_id = wp.id " +
                     "WHERE mwp.user_id = ? " +
                     "ORDER BY mwp.started_at DESC";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, memberId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Timestamp startedAt = rs.getTimestamp("started_at");
                    long daysActive = 0;
                    if (startedAt != null) {
                        daysActive = ChronoUnit.DAYS.between(startedAt.toLocalDateTime().toLocalDate(), LocalDate.now());
                    }
                    
                    ProgressData prog = new ProgressData(
                        rs.getString("plan_name"),
                        rs.getString("status"),
                        startedAt,
                        daysActive
                    );
                    progress.add(prog);
                }
            }
            progressTable.setItems(progress);
        } catch (SQLException e) {
            showError("Error loading progress: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class ProgressData {
        private String planName;
        private String status;
        private Timestamp startedAt;
        private long daysActive;

        public ProgressData(String planName, String status, Timestamp startedAt, long daysActive) {
            this.planName = planName;
            this.status = status;
            this.startedAt = startedAt;
            this.daysActive = daysActive;
        }

        public String getPlanName() { return planName; }
        public String getStatus() { return status; }
        public Timestamp getStartedAt() { return startedAt; }
        public String getStartedAtStr() { return startedAt != null ? startedAt.toString() : ""; }
        public long getDaysActive() { return daysActive; }
    }
}

