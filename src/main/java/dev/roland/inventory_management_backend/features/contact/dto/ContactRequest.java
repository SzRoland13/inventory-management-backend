package dev.roland.inventory_management_backend.features.contact.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import dev.roland.inventory_management_backend.features.contact.enumeration.ContactType;

/**
 * Contact address and communication data.
 *
 * @param type contact classification
 * @param addressLine1 primary address line
 * @param addressLine2 optional secondary address line
 * @param city city or locality
 * @param country two-letter country code
 * @param phone optional phone number
 * @param email optional email address
 */
public record ContactRequest(
    @NotNull ContactType type,
    @NotBlank @Size(max = 255) String addressLine1,
    @Size(max = 255) String addressLine2,
    @NotBlank @Size(max = 100) String city,
    @NotBlank @Size(min = 2, max = 2) String country,
    @Size(max = 50) String phone,
    @Email @Size(max = 255) String email) {}
