package com.gym.controller.admin;

import com.gym.util.DatabaseConnection;
import com.gym.util.NavigationUtil;
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

public class PlanManagementController implements Initializable {

    @FXML private TableView<PlanData> plansTable;
    private ObservableList<PlanData> plans = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        plansTable.setRowFactory(tv -> {
            TableRow<PlanData> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    showEditDialog(row.getItem());
                }
            });
            return row;
        });
        loadPlans();
    }

    @FXML
    private void handleBack(ActionEvent event) {
        NavigationUtil.switchScene(event, "/view/admin/admin-dashboard.fxml");
    }

    @FXML
    private void handleAddPlan(ActionEvent event) {
        showAddDialog();
    }

    @FXML
    private void handleDeletePlan(ActionEvent event) {
        PlanData selected = plansTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select a plan to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Plan");
        confirm.setContentText("Are you sure you want to delete plan: " + selected.getName() + "?");
        
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            String sql = "DELETE FROM plans WHERE id = ?";
            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, selected.getId());
                stmt.executeUpdate();
                showSuccess("Plan deleted successfully!");
                loadPlans();
            } catch (SQLException e) {
                showError("Error deleting plan: " + e.getMessage());
            }
        }
    }

    private void loadPlans() {
        plans.clear();
        String sql = "SELECT id, name, description, price, duration, benefits, is_active FROM plans ORDER BY id";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                PlanData plan = new PlanData(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getDouble("price"),
                    rs.getInt("duration"),
                    rs.getString("benefits"),
                    rs.getBoolean("is_active")
                );
                plans.add(plan);
            }
            plansTable.setItems(plans);
        } catch (SQLException e) {
            showError("Error loading plans: " + e.getMessage());
        }
    }

    private void showAddDialog() {
        Dialog<PlanData> dialog = createPlanDialog(null);
        dialog.showAndWait().ifPresent(plan -> {
            if (savePlan(plan, true)) {
                loadPlans();
            }
        });
    }

    private void showEditDialog(PlanData plan) {
        Dialog<PlanData> dialog = createPlanDialog(plan);
        dialog.showAndWait().ifPresent(updatedPlan -> {
            if (savePlan(updatedPlan, false)) {
                loadPlans();
            }
        });
    }

    private Dialog<PlanData> createPlanDialog(PlanData existingPlan) {
        Dialog<PlanData> dialog = new Dialog<>();
        dialog.setTitle(existingPlan == null ? "Add New Plan" : "Edit Plan");
        dialog.setHeaderText(null);

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField();
        nameField.setPromptText("Plan Name");
        TextArea descriptionField = new TextArea();
        descriptionField.setPromptText("Description");
        descriptionField.setPrefRowCount(3);
        TextField priceField = new TextField();
        priceField.setPromptText("Price");
        TextField durationField = new TextField();
        durationField.setPromptText("Duration (days)");
        TextArea benefitsField = new TextArea();
        benefitsField.setPromptText("Benefits");
        benefitsField.setPrefRowCount(3);
        CheckBox activeCheck = new CheckBox("Active");

        if (existingPlan != null) {
            nameField.setText(existingPlan.getName());
            descriptionField.setText(existingPlan.getDescription());
            priceField.setText(String.valueOf(existingPlan.getPrice()));
            durationField.setText(String.valueOf(existingPlan.getDuration()));
            benefitsField.setText(existingPlan.getBenefits());
            activeCheck.setSelected(existingPlan.isActive());
        }

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descriptionField, 1, 1);
        grid.add(new Label("Price:"), 0, 2);
        grid.add(priceField, 1, 2);
        grid.add(new Label("Duration (days):"), 0, 3);
        grid.add(durationField, 1, 3);
        grid.add(new Label("Benefits:"), 0, 4);
        grid.add(benefitsField, 1, 4);
        grid.add(activeCheck, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    return new PlanData(
                        existingPlan != null ? existingPlan.getId() : 0,
                        nameField.getText(),
                        descriptionField.getText(),
                        Double.parseDouble(priceField.getText()),
                        Integer.parseInt(durationField.getText()),
                        benefitsField.getText(),
                        activeCheck.isSelected()
                    );
                } catch (NumberFormatException e) {
                    showError("Invalid number format");
                    return null;
                }
            }
            return null;
        });

        return dialog;
    }

    private boolean savePlan(PlanData plan, boolean isNew) {
        String sql;
        if (isNew) {
            sql = "INSERT INTO plans (name, description, price, duration, benefits, is_active) VALUES (?, ?, ?, ?, ?, ?)";
        } else {
            sql = "UPDATE plans SET name=?, description=?, price=?, duration=?, benefits=?, is_active=? WHERE id=?";
        }

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, plan.getName());
            stmt.setString(2, plan.getDescription());
            stmt.setDouble(3, plan.getPrice());
            stmt.setInt(4, plan.getDuration());
            stmt.setString(5, plan.getBenefits());
            stmt.setBoolean(6, plan.isActive());
            
            if (!isNew) {
                stmt.setInt(7, plan.getId());
            }

            stmt.executeUpdate();
            showSuccess(isNew ? "Plan added successfully!" : "Plan updated successfully!");
            return true;
        } catch (SQLException e) {
            showError("Error saving plan: " + e.getMessage());
            return false;
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

    public static class PlanData {
        private int id;
        private String name;
        private String description;
        private double price;
        private int duration;
        private String benefits;
        private boolean active;

        public PlanData(int id, String name, String description, double price, int duration, String benefits, boolean active) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.price = price;
            this.duration = duration;
            this.benefits = benefits;
            this.active = active;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public double getPrice() { return price; }
        public int getDuration() { return duration; }
        public String getBenefits() { return benefits; }
        public boolean isActive() { return active; }
    }
}

