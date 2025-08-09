package com.matheus.VehicleManager.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(Exception e) {
        Map<String, Object> response = new HashMap<>();
        response.put("errors", Map.of("error", e.getMessage()));
        return ResponseEntity.badRequest().body(response);
    }
}
