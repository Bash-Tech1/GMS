package com.gym.controller.trainer;

import com.gym.util.NavigationUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class TrainerDashboardController {

    @FXML
    private void handleLogout(ActionEvent event) {
        NavigationUtil.switchScene(event, "/view/auth/login.fxml");
    }

    // Workout Plans Functions
    @FXML
    private void handleCreateWorkoutPlan(ActionEvent event) {
        NavigationUtil.switchScene(event, "/view/trainer/workout-plans.fxml");
    }

    @FXML
    private void handleManageWorkoutPlans(ActionEvent event) {
        NavigationUtil.switchScene(event, "/view/trainer/workout-plans.fxml");
    }

    @FXML
    private void handleAssignWorkoutPlans(ActionEvent event) {
        NavigationUtil.switchScene(event, "/view/trainer/assign-workout-plan.fxml");
    }

    // Class Management Functions
    @FXML
    private void handleCreateClass(ActionEvent event) {
        NavigationUtil.switchScene(event, "/view/trainer/my-classes.fxml");
    }

    @FXML
    private void handleManageClasses(ActionEvent event) {
        NavigationUtil.switchScene(event, "/view/trainer/my-classes.fxml");
    }

    @FXML
    private void handleClassEquipment(ActionEvent event) {
        NavigationUtil.switchScene(event, "/view/trainer/class-equipment.fxml");
    }

    // Diet Charts Functions
    @FXML
    private void handleCreateDietChart(ActionEvent event) {
        NavigationUtil.switchScene(event, "/view/trainer/diet-charts.fxml");
    }

    @FXML
    private void handleManageDietCharts(ActionEvent event) {
        NavigationUtil.switchScene(event, "/view/trainer/diet-charts.fxml");
    }

    // Members & Attendance Functions
    @FXML
    private void handleAssignedMembers(ActionEvent event) {
        NavigationUtil.switchScene(event, "/view/trainer/assigned-members.fxml");
    }

    @FXML
    private void handleMarkAttendance(ActionEvent event) {
        NavigationUtil.switchScene(event, "/view/trainer/mark-attendance.fxml");
    }

    @FXML
    private void handleMemberProgress(ActionEvent event) {
        NavigationUtil.switchScene(event, "/view/trainer/member-progress.fxml");
    }

    // Equipment Functions
    @FXML
    private void handleMyEquipment(ActionEvent event) {
        NavigationUtil.switchScene(event, "/view/trainer/my-equipment.fxml");
    }

    @FXML
    private void handleRequestEquipment(ActionEvent event) {
        NavigationUtil.switchScene(event, "/view/trainer/request-equipment.fxml");
    }
}
