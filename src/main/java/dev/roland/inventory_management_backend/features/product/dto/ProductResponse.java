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
    Units units,
    Pricing pricing,
    Dimensions dimensions,
    List<Long> categoryIds,
    List<ProductAttributeValueResponse> attributes,
    List<ProductStockResponse> stockByWarehouse,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    LocalDateTime deletedAt) {

  /** Product's main and optional secondary units. */
  public record Units(
      UnitResponse main, UnitResponse secondary, BigDecimal secondaryUnitsPerMainUnit) {}

  /** Product prices and tax details. */
  public record Pricing(
      Long currencyId,
      BigDecimal netPrice,
      BigDecimal secondaryNetPrice,
      BigDecimal costPrice,
      BigDecimal vatRate) {}

  /** Physical product measurements. */
  public record Dimensions(
      BigDecimal weight, BigDecimal width, BigDecimal height, BigDecimal depth) {}
}
