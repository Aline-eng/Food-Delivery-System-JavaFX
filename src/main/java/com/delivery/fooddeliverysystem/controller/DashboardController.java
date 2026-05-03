package com.delivery.fooddeliverysystem.controller;

import com.delivery.fooddeliverysystem.model.Order;
import com.delivery.fooddeliverysystem.util.ViewLoader;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML private Label headerTitle, lblAdminUser;
    @FXML private Label statOrders, statRestaurants, statCustomers, statDrivers;
    @FXML private StackPane contentArea;
    @FXML private VBox dashboardPane;
    @FXML private Button btnNavDashboard, btnNavOrders, btnNavRestaurants, btnNavCustomers, btnNavDrivers;
    @FXML private TableView<Order> recentOrdersTable;
    @FXML private TableColumn<Order, String> colROId, colROCustomer, colRORestaurant, colROStatus, colROTotal, colROTime;

    private final AppContext ctx = AppContext.get();

    // Cache for lazily loaded sub-views
    private final Map<String, Node> viewCache = new HashMap<>();

    private static final String ORDERS_FXML      = "orders.fxml";
    private static final String RESTAURANTS_FXML = "restaurants.fxml";
    private static final String CUSTOMERS_FXML   = "customers.fxml";
    private static final String DRIVERS_FXML     = "drivers.fxml";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupRecentOrdersTable();
        refreshStats();
        lblAdminUser.setText("Admin: " + SessionManager.get().getCurrentUser().getUsername());
    }

    private void setupRecentOrdersTable() {
        colROId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getOrderId()));
        colROCustomer.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCustomer().getName()));
        colRORestaurant.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRestaurant().getName()));
        colROStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus().name()));
        colROTotal.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.2f", d.getValue().getTotalPrice())));
        colROTime.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFormattedTime()));
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

    // ── Navigation ──────────────────────────────────────────────────────────

    @FXML private void showDashboard() {
        setActiveButton(btnNavDashboard);
        headerTitle.setText("Dashboard");
        // Show the inline dashboard pane, hide any lazy-loaded view
        contentArea.getChildren().forEach(n -> n.setVisible(false));
        dashboardPane.setVisible(true);
        refreshStats();
    }

    @FXML private void showOrders()      { navigateTo(ORDERS_FXML,      "Orders",      btnNavOrders); }
    @FXML private void showRestaurants() { navigateTo(RESTAURANTS_FXML, "Restaurants", btnNavRestaurants); }
    @FXML private void showCustomers()   { navigateTo(CUSTOMERS_FXML,   "Customers",   btnNavCustomers); }
    @FXML private void showDrivers()     { navigateTo(DRIVERS_FXML,     "Drivers",     btnNavDrivers); }

    private void navigateTo(String fxmlPath, String title, Button activeBtn) {
        setActiveButton(activeBtn);
        headerTitle.setText(title);

        // Hide all current children
        contentArea.getChildren().forEach(n -> n.setVisible(false));

        // Load once, cache, reuse
        Node view = viewCache.computeIfAbsent(fxmlPath, this::loadView);
        if (view != null) {
            if (!contentArea.getChildren().contains(view)) {
                contentArea.getChildren().add(view);
            }
            view.setVisible(true);
        }
    }

    private Node loadView(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(ViewLoader.fxml(fxmlFile));
            return loader.load();
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            System.err.println("Failed to load view [" + fxmlFile + "]: " + cause.getMessage());
            cause.printStackTrace();
            Label errorLabel = new Label("⚠ Failed to load view: " + cause.getMessage());
            errorLabel.getStyleClass().add("msg-error");
            return errorLabel;
        }
    }

    private void setActiveButton(Button active) {
        for (Button b : new Button[]{btnNavDashboard, btnNavOrders, btnNavRestaurants, btnNavCustomers, btnNavDrivers}) {
            b.getStyleClass().remove("nav-button-active");
        }
        active.getStyleClass().add("nav-button-active");
    }

    // ── Logout ───────────────────────────────────────────────────────────────

    @FXML
    private void handleLogout() {
        SessionManager.get().logout();
        viewCache.clear();
        try {
            FXMLLoader loader = new FXMLLoader(ViewLoader.fxml("login.fxml"));
            Stage stage = (Stage) headerTitle.getScene().getWindow();
            stage.setScene(new Scene(loader.load(), 420, 460));
            stage.setTitle("🍔 FoodDash — Login");
            stage.setMinWidth(400);
            stage.setMinHeight(400);
        } catch (IOException e) {
            System.err.println("Logout failed: " + e.getMessage());
        }
    }
}
