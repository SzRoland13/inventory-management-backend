package dev.roland.inventory_management_backend.features.brand.message;

import dev.roland.inventory_management_backend.common.message.MessageKey;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Message keys for brand operations. */
@Getter
@RequiredArgsConstructor
public enum BrandMessageKey implements MessageKey {
  BRANDS_RETRIEVED("brand.list.retrieved"),
  BRAND_CREATED("brand.created"),
  BRAND_UPDATED("brand.updated"),
  BRAND_DELETED("brand.deleted"),
  BRAND_ALREADY_EXISTS("brand.error.already_exists");

  private final String key;
}
