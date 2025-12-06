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

public class RequestEquipmentController implements Initializable {

    @FXML private TableView<EquipmentData> availableEquipmentTable;
    private ObservableList<EquipmentData> availableEquipment = FXCollections.observableArrayList();
    private int trainerId;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        trainerId = SessionManager.getInstance().getCurrentUserId();
        setupTable();
        loadAvailableEquipment();
    }

    private void setupTable() {
        availableEquipmentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    @FXML
    private void handleBack(ActionEvent event) {
        NavigationUtil.switchScene(event, "/view/trainer/trainer-dashboard.fxml");
    }

    @FXML
    private void handleRequest(ActionEvent event) {
        EquipmentData selected = availableEquipmentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select equipment to request.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Request Equipment");
        dialog.setHeaderText(null);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        ComboBox<String> responsibilityCombo = new ComboBox<>();
        responsibilityCombo.getItems().addAll("primary", "secondary", "shared");
        responsibilityCombo.setValue("primary");

        grid.add(new Label("Equipment:"), 0, 0);
        grid.add(new Label(selected.getName()), 1, 0);
        grid.add(new Label("Responsibility Level:"), 0, 1);
        grid.add(responsibilityCombo, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        if (dialog.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            String responsibility = responsibilityCombo.getValue();
            if (responsibility == null) {
                showError("Please select a responsibility level.");
                return;
            }

            String sql = "INSERT INTO trainer_equipment (trainer_id, equipment_id, responsibility_level) VALUES (?, ?, ?) " +
                         "ON DUPLICATE KEY UPDATE responsibility_level = ?, is_active = TRUE";
            
            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, trainerId);
                stmt.setInt(2, selected.getId());
                stmt.setString(3, responsibility);
                stmt.setString(4, responsibility);
                stmt.executeUpdate();
                showSuccess("Equipment request submitted! (Note: Admin approval may be required)");
                loadAvailableEquipment();
            } catch (SQLException e) {
                showError("Error requesting equipment: " + e.getMessage());
            }
        }
    }

    private void loadAvailableEquipment() {
        availableEquipment.clear();
        String sql = "SELECT e.id, e.name, e.description, e.status " +
                     "FROM equipment e " +
                     "WHERE e.status = 'active' AND e.id NOT IN " +
                     "(SELECT equipment_id FROM trainer_equipment WHERE trainer_id = ? AND is_active = TRUE) " +
                     "ORDER BY e.name";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, trainerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    EquipmentData eq = new EquipmentData(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("status")
                    );
                    availableEquipment.add(eq);
                }
            }
            availableEquipmentTable.setItems(availableEquipment);
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
        private int id;
        private String name;
        private String description;
        private String status;

        public EquipmentData(int id, String name, String description, String status) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.status = status;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description != null ? description : ""; }
        public String getStatus() { return status; }
    }
}

