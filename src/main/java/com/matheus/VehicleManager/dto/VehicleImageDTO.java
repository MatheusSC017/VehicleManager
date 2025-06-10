package com.matheus.VehicleManager.dto;

import com.matheus.VehicleManager.enums.VehicleChange;
import com.matheus.VehicleManager.enums.VehicleFuel;
import com.matheus.VehicleManager.enums.VehicleStatus;
import com.matheus.VehicleManager.enums.VehicleType;

import java.math.BigDecimal;

public record VehicleImageDTO(
        Long id,
        VehicleType vehicleType,
        VehicleStatus vehicleStatus,
        String model,
        String brand,
        Integer year,
        String color,
        String plate,
        String chassi,
        BigDecimal mileage,
        BigDecimal price,
        VehicleFuel vehicleFuel,
        VehicleChange vehicleChange,
        Integer doors,
        String motor,
        String power,
        String image) {}
