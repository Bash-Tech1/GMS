package com.gym.controller.member;

import com.gym.util.NavigationUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;

import java.net.URL;
import java.util.ResourceBundle;

public class NutritionTipsController implements Initializable {

    @FXML private TextArea tipsArea;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadTips();
    }

    @FXML
    private void handleBack(ActionEvent event) {
        NavigationUtil.switchScene(event, "/view/member/member-dashboard.fxml");
    }

    private void loadTips() {
        String tips = "🥗 NUTRITION TIPS FOR FITNESS SUCCESS\n\n" +
                     "1. Stay Hydrated\n" +
                     "   • Drink at least 8-10 glasses of water daily\n" +
                     "   • Increase intake during workouts\n" +
                     "   • Water helps with recovery and performance\n\n" +
                     "2. Balanced Meals\n" +
                     "   • Include protein, carbs, and healthy fats\n" +
                     "   • Eat 5-6 small meals throughout the day\n" +
                     "   • Don't skip breakfast\n\n" +
                     "3. Pre-Workout Nutrition\n" +
                     "   • Eat a light meal 1-2 hours before exercise\n" +
                     "   • Include carbs for energy\n" +
                     "   • Avoid heavy, fatty foods\n\n" +
                     "4. Post-Workout Recovery\n" +
                     "   • Consume protein within 30 minutes after workout\n" +
                     "   • Include carbs to replenish glycogen\n" +
                     "   • Examples: protein shake, chicken with rice\n\n" +
                     "5. Healthy Snacking\n" +
                     "   • Choose nuts, fruits, or yogurt\n" +
                     "   • Avoid processed snacks\n" +
                     "   • Keep healthy options readily available\n\n" +
                     "6. Meal Timing\n" +
                     "   • Eat every 3-4 hours\n" +
                     "   • Don't go more than 5 hours without eating\n" +
                     "   • Plan meals ahead of time\n\n" +
                     "7. Portion Control\n" +
                     "   • Use smaller plates\n" +
                     "   • Listen to your body's hunger cues\n" +
                     "   • Avoid overeating\n\n" +
                     "Remember: Nutrition is 70% of your fitness journey!";
        
        tipsArea.setText(tips);
    }
}

