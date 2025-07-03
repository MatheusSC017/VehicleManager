package com.matheus.VehicleManager.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Constraint(validatedBy = UniqueChassiValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueChassi {
    String message() default "O chassi já está em uso";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
