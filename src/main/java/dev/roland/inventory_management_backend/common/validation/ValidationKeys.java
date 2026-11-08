package dev.roland.inventory_management_backend.common.validation;

/** Stable localization keys used by request constraint annotations. */
public final class ValidationKeys {
  public static final String REQUIRED = "validation.required";
  public static final String INVALID_EMAIL = "validation.email.invalid";
  public static final String INVALID_PHONE = "validation.phone.invalid";
  public static final String INVALID_IBAN = "validation.iban.invalid";
  public static final String INVALID_VAT = "validation.vat.invalid";
  public static final String TOO_LONG = "validation.too_long";
  public static final String INVALID_VALUE = "validation.invalid_value";
  public static final String INVALID_COUNTRY = "validation.country.invalid";

  private ValidationKeys() {}
}
