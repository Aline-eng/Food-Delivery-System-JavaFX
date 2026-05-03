package com.delivery.fooddeliverysystem.ui;

import com.delivery.fooddeliverysystem.util.ViewLoader;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(ViewLoader.fxml("login.fxml"));
        Scene scene = new Scene(loader.load(), 420, 460);
        stage.setTitle("🍔 FoodDash — Login");
        stage.setScene(scene);
        stage.setMinWidth(400);
        stage.setMinHeight(400);
        stage.show();
    }
}
