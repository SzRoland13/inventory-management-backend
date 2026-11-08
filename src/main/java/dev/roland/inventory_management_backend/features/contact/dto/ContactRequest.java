package dev.roland.inventory_management_backend.features.contact.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import dev.roland.inventory_management_backend.common.validation.CountryCode;
import dev.roland.inventory_management_backend.common.validation.E164Phone;
import dev.roland.inventory_management_backend.common.validation.EmailAddress;
import dev.roland.inventory_management_backend.common.validation.ValidationKeys;
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
    @NotNull(message = ValidationKeys.REQUIRED) ContactType type,
    @NotBlank(message = ValidationKeys.REQUIRED) @Size(max = 255, message = ValidationKeys.TOO_LONG)
        String addressLine1,
    @Size(max = 255, message = ValidationKeys.TOO_LONG) String addressLine2,
    @NotBlank(message = ValidationKeys.REQUIRED) @Size(max = 100, message = ValidationKeys.TOO_LONG)
        String city,
    @NotBlank(message = ValidationKeys.REQUIRED)
        @Size(min = 2, max = 2, message = ValidationKeys.INVALID_VALUE)
        @CountryCode
        String country,
    @Size(max = 50, message = ValidationKeys.TOO_LONG) @E164Phone String phone,
    @EmailAddress @Size(max = 254, message = ValidationKeys.INVALID_EMAIL) String email) {}
