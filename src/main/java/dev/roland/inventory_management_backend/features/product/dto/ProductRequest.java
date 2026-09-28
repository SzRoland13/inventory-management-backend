package dev.roland.inventory_management_backend.features.product.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import dev.roland.inventory_management_backend.features.product.enumeration.ProductStatus;
import dev.roland.inventory_management_backend.features.product.message.ProductValidationMessage;

/**
 * Product create and update payload. Company ownership comes from the current company context.
 *
 * @param attributes attribute values
 * @param brandId optional company brand identifier
 * @param categoryIds associated category identifiers
 * @param description descriptive text
 * @param dimensions physical dimensions
 * @param ean product European Article Number
 * @param name display name
 * @param pricing product pricing configuration
 * @param sku product stock keeping unit
 * @param status product status
 * @param units product unit configuration
 * @return the operation result
 */
public record ProductRequest(
    @NotBlank(message = ProductValidationMessage.REQUIRED)
        @Size(max = 100, message = ProductValidationMessage.TOO_LONG)
        String sku,
    @Size(max = 20, message = ProductValidationMessage.TOO_LONG) String ean,
    @NotBlank(message = ProductValidationMessage.REQUIRED)
        @Size(max = 255, message = ProductValidationMessage.TOO_LONG)
        String name,
    String description,
    @Positive(message = ProductValidationMessage.POSITIVE) Long brandId,
    ProductStatus status,
    @NotNull(message = ProductValidationMessage.REQUIRED) @Valid Units units,
    @NotNull(message = ProductValidationMessage.REQUIRED) @Valid Pricing pricing,
    @Valid Dimensions dimensions,
    List<@Positive(message = ProductValidationMessage.POSITIVE) Long> categoryIds,
    List<@Valid ProductAttributeValueRequest> attributes) {

  /** Copies request collections while preserving their optional null state. */
  public ProductRequest {
    categoryIds = immutableCopy(categoryIds);
    attributes = immutableCopy(attributes);
  }

  /**
   * Returns a defensive copy of the category identifiers.
   *
   * @return immutable category identifier list, or null when omitted
   */
  @Override
  public List<Long> categoryIds() {
    return categoryIds == null ? null : Collections.unmodifiableList(new ArrayList<>(categoryIds));
  }

  /**
   * Returns a defensive copy of the attribute values.
   *
   * @return immutable attribute list, or null when omitted
   */
  @Override
  public List<ProductAttributeValueRequest> attributes() {
    return attributes == null ? null : Collections.unmodifiableList(new ArrayList<>(attributes));
  }

  private static <T> List<T> immutableCopy(final List<T> values) {
    return values == null ? null : Collections.unmodifiableList(new ArrayList<>(values));
  }

  /**
   * Main and optional secondary unit configuration.
   *
   * @param mainUnitId main unit identifier
   * @param secondaryUnitId optional secondary unit identifier
   * @param secondaryUnitsPerMainUnit number of secondary units in one main unit
   * @return the operation result
   */
  public record Units(
      @NotNull(message = ProductValidationMessage.REQUIRED)
          @Positive(message = ProductValidationMessage.POSITIVE)
          Long mainUnitId,
      @Positive(message = ProductValidationMessage.POSITIVE) Long secondaryUnitId,
      @Positive(message = ProductValidationMessage.POSITIVE)
          @Digits(integer = 16, fraction = 8, message = ProductValidationMessage.DECIMAL_PRECISION)
          BigDecimal secondaryUnitsPerMainUnit) {

    /**
     * Checks whether the supplied values meet the required condition.
     *
     * @return true if a matching record exists
     */
    @AssertTrue(message = ProductValidationMessage.INVALID_UNIT_CONFIGURATION)
    public boolean isConfigurationValid() {
      return (secondaryUnitId == null && secondaryUnitsPerMainUnit == null)
          || (secondaryUnitId != null
              && mainUnitId != null
              && !secondaryUnitId.equals(mainUnitId)
              && secondaryUnitsPerMainUnit != null
              && secondaryUnitsPerMainUnit.signum() > 0);
    }
  }

  /**
   * Product prices and tax rate.
   *
   * @param costPrice product cost price
   * @param currencyId currency identifier
   * @param netPrice net price per main unit
   * @param vatRate value added tax rate
   * @return the operation result
   */
  public record Pricing(
      @Positive(message = ProductValidationMessage.POSITIVE) Long currencyId,
      @NotNull(message = ProductValidationMessage.REQUIRED)
          @DecimalMin(value = "0", message = ProductValidationMessage.NON_NEGATIVE)
          @Digits(integer = 16, fraction = 8, message = ProductValidationMessage.DECIMAL_PRECISION)
          BigDecimal netPrice,
      @DecimalMin(value = "0", message = ProductValidationMessage.NON_NEGATIVE)
          @Digits(integer = 16, fraction = 8, message = ProductValidationMessage.DECIMAL_PRECISION)
          BigDecimal costPrice,
      @NotNull(message = ProductValidationMessage.REQUIRED)
          @DecimalMin(value = "0", message = ProductValidationMessage.NON_NEGATIVE)
          @Digits(integer = 2, fraction = 3, message = ProductValidationMessage.DECIMAL_PRECISION)
          BigDecimal vatRate) {}

  /**
   * Optional physical product measurements.
   *
   * @param depth product depth
   * @param height product height
   * @param weight product weight
   * @param width product width
   * @return the operation result
   */
  public record Dimensions(
      @DecimalMin(value = "0", message = ProductValidationMessage.NON_NEGATIVE)
          @Digits(integer = 12, fraction = 3, message = ProductValidationMessage.DECIMAL_PRECISION)
          BigDecimal weight,
      @DecimalMin(value = "0", message = ProductValidationMessage.NON_NEGATIVE)
          @Digits(integer = 12, fraction = 3, message = ProductValidationMessage.DECIMAL_PRECISION)
          BigDecimal width,
      @DecimalMin(value = "0", message = ProductValidationMessage.NON_NEGATIVE)
          @Digits(integer = 12, fraction = 3, message = ProductValidationMessage.DECIMAL_PRECISION)
          BigDecimal height,
      @DecimalMin(value = "0", message = ProductValidationMessage.NON_NEGATIVE)
          @Digits(integer = 12, fraction = 3, message = ProductValidationMessage.DECIMAL_PRECISION)
          BigDecimal depth) {}
}
