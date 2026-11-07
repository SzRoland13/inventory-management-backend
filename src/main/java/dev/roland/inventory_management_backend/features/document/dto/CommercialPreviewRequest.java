package dev.roland.inventory_management_backend.features.document.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Request to preview commercial source balances and totals without persisting a document.
 *
 * @param draft commercial document data to preview
 * @param excludeDocumentId document to exclude from source balance calculations, if any
 */
public record CommercialPreviewRequest(
    @Valid @NotNull CommercialDraft draft, Long excludeDocumentId) {}
