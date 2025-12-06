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

public class AssignedMembersController implements Initializable {

    @FXML private TableView<MemberData> membersTable;
    private ObservableList<MemberData> members = FXCollections.observableArrayList();
    private int trainerId;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        trainerId = SessionManager.getInstance().getCurrentUserId();
        setupTable();
        loadMembers();
    }

    private void setupTable() {
        membersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    @FXML
    private void handleBack(ActionEvent event) {
        NavigationUtil.switchScene(event, "/view/trainer/trainer-dashboard.fxml");
    }

    private void loadMembers() {
        members.clear();
        String sql = "SELECT DISTINCT u.id, u.full_name as member_name, wp.name as workout_plan, " +
                     "mwp.status, mwp.started_at " +
                     "FROM users u " +
                     "JOIN member_workout_plans mwp ON u.id = mwp.user_id " +
                     "JOIN workout_plans wp ON mwp.workout_plan_id = wp.id " +
                     "WHERE wp.trainer_id = ? AND mwp.status = 'active' " +
                     "ORDER BY u.full_name";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, trainerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    MemberData member = new MemberData(
                        rs.getInt("id"),
                        rs.getString("member_name"),
                        rs.getString("workout_plan"),
                        rs.getString("status"),
                        rs.getTimestamp("started_at")
                    );
                    members.add(member);
                }
            }
            membersTable.setItems(members);
        } catch (SQLException e) {
            showError("Error loading members: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class MemberData {
        private int id;
        private String memberName;
        private String workoutPlan;
        private String status;
        private Timestamp startedAt;

        public MemberData(int id, String memberName, String workoutPlan, String status, Timestamp startedAt) {
            this.id = id;
            this.memberName = memberName;
            this.workoutPlan = workoutPlan;
            this.status = status;
            this.startedAt = startedAt;
        }

        public int getId() { return id; }
        public String getMemberName() { return memberName; }
        public String getWorkoutPlan() { return workoutPlan; }
        public String getStatus() { return status; }
        public Timestamp getStartedAt() { return startedAt; }
        public String getStartedAtStr() { return startedAt != null ? startedAt.toString() : ""; }
    }
}

