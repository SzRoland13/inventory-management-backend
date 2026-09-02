package dev.roland.inventory_management_backend.features.company.dto;

import lombok.Data;

@Data
public class CompanyPreferredCurrencyUpdateRequest {
  private Long companyId;
  private Long currencyId;
}
