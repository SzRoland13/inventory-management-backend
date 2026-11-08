package dev.roland.inventory_management_backend.common.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CommonFormatValidatorTest {
  private final E164PhoneValidator phoneValidator = new E164PhoneValidator();
  private final EmailAddressValidator emailValidator = new EmailAddressValidator();
  private final IbanValidator ibanValidator = new IbanValidator();
  private final VatNumberValidator vatNumberValidator = new VatNumberValidator();
  private final CountryCodeValidator countryCodeValidator = new CountryCodeValidator();

  @Test
  void acceptsOnlyE164PhonesAndAllowsOptionalBlankValues() {
    assertTrue(phoneValidator.isValid("+36123456789", null));
    assertTrue(phoneValidator.isValid("", null));
    assertFalse(phoneValidator.isValid("06 1 234 5678", null));
    assertFalse(phoneValidator.isValid("+0123456789", null));
    assertFalse(phoneValidator.isValid("+1234567890123456", null));
  }

  @Test
  void validatesEmailSyntaxAndAddressLength() {
    assertTrue(emailValidator.isValid("roland@example.com", null));
    assertTrue(emailValidator.isValid("", null));
    assertFalse(emailValidator.isValid("bad address@example.com", null));
    assertFalse(emailValidator.isValid("user@localhost", null));
    assertFalse(emailValidator.isValid("a".repeat(65) + "@example.com", null));
  }

  @Test
  void validatesIbanChecksum() {
    assertTrue(ibanValidator.isValid("HU42 1177 3016 1111 1018 0000 0000", null));
    assertTrue(ibanValidator.isValid("", null));
    assertFalse(ibanValidator.isValid("HU00 1177 3016 1111 1018 0000 0000", null));
  }

  @Test
  void validatesOnlyHungarianAndEuVatShapes() {
    assertTrue(vatNumberValidator.isValid("HU12345678", null));
    assertTrue(vatNumberValidator.isValid("DE123456789", null));
    assertTrue(vatNumberValidator.isValid("", null));
    assertFalse(vatNumberValidator.isValid("HU123", null));
    assertFalse(vatNumberValidator.isValid("12345678-1-12", null));
    assertFalse(vatNumberValidator.isValid("12345678-3-12", null));
    assertFalse(vatNumberValidator.isValid("12345678-22-12", null));
    assertFalse(vatNumberValidator.isValid("DE123", null));
    assertFalse(vatNumberValidator.isValid("US123456", null));
  }

  @Test
  void acceptsIsoAlphaTwoCountryCodes() {
    assertTrue(countryCodeValidator.isValid("HU", null));
    assertTrue(countryCodeValidator.isValid("us", null));
    assertFalse(countryCodeValidator.isValid("XX", null));
  }
}
