package dev.roland.inventory_management_backend.features.document.dto;

import java.math.BigDecimal;

/**
 * Saved exchange-rate snapshot returned with a document.
 *
 * @param sourceCurrencyId source currency identifier
 * @param rate target units per source unit
 */
public record ExchangeRateResponse(Long sourceCurrencyId, BigDecimal rate) {}
