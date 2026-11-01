package dev.roland.inventory_management_backend.features.document.exception;

import dev.roland.inventory_management_backend.common.exception.ApiException;
import dev.roland.inventory_management_backend.features.document.dto.DocumentResponse;
import dev.roland.inventory_management_backend.features.document.message.DocumentApiMessageKey;
import lombok.Getter;

/** Raised when a draft update uses a stale version. */
@Getter
public class DocumentConflictException extends ApiException {
  private final DocumentResponse latestDocument;

  /**
   * Represents the document conflict failure.
   *
   * @param latestDocument latest document value
   */
  public DocumentConflictException(final DocumentResponse latestDocument) {
    super(DocumentApiMessageKey.DOCUMENT_CONFLICT);
    this.latestDocument = latestDocument;
  }
}
