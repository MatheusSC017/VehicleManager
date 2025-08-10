package com.matheus.VehicleManager.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidRequestException(InvalidRequestException e) {
        Map<String, Object> response = new HashMap<>();
        response.put("errors", e.getFieldErrors());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(Exception e) {
        Map<String, Object> response = new HashMap<>();
        response.put("errors", Map.of("error", e.getMessage()));
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<Map<String, Object>> handleIOException(Exception e) {
        Map<String, Object> response = new HashMap<>();
        response.put("errors", Map.of("error", e.getMessage()));
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object >> handleException(Exception e) {
        Map<String, Object> response = new HashMap<>();
        response.put("errors", Map.of("error", e.getMessage()));
        return ResponseEntity.badRequest().body(response);
    }
}
