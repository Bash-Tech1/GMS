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

public class DietChartsController implements Initializable {

    @FXML private TableView<DietChartData> dietChartsTable;
    private ObservableList<DietChartData> dietCharts = FXCollections.observableArrayList();
    private int trainerId;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        trainerId = SessionManager.getInstance().getCurrentUserId();
        setupTable();
        loadDietCharts();
    }

    private void setupTable() {
        dietChartsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    @FXML
    private void handleBack(ActionEvent event) {
        NavigationUtil.switchScene(event, "/view/trainer/trainer-dashboard.fxml");
    }

    @FXML
    private void handleCreate(ActionEvent event) {
        showDietChartDialog(null);
    }

    @FXML
    private void handleEdit(ActionEvent event) {
        DietChartData selected = dietChartsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select a diet chart to edit.");
            return;
        }
        showDietChartDialog(selected);
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        DietChartData selected = dietChartsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select a diet chart to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Diet Chart");
        confirm.setContentText("Are you sure you want to delete this diet chart?");
        
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            String sql = "DELETE FROM diet_charts WHERE id = ? AND trainer_id = ?";
            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, selected.getId());
                stmt.setInt(2, trainerId);
                stmt.executeUpdate();
                showSuccess("Diet chart deleted successfully!");
                loadDietCharts();
            } catch (SQLException e) {
                showError("Error deleting diet chart: " + e.getMessage());
            }
        }
    }

    private void loadDietCharts() {
        dietCharts.clear();
        String sql = "SELECT dc.id, u.full_name as member_name, dc.recommendations, dc.is_active, dc.created_at " +
                     "FROM diet_charts dc " +
                     "JOIN users u ON dc.user_id = u.id " +
                     "WHERE dc.trainer_id = ? ORDER BY dc.created_at DESC";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, trainerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    DietChartData chart = new DietChartData(
                        rs.getInt("id"),
                        rs.getString("member_name"),
                        rs.getString("recommendations"),
                        rs.getBoolean("is_active"),
                        rs.getTimestamp("created_at")
                    );
                    dietCharts.add(chart);
                }
            }
            dietChartsTable.setItems(dietCharts);
        } catch (SQLException e) {
            showError("Error loading diet charts: " + e.getMessage());
        }
    }

    private void showDietChartDialog(DietChartData existing) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Create Diet Chart" : "Edit Diet Chart");
        dialog.setHeaderText(null);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        ComboBox<String> memberCombo = new ComboBox<>();
        loadMembers(memberCombo);
        TextArea recommendationsField = new TextArea();
        recommendationsField.setPrefRowCount(5);

        if (existing != null) {
            memberCombo.setValue(existing.getMemberName());
            recommendationsField.setText(existing.getRecommendations());
        }

        grid.add(new Label("Member:"), 0, 0);
        grid.add(memberCombo, 1, 0);
        grid.add(new Label("Recommendations:"), 0, 1);
        grid.add(recommendationsField, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        if (dialog.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                String memberSelection = memberCombo.getValue();
                String recommendations = recommendationsField.getText().trim();

                if (memberSelection == null || recommendations.isEmpty()) {
                    showError("Please fill all fields.");
                    return;
                }

                int memberId = Integer.parseInt(memberSelection.split(" - ")[0]);

                String sql;
                if (existing == null) {
                    sql = "INSERT INTO diet_charts (user_id, trainer_id, recommendations) VALUES (?, ?, ?)";
                } else {
                    sql = "UPDATE diet_charts SET recommendations = ? WHERE id = ? AND trainer_id = ?";
                }

                try (Connection conn = DatabaseConnection.getInstance().getConnection();
                     PreparedStatement stmt = conn.prepareStatement(sql)) {
                    if (existing == null) {
                        stmt.setInt(1, memberId);
                        stmt.setInt(2, trainerId);
                        stmt.setString(3, recommendations);
                    } else {
                        stmt.setString(1, recommendations);
                        stmt.setInt(2, existing.getId());
                        stmt.setInt(3, trainerId);
                    }
                    stmt.executeUpdate();
                    showSuccess(existing == null ? "Diet chart created successfully!" : "Diet chart updated successfully!");
                    loadDietCharts();
                }
            } catch (SQLException e) {
                showError("Error saving diet chart: " + e.getMessage());
            }
        }
    }

    private void loadMembers(ComboBox<String> combo) {
        combo.getItems().clear();
        String sql = "SELECT id, full_name FROM users WHERE role = 'member' AND is_active = TRUE ORDER BY full_name";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                combo.getItems().add(rs.getInt("id") + " - " + rs.getString("full_name"));
            }
        } catch (SQLException e) {
            showError("Error loading members: " + e.getMessage());
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

    public static class DietChartData {
        private int id;
        private String memberName;
        private String recommendations;
        private boolean active;
        private Timestamp createdAt;

        public DietChartData(int id, String memberName, String recommendations, boolean active, Timestamp createdAt) {
            this.id = id;
            this.memberName = memberName;
            this.recommendations = recommendations;
            this.active = active;
            this.createdAt = createdAt;
        }

        public int getId() { return id; }
        public String getMemberName() { return memberName; }
        public String getRecommendations() { return recommendations; }
        public String getStatus() { return active ? "Active" : "Inactive"; }
        public Timestamp getCreatedAt() { return createdAt; }
        public String getCreatedAtStr() { return createdAt != null ? createdAt.toString() : ""; }
    }
}

