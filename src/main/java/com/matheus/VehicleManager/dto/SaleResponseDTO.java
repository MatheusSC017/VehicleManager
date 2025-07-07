package com.matheus.VehicleManager.dto;

import com.matheus.VehicleManager.enums.SalesStatus;
import com.matheus.VehicleManager.model.Client;

import java.time.LocalDate;

public record SaleResponseDTO(
    Long id,
    Client client,
    VehicleMinimalDTO vehicle,
    LocalDate salesDate,
    LocalDate reserveDate,
    SalesStatus status
) {}
