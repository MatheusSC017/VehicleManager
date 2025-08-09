package com.matheus.VehicleManager.validators;

import com.matheus.VehicleManager.model.Vehicle;
import com.matheus.VehicleManager.service.VehicleService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

public class UniqueChassiValidator implements ConstraintValidator<UniqueChassi, Vehicle> {

    @Autowired
    private VehicleService vehicleService;

    @Override
    public boolean isValid(Vehicle vehicle, ConstraintValidatorContext context) {
        if (vehicle == null || vehicle.getChassi() == null || vehicle.getChassi().isBlank()) {
            return true;
        }

        try {
            Vehicle existingVehicle = vehicleService.findByChassi(vehicle.getChassi());

            if (existingVehicle == null) return true;

            if (vehicle.getId() != null && vehicle.getId().equals(existingVehicle.getId())) {
                return true;
            }

            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("O chassi já está em uso")
                    .addPropertyNode("chassi")
                    .addConstraintViolation();
            return false;
        } catch (Exception e) {
            return true;
        }
    }
}
