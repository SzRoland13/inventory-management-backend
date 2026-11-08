package dev.roland.inventory_management_backend.features.document.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import dev.roland.inventory_management_backend.common.validation.ValidationKeys;

/**
 * Versioned update to a commercial draft.
 *
 * @param version expected document version
 * @param draft replacement draft data
 */
public record CommercialDraftUpdate(
    @NotNull(message = ValidationKeys.REQUIRED) Long version,
    @Valid @NotNull(message = ValidationKeys.REQUIRED) CommercialDraft draft) {}
