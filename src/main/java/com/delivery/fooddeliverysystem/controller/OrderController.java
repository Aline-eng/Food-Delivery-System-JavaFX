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
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.stream.Collectors;

public class OrderController implements Initializable {

    // Form dropdowns
    @FXML private ComboBox<Customer>    customerCombo;
    @FXML private ComboBox<Restaurant>  restaurantCombo;
    @FXML private ListView<MenuItem>    menuListView;
    @FXML private Label                 lblOrderTotal;
    @FXML private ComboBox<String>      statusCombo;
    @FXML private TextField             searchField;
    @FXML private Label                 orderMsg;

    // Table
    @FXML private TableView<Order>      ordersTable;
    @FXML private TableColumn<Order, String> colId, colCustomer, colRestaurant, colItems,
                                             colTotal, colDriver, colStatus, colTime;

    private final AppContext ctx = AppContext.get();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        setupDropdowns();
        refreshOrders();
    }

    private void setupTable() {
        colId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getOrderId()));
        colCustomer.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCustomer().getName()));
        colRestaurant.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRestaurant().getName()));
        colItems.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getItems().stream().map(MenuItem::getName).collect(Collectors.joining(", "))));
        colTotal.setCellValueFactory(d -> new SimpleStringProperty(String.format("$%.2f", d.getValue().getTotalPrice())));
        colDriver.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDriverName()));
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus().name()));
        colTime.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFormattedTime()));
    }

    private void setupDropdowns() {
        // Customer combo
        customerCombo.setItems(FXCollections.observableArrayList(ctx.customerManager.getAll()));
        customerCombo.setCellFactory(lv -> nameCell(c -> c.getName() + " — " + c.getPhone()));
        customerCombo.setButtonCell(nameCell(c -> c == null ? "Select customer..." : c.getName() + " — " + c.getPhone()));

        // Restaurant combo — loading menu on selection
        restaurantCombo.setItems(FXCollections.observableArrayList(ctx.restaurantManager.getAll()));
        restaurantCombo.setCellFactory(lv -> nameCell(r -> r.getName() + " (" + r.getCuisine() + ")"));
        restaurantCombo.setButtonCell(nameCell(r -> r == null ? "Select restaurant..." : r.getName() + " (" + r.getCuisine() + ")"));
        restaurantCombo.setOnAction(e -> loadMenuItems());

        // Menu list — multi-select
        menuListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        menuListView.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(MenuItem item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName() + "  —  $" + String.format("%.2f", item.getPrice()) + "  [" + item.getCategory() + "]");
            }
        });
        menuListView.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> updateTotal());

        // Status combo
        statusCombo.setItems(FXCollections.observableArrayList(
                Arrays.stream(OrderStatus.values()).map(Enum::name).toList()));
    }

    private void loadMenuItems() {
        Restaurant r = restaurantCombo.getValue();
        if (r == null) return;
        menuListView.setItems(FXCollections.observableArrayList(r.getMenu()));
        lblOrderTotal.setText("$0.00");
    }

    private void updateTotal() {
        double total = menuListView.getSelectionModel().getSelectedItems()
                .stream().mapToDouble(MenuItem::getPrice).sum();
        lblOrderTotal.setText(String.format("$%.2f", total));
    }

    @FXML
    private void placeOrder() {
        clearMsg();
        Customer customer    = customerCombo.getValue();
        Restaurant restaurant = restaurantCombo.getValue();
        List<MenuItem> items  = menuListView.getSelectionModel().getSelectedItems();

        if (customer == null)    { showError("Select a customer.");    return; }
        if (restaurant == null)  { showError("Select a restaurant.");  return; }
        if (items.isEmpty())     { showError("Select at least one menu item."); return; }

        try {
            String orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            Order order = new Order(orderId, customer, restaurant);
            items.forEach(order::addItem);
            ctx.orderManager.add(order);
            showSuccess("Order placed! ID: " + orderId);
            clearForm();
            refreshOrders();
        } catch (DeliverySystemException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void assignDriver() {
        clearMsg();
        Order selected = ordersTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showError("Select an order from the table."); return; }
        try {
            ctx.orderManager.assignDriver(selected.getOrderId());
            showSuccess("Driver assigned to order " + selected.getOrderId());
            refreshOrders();
        } catch (DeliverySystemException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void cancelOrder() {
        clearMsg();
        Order selected = ordersTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showError("Select an order from the table."); return; }
        try {
            ctx.orderManager.updateStatus(selected.getOrderId(), OrderStatus.CANCELLED);
            showSuccess("Order cancelled.");
            refreshOrders();
        } catch (DeliverySystemException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void updateStatus() {
        clearMsg();
        Order selected = ordersTable.getSelectionModel().getSelectedItem();
        if (selected == null)          { showError("Select an order from the table."); return; }
        if (statusCombo.getValue() == null) { showError("Select a status."); return; }
        try {
            ctx.orderManager.updateStatus(selected.getOrderId(), OrderStatus.valueOf(statusCombo.getValue()));
            showSuccess("Status updated.");
            refreshOrders();
        } catch (DeliverySystemException e) {
            showError(e.getMessage());
        }
    }

    @FXML private void searchOrders() {
        ordersTable.getItems().setAll(ctx.orderManager.search(searchField.getText().trim()));
    }

    @FXML private void sortOrders() {
        ordersTable.getItems().setAll(ctx.orderManager.getSortedByTime());
    }

    @FXML public void refreshOrders() {
        ordersTable.getItems().setAll(ctx.orderManager.getAll());
        ordersTable.refresh();
        // Refresh combos in case new customers/restaurants were added
        customerCombo.setItems(FXCollections.observableArrayList(ctx.customerManager.getAll()));
        restaurantCombo.setItems(FXCollections.observableArrayList(ctx.restaurantManager.getAll()));
    }

    private void clearForm() {
        customerCombo.setValue(null);
        restaurantCombo.setValue(null);
        menuListView.getItems().clear();
        lblOrderTotal.setText("$0.00");
    }

    // Generic helper for combo cell factories
    private <T> ListCell<T> nameCell(java.util.function.Function<T, String> labelFn) {
        return new ListCell<>() {
            @Override protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : labelFn.apply(item));
            }
        };
    }

    private void showSuccess(String msg) { orderMsg.setText(msg); orderMsg.getStyleClass().setAll("msg-success"); }
    private void showError(String msg)   { orderMsg.setText(msg); orderMsg.getStyleClass().setAll("msg-error"); }
    private void clearMsg()              { orderMsg.setText(""); }
}
