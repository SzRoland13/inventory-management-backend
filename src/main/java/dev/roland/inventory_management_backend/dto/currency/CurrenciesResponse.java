package dev.roland.inventory_management_backend.dto.currency;

import java.util.List;

import dev.roland.inventory_management_backend.model.Currency;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CurrenciesResponse {
  private List<Currency> currencies;
}
