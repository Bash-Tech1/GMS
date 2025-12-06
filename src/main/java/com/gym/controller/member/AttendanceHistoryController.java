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

public class AttendanceHistoryController implements Initializable {

    @FXML private TableView<AttendanceData> attendanceTable;
    private ObservableList<AttendanceData> attendance = FXCollections.observableArrayList();
    private int memberId;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        memberId = SessionManager.getInstance().getCurrentUserId();
        setupTable();
        loadAttendance();
    }

    private void setupTable() {
        attendanceTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    @FXML
    private void handleBack(ActionEvent event) {
        NavigationUtil.switchScene(event, "/view/member/member-dashboard.fxml");
    }

    private void loadAttendance() {
        attendance.clear();
        String sql = "SELECT cs.title as class_name, t.full_name as trainer_name, a.check_in_time, a.status " +
                     "FROM attendance a " +
                     "JOIN class_sessions cs ON a.class_session_id = cs.id " +
                     "JOIN users t ON cs.trainer_id = t.id " +
                     "WHERE a.user_id = ? " +
                     "ORDER BY a.check_in_time DESC";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, memberId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    AttendanceData att = new AttendanceData(
                        rs.getString("class_name"),
                        rs.getString("trainer_name"),
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

    public static class AttendanceData {
        private String className;
        private String trainerName;
        private Timestamp checkInTime;
        private String status;

        public AttendanceData(String className, String trainerName, Timestamp checkInTime, String status) {
            this.className = className;
            this.trainerName = trainerName;
            this.checkInTime = checkInTime;
            this.status = status;
        }

        public String getClassName() { return className; }
        public String getTrainerName() { return trainerName; }
        public Timestamp getCheckInTime() { return checkInTime; }
        public String getStatus() { return status; }
        public String getCheckInTimeStr() { return checkInTime != null ? checkInTime.toString() : ""; }
    }
}

