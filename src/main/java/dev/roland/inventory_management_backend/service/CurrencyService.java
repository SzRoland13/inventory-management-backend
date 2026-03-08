package dev.roland.inventory_management_backend.service;

import dev.roland.inventory_management_backend.dto.currency.CurrenciesResponse;
import dev.roland.inventory_management_backend.model.Currency;

public interface CurrencyService extends BaseService<Currency, Long> {
  CurrenciesResponse getAll();
}
