package dev.roland.inventory_management_backend.features.document.dto;

/**
 * Defines the draft update document API contract.
 *
 * @param draft draft value
 * @param version version value
 */
public record DraftUpdate(String version, Draft draft) {}
