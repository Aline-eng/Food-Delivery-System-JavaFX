package com.delivery.fooddeliverysystem.controller;

import com.delivery.fooddeliverysystem.manager.*;

public class AppContext {
    private static AppContext instance;

    public final CustomerManager customerManager;
    public final RestaurantManager restaurantManager;
    public final DriverManager driverManager;
    public final OrderManager orderManager;
    public final AuthManager authManager;

    private AppContext() {
        customerManager = new CustomerManager();
        restaurantManager = new RestaurantManager();
        driverManager = new DriverManager();
        orderManager = new OrderManager(customerManager, restaurantManager, driverManager);
        authManager = new AuthManager();
    }

    public static AppContext get() {
        if (instance == null) instance = new AppContext();
        return instance;
    }
}
