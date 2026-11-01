package dev.roland.inventory_management_backend.common.exception;

import dev.roland.inventory_management_backend.common.message.MessageKey;

/** Keyed failure raised by object storage operations. */
public class ObjectStorageException extends ApiException {

  /**
   * Creates an object storage failure with its stable response key and underlying cause.
   *
   * @param messageKey localized message associated with the failure
   * @param cause underlying storage error
   */
  public ObjectStorageException(final MessageKey messageKey, final Throwable cause) {
    super(messageKey, cause);
  }
}
