package com.delivery.fooddeliverysystem.controller;

import com.delivery.fooddeliverysystem.model.Order;
import com.delivery.fooddeliverysystem.model.OrderStatus;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML private Label headerTitle;
    @FXML private Label statOrders, statRestaurants, statCustomers, statDrivers;
    @FXML private VBox dashboardPane, ordersPane, restaurantsPane, customersPane, driversPane;
    @FXML private Button btnNavDashboard, btnNavOrders, btnNavRestaurants, btnNavCustomers, btnNavDrivers;

    @FXML private TableView<Order> recentOrdersTable;
    @FXML private TableColumn<Order, String> colROId, colROCustomer, colRORestaurant, colROStatus, colROTotal, colROTime;

    private final AppContext ctx = AppContext.get();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupRecentOrdersTable();
        refreshStats();
    }

    private void setupRecentOrdersTable() {
        colROId.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getOrderId()));
        colROCustomer.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getCustomer().getName()));
        colRORestaurant.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getRestaurant().getName()));
        colROStatus.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getStatus().name()));
        colROTotal.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(String.format("%.2f", d.getValue().getTotalPrice())));
        colROTime.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getFormattedTime()));
    }

    public void refreshStats() {
        List<Order> orders = ctx.orderManager.getSortedByTime();
        statOrders.setText(String.valueOf(ctx.orderManager.getAll().size()));
        statRestaurants.setText(String.valueOf(ctx.restaurantManager.getAll().size()));
        statCustomers.setText(String.valueOf(ctx.customerManager.getAll().size()));
        long available = ctx.driverManager.getAll().stream().filter(d -> d.isAvailable()).count();
        statDrivers.setText(String.valueOf(available));
        recentOrdersTable.getItems().setAll(orders.subList(0, Math.min(10, orders.size())));
    }

    private void showPane(VBox pane, String title, Button activeBtn) {
        dashboardPane.setVisible(false); dashboardPane.setManaged(false);
        ordersPane.setVisible(false);    ordersPane.setManaged(false);
        restaurantsPane.setVisible(false); restaurantsPane.setManaged(false);
        customersPane.setVisible(false); customersPane.setManaged(false);
        driversPane.setVisible(false);   driversPane.setManaged(false);

        pane.setVisible(true); pane.setManaged(true);
        headerTitle.setText(title);

        for (Button b : new Button[]{btnNavDashboard, btnNavOrders, btnNavRestaurants, btnNavCustomers, btnNavDrivers}) {
            b.getStyleClass().remove("nav-button-active");
        }
        activeBtn.getStyleClass().add("nav-button-active");
    }

    @FXML private void showDashboard() {
        showPane(dashboardPane, "Dashboard", btnNavDashboard);
        refreshStats();
    }
    @FXML private void showOrders()      { showPane(ordersPane,      "Orders",      btnNavOrders); }
    @FXML private void showRestaurants() { showPane(restaurantsPane, "Restaurants", btnNavRestaurants); }
    @FXML private void showCustomers()   { showPane(customersPane,   "Customers",   btnNavCustomers); }
    @FXML private void showDrivers()     { showPane(driversPane,     "Drivers",     btnNavDrivers); }
}
