package com.matheus.VehicleManager.exception;

public class SaleNotFoundException extends RuntimeException {
    public SaleNotFoundException(String message) {
        super(message);
    }
}
