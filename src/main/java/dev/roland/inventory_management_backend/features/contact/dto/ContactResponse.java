package dev.roland.inventory_management_backend.features.contact.dto;

import java.time.LocalDateTime;

import dev.roland.inventory_management_backend.features.contact.enumeration.ContactType;

/**
 * Contact response retaining removed contact history.
 *
 * @param id contact identifier
 * @param type contact classification
 * @param addressLine1 primary address line
 * @param addressLine2 optional secondary address line
 * @param city city or locality
 * @param country two-letter country code
 * @param phone optional phone number
 * @param email optional email address
 * @param removed whether the contact was removed
 * @param createdAt creation timestamp
 * @param updatedAt last update timestamp
 */
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
