package dev.roland.inventory_management_backend.features.media_usage.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** Identifies the purpose for which a media asset is attached to an entity. */
@Getter
@AllArgsConstructor
public enum MediaUsageType {
  /** Represents the logo value. */
  LOGO("logo"),
  /** Represents the avatar value. */
  AVATAR("avatar"),
  /** Represents the image value. */
  IMAGE("image"),
  /** Represents the other value. */
  OTHER("other"),
  /** User supplied business document file. */
  DOCUMENT_FILE("document-file"),
  /** Generated business document PDF. */
  DOCUMENT_PDF("document-pdf"),
  ;

  private final String name;
}
