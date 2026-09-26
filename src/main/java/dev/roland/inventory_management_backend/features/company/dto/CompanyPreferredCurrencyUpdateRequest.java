package dev.roland.inventory_management_backend.features.company.dto;

import lombok.Data;

/** Carries input data for company Preferred Currency Update in the company profile API. */
@Data
public class CompanyPreferredCurrencyUpdateRequest {
  private Long companyId;
  private Long currencyId;
}
