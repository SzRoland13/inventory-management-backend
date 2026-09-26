package dev.roland.inventory_management_backend.features.currency.service;

import java.util.Optional;

import dev.roland.inventory_management_backend.common.service.BaseService;
import dev.roland.inventory_management_backend.features.currency.Currency;
import dev.roland.inventory_management_backend.features.currency.dto.CurrenciesResponse;

/** Defines operations supported by the currency feature. */
public interface CurrencyService extends BaseService<Currency, Long> {
  Optional<Currency> findById(Long id);

  /**
   * Returns all supported currencies as an API response DTO.
   *
   * @return currencies response containing every configured currency
   */
  CurrenciesResponse getAll();
}
