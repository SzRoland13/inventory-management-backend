package dev.roland.inventory_management_backend.features.currency.service.impl;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.common.message.MessageKey;
import dev.roland.inventory_management_backend.common.message.NotFoundMessageKey;
import dev.roland.inventory_management_backend.features.currency.Currency;
import dev.roland.inventory_management_backend.features.currency.dto.CurrenciesResponse;
import dev.roland.inventory_management_backend.features.currency.dto.CurrencyResponse;
import dev.roland.inventory_management_backend.features.currency.repository.CurrencyRepository;
import dev.roland.inventory_management_backend.features.currency.service.CurrencyService;
import lombok.AllArgsConstructor;

/** Implements the currency service operations. */
@Service
@AllArgsConstructor
public class CurrencyServiceImpl implements CurrencyService {
  private final CurrencyRepository currencyRepository;

  /** {@inheritDoc} */
  @Override
  public JpaRepository<Currency, Long> getRepository() {
    return currencyRepository;
  }

  /** {@inheritDoc} */
  @Override
  public MessageKey getNotFoundMessageKey() {
    return NotFoundMessageKey.CURRENCY;
  }

  /**
   * {@inheritDoc}
   *
   * @return get all result
   */
  @Override
  public CurrenciesResponse getAll() {
    final List<Currency> currencies = findAll();

    return CurrenciesResponse.builder()
        .currencies(currencies.stream().map(CurrencyResponse::toDto).toList())
        .build();
  }

  @Override
  public java.util.Optional<Currency> findById(Long id) {
    return currencyRepository.findById(id);
  }
}
