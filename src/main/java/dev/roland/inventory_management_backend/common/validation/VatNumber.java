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

/** Validates optional HU and EU VAT number shapes, allowing basic strings for other countries. */
@Documented
@Constraint(validatedBy = VatNumberValidator.class)
@Target({FIELD, PARAMETER, ANNOTATION_TYPE, RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface VatNumber {
  /**
   * Returns the message key used when the VAT number is invalid.
   *
   * @return localization key
   */
  String message() default ValidationKeys.INVALID_VAT;

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
