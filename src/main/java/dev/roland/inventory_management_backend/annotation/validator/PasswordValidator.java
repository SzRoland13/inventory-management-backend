package dev.roland.inventory_management_backend.annotation.validator;

import java.util.regex.Pattern;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import dev.roland.inventory_management_backend.annotation.ValidPassword;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

  private static final Pattern UPPERCASE = Pattern.compile("[A-Z]");
  private static final Pattern LOWERCASE = Pattern.compile("[a-z]");
  private static final Pattern DIGIT = Pattern.compile("\\d");
  private static final Pattern SPECIAL = Pattern.compile("[^a-zA-Z0-9]");
  private static final Pattern REPEATING_CHARS = Pattern.compile("(.)\\1{2,}");

  @Override
  public boolean isValid(String password, ConstraintValidatorContext constraintValidatorContext) {
    // Must not be null
    if (password == null) return false;
    // Must be at least 8 chars long
    if (password.length() < 8) return false;
    // Must contain at least one uppercase character
    if (!UPPERCASE.matcher(password).find()) return false;
    // Must contain at least one lowercase character
    if (!LOWERCASE.matcher(password).find()) return false;
    // Must contain at least one number
    if (!DIGIT.matcher(password).find()) return false;
    // Must contain at least one special character
    if (!SPECIAL.matcher(password).find()) return false;
    // Disallow 3+ repeating characters (aaa, 111, etc.)
    if (REPEATING_CHARS.matcher(password).find()) return false;

    // Disallow simple sequences like 1234, abcd
    String lower = password.toLowerCase();
    String sequences = "abcdefghijklmnopqrstuvwxyz0123456789";
    for (int i = 0; i < sequences.length() - 3; i++) {
      String seq = sequences.substring(i, i + 4);
      if (lower.contains(seq) || lower.contains(new StringBuilder(seq).reverse().toString())) {
        return false;
      }
    }

    return true;
  }
}
