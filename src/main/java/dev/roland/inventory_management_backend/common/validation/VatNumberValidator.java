package dev.roland.inventory_management_backend.common.validation;

import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/** Validates VAT number syntax according to the selected HU/EU country prefix. */
public class VatNumberValidator implements ConstraintValidator<VatNumber, String> {
  private static final Map<String, Pattern> EU_PATTERNS =
      Map.ofEntries(
          Map.entry("AT", Pattern.compile("ATU[0-9]{8}")),
          Map.entry("BE", Pattern.compile("BE0?[0-9]{9}")),
          Map.entry("BG", Pattern.compile("BG[0-9]{9,10}")),
          Map.entry("CY", Pattern.compile("CY[0-9]{8}[A-Z]")),
          Map.entry("CZ", Pattern.compile("CZ[0-9]{8,10}")),
          Map.entry("DE", Pattern.compile("DE[0-9]{9}")),
          Map.entry("DK", Pattern.compile("DK[0-9]{8}")),
          Map.entry("EE", Pattern.compile("EE[0-9]{9}")),
          Map.entry("EL", Pattern.compile("EL[0-9]{9}")),
          Map.entry("ES", Pattern.compile("ES[A-Z0-9][0-9]{7}[A-Z0-9]")),
          Map.entry("FI", Pattern.compile("FI[0-9]{8}")),
          Map.entry("FR", Pattern.compile("FR[A-HJ-NP-Z0-9]{2}[0-9]{9}")),
          Map.entry("HR", Pattern.compile("HR[0-9]{11}")),
          Map.entry("HU", Pattern.compile("HU[0-9]{8}")),
          Map.entry("IE", Pattern.compile("IE[0-9]{7}[A-W][A-I]?")),
          Map.entry("IT", Pattern.compile("IT[0-9]{11}")),
          Map.entry("LT", Pattern.compile("LT([0-9]{9}|[0-9]{12})")),
          Map.entry("LU", Pattern.compile("LU[0-9]{8}")),
          Map.entry("LV", Pattern.compile("LV[0-9]{11}")),
          Map.entry("MT", Pattern.compile("MT[0-9]{8}")),
          Map.entry("NL", Pattern.compile("NL[0-9]{9}B[0-9]{2}")),
          Map.entry("PL", Pattern.compile("PL[0-9]{10}")),
          Map.entry("PT", Pattern.compile("PT[0-9]{9}")),
          Map.entry("RO", Pattern.compile("RO[0-9]{2,10}")),
          Map.entry("SE", Pattern.compile("SE[0-9]{12}")),
          Map.entry("SI", Pattern.compile("SI[0-9]{8}")),
          Map.entry("SK", Pattern.compile("SK[0-9]{10}")));

  @Override
  public boolean isValid(final String value, final ConstraintValidatorContext context) {
    final boolean optional = value == null || value.isBlank();
    final String normalized =
        value == null ? "" : value.replaceAll("[\\s.-]", "").toUpperCase(Locale.ROOT);
    final boolean validBasicShape = normalized.matches("[A-Z0-9]{1,50}");
    final String countryPrefix = normalized.length() >= 2 ? normalized.substring(0, 2) : "";
    final Pattern countryPattern = EU_PATTERNS.get(countryPrefix);
    final boolean validCountryShape =
        countryPattern != null && countryPattern.matcher(normalized).matches();
    return optional || (validBasicShape && validCountryShape);
  }
}
