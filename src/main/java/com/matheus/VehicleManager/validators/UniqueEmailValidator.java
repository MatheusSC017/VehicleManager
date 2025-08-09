package com.matheus.VehicleManager.validators;

import com.matheus.VehicleManager.model.Client;
import com.matheus.VehicleManager.service.ClientService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, Client> {

    @Autowired
    private ClientService clientService;

    @Override
    public boolean isValid(Client client, ConstraintValidatorContext context) {
        if (client == null || client.getEmail() == null || client.getEmail().isBlank()) {
            return true;
        }

        try {
            Client existingClient = clientService.findByEmail(client.getEmail());

            if (existingClient == null) return true;

            if (client.getId() != null && client.getId().equals(existingClient.getId())) {
                return true;
            }

            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("O email já está em uso")
                    .addPropertyNode("email")
                    .addConstraintViolation();
            return false;

        } catch (Exception e) {
            return true;
        }
    }
}
