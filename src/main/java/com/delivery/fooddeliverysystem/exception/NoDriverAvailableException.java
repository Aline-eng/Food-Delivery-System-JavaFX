package com.delivery.fooddeliverysystem.exception;

public class NoDriverAvailableException extends DeliverySystemException {
    public NoDriverAvailableException() {
        super("No delivery driver is currently available.");
    }
}
