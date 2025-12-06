package com.gym.controller.trainer;

import com.gym.util.DatabaseConnection;
import com.gym.util.NavigationUtil;
import com.gym.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class MyClassesController implements Initializable {

    @FXML private TableView<ClassData> classesTable;
    private ObservableList<ClassData> classes = FXCollections.observableArrayList();
    private int trainerId;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        trainerId = SessionManager.getInstance().getCurrentUserId();
        setupTable();
        loadClasses();
    }

    private void setupTable() {
        classesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    @FXML
    private void handleBack(ActionEvent event) {
        NavigationUtil.switchScene(event, "/view/trainer/trainer-dashboard.fxml");
    }

    @FXML
    private void handleCreate(ActionEvent event) {
        showClassDialog(null);
    }

    @FXML
    private void handleEdit(ActionEvent event) {
        ClassData selected = classesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select a class to edit.");
            return;
        }
        showClassDialog(selected);
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        ClassData selected = classesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select a class to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Class");
        confirm.setContentText("Are you sure you want to delete: " + selected.getTitle() + "?");
        
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            String sql = "DELETE FROM class_sessions WHERE id = ? AND trainer_id = ?";
            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, selected.getId());
                stmt.setInt(2, trainerId);
                stmt.executeUpdate();
                showSuccess("Class deleted successfully!");
                loadClasses();
            } catch (SQLException e) {
                showError("Error deleting class: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleEquipment(ActionEvent event) {
        ClassData selected = classesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select a class to manage equipment.");
            return;
        }
        // Store selected class ID in session or pass as parameter
        NavigationUtil.switchScene(event, "/view/trainer/class-equipment.fxml");
    }

    private void loadClasses() {
        classes.clear();
        String sql = "SELECT id, title, description, start_time, end_time, capacity, current_enrollment, status " +
                     "FROM class_sessions WHERE trainer_id = ? ORDER BY start_time DESC";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, trainerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ClassData classData = new ClassData(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getTimestamp("start_time"),
                        rs.getTimestamp("end_time"),
                        rs.getInt("capacity"),
                        rs.getInt("current_enrollment"),
                        rs.getString("status")
                    );
                    classes.add(classData);
                }
            }
            classesTable.setItems(classes);
        } catch (SQLException e) {
            showError("Error loading classes: " + e.getMessage());
        }
    }

    private void showClassDialog(ClassData existing) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Create Class" : "Edit Class");
        dialog.setHeaderText(null);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField titleField = new TextField();
        TextArea descriptionField = new TextArea();
        descriptionField.setPrefRowCount(3);
        TextField startTimeField = new TextField();
        startTimeField.setPromptText("YYYY-MM-DD HH:MM");
        TextField endTimeField = new TextField();
        endTimeField.setPromptText("YYYY-MM-DD HH:MM");
        TextField capacityField = new TextField();
        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("scheduled", "ongoing", "completed", "cancelled");

        if (existing != null) {
            titleField.setText(existing.getTitle());
            descriptionField.setText(existing.getDescription());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            startTimeField.setText(existing.getStartTime().toLocalDateTime().format(formatter));
            endTimeField.setText(existing.getEndTime().toLocalDateTime().format(formatter));
            capacityField.setText(String.valueOf(existing.getCapacity()));
            statusCombo.setValue(existing.getStatus());
        }

        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descriptionField, 1, 1);
        grid.add(new Label("Start Time (YYYY-MM-DD HH:MM):"), 0, 2);
        grid.add(startTimeField, 1, 2);
        grid.add(new Label("End Time (YYYY-MM-DD HH:MM):"), 0, 3);
        grid.add(endTimeField, 1, 3);
        grid.add(new Label("Capacity:"), 0, 4);
        grid.add(capacityField, 1, 4);
        grid.add(new Label("Status:"), 0, 5);
        grid.add(statusCombo, 1, 5);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        if (dialog.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                String title = titleField.getText().trim();
                String description = descriptionField.getText().trim();
                Timestamp startTime = Timestamp.valueOf(startTimeField.getText().trim() + ":00");
                Timestamp endTime = Timestamp.valueOf(endTimeField.getText().trim() + ":00");
                int capacity = Integer.parseInt(capacityField.getText().trim());
                String status = statusCombo.getValue();

                if (title.isEmpty() || capacity <= 0 || status == null) {
                    showError("Please fill all fields correctly.");
                    return;
                }

                String sql;
                if (existing == null) {
                    sql = "INSERT INTO class_sessions (trainer_id, title, description, start_time, end_time, capacity, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
                } else {
                    sql = "UPDATE class_sessions SET title = ?, description = ?, start_time = ?, end_time = ?, capacity = ?, status = ? WHERE id = ? AND trainer_id = ?";
                }

                try (Connection conn = DatabaseConnection.getInstance().getConnection();
                     PreparedStatement stmt = conn.prepareStatement(sql)) {
                    if (existing == null) {
                        stmt.setInt(1, trainerId);
                        stmt.setString(2, title);
                        stmt.setString(3, description);
                        stmt.setTimestamp(4, startTime);
                        stmt.setTimestamp(5, endTime);
                        stmt.setInt(6, capacity);
                        stmt.setString(7, status);
                    } else {
                        stmt.setString(1, title);
                        stmt.setString(2, description);
                        stmt.setTimestamp(3, startTime);
                        stmt.setTimestamp(4, endTime);
                        stmt.setInt(5, capacity);
                        stmt.setString(6, status);
                        stmt.setInt(7, existing.getId());
                        stmt.setInt(8, trainerId);
                    }
                    stmt.executeUpdate();
                    showSuccess(existing == null ? "Class created successfully!" : "Class updated successfully!");
                    loadClasses();
                }
            } catch (Exception e) {
                showError("Error saving class: " + e.getMessage());
            }
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
        private String description;
        private Timestamp startTime;
        private Timestamp endTime;
        private int capacity;
        private int enrolled;
        private String status;

        public ClassData(int id, String title, String description, Timestamp startTime, Timestamp endTime, int capacity, int enrolled, String status) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.startTime = startTime;
            this.endTime = endTime;
            this.capacity = capacity;
            this.enrolled = enrolled;
            this.status = status;
        }

        public int getId() { return id; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public Timestamp getStartTime() { return startTime; }
        public Timestamp getEndTime() { return endTime; }
        public int getCapacity() { return capacity; }
        public int getEnrolled() { return enrolled; }
        public String getStatus() { return status; }
        public String getStartTimeStr() { return startTime != null ? startTime.toString() : ""; }
        public String getEndTimeStr() { return endTime != null ? endTime.toString() : ""; }
    }
}

