package com.gym.controller.admin;

import com.gym.util.DatabaseConnection;
import com.gym.util.NavigationUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class EquipmentManagementController implements Initializable {

    @FXML private TableView<EquipmentData> equipmentTable;
    @FXML private ComboBox<String> statusFilter;

    private ObservableList<EquipmentData> allEquipment = FXCollections.observableArrayList();
    private FilteredList<EquipmentData> filteredEquipment;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupFilters();
        loadEquipment();
    }

    private void setupFilters() {
        statusFilter.getItems().addAll("All", "active", "maintenance", "retired");
        statusFilter.setValue("All");
        
        filteredEquipment = new FilteredList<>(allEquipment, p -> true);
        equipmentTable.setItems(filteredEquipment);
        
        equipmentTable.setRowFactory(tv -> {
            TableRow<EquipmentData> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    showEditDialog(row.getItem());
                }
            });
            return row;
        });
    }

    @FXML
    private void handleBack(ActionEvent event) {
        NavigationUtil.switchScene(event, "/view/admin/admin-dashboard.fxml");
    }

    @FXML
    private void handleAddEquipment(ActionEvent event) {
        showAddDialog();
    }

    @FXML
    private void handleDeleteEquipment(ActionEvent event) {
        EquipmentData selected = equipmentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select equipment to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Equipment");
        confirm.setContentText("Are you sure you want to delete: " + selected.getName() + "?");
        
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            String sql = "DELETE FROM equipment WHERE id = ?";
            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, selected.getId());
                stmt.executeUpdate();
                showSuccess("Equipment deleted successfully!");
                loadEquipment();
            } catch (SQLException e) {
                showError("Error deleting equipment: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleFilter(ActionEvent event) {
        String status = statusFilter.getValue();
        filteredEquipment.setPredicate(equipment -> 
            status == null || status.equals("All") || equipment.getStatus().equalsIgnoreCase(status)
        );
    }

    private void loadEquipment() {
        allEquipment.clear();
        String sql = "SELECT id, name, description, status, purchase_date, last_maintenance_date, is_active FROM equipment ORDER BY id";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                EquipmentData equipment = new EquipmentData(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getString("status"),
                    rs.getDate("purchase_date"),
                    rs.getDate("last_maintenance_date"),
                    rs.getBoolean("is_active")
                );
                allEquipment.add(equipment);
            }
        } catch (SQLException e) {
            showError("Error loading equipment: " + e.getMessage());
        }
    }

    private void showAddDialog() {
        Dialog<EquipmentData> dialog = createEquipmentDialog(null);
        dialog.showAndWait().ifPresent(equipment -> {
            if (saveEquipment(equipment, true)) {
                loadEquipment();
            }
        });
    }

    private void showEditDialog(EquipmentData equipment) {
        Dialog<EquipmentData> dialog = createEquipmentDialog(equipment);
        dialog.showAndWait().ifPresent(updatedEquipment -> {
            if (saveEquipment(updatedEquipment, false)) {
                loadEquipment();
            }
        });
    }

    private Dialog<EquipmentData> createEquipmentDialog(EquipmentData existingEquipment) {
        Dialog<EquipmentData> dialog = new Dialog<>();
        dialog.setTitle(existingEquipment == null ? "Add Equipment" : "Edit Equipment");
        dialog.setHeaderText(null);

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField();
        nameField.setPromptText("Equipment Name");
        TextArea descriptionField = new TextArea();
        descriptionField.setPromptText("Description");
        descriptionField.setPrefRowCount(3);
        ComboBox<String> statusCombo = new ComboBox<>(FXCollections.observableArrayList("active", "maintenance", "retired"));
        statusCombo.setPromptText("Status");
        DatePicker purchaseDatePicker = new DatePicker();
        DatePicker maintenanceDatePicker = new DatePicker();
        CheckBox activeCheck = new CheckBox("Active");

        if (existingEquipment != null) {
            nameField.setText(existingEquipment.getName());
            descriptionField.setText(existingEquipment.getDescription());
            statusCombo.setValue(existingEquipment.getStatus());
            if (existingEquipment.getPurchaseDate() != null) {
                purchaseDatePicker.setValue(existingEquipment.getPurchaseDate().toLocalDate());
            }
            if (existingEquipment.getLastMaintenanceDate() != null) {
                maintenanceDatePicker.setValue(existingEquipment.getLastMaintenanceDate().toLocalDate());
            }
            activeCheck.setSelected(existingEquipment.isActive());
        }

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descriptionField, 1, 1);
        grid.add(new Label("Status:"), 0, 2);
        grid.add(statusCombo, 1, 2);
        grid.add(new Label("Purchase Date:"), 0, 3);
        grid.add(purchaseDatePicker, 1, 3);
        grid.add(new Label("Last Maintenance:"), 0, 4);
        grid.add(maintenanceDatePicker, 1, 4);
        grid.add(activeCheck, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return new EquipmentData(
                    existingEquipment != null ? existingEquipment.getId() : 0,
                    nameField.getText(),
                    descriptionField.getText(),
                    statusCombo.getValue(),
                    purchaseDatePicker.getValue() != null ? Date.valueOf(purchaseDatePicker.getValue()) : null,
                    maintenanceDatePicker.getValue() != null ? Date.valueOf(maintenanceDatePicker.getValue()) : null,
                    activeCheck.isSelected()
                );
            }
            return null;
        });

        return dialog;
    }

    private boolean saveEquipment(EquipmentData equipment, boolean isNew) {
        String sql;
        if (isNew) {
            sql = "INSERT INTO equipment (name, description, status, purchase_date, last_maintenance_date, is_active) VALUES (?, ?, ?, ?, ?, ?)";
        } else {
            sql = "UPDATE equipment SET name=?, description=?, status=?, purchase_date=?, last_maintenance_date=?, is_active=? WHERE id=?";
        }

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, equipment.getName());
            stmt.setString(2, equipment.getDescription());
            stmt.setString(3, equipment.getStatus());
            if (equipment.getPurchaseDate() != null) {
                stmt.setDate(4, equipment.getPurchaseDate());
            } else {
                stmt.setNull(4, Types.DATE);
            }
            if (equipment.getLastMaintenanceDate() != null) {
                stmt.setDate(5, equipment.getLastMaintenanceDate());
            } else {
                stmt.setNull(5, Types.DATE);
            }
            stmt.setBoolean(6, equipment.isActive());
            
            if (!isNew) {
                stmt.setInt(7, equipment.getId());
            }

            stmt.executeUpdate();
            showSuccess(isNew ? "Equipment added successfully!" : "Equipment updated successfully!");
            return true;
        } catch (SQLException e) {
            showError("Error saving equipment: " + e.getMessage());
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

    public static class EquipmentData {
        private int id;
        private String name;
        private String description;
        private String status;
        private Date purchaseDate;
        private Date lastMaintenanceDate;
        private boolean active;

        public EquipmentData(int id, String name, String description, String status, Date purchaseDate, Date lastMaintenanceDate, boolean active) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.status = status;
            this.purchaseDate = purchaseDate;
            this.lastMaintenanceDate = lastMaintenanceDate;
            this.active = active;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description != null ? description : ""; }
        public String getStatus() { return status; }
        public Date getPurchaseDate() { return purchaseDate; }
        public Date getLastMaintenanceDate() { return lastMaintenanceDate; }
        public boolean isActive() { return active; }
        
        public String getPurchaseDateStr() {
            return purchaseDate != null ? purchaseDate.toString() : "";
        }
        
        public String getLastMaintenanceStr() {
            return lastMaintenanceDate != null ? lastMaintenanceDate.toString() : "";
        }
    }
}

