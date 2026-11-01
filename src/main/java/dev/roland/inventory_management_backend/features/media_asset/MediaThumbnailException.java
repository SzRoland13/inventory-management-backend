package dev.roland.inventory_management_backend.features.media_asset;

import dev.roland.inventory_management_backend.common.exception.ApiException;
import dev.roland.inventory_management_backend.common.message.MessageKey;
import lombok.Getter;

/** Keyed failure raised while processing a media thumbnail job. */
@Getter
public class MediaThumbnailException extends ApiException {
  private final boolean permanent;

  public MediaThumbnailException(
      final MessageKey messageKey, final boolean permanent, final Throwable cause) {
    super(messageKey, cause);
    this.permanent = permanent;
  }

  public MediaThumbnailException(final MessageKey messageKey, final boolean permanent) {
    this(messageKey, permanent, null);
  }
}
