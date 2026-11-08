package dev.roland.inventory_management_backend.features.partner.dto;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.Test;

import dev.roland.inventory_management_backend.features.contact.dto.ContactRequest;
import dev.roland.inventory_management_backend.features.contact.enumeration.ContactType;
import dev.roland.inventory_management_backend.features.partner.enumeration.PartnerType;

class PartnerContactValidationTest {
  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  void partnerRequiresSupplierBillingIdentifiersAndValidatesDomesticTaxNumber() {
    assertTrue(
        validator.validate(new PartnerRequest("Customer", PartnerType.CUSTOMER, null)).isEmpty());
    assertTrue(
        validator
            .validate(
                new PartnerRequest(
                    "Supplier",
                    PartnerType.BOTH,
                    "12345678-1-12",
                    "HU12345678",
                    null,
                    "123456789",
                    "GB82WEST12345698765432"))
            .isEmpty());
    assertFalse(
        validator.validate(new PartnerRequest("Supplier", PartnerType.BOTH, null)).isEmpty());
    assertFalse(validator.validate(new PartnerRequest(" ", PartnerType.CUSTOMER, null)).isEmpty());
    assertFalse(validator.validate(new PartnerRequest("Customer", null, null)).isEmpty());
    assertFalse(
        validator
            .validate(new PartnerRequest("Customer", PartnerType.CUSTOMER, "12345678-9-12"))
            .isEmpty());
    assertFalse(
        validator
            .validate(new PartnerRequest("Customer", PartnerType.CUSTOMER, "x".repeat(51)))
            .isEmpty());
  }

  @Test
  void contactRequiresAddressAndRejectsMalformedOptionalEmail() {
    assertTrue(
        validator
            .validate(
                new ContactRequest(
                    ContactType.SHIPPING, "Main St", null, "Budapest", "HU", null, null))
            .isEmpty());
    assertFalse(
        validator
            .validate(
                new ContactRequest(ContactType.BILLING, "", null, "", "", null, "bad-address"))
            .isEmpty());
  }
}
