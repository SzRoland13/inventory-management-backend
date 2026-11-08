package dev.roland.inventory_management_backend.features.partner.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import dev.roland.inventory_management_backend.common.validation.DomesticTaxNumber;
import dev.roland.inventory_management_backend.common.validation.Iban;
import dev.roland.inventory_management_backend.common.validation.ValidationKeys;
import dev.roland.inventory_management_backend.common.validation.VatNumber;
import dev.roland.inventory_management_backend.features.partner.enumeration.PartnerType;

/**
 * Partner create and update payload.
 *
 * @param name partner display name
 * @param type partner classification
 * @param taxNumber optional tax registration number
 * @param vatNumber optional EU VAT number
 * @param registrationNumber optional company registration number
 * @param bankAccount optional bank account number
 * @param iban optional international bank account number
 */
public record PartnerRequest(
    @NotBlank(message = ValidationKeys.REQUIRED) @Size(max = 255, message = ValidationKeys.TOO_LONG)
        String name,
    @NotNull(message = ValidationKeys.REQUIRED) PartnerType type,
    @Size(max = 50, message = ValidationKeys.TOO_LONG) @DomesticTaxNumber String taxNumber,
    @Size(max = 50, message = ValidationKeys.TOO_LONG) @VatNumber String vatNumber,
    @Size(max = 100, message = ValidationKeys.TOO_LONG) String registrationNumber,
    @Size(max = 100, message = ValidationKeys.TOO_LONG) String bankAccount,
    @Size(max = 50, message = ValidationKeys.TOO_LONG) @Iban String iban) {

  /** Keeps the original constructor available for existing integrations. */
  public PartnerRequest(final String name, final PartnerType type, final String taxNumber) {
    this(name, type, taxNumber, null, null, null, null);
  }

  /** Requires complete billing identifiers for suppliers and combined partners. */
  @AssertTrue(message = ValidationKeys.REQUIRED)
  public boolean isSupplierBillingDataComplete() {
    return (type != PartnerType.SUPPLIER && type != PartnerType.BOTH)
        || (hasText(taxNumber) && hasText(vatNumber) && hasText(iban) && hasText(bankAccount));
  }

  private static boolean hasText(final String value) {
    return value != null && !value.isBlank();
  }
}
