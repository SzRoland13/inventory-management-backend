package dev.roland.inventory_management_backend.common.validation;

import java.util.Locale;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/** Checks the general IBAN shape and checksum without country-specific length metadata. */
public class IbanValidator implements ConstraintValidator<Iban, String> {
  private static final int RADIX = 10;
  private static final int MODULUS = 97;

  @Override
  public boolean isValid(final String value, final ConstraintValidatorContext context) {
    return value == null || value.isBlank() || hasValidChecksum(value);
  }

  private boolean hasValidChecksum(final String value) {
    final String iban = value.replaceAll("\\s", "").toUpperCase(Locale.ROOT);
    final boolean validShape = iban.matches("[A-Z]{2}[0-9]{2}[A-Z0-9]{11,30}");

    final String rearranged = validShape ? iban.substring(4) + iban.substring(0, 4) : "";
    int remainder = 0;
    if (validShape) {
      for (final char character : rearranged.toCharArray()) {
        final String digits =
            Character.isLetter(character)
                ? Integer.toString(character - 'A' + RADIX)
                : Character.toString(character);
        for (final char digit : digits.toCharArray()) {
          remainder = (remainder * RADIX + Character.digit(digit, RADIX)) % MODULUS;
        }
      }
    }
    return validShape && remainder == 1;
  }
}
