package dev.roland.inventory_management_backend.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MediaEntityType {
  COMPANY("company"),
  USER("user"),
  PRODUCT("product"),
  WAREHOUSE("warehouse"),
  ;

  private final String name;
}
