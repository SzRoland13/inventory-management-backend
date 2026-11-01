package dev.roland.inventory_management_backend.features.media_asset;

import java.util.Arrays;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Supported source formats for product images and the thumbnail output format. */
@Getter
@RequiredArgsConstructor
public enum ProductImageFormat {
  JPEG("image/jpeg"),
  PNG("image/png"),
  WEBP("image/webp");

  private final String mimeType;

  /**
   * Returns the data URI prefix for this image format.
   *
   * @return base64 data URI prefix
   */
  public String dataUriPrefix() {
    return "data:" + mimeType + ";base64,";
  }

  /**
   * Checks whether the MIME type is one of the supported product image formats.
   *
   * @param mimeType image MIME type
   * @return whether the MIME type is supported
   */
  public static boolean supports(final String mimeType) {
    return Arrays.stream(values()).anyMatch(format -> format.mimeType.equals(mimeType));
  }
}
