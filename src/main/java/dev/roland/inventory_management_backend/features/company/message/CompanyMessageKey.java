package dev.roland.inventory_management_backend.features.company.message;

import dev.roland.inventory_management_backend.common.message.MessageKey;
import lombok.Getter;

/** Message keys for company-related operations. */
@Getter
public enum CompanyMessageKey implements MessageKey {
  /** Message key for logo updated. */
  LOGO_UPDATED("company.logo-updated"),
  /** Message key for currency updated. */
  CURRENCY_UPDATED("company.currency-updated"),
  ;

  private final String key;

  CompanyMessageKey(String key) {
    this.key = key;
  }
}
