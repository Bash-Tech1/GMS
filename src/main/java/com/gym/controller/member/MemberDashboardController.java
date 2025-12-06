package com.gym.controller.member;

import com.gym.util.NavigationUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class MemberDashboardController {

    @FXML
    private void handleLogout(ActionEvent event) {
        NavigationUtil.switchScene(event, "/view/auth/login.fxml");
    }

    // Classes Functions
    @FXML
    private void handleViewClasses(ActionEvent event) {
        NavigationUtil.switchScene(event, "/view/member/view-classes.fxml");
    }

    @FXML
    private void handleBookClass(ActionEvent event) {
        NavigationUtil.switchScene(event, "/view/member/view-classes.fxml");
    }

    @FXML
    private void handleMyBookings(ActionEvent event) {
        NavigationUtil.switchScene(event, "/view/member/my-bookings.fxml");
    }

    // Workout Plans Functions
    @FXML
    private void handleMyWorkoutPlans(ActionEvent event) {
        NavigationUtil.switchScene(event, "/view/member/my-workout-plans.fxml");
    }

    @FXML
    private void handleWorkoutProgress(ActionEvent event) {
        NavigationUtil.switchScene(event, "/view/member/workout-progress.fxml");
    }

    // Diet & Nutrition Functions
    @FXML
    private void handleMyDietChart(ActionEvent event) {
        NavigationUtil.switchScene(event, "/view/member/my-diet-chart.fxml");
    }

    @FXML
    private void handleNutritionTips(ActionEvent event) {
        NavigationUtil.switchScene(event, "/view/member/nutrition-tips.fxml");
    }

    // Payments & Profile Functions
    @FXML
    private void handlePaymentHistory(ActionEvent event) {
        NavigationUtil.switchScene(event, "/view/member/payment-history.fxml");
    }

    @FXML
    private void handleMakePayment(ActionEvent event) {
        NavigationUtil.switchScene(event, "/view/member/make-payment.fxml");
    }

    @FXML
    private void handleMyProfile(ActionEvent event) {
        NavigationUtil.switchScene(event, "/view/member/my-profile.fxml");
    }

    // Attendance & Statistics Functions
    @FXML
    private void handleAttendanceHistory(ActionEvent event) {
        NavigationUtil.switchScene(event, "/view/member/attendance-history.fxml");
    }

    @FXML
    private void handleMyStatistics(ActionEvent event) {
        NavigationUtil.switchScene(event, "/view/member/my-statistics.fxml");
    }
}
