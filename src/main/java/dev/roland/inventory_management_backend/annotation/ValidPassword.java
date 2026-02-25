package dev.roland.inventory_management_backend.annotation;

import java.lang.annotation.*;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import dev.roland.inventory_management_backend.annotation.validator.PasswordValidator;

@Documented
@Constraint(validatedBy = PasswordValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {
  String message() default "Invalid password format";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
