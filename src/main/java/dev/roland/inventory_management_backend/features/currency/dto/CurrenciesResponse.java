package dev.roland.inventory_management_backend.features.currency.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CurrenciesResponse {
  private List<CurrencyResponse> currencies;
}
