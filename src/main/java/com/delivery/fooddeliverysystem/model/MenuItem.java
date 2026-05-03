package com.delivery.fooddeliverysystem.model;

public class MenuItem {
    private String itemId;
    private String name;
    private double price;
    private String category;

    public MenuItem(String itemId, String name, double price, String category) {
        this.itemId = itemId;
        this.name = name;
        this.price = price;
        this.category = category;
    }

    public String getItemId() { return itemId; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public String getCategory() { return category; }
    public void setPrice(double price) { this.price = price; }

    @Override
    public String toString() {
        return itemId + "," + name + "," + price + "," + category;
    }
}
