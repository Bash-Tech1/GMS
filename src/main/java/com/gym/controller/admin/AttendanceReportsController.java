package com.gym.controller.admin;

import com.gym.util.DatabaseConnection;
import com.gym.util.NavigationUtil;
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

public class AttendanceReportsController implements Initializable {

    @FXML private TableView<AttendanceData> attendanceTable;
    private ObservableList<AttendanceData> attendanceList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        loadAttendance();
    }

    private void setupTable() {
        attendanceTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    @FXML
    private void handleBack(ActionEvent event) {
        NavigationUtil.switchScene(event, "/view/admin/admin-dashboard.fxml");
    }

    private void loadAttendance() {
        attendanceList.clear();
        String sql = "SELECT a.id, u.full_name as member_name, cs.title as class_name, " +
                     "a.check_in_time, a.status " +
                     "FROM attendance a " +
                     "JOIN users u ON a.user_id = u.id " +
                     "JOIN class_sessions cs ON a.class_session_id = cs.id " +
                     "ORDER BY a.check_in_time DESC";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                AttendanceData attendance = new AttendanceData(
                    rs.getInt("id"),
                    rs.getString("member_name"),
                    rs.getString("class_name"),
                    rs.getTimestamp("check_in_time"),
                    rs.getString("status")
                );
                attendanceList.add(attendance);
            }
            attendanceTable.setItems(attendanceList);
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
        private int id;
        private String memberName;
        private String className;
        private Timestamp checkInTime;
        private String status;

        public AttendanceData(int id, String memberName, String className, Timestamp checkInTime, String status) {
            this.id = id;
            this.memberName = memberName;
            this.className = className;
            this.checkInTime = checkInTime;
            this.status = status;
        }

        public int getId() { return id; }
        public String getMemberName() { return memberName; }
        public String getClassName() { return className; }
        public Timestamp getCheckInTime() { return checkInTime; }
        public String getStatus() { return status; }
        
        public String getCheckInTimeStr() {
            return checkInTime != null ? checkInTime.toString() : "";
        }
    }
}

