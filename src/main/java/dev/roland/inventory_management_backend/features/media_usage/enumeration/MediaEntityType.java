package dev.roland.inventory_management_backend.features.media_usage.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MediaEntityType {
  /** Represents the company value. */
  COMPANY("company"),
  /** Represents the user value. */
  USER("user"),
  /** Represents the product value. */
  PRODUCT("product"),
  /** Represents the warehouse value. */
  WAREHOUSE("warehouse"),
  ;

  private final String name;
}
