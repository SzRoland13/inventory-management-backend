package dev.roland.inventory_management_backend.features.product.message;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** Validation message keys used by product request DTO constraints. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProductValidationMessage {
  public static final String REQUIRED = "product.validation.required";
  public static final String TOO_LONG = "product.validation.too_long";
  public static final String POSITIVE = "product.validation.positive";
  public static final String NON_NEGATIVE = "product.validation.non_negative";
  public static final String DECIMAL_PRECISION = "product.validation.decimal_precision";
  public static final String INVALID_UNIT_CONFIGURATION =
      "product.validation.invalid_unit_configuration";
  public static final String EXACTLY_ONE_ATTRIBUTE_VALUE =
      "product.validation.exactly_one_attribute_value";
  public static final String PAGE_SIZE_LIMIT = "product.validation.page_size_limit";
}
