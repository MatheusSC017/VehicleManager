package com.matheus.VehicleManager.dto;

import com.matheus.VehicleManager.model.Client;

import java.time.LocalDate;

public record MaintenanceResponseDTO(
    Long id,
    VehicleMinimalDTO vehicle,
    LocalDate startDate,
    LocalDate endDate
) { }
