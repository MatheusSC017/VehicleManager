package com.matheus.VehicleManager.dto;

public record ClientResponseDTO(
    Long id,
    String firstName,
    String lastName,
    String email,
    String phone) {}
