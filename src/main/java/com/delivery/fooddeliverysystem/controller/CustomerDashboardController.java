package com.delivery.fooddeliverysystem.controller;

import com.delivery.fooddeliverysystem.exception.DeliverySystemException;
import com.delivery.fooddeliverysystem.model.Customer;
import com.delivery.fooddeliverysystem.model.MenuItem;
import com.delivery.fooddeliverysystem.model.Order;
import com.delivery.fooddeliverysystem.model.OrderStatus;
import com.delivery.fooddeliverysystem.model.Restaurant;
import com.delivery.fooddeliverysystem.util.ViewLoader;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.stream.Collectors;

public class CustomerDashboardController implements Initializable {

    @FXML private Label lblWelcome;
    @FXML private ComboBox<Restaurant> restaurantCombo;
    @FXML private ListView<MenuItem> menuListView;
    @FXML private Label lblTotal;
    @FXML private Label orderMsg;
    @FXML private TableView<Order> myOrdersTable;
    @FXML private TableColumn<Order, String> colId, colRestaurant, colItems, colTotal, colDriver, colStatus, colTime;

    private final AppContext ctx = AppContext.get();
    private final SessionManager session = SessionManager.get();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        lblWelcome.setText("Welcome back, " + session.getCurrentUser().getUsername() + " 👋");

        setupTable();
        loadRestaurants();
        refreshMyOrders();

        restaurantCombo.setOnAction(e -> loadMenuForSelectedRestaurant());
        menuListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        menuListView.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> updateTotal());

        // Custom cell to show item name + price
        menuListView.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(MenuItem item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName() + "  —  $" + String.format("%.2f", item.getPrice()));
            }
        });

        // Custom combo cell for restaurants
        restaurantCombo.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Restaurant r, boolean empty) {
                super.updateItem(r, empty);
                setText(empty || r == null ? null : r.getName() + " (" + r.getCuisine() + ")");
            }
        });
        restaurantCombo.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Restaurant r, boolean empty) {
                super.updateItem(r, empty);
                setText(empty || r == null ? "Select a restaurant..." : r.getName() + " (" + r.getCuisine() + ")");
            }
        });
    }

    private void setupTable() {
        colId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getOrderId()));
        colRestaurant.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRestaurant().getName()));
        colItems.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getItems().stream().map(MenuItem::getName).collect(Collectors.joining(", "))));
        colTotal.setCellValueFactory(d -> new SimpleStringProperty(String.format("$%.2f", d.getValue().getTotalPrice())));
        colDriver.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDriverName()));
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus().name()));
        colTime.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFormattedTime()));
    }

    private void loadRestaurants() {
        restaurantCombo.setItems(FXCollections.observableArrayList(ctx.restaurantManager.getAll()));
    }

    private void loadMenuForSelectedRestaurant() {
        Restaurant r = restaurantCombo.getValue();
        if (r == null) return;
        menuListView.setItems(FXCollections.observableArrayList(r.getMenu()));
        lblTotal.setText("$0.00");
    }

    private void updateTotal() {
        double total = menuListView.getSelectionModel().getSelectedItems()
                .stream().mapToDouble(MenuItem::getPrice).sum();
        lblTotal.setText(String.format("$%.2f", total));
    }

    @FXML
    private void placeOrder() {
        orderMsg.setText("");
        Restaurant restaurant = restaurantCombo.getValue();
        List<MenuItem> selected = menuListView.getSelectionModel().getSelectedItems();

        if (restaurant == null) { showError("Please select a restaurant."); return; }
        if (selected.isEmpty()) { showError("Please select at least one item."); return; }

        try {
            String customerId = session.getLinkedId();
            Customer customer = ctx.customerManager.findById(customerId);
            String orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            Order order = new Order(orderId, customer, restaurant);
            selected.forEach(order::addItem);
            ctx.orderManager.add(order);
            showSuccess("Order placed! ID: " + orderId);
            menuListView.getSelectionModel().clearSelection();
            lblTotal.setText("$0.00");
            refreshMyOrders();
        } catch (DeliverySystemException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void cancelMyOrder() {
        orderMsg.setText("");
        Order selected = myOrdersTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showError("Select an order to cancel."); return; }
        if (selected.getStatus() != OrderStatus.PENDING) {
            showError("Only PENDING orders can be cancelled."); return;
        }
        try {
            ctx.orderManager.updateStatus(selected.getOrderId(), OrderStatus.CANCELLED);
            showSuccess("Order cancelled.");
            refreshMyOrders();
        } catch (DeliverySystemException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void refreshMyOrders() {
        String customerId = session.getLinkedId();
        List<Order> myOrders = ctx.orderManager.getAll().stream()
                .filter(o -> o.getCustomer().getId().equals(customerId))
                .collect(Collectors.toList());
        myOrdersTable.getItems().setAll(myOrders);
    }

    @FXML
    private void handleLogout() {
        session.logout();
        try {
            FXMLLoader loader = new FXMLLoader(ViewLoader.fxml("login.fxml"));
            Stage stage = (Stage) lblWelcome.getScene().getWindow();
            stage.setScene(new Scene(loader.load(), 420, 460));
            stage.setTitle("🍔 FoodDash — Login");
            stage.setMinWidth(400);
            stage.setMinHeight(400);
        } catch (IOException e) {
            showError("Logout failed: " + e.getMessage());
        }
    }

    private void showSuccess(String msg) { orderMsg.setText(msg); orderMsg.getStyleClass().setAll("msg-success"); }
    private void showError(String msg)   { orderMsg.setText(msg); orderMsg.getStyleClass().setAll("msg-error"); }
}
