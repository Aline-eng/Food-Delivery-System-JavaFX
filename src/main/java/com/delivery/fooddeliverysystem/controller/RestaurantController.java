package com.delivery.fooddeliverysystem.controller;

import com.delivery.fooddeliverysystem.exception.DeliverySystemException;
import com.delivery.fooddeliverysystem.model.MenuItem;
import com.delivery.fooddeliverysystem.model.Restaurant;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class RestaurantController implements Initializable {

    @FXML private TextField fieldRestId, fieldRestName;
    @FXML private ComboBox<String> cuisineCombo;
    @FXML private ComboBox<Restaurant> menuRestCombo;
    @FXML private TextField fieldItemId, fieldItemName, fieldItemPrice;
    @FXML private ComboBox<String> itemCategoryCombo;
    @FXML private TextField searchField;
    @FXML private Label restMsg, menuMsg;
    @FXML private TableView<Restaurant> restaurantsTable;
    @FXML private TableColumn<Restaurant, String> colId, colName, colCuisine, colMenu;

    private static final List<String> CUISINES = List.of(
            "Italian", "Chinese", "Indian", "Mexican", "American",
            "Japanese", "Thai", "Mediterranean", "French", "African", "Other");
    private static final List<String> CATEGORIES = List.of(
            "Starter", "Main", "Side", "Dessert", "Drink", "Snack");

    private final AppContext ctx = AppContext.get();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRestaurantId()));
        colName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));
        colCuisine.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCuisine()));
        colMenu.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMenu().size() + " items"));

        cuisineCombo.setItems(FXCollections.observableArrayList(CUISINES));
        itemCategoryCombo.setItems(FXCollections.observableArrayList(CATEGORIES));

        menuRestCombo.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Restaurant r, boolean empty) {
                super.updateItem(r, empty);
                setText(empty || r == null ? null : r.getName() + " (" + r.getCuisine() + ")");
            }
        });
        menuRestCombo.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Restaurant r, boolean empty) {
                super.updateItem(r, empty);
                setText(empty || r == null ? "Select restaurant..." : r.getName() + " (" + r.getCuisine() + ")");
            }
        });

        refreshRestaurants();
    }

    @FXML
    private void addRestaurant() {
        clearMsgs();
        String id = fieldRestId.getText().trim();
        String name = fieldRestName.getText().trim();
        String cuisine = cuisineCombo.getValue();
        if (id.isBlank())      { showError(restMsg, "Restaurant ID is required."); return; }
        if (name.isBlank())    { showError(restMsg, "Restaurant name is required."); return; }
        if (cuisine == null)   { showError(restMsg, "Please select a cuisine type."); return; }
        try {
            ctx.restaurantManager.add(new Restaurant(id, name, cuisine));
            showSuccess(restMsg, "Restaurant added!");
            fieldRestId.clear(); fieldRestName.clear(); cuisineCombo.setValue(null);
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
        Restaurant rest = menuRestCombo.getValue();
        if (rest == null)              { showError(menuMsg, "Select a restaurant."); return; }
        if (fieldItemId.getText().isBlank())   { showError(menuMsg, "Item ID is required."); return; }
        if (fieldItemName.getText().isBlank()) { showError(menuMsg, "Item name is required."); return; }
        if (itemCategoryCombo.getValue() == null) { showError(menuMsg, "Select a category."); return; }
        try {
            double price = Double.parseDouble(fieldItemPrice.getText().trim());
            MenuItem item = new MenuItem(fieldItemId.getText().trim(), fieldItemName.getText().trim(),
                    price, itemCategoryCombo.getValue());
            ctx.restaurantManager.addMenuItemToRestaurant(rest.getRestaurantId(), item);
            showSuccess(menuMsg, "Menu item added!");
            fieldItemId.clear(); fieldItemName.clear(); fieldItemPrice.clear(); itemCategoryCombo.setValue(null);
            refreshRestaurants();
        } catch (NumberFormatException e) {
            showError(menuMsg, "Invalid price — enter a number like 12.99");
        } catch (DeliverySystemException e) {
            showError(menuMsg, e.getMessage());
        }
    }

    @FXML
    private void removeMenuItem() {
        clearMsgs();
        Restaurant rest = menuRestCombo.getValue();
        if (rest == null)            { showError(menuMsg, "Select a restaurant."); return; }
        if (fieldItemId.getText().isBlank()) { showError(menuMsg, "Enter the Item ID to remove."); return; }
        try {
            ctx.restaurantManager.removeMenuItemFromRestaurant(rest.getRestaurantId(), fieldItemId.getText().trim());
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
        menuRestCombo.setItems(FXCollections.observableArrayList(ctx.restaurantManager.getAll()));
    }

    private void clearMsgs()     { restMsg.setText(""); menuMsg.setText(""); }
    private void showSuccess(Label lbl, String msg) { lbl.setText(msg); lbl.getStyleClass().setAll("msg-success"); }
    private void showError(Label lbl, String msg)   { lbl.setText(msg); lbl.getStyleClass().setAll("msg-error"); }
}
