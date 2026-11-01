package dev.roland.inventory_management_backend.common.exception;

import dev.roland.inventory_management_backend.common.message.MessageKey;

/** Keyed failure raised by object storage operations. */
public class ObjectStorageException extends ApiException {

  public ObjectStorageException(final MessageKey messageKey, final Throwable cause) {
    super(messageKey, cause);
  }
}
