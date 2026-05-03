package com.delivery.fooddeliverysystem.controller;

import com.delivery.fooddeliverysystem.exception.DeliverySystemException;
import com.delivery.fooddeliverysystem.model.Customer;
import com.delivery.fooddeliverysystem.model.DeliveryDriver;
import com.delivery.fooddeliverysystem.model.UserRole;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.UUID;

public class SignupController implements Initializable {

    @FXML private TextField fieldUsername, fieldName, fieldPhone, fieldAddress, fieldVehicle;
    @FXML private PasswordField fieldPassword, fieldConfirm;
    @FXML private ComboBox<String> roleCombo;
    @FXML private VBox customerFields, driverFields;
    @FXML private Label signupMsg;

    private final AppContext ctx = AppContext.get();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        roleCombo.setItems(FXCollections.observableArrayList("Customer", "Driver"));
        roleCombo.setValue("Customer");
        toggleRoleFields("Customer");
        roleCombo.setOnAction(e -> toggleRoleFields(roleCombo.getValue()));
    }

    private void toggleRoleFields(String role) {
        boolean isCustomer = "Customer".equals(role);
        customerFields.setVisible(isCustomer);  customerFields.setManaged(isCustomer);
        driverFields.setVisible(!isCustomer);   driverFields.setManaged(!isCustomer);
    }

    @FXML
    private void handleSignup() {
        signupMsg.setText("");
        String username = fieldUsername.getText().trim();
        String password = fieldPassword.getText();
        String confirm  = fieldConfirm.getText();
        String name     = fieldName.getText().trim();
        String phone    = fieldPhone.getText().trim();
        String role     = roleCombo.getValue();

        if (!password.equals(confirm)) { showError("Passwords do not match."); return; }
        if (name.isBlank())            { showError("Full name is required.");   return; }
        if (phone.isBlank())           { showError("Phone number is required."); return; }

        try {
            String linkedId = generateId(role);
            UserRole userRole = "Customer".equals(role) ? UserRole.CUSTOMER : UserRole.DRIVER;
            ctx.authManager.register(username, password, userRole, linkedId);

            if (userRole == UserRole.CUSTOMER) {
                String address = fieldAddress.getText().trim();
                if (address.isBlank()) { showError("Address is required for customers."); return; }
                ctx.customerManager.add(new Customer(linkedId, name, phone, address));
            } else {
                String vehicle = fieldVehicle.getText().trim();
                if (vehicle.isBlank()) { showError("Vehicle type is required for drivers."); return; }
                ctx.driverManager.add(new DeliveryDriver(linkedId, name, phone, vehicle));
            }

            showSuccess("Account created! You can now log in.");
        } catch (DeliverySystemException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void goToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/delivery/fooddeliverysystem/fxml/login.fxml"));
            Stage stage = (Stage) fieldUsername.getScene().getWindow();
            stage.setScene(new Scene(loader.load(), 420, 460));
            stage.setTitle("🍔 FoodDash — Login");
        } catch (IOException e) {
            showError("Could not open login screen.");
        }
    }

    private String generateId(String role) {
        String prefix = "Customer".equals(role) ? "C-" : "D-";
        return prefix + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private void showSuccess(String msg) { signupMsg.setText(msg); signupMsg.getStyleClass().setAll("msg-success"); }
    private void showError(String msg)   { signupMsg.setText(msg); signupMsg.getStyleClass().setAll("msg-error"); }
}
