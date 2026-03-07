package dev.roland.inventory_management_backend.messageKey;

import lombok.Getter;

@Getter
public enum CompanyMessageKey implements MessageKey {
  LOGO_UPDATED("company.logo-updated"),
  ;

  private final String key;

  CompanyMessageKey(String key) {
    this.key = key;
  }
}
