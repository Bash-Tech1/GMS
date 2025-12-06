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

public class ClassEquipmentController implements Initializable {

    @FXML private ComboBox<String> classCombo;
    @FXML private TableView<EquipmentData> equipmentTable;
    
    private ObservableList<EquipmentData> equipment = FXCollections.observableArrayList();
    private int trainerId;
    private int selectedClassId = -1;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        trainerId = SessionManager.getInstance().getCurrentUserId();
        setupTable();
        loadClasses();
        classCombo.setOnAction(e -> loadEquipmentForClass());
    }

    private void setupTable() {
        equipmentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    @FXML
    private void handleBack(ActionEvent event) {
        NavigationUtil.switchScene(event, "/view/trainer/trainer-dashboard.fxml");
    }

    @FXML
    private void handleAdd(ActionEvent event) {
        if (selectedClassId == -1) {
            showError("Please select a class first.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add Equipment");
        dialog.setHeaderText(null);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        ComboBox<String> equipmentCombo = new ComboBox<>();
        loadEquipmentCombo(equipmentCombo);
        TextField quantityField = new TextField();
        quantityField.setText("1");

        grid.add(new Label("Equipment:"), 0, 0);
        grid.add(equipmentCombo, 1, 0);
        grid.add(new Label("Quantity:"), 0, 1);
        grid.add(quantityField, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        if (dialog.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                String equipmentSelection = equipmentCombo.getValue();
                if (equipmentSelection == null) {
                    showError("Please select equipment.");
                    return;
                }
                int equipmentId = Integer.parseInt(equipmentSelection.split(" - ")[0]);
                int quantity = Integer.parseInt(quantityField.getText().trim());

                String sql = "INSERT INTO class_session_equipment (class_session_id, equipment_id, quantity_needed) VALUES (?, ?, ?) " +
                             "ON DUPLICATE KEY UPDATE quantity_needed = ?";
                
                try (Connection conn = DatabaseConnection.getInstance().getConnection();
                     PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, selectedClassId);
                    stmt.setInt(2, equipmentId);
                    stmt.setInt(3, quantity);
                    stmt.setInt(4, quantity);
                    stmt.executeUpdate();
                    showSuccess("Equipment added successfully!");
                    loadEquipmentForClass();
                }
            } catch (NumberFormatException e) {
                showError("Quantity must be a valid number.");
            } catch (SQLException e) {
                showError("Error adding equipment: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleRemove(ActionEvent event) {
        EquipmentData selected = equipmentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select equipment to remove.");
            return;
        }

        String sql = "DELETE FROM class_session_equipment WHERE class_session_id = ? AND equipment_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, selectedClassId);
            stmt.setInt(2, selected.getEquipmentId());
            stmt.executeUpdate();
            showSuccess("Equipment removed successfully!");
            loadEquipmentForClass();
        } catch (SQLException e) {
            showError("Error removing equipment: " + e.getMessage());
        }
    }

    private void loadClasses() {
        classCombo.getItems().clear();
        String sql = "SELECT id, title FROM class_sessions WHERE trainer_id = ? ORDER BY start_time DESC";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, trainerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    classCombo.getItems().add(rs.getInt("id") + " - " + rs.getString("title"));
                }
            }
        } catch (SQLException e) {
            showError("Error loading classes: " + e.getMessage());
        }
    }

    private void loadEquipmentCombo(ComboBox<String> combo) {
        combo.getItems().clear();
        String sql = "SELECT id, name FROM equipment WHERE status = 'active' ORDER BY name";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                combo.getItems().add(rs.getInt("id") + " - " + rs.getString("name"));
            }
        } catch (SQLException e) {
            showError("Error loading equipment: " + e.getMessage());
        }
    }

    private void loadEquipmentForClass() {
        String selection = classCombo.getValue();
        if (selection == null) {
            selectedClassId = -1;
            equipment.clear();
            return;
        }

        selectedClassId = Integer.parseInt(selection.split(" - ")[0]);
        equipment.clear();
        
        String sql = "SELECT e.id, e.name, cse.quantity_needed " +
                     "FROM class_session_equipment cse " +
                     "JOIN equipment e ON cse.equipment_id = e.id " +
                     "WHERE cse.class_session_id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, selectedClassId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    EquipmentData eq = new EquipmentData(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("quantity_needed")
                    );
                    equipment.add(eq);
                }
            }
            equipmentTable.setItems(equipment);
        } catch (SQLException e) {
            showError("Error loading equipment: " + e.getMessage());
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

    public static class EquipmentData {
        private int equipmentId;
        private String equipmentName;
        private int quantity;

        public EquipmentData(int equipmentId, String equipmentName, int quantity) {
            this.equipmentId = equipmentId;
            this.equipmentName = equipmentName;
            this.quantity = quantity;
        }

        public int getEquipmentId() { return equipmentId; }
        public String getEquipmentName() { return equipmentName; }
        public int getQuantity() { return quantity; }
    }
}

