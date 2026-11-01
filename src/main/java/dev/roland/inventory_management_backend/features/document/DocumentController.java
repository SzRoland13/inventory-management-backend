package dev.roland.inventory_management_backend.features.document;

import jakarta.validation.Valid;

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
import dev.roland.inventory_management_backend.features.document.dto.Capabilities;
import dev.roland.inventory_management_backend.features.document.dto.CloseRemaining;
import dev.roland.inventory_management_backend.features.document.dto.DocumentResponse;
import dev.roland.inventory_management_backend.features.document.dto.Draft;
import dev.roland.inventory_management_backend.features.document.dto.DraftUpdate;
import dev.roland.inventory_management_backend.features.document.dto.FulfillmentDraft;
import dev.roland.inventory_management_backend.features.document.dto.FulfillmentPreview;
import dev.roland.inventory_management_backend.features.document.dto.PageResponse;
import dev.roland.inventory_management_backend.features.document.dto.PreviewResponse;
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

  /**
   * Returns the document operations available to the current user.
   *
   * @return the requested result
   */
  @GetMapping(CAPABILITIES_ENDPOINT)
  public ResponseEntity<ApiResponse<Capabilities>> capabilities() {
    return success(facade.capabilities());
  }

  /**
   * Lists documents visible to the current user.
   *
   * @param page page value
   * @param pageSize page size value
   * @param search search value
   * @param view view value
   * @return the requested result
   */
  @GetMapping
  public ResponseEntity<ApiResponse<PageResponse>> list(
      @RequestParam(required = false) final String view,
      @RequestParam(required = false) final String search,
      @RequestParam(defaultValue = "0") final int page,
      @RequestParam(defaultValue = "20") final int pageSize) {
    return success(facade.list(view, search, page, pageSize));
  }

  /**
   * Returns the requested document.
   *
   * @param id id value
   * @return the requested result
   */
  @GetMapping(ID_PARAM)
  public ResponseEntity<ApiResponse<DocumentResponse>> get(@PathVariable final Long id) {
    return success(facade.get(id));
  }

  /**
   * Creates a document draft.
   *
   * @param draft draft value
   * @return the requested result
   */
  @PostMapping(DRAFTS_ENDPOINT)
  public ResponseEntity<ApiResponse<DocumentResponse>> createDraft(
      @Valid @RequestBody final Draft draft) {
    return success(facade.createDraft(draft));
  }

  /**
   * Updates a document draft.
   *
   * @param id id value
   * @param request request value
   * @return the requested result
   */
  @PutMapping(DRAFT_ENDPOINT)
  public ResponseEntity<ApiResponse<DocumentResponse>> updateDraft(
      @PathVariable final Long id, @Valid @RequestBody final DraftUpdate request) {
    return success(facade.updateDraft(id, request));
  }

  /**
   * Creates a fulfillment draft.
   *
   * @param request request value
   * @return the requested result
   */
  @PostMapping(FULFILLMENT_DRAFTS_ENDPOINT)
  public ResponseEntity<ApiResponse<DocumentResponse>> fulfillmentDraft(
      @RequestBody final FulfillmentDraft request) {
    return success(facade.createFulfillmentDraft(request));
  }

  /**
   * Previews the requested document operation.
   *
   * @param request request value
   * @return the requested result
   */
  @PostMapping(FULFILLMENT_PREVIEW_ENDPOINT)
  public ResponseEntity<ApiResponse<PreviewResponse>> preview(
      @RequestBody final FulfillmentPreview request) {
    return success(facade.preview(request));
  }

  /**
   * Posts the requested document.
   *
   * @param id id value
   * @param idempotencyKey idempotency key value
   * @param version version value
   * @return the requested result
   */
  @PostMapping(POST_ENDPOINT)
  public ResponseEntity<ApiResponse<DocumentResponse>> post(
      @PathVariable final Long id,
      @RequestHeader(value = "Idempotency-Key", required = false) final String idempotencyKey,
      @RequestHeader(value = "If-Match", required = false) final String version) {
    return success(facade.post(id, idempotencyKey, version));
  }

  /**
   * Confirms the requested document.
   *
   * @param id id value
   * @param version version value
   * @return the requested result
   */
  @PostMapping(CONFIRM_ENDPOINT)
  public ResponseEntity<ApiResponse<DocumentResponse>> confirm(
      @PathVariable final Long id,
      @RequestHeader(value = "If-Match", required = false) final String version) {
    return success(facade.confirm(id, version));
  }

  /**
   * Closes the remaining quantity on a document.
   *
   * @param id id value
   * @param request request value
   * @param version version value
   * @return the requested result
   */
  @PostMapping(CLOSE_REMAINING_ENDPOINT)
  public ResponseEntity<ApiResponse<DocumentResponse>> closeRemaining(
      @PathVariable final Long id,
      @RequestBody final CloseRemaining request,
      @RequestHeader(value = "If-Match", required = false) final String version) {
    return success(facade.closeRemaining(id, request, version));
  }

  /**
   * Uploads a file for the requested document.
   *
   * @param file file value
   * @param id id value
   * @return the requested result
   */
  @PostMapping(FILES_ENDPOINT)
  public ResponseEntity<ApiResponse<DocumentResponse>> uploadFile(
      @PathVariable final Long id, @RequestPart("file") final MultipartFile file) {
    return success(facade.uploadFile(id, file));
  }

  /**
   * Queues PDF generation for the requested document.
   *
   * @param id id value
   * @param key key value
   * @param language language value
   * @return the requested result
   */
  @PostMapping(PDF_ENDPOINT)
  public ResponseEntity<ApiResponse<DocumentResponse>> generatePdf(
      @PathVariable final Long id,
      @RequestHeader(value = "Idempotency-Key", required = false) final String key,
      @RequestParam(defaultValue = "en") final String language) {
    return success(facade.generatePdf(id, key, language));
  }

  /**
   * Retries PDF generation for the requested document.
   *
   * @param id id value
   * @param key key value
   * @return the requested result
   */
  @PostMapping(PDF_RETRY_ENDPOINT)
  public ResponseEntity<ApiResponse<DocumentResponse>> retryPdf(
      @PathVariable final Long id,
      @RequestHeader(value = "Idempotency-Key", required = false) final String key) {
    return success(facade.retryPdf(id, key));
  }

  private <T> ResponseEntity<ApiResponse<T>> success(final T payload) {
    return ResponseEntity.ok(ApiResponse.success(GenericMessageKey.REQUEST_SUCCESS, payload));
  }
}
