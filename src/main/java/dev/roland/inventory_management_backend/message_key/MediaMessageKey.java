package dev.roland.inventory_management_backend.message_key;

import lombok.Getter;

@Getter
public enum MediaMessageKey implements MessageKey {
  URL_GENERATED("media.url-generated"),
  MEDIA_DELETED("media.deleted"),
  ;

  private final String key;

  MediaMessageKey(String key) {
    this.key = key;
  }
}
