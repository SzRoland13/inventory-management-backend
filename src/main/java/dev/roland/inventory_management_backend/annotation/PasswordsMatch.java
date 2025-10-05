package dev.roland.inventory_management_backend.annotation;

import dev.roland.inventory_management_backend.annotation.validator.PasswordsMatchValidator;
import jakarta.validation.Constraint;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PasswordsMatchValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordsMatch {
}
