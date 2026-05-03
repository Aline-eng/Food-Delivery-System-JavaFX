package com.delivery.fooddeliverysystem.exception;

public class InvalidInputException extends DeliverySystemException {
    public InvalidInputException(String message) {
        super("Invalid input: " + message);
    }
}
