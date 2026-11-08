package dev.roland.inventory_management_backend.features.company.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import dev.roland.inventory_management_backend.common.validation.ValidationKeys;
import lombok.Data;

/** Carries input data for company Preferred Currency Update in the company profile API. */
@Data
public class CompanyPreferredCurrencyUpdateRequest {
  @NotNull(message = ValidationKeys.REQUIRED)
  @Positive(message = ValidationKeys.INVALID_VALUE)
  private Long companyId;

  private Long currencyId;
}
