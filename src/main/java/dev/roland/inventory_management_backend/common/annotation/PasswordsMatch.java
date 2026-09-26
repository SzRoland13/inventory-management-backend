package dev.roland.inventory_management_backend.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import dev.roland.inventory_management_backend.common.annotation.validator.PasswordsMatchValidator;

/** Declares a validation constraint that requires two password fields to match. */
@Documented
@Constraint(validatedBy = PasswordsMatchValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordsMatch {
  /**
   * Returns the message key used when the password fields do not match.
   *
   * @return validation message key
   */
  String message() default "auth.login.password-not-match";

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
