package com.delivery.fooddeliverysystem.controller;

import com.delivery.fooddeliverysystem.exception.DeliverySystemException;
import com.delivery.fooddeliverysystem.model.MenuItem;
import com.delivery.fooddeliverysystem.model.Restaurant;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

public class RestaurantController implements Initializable {

    @FXML private TextField fieldRestId, fieldRestName, fieldCuisine;
    @FXML private TextField fieldMenuRestId, fieldItemId, fieldItemName, fieldItemPrice, fieldItemCategory;
    @FXML private TextField searchField;
    @FXML private Label restMsg, menuMsg;
    @FXML private TableView<Restaurant> restaurantsTable;
    @FXML private TableColumn<Restaurant, String> colId, colName, colCuisine, colMenu;

    private final AppContext ctx = AppContext.get();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRestaurantId()));
        colName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));
        colCuisine.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCuisine()));
        colMenu.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getMenu().size())));
        refreshRestaurants();
    }

    @FXML
    private void addRestaurant() {
        clearMsgs();
        try {
            ctx.restaurantManager.add(new Restaurant(
                    fieldRestId.getText().trim(),
                    fieldRestName.getText().trim(),
                    fieldCuisine.getText().trim()));
            showSuccess(restMsg, "Restaurant added!");
            clearRestForm();
            refreshRestaurants();
        } catch (DeliverySystemException e) {
            showError(restMsg, e.getMessage());
        }
    }

    @FXML
    private void removeRestaurant() {
        clearMsgs();
        Restaurant selected = restaurantsTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showError(restMsg, "Select a restaurant to remove."); return; }
        try {
            ctx.restaurantManager.remove(selected.getRestaurantId());
            showSuccess(restMsg, "Restaurant removed.");
            refreshRestaurants();
        } catch (DeliverySystemException e) {
            showError(restMsg, e.getMessage());
        }
    }

    @FXML
    private void addMenuItem() {
        clearMsgs();
        try {
            double price = Double.parseDouble(fieldItemPrice.getText().trim());
            MenuItem item = new MenuItem(fieldItemId.getText().trim(), fieldItemName.getText().trim(),
                    price, fieldItemCategory.getText().trim());
            ctx.restaurantManager.addMenuItemToRestaurant(fieldMenuRestId.getText().trim(), item);
            showSuccess(menuMsg, "Menu item added!");
            clearMenuForm();
            refreshRestaurants();
        } catch (NumberFormatException e) {
            showError(menuMsg, "Invalid price format.");
        } catch (DeliverySystemException e) {
            showError(menuMsg, e.getMessage());
        }
    }

    @FXML
    private void removeMenuItem() {
        clearMsgs();
        try {
            ctx.restaurantManager.removeMenuItemFromRestaurant(
                    fieldMenuRestId.getText().trim(), fieldItemId.getText().trim());
            showSuccess(menuMsg, "Menu item removed.");
            refreshRestaurants();
        } catch (DeliverySystemException e) {
            showError(menuMsg, e.getMessage());
        }
    }

    @FXML private void searchRestaurants() {
        restaurantsTable.getItems().setAll(ctx.restaurantManager.search(searchField.getText().trim()));
    }

    @FXML private void sortRestaurants() {
        restaurantsTable.getItems().setAll(ctx.restaurantManager.getSortedByName());
    }

    @FXML public void refreshRestaurants() {
        restaurantsTable.getItems().setAll(ctx.restaurantManager.getAll());
    }

    private void clearRestForm() { fieldRestId.clear(); fieldRestName.clear(); fieldCuisine.clear(); }
    private void clearMenuForm() { fieldMenuRestId.clear(); fieldItemId.clear(); fieldItemName.clear(); fieldItemPrice.clear(); fieldItemCategory.clear(); }
    private void clearMsgs()     { restMsg.setText(""); menuMsg.setText(""); }
    private void showSuccess(Label lbl, String msg) { lbl.setText(msg); lbl.getStyleClass().setAll("msg-success"); }
    private void showError(Label lbl, String msg)   { lbl.setText(msg); lbl.getStyleClass().setAll("msg-error"); }
}
