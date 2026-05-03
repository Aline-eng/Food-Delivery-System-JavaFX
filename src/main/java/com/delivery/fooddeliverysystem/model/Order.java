package com.delivery.fooddeliverysystem.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private String orderId;
    private Customer customer;
    private Restaurant restaurant;
    private List<MenuItem> items;
    private DeliveryDriver driver;
    private OrderStatus status;
    private LocalDateTime orderTime;

    public Order(String orderId, Customer customer, Restaurant restaurant) {
        this.orderId = orderId;
        this.customer = customer;
        this.restaurant = restaurant;
        this.items = new ArrayList<>();
        this.status = OrderStatus.PENDING;
        this.orderTime = LocalDateTime.now();
    }

    public String getOrderId() { return orderId; }
    public Customer getCustomer() { return customer; }
    public Restaurant getRestaurant() { return restaurant; }
    public List<MenuItem> getItems() { return items; }
    public DeliveryDriver getDriver() { return driver; }
    public OrderStatus getStatus() { return status; }
    public LocalDateTime getOrderTime() { return orderTime; }

    public void addItem(MenuItem item) { items.add(item); }
    public void setDriver(DeliveryDriver driver) { this.driver = driver; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public double getTotalPrice() {
        return items.stream().mapToDouble(MenuItem::getPrice).sum();
    }

    public String getDriverName() {
        return driver != null ? driver.getName() : "Unassigned";
    }

    public String getFormattedTime() {
        return orderTime.format(FMT);
    }

    @Override
    public String toString() {
        return orderId + "," + customer.getId() + "," + restaurant.getRestaurantId()
                + "," + status + "," + orderTime.format(FMT)
                + "," + (driver != null ? driver.getId() : "NONE");
    }
}
