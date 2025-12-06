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
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ResourceBundle;

public class EquipmentStatusController implements Initializable {

    @FXML private TableView<EquipmentStatusData> equipmentTable;
    @FXML private Label activeCountLabel;
    @FXML private Label maintenanceCountLabel;
    @FXML private Label retiredCountLabel;

    private ObservableList<EquipmentStatusData> equipment = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        loadEquipmentStatus();
        loadStatusCounts();
    }

    private void setupTable() {
        equipmentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    @FXML
    private void handleBack(ActionEvent event) {
        NavigationUtil.switchScene(event, "/view/admin/admin-dashboard.fxml");
    }

    private void loadStatusCounts() {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            String sql = "SELECT status, COUNT(*) as count FROM equipment GROUP BY status";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                
                while (rs.next()) {
                    String status = rs.getString("status");
                    int count = rs.getInt("count");
                    switch (status) {
                        case "active":
                            activeCountLabel.setText(String.valueOf(count));
                            break;
                        case "maintenance":
                            maintenanceCountLabel.setText(String.valueOf(count));
                            break;
                        case "retired":
                            retiredCountLabel.setText(String.valueOf(count));
                            break;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadEquipmentStatus() {
        equipment.clear();
        String sql = "SELECT id, name, status, last_maintenance_date FROM equipment ORDER BY status, name";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Date lastMaintenance = rs.getDate("last_maintenance_date");
                long daysSince = -1;
                if (lastMaintenance != null) {
                    daysSince = ChronoUnit.DAYS.between(lastMaintenance.toLocalDate(), LocalDate.now());
                }
                
                EquipmentStatusData eq = new EquipmentStatusData(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("status"),
                    lastMaintenance,
                    daysSince
                );
                equipment.add(eq);
            }
            equipmentTable.setItems(equipment);
        } catch (SQLException e) {
            showError("Error loading equipment status: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class EquipmentStatusData {
        private int id;
        private String name;
        private String status;
        private Date lastMaintenance;
        private long daysSince;

        public EquipmentStatusData(int id, String name, String status, Date lastMaintenance, long daysSince) {
            this.id = id;
            this.name = name;
            this.status = status;
            this.lastMaintenance = lastMaintenance;
            this.daysSince = daysSince;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public String getStatus() { return status; }
        public Date getLastMaintenance() { return lastMaintenance; }
        public long getDaysSinceMaintenance() { return daysSince; }
        
        public String getLastMaintenanceStr() {
            return lastMaintenance != null ? lastMaintenance.toString() : "Never";
        }
        
        public String getDaysSinceMaintenanceStr() {
            if (daysSince < 0) return "N/A";
            return daysSince + " days";
        }
    }
}

