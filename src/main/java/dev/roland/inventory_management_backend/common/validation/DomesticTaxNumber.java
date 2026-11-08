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
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.constraints.Pattern;

/** Validates an optional Hungarian domestic tax number in {@code 12345678-1-12} format. */
@Pattern(regexp = "^\\s*(?:[0-9]{8}-[1245]-[0-9]{2})?\\s*$", message = ValidationKeys.INVALID_VALUE)
@ReportAsSingleViolation
@Constraint(validatedBy = {})
@Documented
@Target({FIELD, PARAMETER, ANNOTATION_TYPE, RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface DomesticTaxNumber {
  /**
   * Returns the validation message key.
   *
   * @return the validation message key
   */
  String message() default ValidationKeys.INVALID_VALUE;

  /**
   * Returns the validation groups.
   *
   * @return the validation groups
   */
  Class<?>[] groups() default {};

  /**
   * Returns the validation payload types.
   *
   * @return the validation payload types
   */
  Class<? extends Payload>[] payload() default {};
}
