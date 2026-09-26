package dev.roland.inventory_management_backend.features.product.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import dev.roland.inventory_management_backend.features.product.message.ProductValidationMessage;

/**
 * Supplies one typed value for a product attribute definition.
 *
 * @param booleanValue boolean attribute value
 * @param dateValue date attribute value
 * @param definitionId attribute definition identifier
 * @param numberValue numeric attribute value
 * @param optionId selected option identifier
 * @param textValue text attribute value
 */
public record ProductAttributeValueRequest(
    @NotNull(message = ProductValidationMessage.REQUIRED) Long definitionId,
    @Positive(message = ProductValidationMessage.POSITIVE) Long optionId,
    String textValue,
    @Digits(integer = 12, fraction = 3, message = ProductValidationMessage.DECIMAL_PRECISION)
        BigDecimal numberValue,
    LocalDate dateValue,
    Boolean booleanValue) {
  /**
   * Checks whether the supplied values meet the required condition.
   *
   * @return true if a matching record exists
   */
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
