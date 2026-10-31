package dev.roland.inventory_management_backend.common.async;

import dev.roland.inventory_management_backend.common.exception.ApiException;
import dev.roland.inventory_management_backend.common.message.AsyncJobMessageKey;
import lombok.Getter;

/** Keyed exception for asynchronous job failures, including retry classification. */
@Getter
public class AsyncJobException extends ApiException {

  private final boolean permanent;

  /**
   * Creates a classified asynchronous job failure.
   *
   * @param messageKey localized failure key
   * @param permanent whether retrying cannot succeed
   */
  public AsyncJobException(final AsyncJobMessageKey messageKey, final boolean permanent) {
    super(messageKey);
    this.permanent = permanent;
  }

  /**
   * Creates a classified asynchronous job failure with its cause.
   *
   * @param messageKey localized failure key
   * @param permanent whether retrying cannot succeed
   * @param cause underlying failure
   */
  public AsyncJobException(
      final AsyncJobMessageKey messageKey, final boolean permanent, final Throwable cause) {
    super(messageKey, cause);
    this.permanent = permanent;
  }
}
