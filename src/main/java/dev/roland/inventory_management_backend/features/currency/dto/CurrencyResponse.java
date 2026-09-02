package dev.roland.inventory_management_backend.features.currency.dto;

import dev.roland.inventory_management_backend.features.currency.Currency;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CurrencyResponse {
  private Long id;
  private String code;
  private String name;
  private String symbol;

  public static CurrencyResponse toDto(Currency currency) {
    return CurrencyResponse.builder()
        .id(currency.getId())
        .code(currency.getCode())
        .name(currency.getName())
        .symbol(currency.getSymbol())
        .build();
  }
}
