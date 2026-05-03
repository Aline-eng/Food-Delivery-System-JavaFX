package com.delivery.fooddeliverysystem.controller;

import com.delivery.fooddeliverysystem.exception.DeliverySystemException;
import com.delivery.fooddeliverysystem.model.Customer;
import com.delivery.fooddeliverysystem.model.Order;
import com.delivery.fooddeliverysystem.model.OrderStatus;
import com.delivery.fooddeliverysystem.model.Restaurant;
import com.delivery.fooddeliverysystem.model.MenuItem;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class OrderController implements Initializable {

    @FXML private TextField fieldOrderId, fieldCustomerId, fieldRestaurantId, fieldItemIds, searchField;
    @FXML private ComboBox<String> statusCombo;
    @FXML private Label orderMsg;
    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, String> colId, colCustomer, colRestaurant, colItems, colTotal, colDriver, colStatus, colTime;

    private final AppContext ctx = AppContext.get();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getOrderId()));
        colCustomer.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCustomer().getName()));
        colRestaurant.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRestaurant().getName()));
        colItems.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getItems().stream().map(MenuItem::getName).reduce("", (a, b) -> a.isEmpty() ? b : a + ", " + b)));
        colTotal.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.2f", d.getValue().getTotalPrice())));
        colDriver.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDriverName()));
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus().name()));
        colTime.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFormattedTime()));

        statusCombo.setItems(FXCollections.observableArrayList(
                Arrays.stream(OrderStatus.values()).map(Enum::name).toList()));
        refreshOrders();
    }

    @FXML
    private void placeOrder() {
        clearMsg();
        try {
            Customer customer = ctx.customerManager.findById(fieldCustomerId.getText().trim());
            Restaurant restaurant = ctx.restaurantManager.findById(fieldRestaurantId.getText().trim());
            Order order = new Order(fieldOrderId.getText().trim(), customer, restaurant);
            for (String itemId : fieldItemIds.getText().split(",")) {
                MenuItem item = restaurant.findMenuItem(itemId.trim());
                if (item == null) throw new DeliverySystemException("Menu item not found: " + itemId.trim());
                order.addItem(item);
            }
            ctx.orderManager.add(order);
            showSuccess("Order placed successfully!");
            clearForm();
            refreshOrders();
        } catch (DeliverySystemException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void assignDriver() {
        clearMsg();
        String id = getSelectedOrderId();
        if (id == null) return;
        try {
            ctx.orderManager.assignDriver(id);
            showSuccess("Driver assigned!");
            refreshOrders();
        } catch (DeliverySystemException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void cancelOrder() {
        clearMsg();
        String id = getSelectedOrderId();
        if (id == null) return;
        try {
            ctx.orderManager.updateStatus(id, OrderStatus.CANCELLED);
            showSuccess("Order cancelled.");
            refreshOrders();
        } catch (DeliverySystemException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void updateStatus() {
        clearMsg();
        String id = getSelectedOrderId();
        if (id == null) return;
        if (statusCombo.getValue() == null) { showError("Select a status."); return; }
        try {
            ctx.orderManager.updateStatus(id, OrderStatus.valueOf(statusCombo.getValue()));
            showSuccess("Status updated.");
            refreshOrders();
        } catch (DeliverySystemException e) {
            showError(e.getMessage());
        }
    }

    @FXML private void searchOrders() {
        List<Order> results = ctx.orderManager.search(searchField.getText().trim());
        ordersTable.getItems().setAll(results);
    }

    @FXML private void sortOrders() {
        ordersTable.getItems().setAll(ctx.orderManager.getSortedByTime());
    }

    @FXML public void refreshOrders() {
        ordersTable.getItems().setAll(ctx.orderManager.getAll());
    }

    private String getSelectedOrderId() {
        Order selected = ordersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            String id = fieldOrderId.getText().trim();
            if (id.isEmpty()) { showError("Select an order or enter an Order ID."); return null; }
            return id;
        }
        return selected.getOrderId();
    }

    private void clearForm() {
        fieldOrderId.clear(); fieldCustomerId.clear();
        fieldRestaurantId.clear(); fieldItemIds.clear();
    }

    private void showSuccess(String msg) { orderMsg.setText(msg); orderMsg.getStyleClass().setAll("msg-success"); }
    private void showError(String msg)   { orderMsg.setText(msg); orderMsg.getStyleClass().setAll("msg-error"); }
    private void clearMsg()              { orderMsg.setText(""); }
}
