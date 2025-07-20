package com.matheus.VehicleManager.exception;

import java.util.Map;

public class InvalidRequestException extends RuntimeException {
    private final Map<String, String> fieldErrors;

    public InvalidRequestException(Map<String, String> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }

}
