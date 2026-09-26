package dev.roland.inventory_management_backend.features.product.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import dev.roland.inventory_management_backend.features.product.enumeration.ProductStatus;

/** Product API representation including its unit, category, and custom-attribute data. */
public record ProductResponse(
    Long id,
    String sku,
    String ean,
    String name,
    String description,
    String brand,
    ProductStatus status,
    Long unitId,
    String unitCode,
    String unitName,
    Long secondaryUnitId,
    String secondaryUnitCode,
    String secondaryUnitName,
    BigDecimal secondaryUnitsPerMainUnit,
    BigDecimal secondaryNetPrice,
    Long currencyId,
    BigDecimal netPrice,
    BigDecimal costPrice,
    BigDecimal vatRate,
    BigDecimal weight,
    BigDecimal width,
    BigDecimal height,
    BigDecimal depth,
    List<Long> categoryIds,
    List<ProductAttributeValueResponse> attributes,
    List<ProductStockResponse> stockByWarehouse,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    LocalDateTime deletedAt) {}
