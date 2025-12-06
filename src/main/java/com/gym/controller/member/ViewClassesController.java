package com.gym.controller.member;

import com.gym.util.DatabaseConnection;
import com.gym.util.NavigationUtil;
import com.gym.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class ViewClassesController implements Initializable {

    @FXML private TableView<ClassData> classesTable;
    @FXML private ComboBox<String> statusFilter;
    
    private ObservableList<ClassData> allClasses = FXCollections.observableArrayList();
    private FilteredList<ClassData> filteredClasses;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        setupFilters();
        loadClasses();
    }

    private void setupTable() {
        classesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void setupFilters() {
        statusFilter.getItems().addAll("All", "scheduled", "ongoing", "completed", "cancelled");
        statusFilter.setValue("All");
        
        filteredClasses = new FilteredList<>(allClasses, p -> true);
        classesTable.setItems(filteredClasses);
    }

    @FXML
    private void handleBack(ActionEvent event) {
        NavigationUtil.switchScene(event, "/view/member/member-dashboard.fxml");
    }

    @FXML
    private void handleFilter(ActionEvent event) {
        String status = statusFilter.getValue();
        if (status == null || "All".equals(status)) {
            filteredClasses.setPredicate(p -> true);
        } else {
            filteredClasses.setPredicate(p -> status.equalsIgnoreCase(p.getStatus()));
        }
    }

    @FXML
    private void handleBook(ActionEvent event) {
        ClassData selected = classesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select a class to book.");
            return;
        }

        if (!"scheduled".equals(selected.getStatus())) {
            showError("You can only book scheduled classes.");
            return;
        }

        if (selected.getAvailableSpots() <= 0) {
            showError("This class is full. No available spots.");
            return;
        }

        int memberId = SessionManager.getInstance().getCurrentUserId();
        String sql = "INSERT INTO bookings (user_id, class_session_id, status) VALUES (?, ?, 'confirmed') " +
                     "ON DUPLICATE KEY UPDATE status = 'confirmed'";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, memberId);
            stmt.setInt(2, selected.getId());
            stmt.executeUpdate();
            
            // Update enrollment count
            String updateSql = "UPDATE class_sessions SET current_enrollment = current_enrollment + 1 WHERE id = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setInt(1, selected.getId());
                updateStmt.executeUpdate();
            }
            
            showSuccess("Class booked successfully!");
            loadClasses();
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate")) {
                showError("You have already booked this class.");
            } else {
                showError("Error booking class: " + e.getMessage());
            }
        }
    }

    private void loadClasses() {
        allClasses.clear();
        String sql = "SELECT cs.id, cs.title, u.full_name as trainer_name, cs.start_time, cs.end_time, " +
                     "cs.capacity, cs.current_enrollment, cs.status " +
                     "FROM class_sessions cs " +
                     "JOIN users u ON cs.trainer_id = u.id " +
                     "WHERE cs.is_active = TRUE " +
                     "ORDER BY cs.start_time DESC";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                int capacity = rs.getInt("capacity");
                int enrolled = rs.getInt("current_enrollment");
                ClassData classData = new ClassData(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("trainer_name"),
                    rs.getTimestamp("start_time"),
                    rs.getTimestamp("end_time"),
                    capacity,
                    enrolled,
                    capacity - enrolled,
                    rs.getString("status")
                );
                allClasses.add(classData);
            }
            handleFilter(null);
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

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
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
        private int availableSpots;
        private String status;

        public ClassData(int id, String title, String trainerName, Timestamp startTime, Timestamp endTime, 
                        int capacity, int enrolled, int availableSpots, String status) {
            this.id = id;
            this.title = title;
            this.trainerName = trainerName;
            this.startTime = startTime;
            this.endTime = endTime;
            this.capacity = capacity;
            this.enrolled = enrolled;
            this.availableSpots = availableSpots;
            this.status = status;
        }

        public int getId() { return id; }
        public String getTitle() { return title; }
        public String getTrainerName() { return trainerName; }
        public Timestamp getStartTime() { return startTime; }
        public Timestamp getEndTime() { return endTime; }
        public int getCapacity() { return capacity; }
        public int getEnrolled() { return enrolled; }
        public int getAvailableSpots() { return availableSpots; }
        public String getStatus() { return status; }
        public String getStartTimeStr() { return startTime != null ? startTime.toString() : ""; }
        public String getEndTimeStr() { return endTime != null ? endTime.toString() : ""; }
    }
}

