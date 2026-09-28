package dev.roland.inventory_management_backend.common.annotation.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.Test;

import dev.roland.inventory_management_backend.features.auth.dto.PasswordSetupRequest;

class PasswordsMatchValidatorTest {

  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  void acceptsMatchingPasswords() {
    final Set<ConstraintViolation<PasswordSetupRequest>> violations =
        validator.validate(request("Correct!42", "Correct!42"));

    assertTrue(violations.isEmpty());
  }

  @Test
  void reportsMismatchedPasswordOnTheConfirmationField() {
    final Set<ConstraintViolation<PasswordSetupRequest>> violations =
        validator.validate(request("Correct!42", "Different!42"));

    assertEquals(1, violations.size());
    final ConstraintViolation<PasswordSetupRequest> violation = violations.iterator().next();
    assertEquals("repeatPassword", violation.getPropertyPath().toString());
    assertEquals("auth.login.password-not-match", violation.getMessage());
  }

  private PasswordSetupRequest request(final String password, final String repeatPassword) {
    final PasswordSetupRequest request = new PasswordSetupRequest();
    request.setEmail("alice@example.com");
    request.setPassword(password);
    request.setRepeatPassword(repeatPassword);
    return request;
  }
}
