package dev.roland.inventory_management_backend.common.annotation.validator;

import java.util.regex.Pattern;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import dev.roland.inventory_management_backend.common.annotation.ValidPassword;

/** Checks password input against the application password requirements. */
public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

  private static final Pattern UPPERCASE = Pattern.compile("[A-Z]");
  private static final Pattern LOWERCASE = Pattern.compile("[a-z]");
  private static final Pattern DIGIT = Pattern.compile("\\d");
  private static final Pattern SPECIAL = Pattern.compile("[^a-zA-Z0-9]");
  private static final Pattern REPEATING_CHARS = Pattern.compile("(.)\\1{2,}");

  @Override
  public boolean isValid(
      final String password, final ConstraintValidatorContext constraintValidatorContext) {
    boolean valid = password != null && password.length() >= 8;
    if (valid) {
      valid =
          UPPERCASE.matcher(password).find()
              && LOWERCASE.matcher(password).find()
              && DIGIT.matcher(password).find()
              && SPECIAL.matcher(password).find()
              && !REPEATING_CHARS.matcher(password).find();
    }

    if (valid) {
      final String lower = password.toLowerCase();
      final String sequences = "abcdefghijklmnopqrstuvwxyz0123456789";
      for (int i = 0; valid && i < sequences.length() - 3; i++) {
        final String sequence = sequences.substring(i, i + 4);
        valid =
            !lower.contains(sequence)
                && !lower.contains(new StringBuilder(sequence).reverse().toString());
      }
    }
    return valid;
  }
}
