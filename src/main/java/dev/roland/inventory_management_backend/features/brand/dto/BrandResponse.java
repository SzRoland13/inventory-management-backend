package dev.roland.inventory_management_backend.features.brand.dto;

/**
 * Brand option returned to product catalog clients.
 *
 * @param id brand identifier
 * @param name brand display name
 */
public record BrandResponse(Long id, String name) {}
