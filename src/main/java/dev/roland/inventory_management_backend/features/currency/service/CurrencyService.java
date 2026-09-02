package dev.roland.inventory_management_backend.features.currency.service;

import dev.roland.inventory_management_backend.common.service.BaseService;
import dev.roland.inventory_management_backend.features.currency.Currency;
import dev.roland.inventory_management_backend.features.currency.dto.CurrenciesResponse;

public interface CurrencyService extends BaseService<Currency, Long> {
  /**
   * Returns all supported currencies as an API response DTO.
   *
   * @return currencies response containing every configured currency
   */
  CurrenciesResponse getAll();
}
