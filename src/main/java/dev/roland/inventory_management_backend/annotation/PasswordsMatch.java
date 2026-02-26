package dev.roland.inventory_management_backend.annotation;

import java.lang.annotation.*;

import jakarta.validation.Constraint;

import dev.roland.inventory_management_backend.annotation.validator.PasswordsMatchValidator;

@Documented
@Constraint(validatedBy = PasswordsMatchValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordsMatch {}
