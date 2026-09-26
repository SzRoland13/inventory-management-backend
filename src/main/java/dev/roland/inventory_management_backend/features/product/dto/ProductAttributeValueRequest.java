package dev.roland.inventory_management_backend.features.product.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import dev.roland.inventory_management_backend.features.product.message.ProductValidationMessage;

/** Supplies one typed value for a product attribute definition. */
public record ProductAttributeValueRequest(
    @NotNull(message = ProductValidationMessage.REQUIRED) Long definitionId,
    @Positive(message = ProductValidationMessage.POSITIVE) Long optionId,
    String textValue,
    @Digits(integer = 12, fraction = 3, message = ProductValidationMessage.DECIMAL_PRECISION)
        BigDecimal numberValue,
    LocalDate dateValue,
    Boolean booleanValue) {
  @AssertTrue(message = ProductValidationMessage.EXACTLY_ONE_ATTRIBUTE_VALUE)
  public boolean isExactlyOneValueSupplied() {
    int count = 0;
    count += optionId == null ? 0 : 1;
    count += textValue == null ? 0 : 1;
    count += numberValue == null ? 0 : 1;
    count += dateValue == null ? 0 : 1;
    count += booleanValue == null ? 0 : 1;
    return count == 1;
  }
}
