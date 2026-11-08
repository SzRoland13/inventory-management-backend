package dev.roland.inventory_management_backend.features.document.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import dev.roland.inventory_management_backend.common.validation.ValidationKeys;

/**
 * Request to preview commercial source balances and totals without persisting a document.
 *
 * @param draft commercial document data to preview
 * @param excludeDocumentId document to exclude from source balance calculations, if any
 */
public record CommercialPreviewRequest(
    @Valid @NotNull(message = ValidationKeys.REQUIRED) CommercialDraft draft,
    @Positive(message = ValidationKeys.INVALID_VALUE) Long excludeDocumentId) {}
