package com.delivery.fooddeliverysystem.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/delivery/fooddeliverysystem/fxml/dashboard.fxml"));
        Scene scene = new Scene(loader.load(), 1050, 680);
        stage.setTitle("🍔 FoodDash — Delivery System");
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.show();
    }
}
