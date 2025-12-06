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

public class MyWorkoutPlansController implements Initializable {

    @FXML private TableView<WorkoutPlanData> workoutPlansTable;
    private ObservableList<WorkoutPlanData> workoutPlans = FXCollections.observableArrayList();
    private int memberId;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        memberId = SessionManager.getInstance().getCurrentUserId();
        setupTable();
        loadWorkoutPlans();
    }

    private void setupTable() {
        workoutPlansTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    @FXML
    private void handleBack(ActionEvent event) {
        NavigationUtil.switchScene(event, "/view/member/member-dashboard.fxml");
    }

    private void loadWorkoutPlans() {
        workoutPlans.clear();
        String sql = "SELECT wp.name as plan_name, t.full_name as trainer_name, wp.duration, wp.difficulty, " +
                     "mwp.status, mwp.started_at, wp.description " +
                     "FROM member_workout_plans mwp " +
                     "JOIN workout_plans wp ON mwp.workout_plan_id = wp.id " +
                     "JOIN users t ON wp.trainer_id = t.id " +
                     "WHERE mwp.user_id = ? AND mwp.status != 'cancelled' " +
                     "ORDER BY mwp.started_at DESC";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, memberId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    WorkoutPlanData plan = new WorkoutPlanData(
                        rs.getString("plan_name"),
                        rs.getString("trainer_name"),
                        rs.getInt("duration"),
                        rs.getString("difficulty"),
                        rs.getString("status"),
                        rs.getTimestamp("started_at"),
                        rs.getString("description")
                    );
                    workoutPlans.add(plan);
                }
            }
            workoutPlansTable.setItems(workoutPlans);
        } catch (SQLException e) {
            showError("Error loading workout plans: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class WorkoutPlanData {
        private String planName;
        private String trainerName;
        private int duration;
        private String difficulty;
        private String status;
        private Timestamp startedAt;
        private String description;

        public WorkoutPlanData(String planName, String trainerName, int duration, String difficulty, 
                              String status, Timestamp startedAt, String description) {
            this.planName = planName;
            this.trainerName = trainerName;
            this.duration = duration;
            this.difficulty = difficulty;
            this.status = status;
            this.startedAt = startedAt;
            this.description = description;
        }

        public String getPlanName() { return planName; }
        public String getTrainerName() { return trainerName; }
        public int getDuration() { return duration; }
        public String getDifficulty() { return difficulty; }
        public String getStatus() { return status; }
        public Timestamp getStartedAt() { return startedAt; }
        public String getDescription() { return description; }
        public String getStartedAtStr() { return startedAt != null ? startedAt.toString() : ""; }
    }
}

