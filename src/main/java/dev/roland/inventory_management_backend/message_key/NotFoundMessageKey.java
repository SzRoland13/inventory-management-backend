package dev.roland.inventory_management_backend.message_key;

import lombok.Getter;

/** Message keys identifying domain entities that could not be found. */
@Getter
public enum NotFoundMessageKey implements MessageKey {
  /** Message key for user. */
  USER("not-found.user"),
  /** Message key for one-time code. */
  ONE_TIME_CODE("not-found.one-time-code"),
  /** Message key for refresh token. */
  REFRESH_TOKEN("not-found.refresh-token"),
  /** Message key for company. */
  COMPANY("not-found.company"),
  /** Message key for media asset. */
  MEDIA_ASSET("not-found.media-asset"),
  /** Message key for media usage. */
  MEDIA_USAGE("not-found.media-usage"),
  /** Message key for currency. */
  CURRENCY("not-found.currency"),
  /** Message key for document sequence. */
  DOCUMENT_SEQUENCE("not-found.document-sequence"),
  /** Message key for document prefix. */
  DOCUMENT_PREFIX("not-found.document-prefix"),
  /** Message key for document. */
  DOCUMENT("not-found.document"),
  /** Message key for document line. */
  DOCUMENT_LINE("not-found.document-line"),
  /** Message key for document relation. */
  DOCUMENT_RELATION("not-found.document-relation"),
  /** Message key for product. */
  PRODUCT("not-found.product"),
  /** Message key for product attribute definition. */
  PRODUCT_ATTRIBUTE_DEFINITION("not-found.product-attribute-definition"),
  /** Message key for product attribute option. */
  PRODUCT_ATTRIBUTE_OPTION("not-found.product-attribute-option"),
  /** Message key for product attribute value. */
  PRODUCT_ATTRIBUTE_VALUE("not-found.product-attribute-value"),
  /** Message key for product category. */
  PRODUCT_CATEGORY("not-found.product-category"),
  /** Message key for stock balance. */
  STOCK_BALANCE("not-found.stock-balance"),
  /** Message key for stock movement. */
  STOCK_MOVEMENT("not-found.stock-movement"),
  /** Message key for unit. */
  UNIT("not-found.unit"),
  ;

  private final String key;

  NotFoundMessageKey(String key) {
    this.key = key;
  }
}
