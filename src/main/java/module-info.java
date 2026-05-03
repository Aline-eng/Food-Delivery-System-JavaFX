module com.delivery.fooddeliverysystem {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;

    opens com.delivery.fooddeliverysystem to javafx.fxml;
    exports com.delivery.fooddeliverysystem;
}