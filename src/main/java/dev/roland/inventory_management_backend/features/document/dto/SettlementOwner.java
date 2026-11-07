package dev.roland.inventory_management_backend.features.document.dto;

import dev.roland.inventory_management_backend.features.user.enumeration.UserRole;

/**
 * Minimal company-scoped user data for assigning financial settlement ownership.
 *
 * @param id user identifier
 * @param username user name
 * @param role user role
 */
public record SettlementOwner(Long id, String username, UserRole role) {}
