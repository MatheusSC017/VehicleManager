package com.matheus.VehicleManager.dto;

import com.matheus.VehicleManager.enums.FileType;

public record FileDTO(
        Long id,
        String path,
        FileType type) {}
