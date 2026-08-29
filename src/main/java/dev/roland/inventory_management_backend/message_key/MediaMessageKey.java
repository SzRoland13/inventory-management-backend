package dev.roland.inventory_management_backend.message_key;

import lombok.Getter;

/** Message keys for media upload, preview, and deletion responses. */
@Getter
public enum MediaMessageKey implements MessageKey {
  /** Message key for url generated. */
  URL_GENERATED("media.url-generated"),
  /** Message key for media deleted. */
  MEDIA_DELETED("media.deleted"),
  ;

  private final String key;

  MediaMessageKey(String key) {
    this.key = key;
  }
}
