package com.matheus.VehicleManager.dto;

import com.matheus.VehicleManager.enums.FinancingStatus;

public record FinancingStatusRequestDTO(
    FinancingStatus status
) {
}
