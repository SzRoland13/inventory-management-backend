package dev.roland.inventory_management_backend.features.product.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import dev.roland.inventory_management_backend.features.brand.dto.BrandResponse;
import dev.roland.inventory_management_backend.features.product.enumeration.ProductStatus;

/**
 * Product API representation including its unit, category, and custom-attribute data.
 *
 * @param attributes attribute values
 * @param brand selected brand, if any
 * @param categoryIds associated category identifiers
 * @param createdAt creation timestamp
 * @param deletedAt archival timestamp, or null when active
 * @param description descriptive text
 * @param dimensions physical dimensions
 * @param ean product European Article Number
 * @param id unique identifier
 * @param name display name
 * @param pricing product pricing configuration
 * @param sku product stock keeping unit
 * @param status product status
 * @param stockByWarehouse stock balances grouped by warehouse
 * @param units product unit configuration
 * @param updatedAt last update timestamp
 * @return the operation result
 */
public record ProductResponse(
    Long id,
    String sku,
    String ean,
    String name,
    String description,
    BrandResponse brand,
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

  /** Copies response collections to keep the representation immutable. */
  public ProductResponse {
    categoryIds = List.copyOf(categoryIds);
    attributes = List.copyOf(attributes);
    stockByWarehouse = List.copyOf(stockByWarehouse);
  }

  /**
   * Product's main and optional secondary units.
   *
   * @param main main unit
   * @param secondary optional secondary unit
   * @param secondaryUnitsPerMainUnit number of secondary units in one main unit
   * @return the operation result
   */
  public record Units(
      UnitResponse main, UnitResponse secondary, BigDecimal secondaryUnitsPerMainUnit) {}

  /**
   * Product prices and tax details.
   *
   * @param costPrice product cost price
   * @param currencyId currency identifier
   * @param netPrice net price per main unit
   * @param secondaryNetPrice derived net price per secondary unit
   * @param vatRate value added tax rate
   * @return the operation result
   */
  public record Pricing(
      Long currencyId,
      BigDecimal netPrice,
      BigDecimal secondaryNetPrice,
      BigDecimal costPrice,
      BigDecimal vatRate) {}

  /**
   * Physical product measurements.
   *
   * @param depth product depth
   * @param height product height
   * @param weight product weight
   * @param width product width
   * @return the operation result
   */
  public record Dimensions(
      BigDecimal weight, BigDecimal width, BigDecimal height, BigDecimal depth) {}
}
