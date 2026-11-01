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

  public String dataUriPrefix() {
    return "data:" + mimeType + ";base64,";
  }

  public static boolean supports(final String mimeType) {
    return Arrays.stream(values()).anyMatch(format -> format.mimeType.equals(mimeType));
  }
}
