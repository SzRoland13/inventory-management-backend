package dev.roland.inventory_management_backend.features.partner.dto;

import java.time.LocalDateTime;
import java.util.List;

import dev.roland.inventory_management_backend.features.contact.dto.ContactResponse;
import dev.roland.inventory_management_backend.features.partner.enumeration.PartnerType;

/** Partner response including its contact history. */
public record PartnerResponse(
    Long id,
    String name,
    PartnerType type,
    String taxNumber,
    boolean archived,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    List<ContactResponse> contacts) {}
