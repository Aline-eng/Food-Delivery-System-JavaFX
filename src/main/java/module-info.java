module com.delivery.fooddeliverysystem {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;

    opens com.delivery.fooddeliverysystem to javafx.fxml;
    opens com.delivery.fooddeliverysystem.ui to javafx.fxml;
    opens com.delivery.fooddeliverysystem.controller to javafx.fxml;
    opens com.delivery.fooddeliverysystem.model to javafx.fxml;

    exports com.delivery.fooddeliverysystem;
    exports com.delivery.fooddeliverysystem.ui;
    exports com.delivery.fooddeliverysystem.controller;
    exports com.delivery.fooddeliverysystem.model;
    exports com.delivery.fooddeliverysystem.manager;
    exports com.delivery.fooddeliverysystem.exception;
    exports com.delivery.fooddeliverysystem.util;
}
