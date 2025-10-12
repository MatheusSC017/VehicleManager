package com.matheus.VehicleManager.dto;

public record InsertFileRequestDTO(
    Long vehicleId,
    PresignedRequestDTO[] images
) {
}
