package dev.roland.inventory_management_backend.features.product.message;

import dev.roland.inventory_management_backend.common.message.MessageKey;

/** Message keys for product validation and lifecycle operations. */
public enum ProductMessageKey implements MessageKey {
  PRODUCTS_RETRIEVED("product.list.retrieved"),
  PRODUCT_RETRIEVED("product.retrieved"),
  PRODUCT_CREATED("product.created"),
  PRODUCT_UPDATED("product.updated"),
  PRODUCT_ARCHIVED("product.archived"),
  PRODUCT_RESTORED("product.restored"),
  ARCHIVED_PRODUCTS_RETRIEVED("product.archived_list.retrieved"),
  INVALID_PRODUCT_DATA("product.error.invalid_data"),
  VALIDATION_REQUIRED(ProductValidationMessage.REQUIRED),
  VALIDATION_TOO_LONG(ProductValidationMessage.TOO_LONG),
  VALIDATION_POSITIVE(ProductValidationMessage.POSITIVE),
  VALIDATION_NON_NEGATIVE(ProductValidationMessage.NON_NEGATIVE),
  VALIDATION_DECIMAL_PRECISION(ProductValidationMessage.DECIMAL_PRECISION),
  VALIDATION_INVALID_UNIT_CONFIGURATION(ProductValidationMessage.INVALID_UNIT_CONFIGURATION),
  VALIDATION_EXACTLY_ONE_ATTRIBUTE_VALUE(ProductValidationMessage.EXACTLY_ONE_ATTRIBUTE_VALUE),
  VALIDATION_PAGE_SIZE_LIMIT(ProductValidationMessage.PAGE_SIZE_LIMIT),
  PRODUCT_SKU_ALREADY_EXISTS("product.error.sku_exists"),
  PRODUCT_ALREADY_ARCHIVED("product.error.already_archived"),
  PRODUCT_NOT_ARCHIVED("product.error.not_archived"),
  PRODUCT_UNIT_IN_USE("product.error.unit_in_use");

  private final String key;

  ProductMessageKey(final String key) {
    this.key = key;
  }

  @Override
  public String getKey() {
    return key;
  }
}
