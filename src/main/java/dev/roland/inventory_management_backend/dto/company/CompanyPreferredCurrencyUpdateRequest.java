package dev.roland.inventory_management_backend.dto.company;

import lombok.Data;

@Data
public class CompanyPreferredCurrencyUpdateRequest {
  private Long companyId;
  private Long currencyId;
}
