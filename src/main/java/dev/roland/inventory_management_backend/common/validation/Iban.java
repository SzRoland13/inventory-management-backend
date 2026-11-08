package dev.roland.inventory_management_backend.common.validation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.RECORD_COMPONENT;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/** Validates an optional IBAN using its syntax and ISO 13616 mod-97 checksum. */
@Documented
@Constraint(validatedBy = IbanValidator.class)
@Target({FIELD, PARAMETER, ANNOTATION_TYPE, RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface Iban {
  /**
   * Returns the message key used when the IBAN is invalid.
   *
   * @return localization key
   */
  String message() default ValidationKeys.INVALID_IBAN;

  /**
   * Returns the validation groups.
   *
   * @return validation groups
   */
  Class<?>[] groups() default {};

  /**
   * Returns the payload types.
   *
   * @return validation payload types
   */
  Class<? extends Payload>[] payload() default {};
}
