package dev.roland.inventory_management_backend.features.document.message;

import dev.roland.inventory_management_backend.common.message.MessageKey;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Localization keys for document workflow API operations. */
@Getter
@RequiredArgsConstructor
public enum DocumentApiMessageKey implements MessageKey {
  DOCUMENT_SAVED("document.success.saved"),
  DOCUMENT_CREATED("document.success.created"),
  DOCUMENT_POSTED("document.success.posted"),
  DOCUMENT_CONFIRMED("document.success.confirmed"),
  DOCUMENT_FILE_UPLOADED("document.success.file_uploaded"),
  PDF_QUEUED("document.success.pdf_queued"),
  DOCUMENT_CONFLICT("document.error.conflict"),
  DOCUMENT_VERSION_REQUIRED("document.error.version_required"),
  DOCUMENT_INVALID_TRANSITION("document.error.invalid_transition"),
  DOCUMENT_INVALID_LINES("document.error.invalid_lines"),
  DOCUMENT_INSUFFICIENT_STOCK("document.error.insufficient_stock"),
  DOCUMENT_CROSS_COMPANY_REFERENCE("document.error.cross_company_reference"),
  DOCUMENT_IDEMPOTENCY_KEY_REQUIRED("document.error.idempotency_key_required"),
  DOCUMENT_REASON_REQUIRED("document.error.reason_required"),
  DOCUMENT_DIRECT_FULFILLMENT_NOT_ALLOWED("document.error.direct_fulfillment_not_allowed"),
  DOCUMENT_INVALID_PDF_LANGUAGE("document.error.pdf_language_invalid"),
  DOCUMENT_WAREHOUSE_REQUIRED("document.error.warehouse_required"),
  DOCUMENT_SEQUENCE_UNAVAILABLE("document.error.sequence_unavailable"),
  DOCUMENT_PDF_JOB_NOT_FOUND("document.error.pdf_job_not_found"),
  DOCUMENT_PDF_JOB_INVALID("document.error.pdf_job_invalid"),
  DOCUMENT_PDF_PAYLOAD_INVALID("document.error.pdf_payload_invalid"),
  DOCUMENT_PDF_RENDER_FAILED("document.error.pdf_render_failed"),
  DOCUMENT_PDF_EMPTY_RESULT("document.error.pdf_empty_result"),
  DOCUMENT_INVALID_QUEUE_VIEW("document.error.invalid_queue_view");

  private final String key;
}
