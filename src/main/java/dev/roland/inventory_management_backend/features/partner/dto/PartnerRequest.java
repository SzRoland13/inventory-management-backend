package dev.roland.inventory_management_backend.features.partner.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import dev.roland.inventory_management_backend.features.partner.enumeration.PartnerType;

/**
 * Partner create and update payload.
 *
 * @param name partner display name
 * @param type partner classification
 * @param taxNumber optional tax registration number
 */
public record PartnerRequest(
    @NotBlank @Size(max = 255) String name,
    @NotNull PartnerType type,
    @Size(max = 50) String taxNumber) {}
