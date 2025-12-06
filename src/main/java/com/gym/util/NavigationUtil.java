package com.gym.util;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class NavigationUtil {

    private NavigationUtil() {
    }

    public static void switchScene(ActionEvent event, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(NavigationUtil.class.getResource(fxmlPath));
            Node source = (Node) event.getSource();
            Scene currentScene = source.getScene();
            Stage stage = (Stage) currentScene.getWindow();
            
            // Preserve window state BEFORE any changes
            double x = stage.getX();
            double y = stage.getY();
            double width = currentScene.getWidth() > 0 ? currentScene.getWidth() : 900;
            double height = currentScene.getHeight() > 0 ? currentScene.getHeight() : 600;
            boolean wasMaximized = stage.isMaximized();
            boolean wasFullScreen = stage.isFullScreen();
            boolean wasIconified = stage.isIconified();
            
            // Prevent iconification during scene switch
            if (wasIconified) {
                stage.setIconified(false);
            }
            
            // Create new scene with preserved size
            Scene newScene = new Scene(root, width, height);
            newScene.getStylesheets().add(NavigationUtil.class.getResource("/css/styles.css").toExternalForm());
            
            // Set scene
            stage.setScene(newScene);
            
            // Restore window state AFTER setting scene
            if (wasMaximized) {
                stage.setMaximized(true);
            }
            if (wasFullScreen) {
                stage.setFullScreen(true);
            }
            
            // Restore position if not maximized
            if (!wasMaximized && x > 0 && y > 0) {
                stage.setX(x);
                stage.setY(y);
            }
            
            // Ensure window stays visible, focused, and not minimized
            stage.setIconified(false);
            stage.show();
            stage.toFront();
            stage.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


