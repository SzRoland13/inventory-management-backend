package dev.roland.inventory_management_backend.features.document_file;

import dev.roland.inventory_management_backend.common.exception.ApiException;
import dev.roland.inventory_management_backend.common.message.MessageKey;
import lombok.Getter;

/** Keyed failure raised while validating or rendering a queued document PDF job. */
@Getter
public class DocumentPdfJobException extends ApiException {
  private final boolean permanent;

  /**
   * Represents the document pdf job failure.
   *
   * @param cause cause value
   * @param messageKey message key value
   * @param permanent permanent value
   */
  public DocumentPdfJobException(
      final MessageKey messageKey, final boolean permanent, final Throwable cause) {
    super(messageKey, cause);
    this.permanent = permanent;
  }

  /**
   * Represents the document pdf job failure.
   *
   * @param messageKey message key value
   * @param permanent permanent value
   */
  public DocumentPdfJobException(final MessageKey messageKey, final boolean permanent) {
    this(messageKey, permanent, null);
  }
}
