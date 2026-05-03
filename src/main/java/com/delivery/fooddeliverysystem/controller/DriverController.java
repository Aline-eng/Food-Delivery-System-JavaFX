package com.delivery.fooddeliverysystem.controller;

import com.delivery.fooddeliverysystem.exception.DeliverySystemException;
import com.delivery.fooddeliverysystem.model.DeliveryDriver;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

public class DriverController implements Initializable {

    @FXML private TextField fieldId, fieldName, fieldPhone, searchField;
    @FXML private ComboBox<String> vehicleCombo;
    @FXML private Label driverMsg;
    @FXML private TableView<DeliveryDriver> driversTable;
    @FXML private TableColumn<DeliveryDriver, String> colId, colName, colPhone, colVehicle, colAvailable;

    private static final java.util.List<String> VEHICLES =
            java.util.List.of("Motorcycle", "Car", "Bicycle", "Scooter", "Van", "Truck");

    private final AppContext ctx = AppContext.get();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getId()));
        colName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));
        colPhone.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPhone()));
        colVehicle.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getVehicleType()));
        colAvailable.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().isAvailable() ? "✅ Yes" : "❌ No"));
        vehicleCombo.setItems(javafx.collections.FXCollections.observableArrayList(VEHICLES));
        refreshDrivers();
    }

    @FXML
    private void addDriver() {
        clearMsg();
        if (fieldId.getText().isBlank())    { showError("Driver ID is required.");    return; }
        if (fieldName.getText().isBlank())  { showError("Driver name is required.");  return; }
        if (fieldPhone.getText().isBlank()) { showError("Phone number is required."); return; }
        if (vehicleCombo.getValue() == null){ showError("Select a vehicle type.");    return; }
        try {
            ctx.driverManager.add(new DeliveryDriver(
                    fieldId.getText().trim(), fieldName.getText().trim(),
                    fieldPhone.getText().trim(), vehicleCombo.getValue()));
            showSuccess("Driver added!");
            clearForm();
            refreshDrivers();
        } catch (DeliverySystemException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void removeDriver() {
        clearMsg();
        DeliveryDriver selected = driversTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showError("Select a driver to remove."); return; }
        try {
            ctx.driverManager.remove(selected.getId());
            showSuccess("Driver removed.");
            refreshDrivers();
        } catch (DeliverySystemException e) {
            showError(e.getMessage());
        }
    }

    @FXML private void searchDrivers() {
        driversTable.getItems().setAll(ctx.driverManager.search(searchField.getText().trim()));
    }

    @FXML private void sortDrivers() {
        driversTable.getItems().setAll(ctx.driverManager.getSortedByName());
    }

    @FXML public void refreshDrivers() {
        driversTable.getItems().setAll(ctx.driverManager.getAll());
    }

    private void clearForm() { fieldId.clear(); fieldName.clear(); fieldPhone.clear(); vehicleCombo.setValue(null); }
    private void showSuccess(String msg) { driverMsg.setText(msg); driverMsg.getStyleClass().setAll("msg-success"); }
    private void showError(String msg)   { driverMsg.setText(msg); driverMsg.getStyleClass().setAll("msg-error"); }
    private void clearMsg()              { driverMsg.setText(""); }
}
