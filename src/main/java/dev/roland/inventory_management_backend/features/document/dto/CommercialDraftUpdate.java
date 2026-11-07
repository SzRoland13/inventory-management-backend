package dev.roland.inventory_management_backend.features.document.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Versioned update to a commercial draft.
 *
 * @param version expected document version
 * @param draft replacement draft data
 */
public record CommercialDraftUpdate(@NotNull Long version, @Valid @NotNull CommercialDraft draft) {}
