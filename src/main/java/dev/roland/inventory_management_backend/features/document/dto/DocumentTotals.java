package dev.roland.inventory_management_backend.features.document.dto;

import java.math.BigDecimal;

/**
 * Defines the document totals document API contract.
 *
 * @param currencyCode currency code value
 * @param gross gross value
 * @param net net value
 * @param roundingAdjustment rounding adjustment value
 * @param tax tax value
 */
public record DocumentTotals(
    String currencyCode,
    BigDecimal net,
    BigDecimal tax,
    BigDecimal gross,
    BigDecimal roundingAdjustment) {}
