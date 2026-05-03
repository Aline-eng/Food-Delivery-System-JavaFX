package com.delivery.fooddeliverysystem.exception;

public class ItemNotFoundException extends DeliverySystemException {
    public ItemNotFoundException(String id) {
        super("Item not found with ID: " + id);
    }
}
