package com.matheus.VehicleManager.dto;

public record PresignedRequestDTO(
    String filename,
    String contentType
) {
}
