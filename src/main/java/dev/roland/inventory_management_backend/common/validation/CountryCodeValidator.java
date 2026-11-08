package dev.roland.inventory_management_backend.common.validation;

import java.util.Locale;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/** Checks country codes against the JDK's ISO 3166-1 alpha-2 list. */
public class CountryCodeValidator implements ConstraintValidator<CountryCode, String> {
  @Override
  public boolean isValid(final String value, final ConstraintValidatorContext context) {
    return value != null
        && Locale.getISOCountries(Locale.IsoCountryCode.PART1_ALPHA2)
            .contains(value.trim().toUpperCase(Locale.ROOT));
  }
}
