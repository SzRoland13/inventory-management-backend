package dev.roland.inventory_management_backend.features.media_asset.message;

import dev.roland.inventory_management_backend.common.message.MessageKey;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Message keys for media upload, preview, and thumbnail processing. */
@Getter
@RequiredArgsConstructor
public enum MediaMessageKey implements MessageKey {
  /** Message key for url generated. */
  URL_GENERATED("media.url-generated"),
  THUMBNAIL_JOB_NOT_FOUND("media.thumbnail.job-not-found"),
  THUMBNAIL_JOB_INVALID("media.thumbnail.job-invalid"),
  THUMBNAIL_PAYLOAD_INVALID("media.thumbnail.payload-invalid"),
  THUMBNAIL_IMAGE_INVALID("media.thumbnail.image-invalid"),
  THUMBNAIL_ENCODER_UNAVAILABLE("media.thumbnail.encoder-unavailable"),
  THUMBNAIL_GENERATION_FAILED("media.thumbnail.generation-failed"),
  ;

  private final String key;
}
