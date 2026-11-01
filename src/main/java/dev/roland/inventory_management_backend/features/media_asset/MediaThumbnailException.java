package dev.roland.inventory_management_backend.features.media_asset;

import dev.roland.inventory_management_backend.common.exception.ApiException;
import dev.roland.inventory_management_backend.common.message.MessageKey;
import lombok.Getter;

/** Keyed failure raised while processing a media thumbnail job. */
@Getter
public class MediaThumbnailException extends ApiException {
  private final boolean permanent;

  /**
   * Creates a keyed thumbnail failure with retry classification and an underlying cause.
   *
   * @param messageKey localized message associated with the failure
   * @param permanent whether retrying cannot succeed
   * @param cause underlying failure
   */
  public MediaThumbnailException(
      final MessageKey messageKey, final boolean permanent, final Throwable cause) {
    super(messageKey, cause);
    this.permanent = permanent;
  }

  /**
   * Creates a keyed thumbnail failure with retry classification.
   *
   * @param messageKey localized message associated with the failure
   * @param permanent whether retrying cannot succeed
   */
  public MediaThumbnailException(final MessageKey messageKey, final boolean permanent) {
    this(messageKey, permanent, null);
  }
}
