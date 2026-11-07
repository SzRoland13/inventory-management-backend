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
import dev.roland.inventory_management_backend.features.document.dto.FulfillmentPost;
import dev.roland.inventory_management_backend.features.document.dto.FulfillmentPreview;
import dev.roland.inventory_management_backend.features.document.dto.PageResponse;
import dev.roland.inventory_management_backend.features.document.dto.PreviewResponse;
import dev.roland.inventory_management_backend.features.document.dto.WarehouseOperationPreview;
import dev.roland.inventory_management_backend.features.document.dto.WarehouseOperationRequest;
import dev.roland.inventory_management_backend.features.document.enumeration.WarehouseOperationType;
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
  public static final String TRANSFER_DRAFTS = "/transfers/drafts";
  public static final String TRANSFER_PREVIEW = "/transfers/preview";
  public static final String TRANSFER_RESOLUTION_PREVIEW = "/transfers/{id}/resolution-preview";
  public static final String TRANSFER_DISPATCH = "/transfers/{id}/dispatch";
  public static final String TRANSFER_RECEIPT = "/transfers/{id}/receipt";
  public static final String TRANSFER_RESOLVE = "/transfers/{id}/resolve";
  public static final String RETURNS = "/returns";
  public static final String RETURN_PREVIEW = "/returns/preview";
  public static final String QUARANTINE_RELEASE_PREVIEW = ID_PARAM + "/quarantine-release/preview";
  public static final String QUARANTINE_RELEASE = ID_PARAM + "/quarantine-release";
  public static final String REPLACEMENT_DELIVERY_PREVIEW =
      "/returns/replacement-deliveries/preview";
  public static final String REPLACEMENT_DELIVERY = "/returns/replacement-deliveries";
  public static final String STOCK_COUNTS = "/stock-counts";
  public static final String STOCK_COUNT_RECONCILIATION_PREVIEW =
      "/stock-counts/{id}/reconciliation-preview";
  public static final String STOCK_COUNT_POST = "/stock-counts/{id}/post";
  public static final String CORRECTION_PREVIEW = ID_PARAM + "/corrections/preview";
  public static final String CORRECTION = ID_PARAM + "/corrections";
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
   * @param request optional movement-time override
   * @return the requested result
   */
  @PostMapping(POST_ENDPOINT)
  public ResponseEntity<ApiResponse<DocumentResponse>> post(
      @PathVariable final Long id,
      @RequestBody(required = false) final FulfillmentPost request,
      @RequestHeader(value = "Idempotency-Key", required = false) final String idempotencyKey,
      @RequestHeader(value = "If-Match", required = false) final String version) {
    return success(
        facade.post(id, idempotencyKey, version, request == null ? null : request.effectiveAt()));
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

  /**
   * Creates a transfer draft from the supplied warehouse operation.
   *
   * @param request the request value
   * @return the resulting value
   */
  @PostMapping(TRANSFER_DRAFTS)
  public ResponseEntity<ApiResponse<DocumentResponse>> createTransferDraft(
      @RequestBody final WarehouseOperationRequest request) {
    return success(
        facade.applyWarehouseOperation(
            null, WarehouseOperationType.TRANSFER_DRAFT, request, null, null));
  }

  /**
   * Previews a new warehouse transfer.
   *
   * @param request the request value
   * @return the resulting value
   */
  @PostMapping(TRANSFER_PREVIEW)
  public ResponseEntity<ApiResponse<WarehouseOperationPreview>> previewTransfer(
      @RequestBody final WarehouseOperationRequest request) {
    return success(
        facade.previewWarehouseOperation(null, WarehouseOperationType.TRANSFER_PREVIEW, request));
  }

  /**
   * Previews resolution of an existing transfer.
   *
   * @param id transfer document identifier
   * @param request transfer resolution details
   * @return the transfer resolution preview
   */
  @PostMapping(TRANSFER_RESOLUTION_PREVIEW)
  public ResponseEntity<ApiResponse<WarehouseOperationPreview>> previewTransferResolution(
      @PathVariable final Long id, @RequestBody final WarehouseOperationRequest request) {
    return success(
        facade.previewWarehouseOperation(id, WarehouseOperationType.TRANSFER_RESOLUTION, request));
  }

  /**
   * Dispatches an existing transfer to its transit warehouse.
   *
   * @param id transfer document identifier
   * @param key idempotency key
   * @param request dispatch details
   * @param version expected document version
   * @return the dispatched transfer response
   */
  @PostMapping(TRANSFER_DISPATCH)
  public ResponseEntity<ApiResponse<DocumentResponse>> dispatchTransfer(
      @PathVariable final Long id,
      @RequestBody final WarehouseOperationRequest request,
      @RequestHeader(value = "Idempotency-Key", required = false) final String key,
      @RequestHeader(value = "If-Match", required = false) final String version) {
    return success(
        facade.applyWarehouseOperation(
            id, WarehouseOperationType.TRANSFER_DISPATCH, request, key, version));
  }

  /**
   * Receives goods from a transfer into its destination warehouse.
   *
   * @param id transfer document identifier
   * @param key idempotency key
   * @param request receipt details
   * @param version expected document version
   * @return the received transfer response
   */
  @PostMapping(TRANSFER_RECEIPT)
  public ResponseEntity<ApiResponse<DocumentResponse>> receiveTransfer(
      @PathVariable final Long id,
      @RequestBody final WarehouseOperationRequest request,
      @RequestHeader(value = "Idempotency-Key", required = false) final String key,
      @RequestHeader(value = "If-Match", required = false) final String version) {
    return success(
        facade.applyWarehouseOperation(
            id, WarehouseOperationType.TRANSFER_RECEIPT, request, key, version));
  }

  /**
   * Resolves a transfer that has goods in transit.
   *
   * @param id transfer document identifier
   * @param key idempotency key
   * @param request resolution details
   * @param version expected document version
   * @return the resolved transfer response
   */
  @PostMapping(TRANSFER_RESOLVE)
  public ResponseEntity<ApiResponse<DocumentResponse>> resolveTransfer(
      @PathVariable final Long id,
      @RequestBody final WarehouseOperationRequest request,
      @RequestHeader(value = "Idempotency-Key", required = false) final String key,
      @RequestHeader(value = "If-Match", required = false) final String version) {
    return success(
        facade.applyWarehouseOperation(
            id, WarehouseOperationType.TRANSFER_RESOLVE, request, key, version));
  }

  /**
   * Creates a return document from the supplied request.
   *
   * @param key idempotency key
   * @param request return details
   * @return the created return response
   */
  @PostMapping(RETURNS)
  public ResponseEntity<ApiResponse<DocumentResponse>> createReturn(
      @RequestBody final WarehouseOperationRequest request,
      @RequestHeader(value = "Idempotency-Key", required = false) final String key) {
    return success(
        facade.applyWarehouseOperation(
            null, WarehouseOperationType.RETURN_CREATE, request, key, null));
  }

  /**
   * Previews a return operation.
   *
   * @param request the request value
   * @return the resulting value
   */
  @PostMapping(RETURN_PREVIEW)
  public ResponseEntity<ApiResponse<WarehouseOperationPreview>> previewReturn(
      @RequestBody final WarehouseOperationRequest request) {
    return success(
        facade.previewWarehouseOperation(null, WarehouseOperationType.RETURN_CREATE, request));
  }

  /**
   * Previews release of eligible quarantined goods.
   *
   * @param id the id value
   * @param request the request value
   * @return the resulting value
   */
  @PostMapping(QUARANTINE_RELEASE_PREVIEW)
  public ResponseEntity<ApiResponse<WarehouseOperationPreview>> previewQuarantineRelease(
      @PathVariable final Long id, @RequestBody final WarehouseOperationRequest request) {
    return success(
        facade.previewWarehouseOperation(id, WarehouseOperationType.QUARANTINE_RELEASE, request));
  }

  /**
   * Releases eligible returned goods from quarantine.
   *
   * @param id the id value
   * @param key the key value
   * @param request the request value
   * @param version the version value
   * @return the resulting value
   */
  @PostMapping(QUARANTINE_RELEASE)
  public ResponseEntity<ApiResponse<DocumentResponse>> releaseQuarantine(
      @PathVariable final Long id,
      @RequestBody final WarehouseOperationRequest request,
      @RequestHeader(value = "Idempotency-Key", required = false) final String key,
      @RequestHeader(value = "If-Match", required = false) final String version) {
    return success(
        facade.applyWarehouseOperation(
            id, WarehouseOperationType.QUARANTINE_RELEASE, request, key, version));
  }

  /**
   * Previews a replacement delivery.
   *
   * @param request the request value
   * @return the resulting value
   */
  @PostMapping(REPLACEMENT_DELIVERY_PREVIEW)
  public ResponseEntity<ApiResponse<WarehouseOperationPreview>> previewReplacement(
      @RequestBody final WarehouseOperationRequest request) {
    return success(
        facade.previewWarehouseOperation(
            null, WarehouseOperationType.REPLACEMENT_DELIVERY, request));
  }

  /**
   * Creates a replacement delivery.
   *
   * @param key the key value
   * @param request the request value
   * @return the resulting value
   */
  @PostMapping(REPLACEMENT_DELIVERY)
  public ResponseEntity<ApiResponse<DocumentResponse>> createReplacement(
      @RequestBody final WarehouseOperationRequest request,
      @RequestHeader(value = "Idempotency-Key", required = false) final String key) {
    return success(
        facade.applyWarehouseOperation(
            null, WarehouseOperationType.REPLACEMENT_DELIVERY, request, key, null));
  }

  /**
   * Creates a stock-count document.
   *
   * @param request the request value
   * @return the resulting value
   */
  @PostMapping(STOCK_COUNTS)
  public ResponseEntity<ApiResponse<DocumentResponse>> createStockCount(
      @RequestBody final WarehouseOperationRequest request) {
    return success(
        facade.applyWarehouseOperation(
            null, WarehouseOperationType.STOCK_COUNT_CREATE, request, null, null));
  }

  /**
   * Previews reconciliation of a stock count.
   *
   * @param id the id value
   * @param request the request value
   * @return the resulting value
   */
  @PostMapping(STOCK_COUNT_RECONCILIATION_PREVIEW)
  public ResponseEntity<ApiResponse<WarehouseOperationPreview>> previewStockCount(
      @PathVariable final Long id, @RequestBody final WarehouseOperationRequest request) {
    return success(
        facade.previewWarehouseOperation(
            id, WarehouseOperationType.STOCK_COUNT_RECONCILE, request));
  }

  /**
   * Posts a stock-count reconciliation.
   *
   * @param id the id value
   * @param key the key value
   * @param request the request value
   * @param version the version value
   * @return the resulting value
   */
  @PostMapping(STOCK_COUNT_POST)
  public ResponseEntity<ApiResponse<DocumentResponse>> postStockCount(
      @PathVariable final Long id,
      @RequestBody final WarehouseOperationRequest request,
      @RequestHeader(value = "Idempotency-Key", required = false) final String key,
      @RequestHeader(value = "If-Match", required = false) final String version) {
    return success(
        facade.applyWarehouseOperation(
            id, WarehouseOperationType.STOCK_COUNT_POST, request, key, version));
  }

  /**
   * Previews a correction to a completed document.
   *
   * @param id the id value
   * @param request the request value
   * @return the resulting value
   */
  @PostMapping(CORRECTION_PREVIEW)
  public ResponseEntity<ApiResponse<WarehouseOperationPreview>> previewCorrection(
      @PathVariable final Long id, @RequestBody final WarehouseOperationRequest request) {
    return success(
        facade.previewWarehouseOperation(id, WarehouseOperationType.CORRECTION, request));
  }

  /**
   * Creates a correction for a completed document.
   *
   * @param id the id value
   * @param key the key value
   * @param request the request value
   * @param version the version value
   * @return the resulting value
   */
  @PostMapping(CORRECTION)
  public ResponseEntity<ApiResponse<DocumentResponse>> correctDocument(
      @PathVariable final Long id,
      @RequestBody final WarehouseOperationRequest request,
      @RequestHeader(value = "Idempotency-Key", required = false) final String key,
      @RequestHeader(value = "If-Match", required = false) final String version) {
    return success(
        facade.applyWarehouseOperation(
            id, WarehouseOperationType.CORRECTION, request, key, version));
  }

  private <T> ResponseEntity<ApiResponse<T>> success(final T payload) {
    return ResponseEntity.ok(ApiResponse.success(GenericMessageKey.REQUEST_SUCCESS, payload));
  }
}
