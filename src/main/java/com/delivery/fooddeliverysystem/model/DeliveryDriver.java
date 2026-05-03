package com.delivery.fooddeliverysystem.model;

public class DeliveryDriver extends Person {
    private boolean available;
    private String vehicleType;

    public DeliveryDriver(String id, String name, String phone, String vehicleType) {
        super(id, name, phone);
        this.vehicleType = vehicleType;
        this.available = true;
    }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    public String getVehicleType() { return vehicleType; }

    @Override
    public String getRole() { return "Driver"; }

    @Override
    public String toString() {
        return super.toString() + "," + vehicleType + "," + available;
    }
}
