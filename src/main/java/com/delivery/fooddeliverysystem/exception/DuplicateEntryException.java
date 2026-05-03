package com.delivery.fooddeliverysystem.exception;

public class DuplicateEntryException extends DeliverySystemException {
    public DuplicateEntryException(String id) {
        super("Entry already exists with ID: " + id);
    }
}
