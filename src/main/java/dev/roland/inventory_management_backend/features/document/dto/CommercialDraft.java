package dev.roland.inventory_management_backend.features.document.dto;

import static dev.roland.inventory_management_backend.features.document.dto.DocumentApiDtoCollections.immutableList;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import dev.roland.inventory_management_backend.features.document.enumeration.DocumentType;

/**
 * Request to create an invoice, supplier bill, or financial credit from source lines.
 *
 * @param type commercial document type
 * @param sourceDocumentIds source document identifiers
 * @param date document date
 * @param currencyId document currency identifier
 * @param paymentTerms payment terms snapshot
 * @param reason credit reason
 * @param referenceNumber external reference number
 * @param settlementOwnerId settlement owner identifier
 * @param lines allocated source lines
 */
public record CommercialDraft(
    @NotNull DocumentType type,
    @NotEmpty List<Long> sourceDocumentIds,
    @NotNull LocalDate date,
    @NotNull Long currencyId,
    String paymentTerms,
    String reason,
    String referenceNumber,
    Long settlementOwnerId,
    @NotEmpty List<@Valid CommercialLineDraft> lines) {
  /** Copies list values so callers cannot mutate the request after construction. */
  public CommercialDraft {
    sourceDocumentIds = immutableList(sourceDocumentIds);
    lines = immutableList(lines);
  }

  /**
   * Returns an immutable copy of source document identifiers.
   *
   * @return immutable source document identifiers
   */
  @Override
  public List<Long> sourceDocumentIds() {
    return immutableList(sourceDocumentIds);
  }

  /**
   * Returns an immutable copy of source line allocations.
   *
   * @return immutable source line allocations
   */
  @Override
  public List<CommercialLineDraft> lines() {
    return immutableList(lines);
  }
}
