package dev.roland.inventory_management_backend.features.document.facade;

import org.springframework.web.multipart.MultipartFile;

import dev.roland.inventory_management_backend.features.document.dto.Capabilities;
import dev.roland.inventory_management_backend.features.document.dto.CloseRemaining;
import dev.roland.inventory_management_backend.features.document.dto.DocumentResponse;
import dev.roland.inventory_management_backend.features.document.dto.Draft;
import dev.roland.inventory_management_backend.features.document.dto.DraftUpdate;
import dev.roland.inventory_management_backend.features.document.dto.FulfillmentDraft;
import dev.roland.inventory_management_backend.features.document.dto.FulfillmentPreview;
import dev.roland.inventory_management_backend.features.document.dto.PageResponse;
import dev.roland.inventory_management_backend.features.document.dto.PreviewResponse;
import dev.roland.inventory_management_backend.features.document.dto.WarehouseOperationPreview;
import dev.roland.inventory_management_backend.features.document.dto.WarehouseOperationRequest;
import dev.roland.inventory_management_backend.features.document.enumeration.WarehouseOperationType;

/** Coordinates document workflow operations. */
public interface DocumentWorkflowFacade {
  /**
   * Returns the document operations available to the current user.
   *
   * @return the requested result
   */
  Capabilities capabilities();

  /**
   * Lists documents visible to the current user.
   *
   * @param page page value
   * @param pageSize page size value
   * @param search search value
   * @param view view value
   * @return the requested result
   */
  PageResponse list(String view, String search, int page, int pageSize);

  /**
   * Returns the requested document.
   *
   * @param id id value
   * @return the requested result
   */
  DocumentResponse get(Long id);

  /**
   * Creates a document draft.
   *
   * @param draft draft value
   * @return the requested result
   */
  DocumentResponse createDraft(Draft draft);

  /**
   * Updates a document draft.
   *
   * @param id id value
   * @param update update value
   * @return the requested result
   */
  DocumentResponse updateDraft(Long id, DraftUpdate update);

  /**
   * Creates a fulfillment draft from a source document.
   *
   * @param request request value
   * @return the requested result
   */
  DocumentResponse createFulfillmentDraft(FulfillmentDraft request);

  /**
   * Previews the requested document operation.
   *
   * @param request request value
   * @return the requested result
   */
  PreviewResponse preview(FulfillmentPreview request);

  /**
   * Posts the requested document.
   *
   * @param expectedVersion expected version value
   * @param id id value
   * @param idempotencyKey idempotency key value
   * @return the requested result
   */
  DocumentResponse post(Long id, String idempotencyKey, String expectedVersion);

  /**
   * Confirms the requested document.
   *
   * @param expectedVersion expected version value
   * @param id id value
   * @return the requested result
   */
  DocumentResponse confirm(Long id, String expectedVersion);

  /**
   * Closes the remaining quantity on a document.
   *
   * @param expectedVersion expected version value
   * @param id id value
   * @param request request value
   * @return the requested result
   */
  DocumentResponse closeRemaining(Long id, CloseRemaining request, String expectedVersion);

  /**
   * Uploads a file for the requested document.
   *
   * @param file file value
   * @param id id value
   * @return the requested result
   */
  DocumentResponse uploadFile(Long id, MultipartFile file);

  /**
   * Queues PDF generation for the requested document.
   *
   * @param id id value
   * @param idempotencyKey idempotency key value
   * @param language language value
   * @return the requested result
   */
  DocumentResponse generatePdf(Long id, String idempotencyKey, String language);

  /**
   * Retries PDF generation for the requested document.
   *
   * @param id id value
   * @param idempotencyKey idempotency key value
   * @return the requested result
   */
  DocumentResponse retryPdf(Long id, String idempotencyKey);

  /**
   * Previews a warehouse operation for a document.
   *
   * @param id the id value
   * @param operation the operation value
   * @param request the request value
   * @return the resulting value
   */
  WarehouseOperationPreview previewWarehouseOperation(
      Long id, WarehouseOperationType operation, WarehouseOperationRequest request);

  /**
   * Applies a warehouse operation to a document.
   *
   * @param expectedVersion the expectedVersion value
   * @param id the id value
   * @param idempotencyKey the idempotencyKey value
   * @param operation the operation value
   * @param request the request value
   * @return the resulting value
   */
  DocumentResponse applyWarehouseOperation(
      Long id,
      WarehouseOperationType operation,
      WarehouseOperationRequest request,
      String idempotencyKey,
      String expectedVersion);
}
