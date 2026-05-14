package dev.roland.inventory_management_backend.message_key;

import lombok.Getter;

@Getter
public enum CompanyMessageKey implements MessageKey {
  LOGO_UPDATED("company.logo-updated"),
  CURRENCY_UPDATED("company.currency-updated"),
  ;

  private final String key;

  CompanyMessageKey(String key) {
    this.key = key;
  }
}
