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
import java.util.ResourceBundle;

public class WorkoutPlansController implements Initializable {

    @FXML private TableView<WorkoutPlanData> workoutPlansTable;
    private ObservableList<WorkoutPlanData> workoutPlans = FXCollections.observableArrayList();
    private int trainerId;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        trainerId = SessionManager.getInstance().getCurrentUserId();
        setupTable();
        loadWorkoutPlans();
    }

    private void setupTable() {
        workoutPlansTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        workoutPlansTable.setRowFactory(tv -> {
            TableRow<WorkoutPlanData> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    handleEdit(null);
                }
            });
            return row;
        });
    }

    @FXML
    private void handleBack(ActionEvent event) {
        NavigationUtil.switchScene(event, "/view/trainer/trainer-dashboard.fxml");
    }

    @FXML
    private void handleCreate(ActionEvent event) {
        showWorkoutPlanDialog(null);
    }

    @FXML
    private void handleEdit(ActionEvent event) {
        WorkoutPlanData selected = workoutPlansTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select a workout plan to edit.");
            return;
        }
        showWorkoutPlanDialog(selected);
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        WorkoutPlanData selected = workoutPlansTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select a workout plan to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Workout Plan");
        confirm.setContentText("Are you sure you want to delete: " + selected.getName() + "?");
        
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            String sql = "DELETE FROM workout_plans WHERE id = ? AND trainer_id = ?";
            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, selected.getId());
                stmt.setInt(2, trainerId);
                stmt.executeUpdate();
                showSuccess("Workout plan deleted successfully!");
                loadWorkoutPlans();
            } catch (SQLException e) {
                showError("Error deleting workout plan: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleAssign(ActionEvent event) {
        WorkoutPlanData selected = workoutPlansTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select a workout plan to assign.");
            return;
        }
        NavigationUtil.switchScene(event, "/view/trainer/assign-workout-plan.fxml");
    }

    private void loadWorkoutPlans() {
        workoutPlans.clear();
        String sql = "SELECT id, name, description, duration, difficulty, is_active, created_at " +
                     "FROM workout_plans WHERE trainer_id = ? ORDER BY created_at DESC";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, trainerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    WorkoutPlanData plan = new WorkoutPlanData(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getInt("duration"),
                        rs.getString("difficulty"),
                        rs.getBoolean("is_active"),
                        rs.getTimestamp("created_at")
                    );
                    workoutPlans.add(plan);
                }
            }
            workoutPlansTable.setItems(workoutPlans);
        } catch (SQLException e) {
            showError("Error loading workout plans: " + e.getMessage());
        }
    }

    private void showWorkoutPlanDialog(WorkoutPlanData existing) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Create Workout Plan" : "Edit Workout Plan");
        dialog.setHeaderText(null);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField();
        TextArea descriptionField = new TextArea();
        descriptionField.setPrefRowCount(3);
        TextField durationField = new TextField();
        ComboBox<String> difficultyCombo = new ComboBox<>();
        difficultyCombo.getItems().addAll("beginner", "intermediate", "advanced");

        if (existing != null) {
            nameField.setText(existing.getName());
            descriptionField.setText(existing.getDescription());
            durationField.setText(String.valueOf(existing.getDuration()));
            difficultyCombo.setValue(existing.getDifficulty());
        }

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descriptionField, 1, 1);
        grid.add(new Label("Duration (days):"), 0, 2);
        grid.add(durationField, 1, 2);
        grid.add(new Label("Difficulty:"), 0, 3);
        grid.add(difficultyCombo, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        if (dialog.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                String name = nameField.getText().trim();
                String description = descriptionField.getText().trim();
                int duration = Integer.parseInt(durationField.getText().trim());
                String difficulty = difficultyCombo.getValue();

                if (name.isEmpty() || duration <= 0 || difficulty == null) {
                    showError("Please fill all fields correctly.");
                    return;
                }

                String sql;
                if (existing == null) {
                    sql = "INSERT INTO workout_plans (name, trainer_id, description, duration, difficulty) VALUES (?, ?, ?, ?, ?)";
                } else {
                    sql = "UPDATE workout_plans SET name = ?, description = ?, duration = ?, difficulty = ? WHERE id = ? AND trainer_id = ?";
                }

                try (Connection conn = DatabaseConnection.getInstance().getConnection();
                     PreparedStatement stmt = conn.prepareStatement(sql)) {
                    if (existing == null) {
                        stmt.setString(1, name);
                        stmt.setInt(2, trainerId);
                        stmt.setString(3, description);
                        stmt.setInt(4, duration);
                        stmt.setString(5, difficulty);
                    } else {
                        stmt.setString(1, name);
                        stmt.setString(2, description);
                        stmt.setInt(3, duration);
                        stmt.setString(4, difficulty);
                        stmt.setInt(5, existing.getId());
                        stmt.setInt(6, trainerId);
                    }
                    stmt.executeUpdate();
                    showSuccess(existing == null ? "Workout plan created successfully!" : "Workout plan updated successfully!");
                    loadWorkoutPlans();
                }
            } catch (NumberFormatException e) {
                showError("Duration must be a valid number.");
            } catch (SQLException e) {
                showError("Error saving workout plan: " + e.getMessage());
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

    public static class WorkoutPlanData {
        private int id;
        private String name;
        private String description;
        private int duration;
        private String difficulty;
        private boolean active;
        private Timestamp createdAt;

        public WorkoutPlanData(int id, String name, String description, int duration, String difficulty, boolean active, Timestamp createdAt) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.duration = duration;
            this.difficulty = difficulty;
            this.active = active;
            this.createdAt = createdAt;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public int getDuration() { return duration; }
        public String getDifficulty() { return difficulty; }
        public String getStatus() { return active ? "Active" : "Inactive"; }
        public Timestamp getCreatedAt() { return createdAt; }
        public String getCreatedAtStr() { return createdAt != null ? createdAt.toString() : ""; }
    }
}

