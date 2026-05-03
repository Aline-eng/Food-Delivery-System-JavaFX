package com.delivery.fooddeliverysystem.model;

import java.util.ArrayList;
import java.util.List;

public class Restaurant {
    private String restaurantId;
    private String name;
    private String cuisine;
    private List<MenuItem> menu;

    public Restaurant(String restaurantId, String name, String cuisine) {
        this.restaurantId = restaurantId;
        this.name = name;
        this.cuisine = cuisine;
        this.menu = new ArrayList<>();
    }

    public String getRestaurantId() { return restaurantId; }
    public String getName() { return name; }
    public String getCuisine() { return cuisine; }
    public List<MenuItem> getMenu() { return menu; }

    public void addMenuItem(MenuItem item) { menu.add(item); }

    public boolean removeMenuItem(String itemId) {
        return menu.removeIf(item -> item.getItemId().equals(itemId));
    }

    public MenuItem findMenuItem(String itemId) {
        return menu.stream().filter(i -> i.getItemId().equals(itemId)).findFirst().orElse(null);
    }

    @Override
    public String toString() {
        return restaurantId + "," + name + "," + cuisine;
    }
}
