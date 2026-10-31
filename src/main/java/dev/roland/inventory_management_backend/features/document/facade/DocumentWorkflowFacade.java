package dev.roland.inventory_management_backend.features.document.facade;

import org.springframework.web.multipart.MultipartFile;

import dev.roland.inventory_management_backend.features.document.dto.DocumentApiDtos.*;

public interface DocumentWorkflowFacade {
  Capabilities capabilities();

  PageResponse list(String view, String search, int page, int pageSize);

  DocumentResponse get(Long id);

  DocumentResponse createDraft(Draft draft);

  DocumentResponse updateDraft(Long id, DraftUpdate update);

  DocumentResponse createFulfillmentDraft(FulfillmentDraft request);

  PreviewResponse preview(FulfillmentPreview request);

  DocumentResponse post(Long id, String idempotencyKey, String expectedVersion);

  DocumentResponse confirm(Long id, String expectedVersion);

  DocumentResponse closeRemaining(Long id, CloseRemaining request, String expectedVersion);

  DocumentResponse uploadFile(Long id, MultipartFile file);

  DocumentResponse generatePdf(Long id, String idempotencyKey, String language);

  DocumentResponse retryPdf(Long id, String idempotencyKey);
}
