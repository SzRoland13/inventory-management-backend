package dev.roland.inventory_management_backend.features.document_line.message;

import dev.roland.inventory_management_backend.common.message.MessageKey;

/** Message keys for document line validation errors. */
public enum DocumentLineMessageKey implements MessageKey {
  PRODUCT_REQUIRED("document_line.error.product_required"),
  UNIT_REQUIRED("document_line.error.unit_required"),
  INVALID_UNIT("document_line.error.invalid_unit"),
  INVALID_CONVERSION_FACTOR("document_line.error.invalid_conversion_factor");

  private final String key;

  DocumentLineMessageKey(final String key) {
    this.key = key;
  }

  @Override
  public String getKey() {
    return key;
  }
}
