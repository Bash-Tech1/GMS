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

public class AssignWorkoutPlanController implements Initializable {

    @FXML private ComboBox<String> workoutPlanCombo;
    @FXML private ComboBox<String> memberCombo;
    @FXML private TableView<AssignmentData> assignmentsTable;
    
    private ObservableList<AssignmentData> assignments = FXCollections.observableArrayList();
    private int trainerId;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        trainerId = SessionManager.getInstance().getCurrentUserId();
        setupTable();
        loadWorkoutPlans();
        loadMembers();
        loadAssignments();
    }

    private void setupTable() {
        assignmentsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    @FXML
    private void handleBack(ActionEvent event) {
        NavigationUtil.switchScene(event, "/view/trainer/trainer-dashboard.fxml");
    }

    @FXML
    private void handleAssign(ActionEvent event) {
        String planSelection = workoutPlanCombo.getValue();
        String memberSelection = memberCombo.getValue();
        
        if (planSelection == null || memberSelection == null) {
            showError("Please select both a workout plan and a member.");
            return;
        }

        int planId = Integer.parseInt(planSelection.split(" - ")[0]);
        int memberId = Integer.parseInt(memberSelection.split(" - ")[0]);

        String sql = "INSERT INTO member_workout_plans (user_id, workout_plan_id, status) VALUES (?, ?, 'active') " +
                     "ON DUPLICATE KEY UPDATE status = 'active', started_at = CURRENT_TIMESTAMP";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, memberId);
            stmt.setInt(2, planId);
            stmt.executeUpdate();
            showSuccess("Workout plan assigned successfully!");
            loadAssignments();
        } catch (SQLException e) {
            showError("Error assigning workout plan: " + e.getMessage());
        }
    }

    @FXML
    private void handleRemove(ActionEvent event) {
        AssignmentData selected = assignmentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select an assignment to remove.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Remove");
        confirm.setHeaderText("Remove Assignment");
        confirm.setContentText("Are you sure you want to remove this assignment?");
        
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            String sql = "UPDATE member_workout_plans SET status = 'cancelled' WHERE id = ?";
            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, selected.getId());
                stmt.executeUpdate();
                showSuccess("Assignment removed successfully!");
                loadAssignments();
            } catch (SQLException e) {
                showError("Error removing assignment: " + e.getMessage());
            }
        }
    }

    private void loadWorkoutPlans() {
        workoutPlanCombo.getItems().clear();
        String sql = "SELECT id, name FROM workout_plans WHERE trainer_id = ? AND is_active = TRUE";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, trainerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    workoutPlanCombo.getItems().add(rs.getInt("id") + " - " + rs.getString("name"));
                }
            }
        } catch (SQLException e) {
            showError("Error loading workout plans: " + e.getMessage());
        }
    }

    private void loadMembers() {
        memberCombo.getItems().clear();
        // Load all active members
        String sql = "SELECT id, full_name FROM users WHERE role = 'member' AND is_active = TRUE ORDER BY full_name";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                memberCombo.getItems().add(rs.getInt("id") + " - " + rs.getString("full_name"));
            }
        } catch (SQLException e) {
            showError("Error loading members: " + e.getMessage());
        }
    }

    private void loadAssignments() {
        assignments.clear();
        String sql = "SELECT mwp.id, u.full_name as member_name, wp.name as plan_name, " +
                     "mwp.status, mwp.started_at " +
                     "FROM member_workout_plans mwp " +
                     "JOIN users u ON mwp.user_id = u.id " +
                     "JOIN workout_plans wp ON mwp.workout_plan_id = wp.id " +
                     "WHERE wp.trainer_id = ? AND mwp.status != 'cancelled' " +
                     "ORDER BY mwp.started_at DESC";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, trainerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    AssignmentData assignment = new AssignmentData(
                        rs.getInt("id"),
                        rs.getString("member_name"),
                        rs.getString("plan_name"),
                        rs.getString("status"),
                        rs.getTimestamp("started_at")
                    );
                    assignments.add(assignment);
                }
            }
            assignmentsTable.setItems(assignments);
        } catch (SQLException e) {
            showError("Error loading assignments: " + e.getMessage());
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

    public static class AssignmentData {
        private int id;
        private String memberName;
        private String planName;
        private String status;
        private Timestamp startedAt;

        public AssignmentData(int id, String memberName, String planName, String status, Timestamp startedAt) {
            this.id = id;
            this.memberName = memberName;
            this.planName = planName;
            this.status = status;
            this.startedAt = startedAt;
        }

        public int getId() { return id; }
        public String getMemberName() { return memberName; }
        public String getPlanName() { return planName; }
        public String getStatus() { return status; }
        public Timestamp getStartedAt() { return startedAt; }
        public String getStartedAtStr() { return startedAt != null ? startedAt.toString() : ""; }
    }
}

