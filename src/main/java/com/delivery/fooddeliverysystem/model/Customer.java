package com.delivery.fooddeliverysystem.model;

public class Customer extends Person {
    private String address;

    public Customer(String id, String name, String phone, String address) {
        super(id, name, phone);
        this.address = address;
    }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    @Override
    public String getRole() { return "Customer"; }

    @Override
    public String toString() {
        return super.toString() + "," + address;
    }
}
