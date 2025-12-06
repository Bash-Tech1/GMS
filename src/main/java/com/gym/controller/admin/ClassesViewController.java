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

public class ClassesViewController implements Initializable {

    @FXML private TableView<ClassData> classesTable;
    private ObservableList<ClassData> classes = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        loadClasses();
    }

    private void setupTable() {
        classesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    @FXML
    private void handleBack(ActionEvent event) {
        NavigationUtil.switchScene(event, "/view/admin/admin-dashboard.fxml");
    }

    private void loadClasses() {
        classes.clear();
        String sql = "SELECT cs.id, cs.title, u.full_name as trainer_name, cs.start_time, " +
                     "cs.end_time, cs.capacity, cs.current_enrollment, cs.status " +
                     "FROM class_sessions cs " +
                     "JOIN users u ON cs.trainer_id = u.id " +
                     "ORDER BY cs.start_time DESC";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                ClassData classData = new ClassData(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("trainer_name"),
                    rs.getTimestamp("start_time"),
                    rs.getTimestamp("end_time"),
                    rs.getInt("capacity"),
                    rs.getInt("current_enrollment"),
                    rs.getString("status")
                );
                classes.add(classData);
            }
            classesTable.setItems(classes);
        } catch (SQLException e) {
            showError("Error loading classes: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class ClassData {
        private int id;
        private String title;
        private String trainerName;
        private Timestamp startTime;
        private Timestamp endTime;
        private int capacity;
        private int enrolled;
        private String status;

        public ClassData(int id, String title, String trainerName, Timestamp startTime, Timestamp endTime, int capacity, int enrolled, String status) {
            this.id = id;
            this.title = title;
            this.trainerName = trainerName;
            this.startTime = startTime;
            this.endTime = endTime;
            this.capacity = capacity;
            this.enrolled = enrolled;
            this.status = status;
        }

        public int getId() { return id; }
        public String getTitle() { return title; }
        public String getTrainerName() { return trainerName; }
        public Timestamp getStartTime() { return startTime; }
        public Timestamp getEndTime() { return endTime; }
        public int getCapacity() { return capacity; }
        public int getEnrolled() { return enrolled; }
        public String getStatus() { return status; }
        
        public String getStartTimeStr() {
            return startTime != null ? startTime.toString() : "";
        }
        
        public String getEndTimeStr() {
            return endTime != null ? endTime.toString() : "";
        }
    }
}

