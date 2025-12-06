package com.gym;

import com.gym.util.DatabaseConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.sql.Connection;

public class GymApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Test DB connection on startup (non-fatal)
        try (Connection ignored = DatabaseConnection.getInstance().getConnection()) {
            System.out.println("Database connection OK");
        } catch (Exception e) {
            System.err.println("Database connection failed: " + e.getMessage());
        }

        Parent root = FXMLLoader.load(getClass().getResource("/view/auth/login.fxml"));
        Scene scene = new Scene(root, 900, 600);
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        primaryStage.setTitle("Gym Management System");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}



