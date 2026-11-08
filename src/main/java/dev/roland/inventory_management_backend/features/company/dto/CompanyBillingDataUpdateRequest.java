package dev.roland.inventory_management_backend.features.company.dto;

import jakarta.validation.constraints.Size;

import dev.roland.inventory_management_backend.common.validation.DomesticTaxNumber;
import dev.roland.inventory_management_backend.common.validation.Iban;
import dev.roland.inventory_management_backend.common.validation.ValidationKeys;
import dev.roland.inventory_management_backend.common.validation.VatNumber;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Carries input data for company Billing Data Update in the company profile API. */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompanyBillingDataUpdateRequest {
  @Size(max = 50, message = ValidationKeys.TOO_LONG)
  @DomesticTaxNumber
  private String taxNumber;

  @Size(max = 50, message = ValidationKeys.TOO_LONG)
  @VatNumber
  private String vatNumber;

  @Size(max = 100, message = ValidationKeys.TOO_LONG)
  private String registrationNumber;

  @Size(max = 100, message = ValidationKeys.TOO_LONG)
  private String bankAccount;

  @Size(max = 50, message = ValidationKeys.TOO_LONG)
  @Iban
  private String iban;
}
