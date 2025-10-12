package com.matheus.VehicleManager.dto;

import java.util.List;

public record UpdateFileRequestDTO(
    Long vehicleId,
    PresignedRequestDTO[] images,
    List<Long> selectedImageIds
) {
}
