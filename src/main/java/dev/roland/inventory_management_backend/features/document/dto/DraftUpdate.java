package dev.roland.inventory_management_backend.features.document.dto;

import jakarta.validation.Valid;

/**
 * Defines the draft update document API contract.
 *
 * @param draft draft value
 * @param version version value
 */
public record DraftUpdate(String version, @Valid Draft draft) {}
