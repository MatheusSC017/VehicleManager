package com.matheus.VehicleManager.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Constraint(validatedBy = UniqueEmailValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueEmail {
    String message() default "O email já está em uso";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

