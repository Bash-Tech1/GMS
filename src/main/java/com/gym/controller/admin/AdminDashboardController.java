package com.gym.controller.admin;

import com.gym.util.NavigationUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class AdminDashboardController {

    @FXML
    private void handleLogout(ActionEvent event) {
        NavigationUtil.switchScene(event, "/view/auth/login.fxml");
    }

    // User Management Functions
    @FXML
    private void handleManageUsers(ActionEvent event) {
        NavigationUtil.switchScene(event, "/view/admin/user-management.fxml");
    }

    @FXML
    private void handleManageTrainers(ActionEvent event) {
        NavigationUtil.switchScene(event, "/view/admin/user-management.fxml");
    }

    @FXML
    private void handleManageMembers(ActionEvent event) {
        NavigationUtil.switchScene(event, "/view/admin/user-management.fxml");
    }

    // Plans & Equipment Functions
    @FXML
    private void handleManagePlans(ActionEvent event) {
        NavigationUtil.switchScene(event, "/view/admin/plan-management.fxml");
    }

    @FXML
    private void handleManageEquipment(ActionEvent event) {
        NavigationUtil.switchScene(event, "/view/admin/equipment-management.fxml");
    }

    @FXML
    private void handleEquipmentStatus(ActionEvent event) {
        NavigationUtil.switchScene(event, "/view/admin/equipment-status.fxml");
    }

    // Reports & Analytics Functions
    @FXML
    private void handleViewReports(ActionEvent event) {
        NavigationUtil.switchScene(event, "/view/admin/reports.fxml");
    }

    @FXML
    private void handleViewPayments(ActionEvent event) {
        NavigationUtil.switchScene(event, "/view/admin/payment-history.fxml");
    }

    @FXML
    private void handleAttendanceReports(ActionEvent event) {
        NavigationUtil.switchScene(event, "/view/admin/attendance-reports.fxml");
    }

    // Classes & Bookings Functions
    @FXML
    private void handleViewClasses(ActionEvent event) {
        NavigationUtil.switchScene(event, "/view/admin/classes-view.fxml");
    }

    @FXML
    private void handleViewBookings(ActionEvent event) {
        NavigationUtil.switchScene(event, "/view/admin/bookings-view.fxml");
    }
}
