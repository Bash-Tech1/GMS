package com.gym.controller.trainer;

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

public class MemberProgressController implements Initializable {

    @FXML private ComboBox<String> memberCombo;
    @FXML private TableView<WorkoutPlanData> workoutPlansTable;
    @FXML private TableView<AttendanceData> attendanceTable;
    
    private ObservableList<WorkoutPlanData> workoutPlans = FXCollections.observableArrayList();
    private ObservableList<AttendanceData> attendance = FXCollections.observableArrayList();
    private int trainerId;
    private int selectedMemberId = -1;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        trainerId = SessionManager.getInstance().getCurrentUserId();
        setupTables();
        loadMembers();
    }

    private void setupTables() {
        workoutPlansTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        attendanceTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    @FXML
    private void handleBack(ActionEvent event) {
        NavigationUtil.switchScene(event, "/view/trainer/trainer-dashboard.fxml");
    }

    @FXML
    private void handleLoadProgress(ActionEvent event) {
        String selection = memberCombo.getValue();
        if (selection == null) {
            showError("Please select a member first.");
            return;
        }
        selectedMemberId = Integer.parseInt(selection.split(" - ")[0]);
        loadWorkoutPlans();
        loadAttendance();
    }

    private void loadMembers() {
        memberCombo.getItems().clear();
        String sql = "SELECT DISTINCT u.id, u.full_name " +
                     "FROM users u " +
                     "JOIN member_workout_plans mwp ON u.id = mwp.user_id " +
                     "JOIN workout_plans wp ON mwp.workout_plan_id = wp.id " +
                     "WHERE wp.trainer_id = ? AND u.role = 'member' " +
                     "ORDER BY u.full_name";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, trainerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    memberCombo.getItems().add(rs.getInt("id") + " - " + rs.getString("full_name"));
                }
            }
        } catch (SQLException e) {
            showError("Error loading members: " + e.getMessage());
        }
    }

    private void loadWorkoutPlans() {
        workoutPlans.clear();
        if (selectedMemberId == -1) return;

        String sql = "SELECT wp.name as plan_name, mwp.status, mwp.started_at " +
                     "FROM member_workout_plans mwp " +
                     "JOIN workout_plans wp ON mwp.workout_plan_id = wp.id " +
                     "WHERE mwp.user_id = ? AND wp.trainer_id = ? " +
                     "ORDER BY mwp.started_at DESC";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, selectedMemberId);
            stmt.setInt(2, trainerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    WorkoutPlanData plan = new WorkoutPlanData(
                        rs.getString("plan_name"),
                        rs.getString("status"),
                        rs.getTimestamp("started_at")
                    );
                    workoutPlans.add(plan);
                }
            }
            workoutPlansTable.setItems(workoutPlans);
        } catch (SQLException e) {
            showError("Error loading workout plans: " + e.getMessage());
        }
    }

    private void loadAttendance() {
        attendance.clear();
        if (selectedMemberId == -1) return;

        String sql = "SELECT cs.title as class_name, a.check_in_time, a.status " +
                     "FROM attendance a " +
                     "JOIN class_sessions cs ON a.class_session_id = cs.id " +
                     "WHERE a.user_id = ? AND cs.trainer_id = ? " +
                     "ORDER BY a.check_in_time DESC";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, selectedMemberId);
            stmt.setInt(2, trainerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    AttendanceData att = new AttendanceData(
                        rs.getString("class_name"),
                        rs.getTimestamp("check_in_time"),
                        rs.getString("status")
                    );
                    attendance.add(att);
                }
            }
            attendanceTable.setItems(attendance);
        } catch (SQLException e) {
            showError("Error loading attendance: " + e.getMessage());
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
        private String status;
        private Timestamp startedAt;

        public WorkoutPlanData(String planName, String status, Timestamp startedAt) {
            this.planName = planName;
            this.status = status;
            this.startedAt = startedAt;
        }

        public String getPlanName() { return planName; }
        public String getStatus() { return status; }
        public Timestamp getStartedAt() { return startedAt; }
        public String getStartedAtStr() { return startedAt != null ? startedAt.toString() : ""; }
    }

    public static class AttendanceData {
        private String className;
        private Timestamp date;
        private String status;

        public AttendanceData(String className, Timestamp date, String status) {
            this.className = className;
            this.date = date;
            this.status = status;
        }

        public String getClassName() { return className; }
        public Timestamp getDate() { return date; }
        public String getDateStr() { return date != null ? date.toString() : ""; }
        public String getStatus() { return status; }
    }
}

