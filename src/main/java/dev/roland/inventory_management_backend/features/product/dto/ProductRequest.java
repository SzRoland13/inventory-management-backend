package dev.roland.inventory_management_backend.features.product.dto;

import java.math.BigDecimal;
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

/** Product create and update payload. Company ownership comes from the current company context. */
public record ProductRequest(
    @NotBlank(message = ProductValidationMessage.REQUIRED)
        @Size(max = 100, message = ProductValidationMessage.TOO_LONG)
        String sku,
    @Size(max = 20, message = ProductValidationMessage.TOO_LONG) String ean,
    @NotBlank(message = ProductValidationMessage.REQUIRED)
        @Size(max = 255, message = ProductValidationMessage.TOO_LONG)
        String name,
    String description,
    @Size(max = 100, message = ProductValidationMessage.TOO_LONG) String brand,
    ProductStatus status,
    @NotNull(message = ProductValidationMessage.REQUIRED) @Valid Units units,
    @NotNull(message = ProductValidationMessage.REQUIRED) @Valid Pricing pricing,
    @Valid Dimensions dimensions,
    List<@Positive(message = ProductValidationMessage.POSITIVE) Long> categoryIds,
    List<@Valid ProductAttributeValueRequest> attributes) {

  /** Main and optional secondary unit configuration. */
  public record Units(
      @NotNull(message = ProductValidationMessage.REQUIRED)
          @Positive(message = ProductValidationMessage.POSITIVE)
          Long mainUnitId,
      @Positive(message = ProductValidationMessage.POSITIVE) Long secondaryUnitId,
      @Positive(message = ProductValidationMessage.POSITIVE)
          @Digits(integer = 16, fraction = 8, message = ProductValidationMessage.DECIMAL_PRECISION)
          BigDecimal secondaryUnitsPerMainUnit) {

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

  /** Product prices and tax rate. */
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

  /** Optional physical product measurements. */
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
