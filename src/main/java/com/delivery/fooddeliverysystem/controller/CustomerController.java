package com.delivery.fooddeliverysystem.controller;

import com.delivery.fooddeliverysystem.exception.DeliverySystemException;
import com.delivery.fooddeliverysystem.model.Customer;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

public class CustomerController implements Initializable {

    @FXML private TextField fieldId, fieldName, fieldPhone, fieldAddress, searchField;
    @FXML private Label customerMsg;
    @FXML private TableView<Customer> customersTable;
    @FXML private TableColumn<Customer, String> colId, colName, colPhone, colAddress;

    private final AppContext ctx = AppContext.get();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getId()));
        colName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));
        colPhone.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPhone()));
        colAddress.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAddress()));
        refreshCustomers();
    }

    @FXML
    private void addCustomer() {
        clearMsg();
        try {
            ctx.customerManager.add(new Customer(
                    fieldId.getText().trim(), fieldName.getText().trim(),
                    fieldPhone.getText().trim(), fieldAddress.getText().trim()));
            showSuccess("Customer added!");
            clearForm();
            refreshCustomers();
        } catch (DeliverySystemException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void removeCustomer() {
        clearMsg();
        Customer selected = customersTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showError("Select a customer to remove."); return; }
        try {
            ctx.customerManager.remove(selected.getId());
            showSuccess("Customer removed.");
            refreshCustomers();
        } catch (DeliverySystemException e) {
            showError(e.getMessage());
        }
    }

    @FXML private void searchCustomers() {
        customersTable.getItems().setAll(ctx.customerManager.search(searchField.getText().trim()));
    }

    @FXML private void sortCustomers() {
        customersTable.getItems().setAll(ctx.customerManager.getSortedByName());
    }

    @FXML public void refreshCustomers() {
        customersTable.getItems().setAll(ctx.customerManager.getAll());
    }

    private void clearForm() { fieldId.clear(); fieldName.clear(); fieldPhone.clear(); fieldAddress.clear(); }
    private void showSuccess(String msg) { customerMsg.setText(msg); customerMsg.getStyleClass().setAll("msg-success"); }
    private void showError(String msg)   { customerMsg.setText(msg); customerMsg.getStyleClass().setAll("msg-error"); }
    private void clearMsg()              { customerMsg.setText(""); }
}
