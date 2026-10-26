package dev.roland.inventory_management_backend.features.contact.dto;

import java.time.LocalDateTime;

import dev.roland.inventory_management_backend.features.contact.enumeration.ContactType;

/** Contact response retaining removed contact history. */
public record ContactResponse(
    Long id,
    ContactType type,
    String addressLine1,
    String addressLine2,
    String city,
    String country,
    String phone,
    String email,
    boolean removed,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {}
