package com.delivery.fooddeliverysystem.controller;

import com.delivery.fooddeliverysystem.exception.DeliverySystemException;
import com.delivery.fooddeliverysystem.model.Order;
import com.delivery.fooddeliverysystem.model.OrderStatus;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class DriverDashboardController implements Initializable {

    @FXML private Label lblWelcome, lblAvailability, driverMsg;
    @FXML private TableView<Order> assignedOrdersTable;
    @FXML private TableColumn<Order, String> colId, colCustomer, colRestaurant, colItems, colTotal, colStatus, colTime;
    @FXML private ComboBox<String> statusCombo;

    private final AppContext ctx = AppContext.get();
    private final SessionManager session = SessionManager.get();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        lblWelcome.setText("Welcome, " + session.getCurrentUser().getUsername() + " 🚗");
        setupTable();
        statusCombo.setItems(FXCollections.observableArrayList(
                Arrays.asList("PREPARING", "OUT_FOR_DELIVERY", "DELIVERED")));
        refreshMyDeliveries();
        updateAvailabilityLabel();
    }

    private void setupTable() {
        colId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getOrderId()));
        colCustomer.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCustomer().getName()));
        colRestaurant.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRestaurant().getName()));
        colItems.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getItems().stream().map(i -> i.getName()).collect(Collectors.joining(", "))));
        colTotal.setCellValueFactory(d -> new SimpleStringProperty(String.format("$%.2f", d.getValue().getTotalPrice())));
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus().name()));
        colTime.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFormattedTime()));
    }

    @FXML
    private void refreshMyDeliveries() {
        String driverId = session.getLinkedId();
        List<Order> myOrders = ctx.orderManager.getAll().stream()
                .filter(o -> o.getDriver() != null && o.getDriver().getId().equals(driverId))
                .collect(Collectors.toList());
        assignedOrdersTable.getItems().setAll(myOrders);
        updateAvailabilityLabel();
    }

    @FXML
    private void updateOrderStatus() {
        driverMsg.setText("");
        Order selected = assignedOrdersTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showError("Select an order first."); return; }
        if (statusCombo.getValue() == null) { showError("Select a status."); return; }
        try {
            ctx.orderManager.updateStatus(selected.getOrderId(), OrderStatus.valueOf(statusCombo.getValue()));
            showSuccess("Status updated to " + statusCombo.getValue());
            refreshMyDeliveries();
        } catch (DeliverySystemException e) {
            showError(e.getMessage());
        }
    }

    private void updateAvailabilityLabel() {
        try {
            var driver = ctx.driverManager.findById(session.getLinkedId());
            lblAvailability.setText(driver.isAvailable() ? "Status: ✅ Available" : "Status: 🚗 On Delivery");
            lblAvailability.getStyleClass().setAll(driver.isAvailable() ? "msg-success" : "msg-error");
        } catch (DeliverySystemException ignored) {}
    }

    @FXML
    private void handleLogout() {
        session.logout();
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/delivery/fooddeliverysystem/fxml/login.fxml"));
            Stage stage = (Stage) lblWelcome.getScene().getWindow();
            stage.setScene(new Scene(loader.load(), 420, 460));
            stage.setTitle("🍔 FoodDash — Login");
        } catch (IOException e) {
            showError("Logout failed.");
        }
    }

    private void showSuccess(String msg) { driverMsg.setText(msg); driverMsg.getStyleClass().setAll("msg-success"); }
    private void showError(String msg)   { driverMsg.setText(msg); driverMsg.getStyleClass().setAll("msg-error"); }
}
