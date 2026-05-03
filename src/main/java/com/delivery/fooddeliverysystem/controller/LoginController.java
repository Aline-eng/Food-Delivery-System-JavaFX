package com.delivery.fooddeliverysystem.controller;

import com.delivery.fooddeliverysystem.exception.DeliverySystemException;
import com.delivery.fooddeliverysystem.model.UserAccount;
import com.delivery.fooddeliverysystem.util.ViewLoader;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML private TextField fieldUsername;
    @FXML private PasswordField fieldPassword;
    @FXML private Label loginMsg;

    private final AppContext ctx = AppContext.get();

    @Override
    public void initialize(URL url, ResourceBundle rb) {}

    @FXML
    private void handleLogin() {
        loginMsg.setText("");
        try {
            UserAccount user = ctx.authManager.login(
                    fieldUsername.getText().trim(),
                    fieldPassword.getText());
            SessionManager.get().login(user);
            openDashboard();
        } catch (DeliverySystemException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void goToSignup() {
        try {
            FXMLLoader loader = new FXMLLoader(ViewLoader.fxml("signup.fxml"));
            Stage stage = (Stage) fieldUsername.getScene().getWindow();
            stage.setScene(new Scene(loader.load(), 520, 560));
            stage.setTitle("🍔 FoodDash — Create Account");
        } catch (IOException e) {
            showError("Could not open signup screen: " + e.getMessage());
        }
    }

    private void openDashboard() {
        try {
            String fxmlFile = switch (SessionManager.get().getCurrentUser().getRole()) {
                case CUSTOMER -> "customer_dashboard.fxml";
                case DRIVER   -> "driver_dashboard.fxml";
                default       -> "dashboard.fxml";
            };
            URL url = ViewLoader.fxml(fxmlFile);
            FXMLLoader loader = new FXMLLoader(url);
            loader.setLocation(url);
            Stage stage = (Stage) fieldUsername.getScene().getWindow();
            Scene scene = new Scene(loader.load(), 1050, 680);
            stage.setScene(scene);
            stage.setTitle("🍔 FoodDash — Delivery System");
            stage.setMinWidth(900);
            stage.setMinHeight(600);
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            showError("Could not load dashboard: " + cause.getMessage());
            cause.printStackTrace();
        }
    }

    private void showError(String msg) {
        loginMsg.setText(msg);
        loginMsg.getStyleClass().setAll("msg-error");
    }
}
