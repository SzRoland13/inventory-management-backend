package dev.roland.inventory_management_backend.features.currency.dto;

import dev.roland.inventory_management_backend.features.currency.Currency;
import lombok.Builder;
import lombok.Data;

/** Shapes currency data returned to API clients. */
@Data
@Builder
public class CurrencyResponse {
  private Long id;
  private String code;
  private String name;
  private String symbol;

  /**
   * Builds a currency response from the supplied currency.
   *
   * @param currency currency supplied to this method
   * @return to dto result
   */
  public static CurrencyResponse toDto(final Currency currency) {
    return CurrencyResponse.builder()
        .id(currency.getId())
        .code(currency.getCode())
        .name(currency.getName())
        .symbol(currency.getSymbol())
        .build();
  }
}
