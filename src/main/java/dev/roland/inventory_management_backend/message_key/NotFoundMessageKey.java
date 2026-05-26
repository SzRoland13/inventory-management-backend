package dev.roland.inventory_management_backend.message_key;

import lombok.Getter;

@Getter
public enum NotFoundMessageKey implements MessageKey {
  USER("not-found.user"),
  ONE_TIME_CODE("not-found.one-time-code"),
  REFRESH_TOKEN("not-found.refresh-token"),
  COMPANY("not-found.company"),
  MEDIA_ASSET("not-found.media-asset"),
  MEDIA_USAGE("not-found.media-usage"),
  CURRENCY("not-found.currency"),
  DOCUMENT_SEQUENCE("not-found.document-sequence"),
  DOCUMENT_PREFIX("not-found.document-prefix"),
  DOCUMENT("not-found.document"),
  DOCUMENT_LINE("not-found.document-line"),
  DOCUMENT_RELATION("not-found.document-relation"),
  PRODUCT("not-found.product"),
  PRODUCT_ATTRIBUTE_DEFINITION("not-found.product-attribute-definition"),
  PRODUCT_ATTRIBUTE_OPTION("not-found.product-attribute-option"),
  PRODUCT_ATTRIBUTE_VALUE("not-found.product-attribute-value"),
  PRODUCT_CATEGORY("not-found.product-category"),
  STOCK_BALANCE("not-found.stock-balance"),
  STOCK_MOVEMENT("not-found.stock-movement"),
  UNIT("not-found.unit"),
  ;

  private final String key;

  NotFoundMessageKey(String key) {
    this.key = key;
  }
}
