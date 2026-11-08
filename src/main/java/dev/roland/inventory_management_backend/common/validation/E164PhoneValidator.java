package dev.roland.inventory_management_backend.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/** Implements the E.164 syntax check while allowing an omitted optional number. */
public class E164PhoneValidator implements ConstraintValidator<E164Phone, String> {
  @Override
  public boolean isValid(final String value, final ConstraintValidatorContext context) {
    return value == null || value.isBlank() || value.matches("\\+[1-9][0-9]{1,14}");
  }
}
