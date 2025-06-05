package com.matheus.VehicleManager.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Constraint(validatedBy = UniqueUsernameValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueUsername {
    String message() default "O nome de usuário já está em uso";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
