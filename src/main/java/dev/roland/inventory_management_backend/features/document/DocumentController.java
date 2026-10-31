package dev.roland.inventory_management_backend.features.document;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import dev.roland.inventory_management_backend.common.dto.ApiResponse;
import dev.roland.inventory_management_backend.common.message.GenericMessageKey;
import dev.roland.inventory_management_backend.features.document.dto.DocumentApiDtos.*;
import dev.roland.inventory_management_backend.features.document.facade.DocumentWorkflowFacade;
import lombok.RequiredArgsConstructor;

/** Exposes company-scoped document workflows to the application clients. */
@RestController
@RequestMapping(DocumentController.DOCUMENT_BASE_ENDPOINT)
@RequiredArgsConstructor
public class DocumentController {
  public static final String DOCUMENT_BASE_ENDPOINT = "api/v1/documents";
  public static final String CAPABILITIES_ENDPOINT = "/capabilities";
  public static final String ID_PARAM = "/{id}";
  public static final String DRAFTS_ENDPOINT = "/drafts";
  public static final String DRAFT_ENDPOINT = ID_PARAM + "/draft";
  public static final String FULFILLMENT_DRAFTS_ENDPOINT = "/fulfillments/drafts";
  public static final String FULFILLMENT_PREVIEW_ENDPOINT = "/fulfillments/preview";
  public static final String POST_ENDPOINT = ID_PARAM + "/post";
  public static final String CONFIRM_ENDPOINT = ID_PARAM + "/confirm";
  public static final String CLOSE_REMAINING_ENDPOINT = ID_PARAM + "/close-remaining";
  public static final String FILES_ENDPOINT = ID_PARAM + "/files";
  public static final String PDF_ENDPOINT = ID_PARAM + "/pdf";
  public static final String PDF_RETRY_ENDPOINT = PDF_ENDPOINT + "/retry";

  private final DocumentWorkflowFacade facade;

  @GetMapping(CAPABILITIES_ENDPOINT)
  public ResponseEntity<ApiResponse<Capabilities>> capabilities() {
    return success(facade.capabilities());
  }

  @GetMapping
  public ResponseEntity<ApiResponse<PageResponse>> list(
      @RequestParam(required = false) String view,
      @RequestParam(required = false) String search,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int pageSize) {
    return success(facade.list(view, search, page, pageSize));
  }

  @GetMapping(ID_PARAM)
  public ResponseEntity<ApiResponse<DocumentResponse>> get(@PathVariable Long id) {
    return success(facade.get(id));
  }

  @PostMapping(DRAFTS_ENDPOINT)
  public ResponseEntity<ApiResponse<DocumentResponse>> createDraft(@RequestBody Draft draft) {
    return success(facade.createDraft(draft));
  }

  @PutMapping(DRAFT_ENDPOINT)
  public ResponseEntity<ApiResponse<DocumentResponse>> updateDraft(
      @PathVariable Long id, @RequestBody DraftUpdate request) {
    return success(facade.updateDraft(id, request));
  }

  @PostMapping(FULFILLMENT_DRAFTS_ENDPOINT)
  public ResponseEntity<ApiResponse<DocumentResponse>> fulfillmentDraft(
      @RequestBody FulfillmentDraft request) {
    return success(facade.createFulfillmentDraft(request));
  }

  @PostMapping(FULFILLMENT_PREVIEW_ENDPOINT)
  public ResponseEntity<ApiResponse<PreviewResponse>> preview(
      @RequestBody FulfillmentPreview request) {
    return success(facade.preview(request));
  }

  @PostMapping(POST_ENDPOINT)
  public ResponseEntity<ApiResponse<DocumentResponse>> post(
      @PathVariable Long id,
      @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
      @RequestHeader(value = "If-Match", required = false) String version) {
    return success(facade.post(id, idempotencyKey, version));
  }

  @PostMapping(CONFIRM_ENDPOINT)
  public ResponseEntity<ApiResponse<DocumentResponse>> confirm(
      @PathVariable Long id, @RequestHeader(value = "If-Match", required = false) String version) {
    return success(facade.confirm(id, version));
  }

  @PostMapping(CLOSE_REMAINING_ENDPOINT)
  public ResponseEntity<ApiResponse<DocumentResponse>> closeRemaining(
      @PathVariable Long id,
      @RequestBody CloseRemaining request,
      @RequestHeader(value = "If-Match", required = false) String version) {
    return success(facade.closeRemaining(id, request, version));
  }

  @PostMapping(FILES_ENDPOINT)
  public ResponseEntity<ApiResponse<DocumentResponse>> uploadFile(
      @PathVariable Long id, @RequestPart("file") MultipartFile file) {
    return success(facade.uploadFile(id, file));
  }

  @PostMapping(PDF_ENDPOINT)
  public ResponseEntity<ApiResponse<DocumentResponse>> generatePdf(
      @PathVariable Long id,
      @RequestHeader(value = "Idempotency-Key", required = false) String key,
      @RequestParam(defaultValue = "en") String language) {
    return success(facade.generatePdf(id, key, language));
  }

  @PostMapping(PDF_RETRY_ENDPOINT)
  public ResponseEntity<ApiResponse<DocumentResponse>> retryPdf(
      @PathVariable Long id,
      @RequestHeader(value = "Idempotency-Key", required = false) String key) {
    return success(facade.retryPdf(id, key));
  }

  private <T> ResponseEntity<ApiResponse<T>> success(final T payload) {
    return ResponseEntity.ok(ApiResponse.success(GenericMessageKey.REQUEST_SUCCESS, payload));
  }
}
