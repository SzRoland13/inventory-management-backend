package dev.roland.inventory_management_backend.annotation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import dev.roland.inventory_management_backend.annotation.PasswordsMatch;
import dev.roland.inventory_management_backend.dto.auth.PasswordSetupRequest;

public class PasswordsMatchValidator
    implements ConstraintValidator<PasswordsMatch, PasswordSetupRequest> {

  @Override
  public boolean isValid(
      PasswordSetupRequest request, ConstraintValidatorContext constraintValidatorContext) {
    if (request.getPassword() == null || request.getRepeatPassword() == null) return false;
    return request.getPassword().equals(request.getRepeatPassword());
  }
}
