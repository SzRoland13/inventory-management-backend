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
  void partnerRequiresNameAndTypeAndBoundsTaxNumberLength() {
    assertTrue(
        validator.validate(new PartnerRequest("Customer", PartnerType.BOTH, null)).isEmpty());
    assertFalse(validator.validate(new PartnerRequest(" ", PartnerType.CUSTOMER, null)).isEmpty());
    assertFalse(validator.validate(new PartnerRequest("Customer", null, null)).isEmpty());
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
