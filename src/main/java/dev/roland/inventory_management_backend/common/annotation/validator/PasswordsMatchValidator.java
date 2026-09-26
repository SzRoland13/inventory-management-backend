package dev.roland.inventory_management_backend.common.annotation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import dev.roland.inventory_management_backend.common.annotation.PasswordsMatch;
import dev.roland.inventory_management_backend.features.auth.dto.PasswordSetupRequest;

/** Checks that the password and confirmation fields contain the same value. */
public class PasswordsMatchValidator
    implements ConstraintValidator<PasswordsMatch, PasswordSetupRequest> {

  @Override
  public boolean isValid(
      final PasswordSetupRequest request,
      final ConstraintValidatorContext constraintValidatorContext) {
    if (request.getPassword() == null || request.getRepeatPassword() == null) {
      return false;
    }
    return request.getPassword().equals(request.getRepeatPassword());
  }
}
