package dev.roland.inventory_management_backend.features.document.exception;

import dev.roland.inventory_management_backend.features.document.dto.DocumentApiDtos.DocumentResponse;
import dev.roland.inventory_management_backend.features.document.message.DocumentApiMessageKey;
import lombok.Getter;

/** Raised when a draft update uses a stale version. */
@Getter
public class DocumentConflictException extends RuntimeException {
  private final DocumentResponse latestDocument;

  public DocumentConflictException(final DocumentResponse latestDocument) {
    super(DocumentApiMessageKey.DOCUMENT_CONFLICT.getKey());
    this.latestDocument = latestDocument;
  }
}
