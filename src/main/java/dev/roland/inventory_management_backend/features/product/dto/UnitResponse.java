package dev.roland.inventory_management_backend.features.product.dto;

/**
 * Unit-of-measure API representation.
 *
 * @param code unit or entity code
 * @param id unique identifier
 * @param name display name
 * @param symbol unit symbol
 * @param system whether this is a system unit
 */
public record UnitResponse(Long id, String code, String name, String symbol, boolean system) {}
