package dev.roland.inventory_management_backend.common.exception;

import dev.roland.inventory_management_backend.common.message.MessageKey;

/** Exception for email composition and mail factory configuration failures. */
public class MailException extends ApiException {

  /**
   * Creates a mail exception with a localized message key.
   *
   * @param messageKey key identifying the mail error
   */
  public MailException(final MessageKey messageKey) {
    super(messageKey);
  }
}
