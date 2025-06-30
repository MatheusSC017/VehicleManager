package com.matheus.VehicleManager.dto;

import com.matheus.VehicleManager.enums.FileType;

public record FileResponseDTO(
        Long id,
        String path,
        FileType type) {}
