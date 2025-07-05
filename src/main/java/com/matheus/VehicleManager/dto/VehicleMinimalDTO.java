package com.matheus.VehicleManager.dto;

public record VehicleMinimalDTO(
    Long id,
    String chassi,
    String brand,
    String model
) {}
