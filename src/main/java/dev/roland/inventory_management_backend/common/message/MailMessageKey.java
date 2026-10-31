package dev.roland.inventory_management_backend.common.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Localization keys for email composition failures. */
@Getter
@RequiredArgsConstructor
public enum MailMessageKey implements MessageKey {
  INVALID_MAILABLE("mail.invalid-mailable"),
  FACTORY_CONFIGURATION_INVALID("mail.factory-configuration-invalid"),
  FACTORY_NOT_FOUND("mail.factory-not-found");

  private final String key;
}
