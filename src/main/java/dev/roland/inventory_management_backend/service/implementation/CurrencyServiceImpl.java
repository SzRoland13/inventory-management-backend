package dev.roland.inventory_management_backend.service.implementation;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.dto.currency.CurrenciesResponse;
import dev.roland.inventory_management_backend.dto.currency.CurrencyResponse;
import dev.roland.inventory_management_backend.messageKey.MessageKey;
import dev.roland.inventory_management_backend.messageKey.NotFoundMessageKey;
import dev.roland.inventory_management_backend.model.Currency;
import dev.roland.inventory_management_backend.repository.CurrencyRepository;
import dev.roland.inventory_management_backend.service.CurrencyService;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class CurrencyServiceImpl implements CurrencyService {
  private final CurrencyRepository currencyRepository;

  @Override
  public JpaRepository<Currency, Long> getRepository() {
    return currencyRepository;
  }

  @Override
  public MessageKey getNotFoundMessageKey() {
    return NotFoundMessageKey.CURRENCY;
  }

  @Override
  public CurrenciesResponse getAll() {
    List<Currency> currencies = findAll();

    return CurrenciesResponse.builder()
        .currencies(currencies.stream().map(CurrencyResponse::toDto).toList())
        .build();
  }
}
