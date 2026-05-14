package dev.roland.inventory_management_backend.messageKey;

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
  ;

  private final String key;

  NotFoundMessageKey(String key) {
    this.key = key;
  }
}
