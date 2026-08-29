package dev.roland.inventory_management_backend.dto.company;

import dev.roland.inventory_management_backend.features.currency.Currency;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdatedPreferredCurrencyResponse {
  private Long companyId;
  private Currency currency;
}
