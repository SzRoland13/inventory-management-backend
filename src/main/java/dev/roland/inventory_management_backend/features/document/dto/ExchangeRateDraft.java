package dev.roland.inventory_management_backend.features.document.dto;

import java.math.BigDecimal;

/**
 * A target-currency amount per one unit of source currency.
 *
 * @param sourceCurrencyId source currency identifier
 * @param rate target units per source unit
 */
public record ExchangeRateDraft(Long sourceCurrencyId, BigDecimal rate) {}
