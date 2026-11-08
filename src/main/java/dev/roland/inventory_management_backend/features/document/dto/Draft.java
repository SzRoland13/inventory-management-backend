package dev.roland.inventory_management_backend.features.document.dto;

import static dev.roland.inventory_management_backend.features.document.dto.DocumentApiDtoCollections.immutableList;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import dev.roland.inventory_management_backend.common.validation.ValidationKeys;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentType;

/**
 * Defines the draft document API contract.
 *
 * @param date date value
 * @param currencyId target currency identifier
 * @param exchangeRates source currency rates
 * @param lines lines value
 * @param notes notes value
 * @param partnerId partner identifier value
 * @param type type value
 * @param warehouseId warehouse identifier value
 * @param paymentTerms payment terms snapshot
 */
public record Draft(
    @NotNull(message = ValidationKeys.REQUIRED) DocumentType type,
    @NotNull(message = ValidationKeys.REQUIRED) LocalDate date,
    @NotNull(message = ValidationKeys.REQUIRED) @Positive(message = ValidationKeys.INVALID_VALUE)
        Long partnerId,
    @Positive(message = ValidationKeys.INVALID_VALUE) Long warehouseId,
    @Size(max = 2000, message = ValidationKeys.TOO_LONG) String notes,
    @NotEmpty(message = ValidationKeys.REQUIRED) @Valid List<LineDraft> lines,
    @NotNull(message = ValidationKeys.REQUIRED) @Positive(message = ValidationKeys.INVALID_VALUE)
        Long currencyId,
    @Valid List<ExchangeRateDraft> exchangeRates,
    @Size(max = 500, message = ValidationKeys.TOO_LONG) String paymentTerms) {
  /**
   * Copies the line list to prevent external mutation.
   *
   * @param type document type
   * @param date document date
   * @param partnerId partner identifier
   * @param warehouseId warehouse identifier
   * @param notes document notes
   * @param lines source line list
   */
  public Draft {
    lines = immutableList(lines);
    exchangeRates = immutableList(exchangeRates);
  }

  /**
   * Retains source compatibility for callers that rely on the company preferred currency.
   *
   * @param type document type
   * @param date document date
   * @param partnerId partner identifier
   * @param warehouseId warehouse identifier
   * @param notes document notes
   * @param lines document lines
   */
  public Draft(
      final DocumentType type,
      final LocalDate date,
      final Long partnerId,
      final Long warehouseId,
      final String notes,
      final List<LineDraft> lines) {
    this(type, date, partnerId, warehouseId, notes, lines, null, List.of(), null);
  }

  /** Retains source compatibility for callers without payment terms. */
  public Draft(
      final DocumentType type,
      final LocalDate date,
      final Long partnerId,
      final Long warehouseId,
      final String notes,
      final List<LineDraft> lines,
      final Long currencyId,
      final List<ExchangeRateDraft> exchangeRates) {
    this(type, date, partnerId, warehouseId, notes, lines, currencyId, exchangeRates, null);
  }

  /**
   * Returns a defensive copy of the draft lines.
   *
   * @return immutable draft lines
   */
  @Override
  public List<LineDraft> lines() {
    return immutableList(lines);
  }

  @Override
  public List<ExchangeRateDraft> exchangeRates() {
    return immutableList(exchangeRates);
  }
}
