package com.gym.controller.member;

import com.gym.util.DatabaseConnection;
import com.gym.util.NavigationUtil;
import com.gym.util.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class MyDietChartController implements Initializable {

    @FXML private TextArea recommendationsArea;
    @FXML private Label trainerLabel;
    private int memberId;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        memberId = SessionManager.getInstance().getCurrentUserId();
        loadDietChart();
    }

    @FXML
    private void handleBack(ActionEvent event) {
        NavigationUtil.switchScene(event, "/view/member/member-dashboard.fxml");
    }

    private void loadDietChart() {
        String sql = "SELECT dc.recommendations, t.full_name as trainer_name " +
                     "FROM diet_charts dc " +
                     "JOIN users t ON dc.trainer_id = t.id " +
                     "WHERE dc.user_id = ? AND dc.is_active = TRUE " +
                     "ORDER BY dc.created_at DESC " +
                     "LIMIT 1";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, memberId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    recommendationsArea.setText(rs.getString("recommendations"));
                    trainerLabel.setText(rs.getString("trainer_name"));
                } else {
                    recommendationsArea.setText("No diet chart assigned yet. Please contact your trainer.");
                    trainerLabel.setText("N/A");
                }
            }
        } catch (SQLException e) {
            recommendationsArea.setText("Error loading diet chart: " + e.getMessage());
        }
    }
}

