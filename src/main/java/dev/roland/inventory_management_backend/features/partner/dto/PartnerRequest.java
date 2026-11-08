package dev.roland.inventory_management_backend.features.partner.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import dev.roland.inventory_management_backend.common.validation.ValidationKeys;
import dev.roland.inventory_management_backend.features.partner.enumeration.PartnerType;

/**
 * Partner create and update payload.
 *
 * @param name partner display name
 * @param type partner classification
 * @param taxNumber optional tax registration number
 */
public record PartnerRequest(
    @NotBlank(message = ValidationKeys.REQUIRED) @Size(max = 255, message = ValidationKeys.TOO_LONG)
        String name,
    @NotNull(message = ValidationKeys.REQUIRED) PartnerType type,
    @Size(max = 50, message = ValidationKeys.TOO_LONG) String taxNumber) {}
