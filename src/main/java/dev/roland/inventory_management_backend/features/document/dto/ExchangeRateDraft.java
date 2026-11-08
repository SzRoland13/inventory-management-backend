package dev.roland.inventory_management_backend.features.document.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import dev.roland.inventory_management_backend.common.validation.ValidationKeys;

/**
 * A target-currency amount per one unit of source currency.
 *
 * @param sourceCurrencyId source currency identifier
 * @param rate target units per source unit
 */
public record ExchangeRateDraft(
    @NotNull(message = ValidationKeys.REQUIRED) @Positive(message = ValidationKeys.INVALID_VALUE)
        Long sourceCurrencyId,
    @NotNull(message = ValidationKeys.REQUIRED)
        @Positive(message = ValidationKeys.INVALID_VALUE)
        @Digits(integer = 12, fraction = 8, message = ValidationKeys.INVALID_VALUE)
        BigDecimal rate) {}
