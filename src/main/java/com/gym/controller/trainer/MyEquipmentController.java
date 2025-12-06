package com.gym.controller.trainer;

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

public class MyEquipmentController implements Initializable {

    @FXML private TableView<EquipmentData> equipmentTable;
    private ObservableList<EquipmentData> equipment = FXCollections.observableArrayList();
    private int trainerId;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        trainerId = SessionManager.getInstance().getCurrentUserId();
        setupTable();
        loadEquipment();
    }

    private void setupTable() {
        equipmentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    @FXML
    private void handleBack(ActionEvent event) {
        NavigationUtil.switchScene(event, "/view/trainer/trainer-dashboard.fxml");
    }

    private void loadEquipment() {
        equipment.clear();
        String sql = "SELECT e.name as equipment_name, te.responsibility_level, te.assigned_date, e.status " +
                     "FROM trainer_equipment te " +
                     "JOIN equipment e ON te.equipment_id = e.id " +
                     "WHERE te.trainer_id = ? AND te.is_active = TRUE " +
                     "ORDER BY te.assigned_date DESC";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, trainerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    EquipmentData eq = new EquipmentData(
                        rs.getString("equipment_name"),
                        rs.getString("responsibility_level"),
                        rs.getTimestamp("assigned_date"),
                        rs.getString("status")
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

    public static class EquipmentData {
        private String equipmentName;
        private String responsibility;
        private Timestamp assignedDate;
        private String status;

        public EquipmentData(String equipmentName, String responsibility, Timestamp assignedDate, String status) {
            this.equipmentName = equipmentName;
            this.responsibility = responsibility;
            this.assignedDate = assignedDate;
            this.status = status;
        }

        public String getEquipmentName() { return equipmentName; }
        public String getResponsibility() { return responsibility; }
        public Timestamp getAssignedDate() { return assignedDate; }
        public String getAssignedDateStr() { return assignedDate != null ? assignedDate.toString() : ""; }
        public String getStatus() { return status; }
    }
}

