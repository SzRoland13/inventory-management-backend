package dev.roland.inventory_management_backend.features.product.dto;

/** Unit-of-measure API representation. */
public record UnitResponse(Long id, String code, String name, String symbol, boolean system) {}
