package dev.roland.inventory_management_backend.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import dev.roland.inventory_management_backend.common.annotation.validator.PasswordValidator;

/** Declares the validation constraint for acceptable password values. */
@Documented
@Constraint(validatedBy = PasswordValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {
  /**
   * Returns the validation message for an invalid password.
   *
   * @return message shown when validation fails
   */
  String message() default "Invalid password format";

  /**
   * Returns the validation groups for this constraint.
   *
   * @return groups that include this constraint
   */
  Class<?>[] groups() default {};

  /**
   * Returns the payload types associated with this constraint.
   *
   * @return payload types for this constraint
   */
  Class<? extends Payload>[] payload() default {};
}
