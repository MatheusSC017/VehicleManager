package com.matheus.VehicleManager.validators;

import com.matheus.VehicleManager.service.ClientService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {

    @Autowired
    private ClientService clientService;

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (clientService == null) return true;
        return clientService.isEmailUnique(email);
    }
}
