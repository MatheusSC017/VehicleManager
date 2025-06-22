package com.matheus.VehicleManager.dto;

public record ClientDTO(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phone) {}