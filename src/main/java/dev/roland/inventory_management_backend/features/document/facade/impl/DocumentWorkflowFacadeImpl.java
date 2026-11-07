package dev.roland.inventory_management_backend.features.document.facade.impl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import dev.roland.inventory_management_backend.common.exception.ApiException;
import dev.roland.inventory_management_backend.common.service.CurrentCompanyProvider;
import dev.roland.inventory_management_backend.features.company.Company;
import dev.roland.inventory_management_backend.features.document.Document;
import dev.roland.inventory_management_backend.features.document.dto.Capabilities;
import dev.roland.inventory_management_backend.features.document.dto.CloseRemaining;
import dev.roland.inventory_management_backend.features.document.dto.CommercialDecision;
import dev.roland.inventory_management_backend.features.document.dto.CommercialDraft;
import dev.roland.inventory_management_backend.features.document.dto.CommercialDraftUpdate;
import dev.roland.inventory_management_backend.features.document.dto.DocumentResponse;
import dev.roland.inventory_management_backend.features.document.dto.Draft;
import dev.roland.inventory_management_backend.features.document.dto.DraftUpdate;
import dev.roland.inventory_management_backend.features.document.dto.FulfillmentDraft;
import dev.roland.inventory_management_backend.features.document.dto.FulfillmentPreview;
import dev.roland.inventory_management_backend.features.document.dto.PageResponse;
import dev.roland.inventory_management_backend.features.document.dto.PreviewResponse;
import dev.roland.inventory_management_backend.features.document.dto.SettlementAssignment;
import dev.roland.inventory_management_backend.features.document.dto.SettlementOwner;
import dev.roland.inventory_management_backend.features.document.dto.WarehouseOperationPreview;
import dev.roland.inventory_management_backend.features.document.dto.WarehouseOperationRequest;
import dev.roland.inventory_management_backend.features.document.enumeration.CommercialApprovalStatus;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentQueueView;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentStatus;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentType;
import dev.roland.inventory_management_backend.features.document.enumeration.WarehouseOperationType;
import dev.roland.inventory_management_backend.features.document.facade.DocumentWorkflowFacade;
import dev.roland.inventory_management_backend.features.document.facade.workflow.CommercialDocumentWorkflowService;
import dev.roland.inventory_management_backend.features.document.facade.workflow.DocumentDraftWorkflowService;
import dev.roland.inventory_management_backend.features.document.facade.workflow.DocumentFileWorkflowService;
import dev.roland.inventory_management_backend.features.document.facade.workflow.DocumentFulfillmentWorkflowService;
import dev.roland.inventory_management_backend.features.document.facade.workflow.DocumentWorkflowAccessService;
import dev.roland.inventory_management_backend.features.document.facade.workflow.WarehouseOperationCommandService;
import dev.roland.inventory_management_backend.features.document.facade.workflow.WarehouseOperationPreviewService;
import dev.roland.inventory_management_backend.features.document.mapper.DocumentResponseMapper;
import dev.roland.inventory_management_backend.features.document.message.DocumentApiMessageKey;
import dev.roland.inventory_management_backend.features.document.service.DocumentService;
import dev.roland.inventory_management_backend.features.document.utils.DocumentWorkflowValues;
import dev.roland.inventory_management_backend.features.user.User;
import dev.roland.inventory_management_backend.features.user.enumeration.UserRole;
import lombok.RequiredArgsConstructor;

/** Coordinates company-scoped document workflows. */
@Service
@RequiredArgsConstructor
public class DocumentWorkflowFacadeImpl implements DocumentWorkflowFacade {
  static final List<DocumentType> CREATABLE_TYPES =
      List.of(
          DocumentType.PURCHASE_ORDER,
          DocumentType.SALES_ORDER,
          DocumentType.GOODS_RECEIPT,
          DocumentType.DELIVERY_NOTE,
          DocumentType.TRANSFER_REQUEST,
          DocumentType.STOCK_COUNT,
          DocumentType.RETURN,
          DocumentType.SUPPLIER_BILL,
          DocumentType.CUSTOMER_INVOICE,
          DocumentType.FINANCIAL_CREDIT);
  static final List<DocumentQueueView> VIEWS = List.of(DocumentQueueView.values());
  private final DocumentService documentService;
  private final CurrentCompanyProvider companyProvider;
  private final DocumentWorkflowAccessService accessService;
  private final DocumentResponseMapper documentResponseMapper;
  private final WarehouseOperationCommandService warehouseOperationCommandService;
  private final WarehouseOperationPreviewService warehouseOperationPreviewService;
  private final DocumentFulfillmentWorkflowService fulfillmentWorkflowService;
  private final DocumentFileWorkflowService fileWorkflowService;
  private final DocumentDraftWorkflowService draftWorkflowService;
  private final CommercialDocumentWorkflowService commercialWorkflowService;

  @Override
  public Capabilities capabilities() {
    final User user = accessService.currentActor();
    return new Capabilities(
        CREATABLE_TYPES.stream().filter(type -> accessService.canCreateType(type, user)).toList(),
        VIEWS.stream().filter(view -> canView(view, user)).toList());
  }

  @Transactional
  @Override
  public PageResponse list(
      final String view, final String search, final int page, final int pageSize) {
    if (page < 0 || pageSize < 1 || pageSize > 100) {
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_LINES);
    }
    final DocumentQueueView selectedView = queueView(view);
    final Company company = companyProvider.getCurrentCompany();
    final User actor = accessService.currentActor();
    final List<Document> all =
        documentService.findAllByCompanyIdOrderByUpdatedAtDesc(company.getId());
    final Map<String, Long> counts = new HashMap<>();
    VIEWS.forEach(
        queueView -> {
          if (canView(queueView, actor)) {
            counts.put(
                queueView.name(),
                all.stream().filter(d -> matchesView(d, queueView, actor)).count());
          }
        });
    final String normalizedSearch = search == null ? "" : search.trim().toLowerCase();
    final List<Document> selected =
        all.stream()
            .filter(d -> matchesView(d, selectedView, actor))
            .filter(
                d ->
                    normalizedSearch.isEmpty()
                        || DocumentWorkflowValues.number(d).toLowerCase().contains(normalizedSearch)
                        || (d.getPartnerNameSnapshot() != null
                            && d.getPartnerNameSnapshot().toLowerCase().contains(normalizedSearch)))
            .toList();
    final int from = Math.min(page * pageSize, selected.size());
    final List<DocumentResponse> items =
        selected.subList(from, Math.min(from + pageSize, selected.size())).stream()
            .map(documentResponseMapper::toResponse)
            .toList();
    return new PageResponse(items, selected.size(), page, pageSize, counts);
  }

  @Transactional
  @Override
  public DocumentResponse get(final Long id) {
    return documentResponseMapper.toResponse(accessService.find(id));
  }

  @Transactional
  @Override
  public DocumentResponse createDraft(final Draft draft) {
    return draftWorkflowService.createDraft(draft);
  }

  @Transactional
  @Override
  public DocumentResponse updateDraft(final Long id, final DraftUpdate update) {
    return draftWorkflowService.updateDraft(id, update);
  }

  @Transactional
  @Override
  public DocumentResponse createCommercialDraft(final CommercialDraft request) {
    return commercialWorkflowService.createDraft(request);
  }

  @Transactional
  @Override
  public DocumentResponse updateCommercialDraft(
      final Long id, final CommercialDraftUpdate request) {
    return commercialWorkflowService.updateDraft(id, request);
  }

  @Transactional
  @Override
  public DocumentResponse decideCommercial(
      final Long id, final CommercialDecision request, final boolean approve) {
    return commercialWorkflowService.decide(id, request, approve);
  }

  @Transactional
  @Override
  public DocumentResponse finalizeCommercial(final Long id, final String version) {
    return commercialWorkflowService.finalizeDocument(id, version);
  }

  @Transactional
  @Override
  public DocumentResponse assignSettlementOwner(final Long id, final SettlementAssignment request) {
    return commercialWorkflowService.assignSettlementOwner(id, request);
  }

  @Override
  public List<SettlementOwner> settlementOwners() {
    return commercialWorkflowService.settlementOwners();
  }

  @Transactional
  @Override
  public DocumentResponse createFulfillmentDraft(final FulfillmentDraft request) {
    return fulfillmentWorkflowService.createFulfillmentDraft(request);
  }

  @Transactional
  @Override
  public PreviewResponse preview(final FulfillmentPreview request) {
    return fulfillmentWorkflowService.preview(request);
  }

  @Transactional
  @Override
  public DocumentResponse post(
      final Long id, final String idempotencyKey, final String expectedVersion) {
    return fulfillmentWorkflowService.post(id, idempotencyKey, expectedVersion, null);
  }

  @Transactional
  @Override
  public DocumentResponse post(
      final Long id,
      final String idempotencyKey,
      final String expectedVersion,
      final LocalDateTime effectiveAt) {
    return fulfillmentWorkflowService.post(id, idempotencyKey, expectedVersion, effectiveAt);
  }

  @Transactional
  @Override
  public DocumentResponse confirm(final Long id, final String expectedVersion) {
    return fulfillmentWorkflowService.confirm(id, expectedVersion);
  }

  @Transactional
  @Override
  public DocumentResponse closeRemaining(
      final Long id, final CloseRemaining request, final String expectedVersion) {
    return fulfillmentWorkflowService.closeRemaining(id, request, expectedVersion);
  }

  @Transactional
  @Override
  public DocumentResponse uploadFile(final Long id, final MultipartFile file) {
    return fileWorkflowService.uploadFile(id, file);
  }

  @Transactional
  @Override
  public DocumentResponse generatePdf(
      final Long id, final String idempotencyKey, final String language) {
    return fileWorkflowService.generatePdf(id, idempotencyKey, language);
  }

  @Transactional
  @Override
  public DocumentResponse retryPdf(final Long id, final String idempotencyKey) {
    return fileWorkflowService.retryPdf(id, idempotencyKey);
  }

  @Transactional
  @Override
  public WarehouseOperationPreview previewWarehouseOperation(
      final Long id,
      final WarehouseOperationType operation,
      final WarehouseOperationRequest request) {
    return warehouseOperationPreviewService.preview(id, operation, request);
  }

  @Transactional
  @Override
  public DocumentResponse applyWarehouseOperation(
      final Long id,
      final WarehouseOperationType operation,
      final WarehouseOperationRequest request,
      final String suppliedKey,
      final String suppliedVersion) {
    return warehouseOperationCommandService.apply(
        id, operation, request, suppliedKey, suppliedVersion);
  }

  /**
   * Performs the queueView operation.
   *
   * @param view the view value
   * @return the resulting value
   * @throws ApiException when the operation cannot be completed
   */
  DocumentQueueView queueView(final String view) {
    final DocumentQueueView selected =
        view == null || view.isBlank()
            ? DocumentQueueView.ALL
            : VIEWS.stream()
                .filter(queueView -> queueView.name().equals(view))
                .findFirst()
                .orElseThrow(
                    () -> new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_QUEUE_VIEW));
    if (!canView(selected, accessService.currentActor())) {
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_QUEUE_VIEW);
    }
    return selected;
  }

  /**
   * Performs the matchesView operation.
   *
   * @param actor the actor value
   * @param d the d value
   * @param view the view value
   * @return the resulting value
   */
  boolean matchesView(final Document d, final DocumentQueueView view, final User actor) {
    return switch (view) {
      case MY_DRAFTS -> d.getStatus() == DocumentStatus.DRAFT
          && d.getCreatedByUser() != null
          && d.getCreatedByUser().getId().equals(actor.getId());
      case READY_TO_RECEIVE -> d.getType() == DocumentType.PURCHASE_ORDER
          && (d.getStatus() == DocumentStatus.CONFIRMED
              || d.getStatus() == DocumentStatus.PARTIALLY_COMPLETED)
          && accessService.hasRemaining(d);
      case READY_TO_SHIP -> d.getType() == DocumentType.SALES_ORDER
          && (d.getStatus() == DocumentStatus.CONFIRMED
              || d.getStatus() == DocumentStatus.PARTIALLY_COMPLETED)
          && accessService.hasRemaining(d);
      case PARTIALLY_FULFILLED -> d.getStatus() == DocumentStatus.PARTIALLY_COMPLETED;
      case ALL -> true;
      case TRANSFERS -> d.getType() == DocumentType.TRANSFER_REQUEST
          || d.getType() == DocumentType.TRANSFER_DELIVERY
          || d.getType() == DocumentType.TRANSFER_RECEIPT
          || d.getType() == DocumentType.WAREHOUSE_TRANSFER;
      case IN_TRANSIT -> DocumentWorkflowValues.safe(d.getLines()).stream()
          .anyMatch(l -> DocumentWorkflowValues.nz(l.getInTransitQuantity()).signum() > 0);
      case RETURNS -> d.getType() == DocumentType.RETURN;
      case STOCK_COUNTS -> d.getType() == DocumentType.STOCK_COUNT
          || d.getType() == DocumentType.STOCK_ADJUSTMENT;
      case CORRECTIONS -> d.getType() == DocumentType.CORRECTION;
      case SUPPLIER_BILLS -> d.getType() == DocumentType.SUPPLIER_BILL;
      case CUSTOMER_INVOICES -> d.getType() == DocumentType.CUSTOMER_INVOICE;
      case FINANCIAL_CREDITS -> d.getType() == DocumentType.FINANCIAL_CREDIT;
      case NEEDS_APPROVAL -> d.isApprovalRequired()
          && d.getApprovalStatus() == CommercialApprovalStatus.PENDING;
      case FINANCIAL_SETTLEMENT -> d.isFinancialSettlementRequired();
    };
  }

  /**
   * Performs the canView operation.
   *
   * @param user the user value
   * @param view the view value
   * @return the resulting value
   */
  boolean canView(final DocumentQueueView view, final User user) {
    return view == DocumentQueueView.ALL
        || view == DocumentQueueView.MY_DRAFTS
        || view == DocumentQueueView.READY_TO_RECEIVE
        || view == DocumentQueueView.READY_TO_SHIP
        || view == DocumentQueueView.PARTIALLY_FULFILLED
        || (user != null && user.getRole() != UserRole.SALES);
  }
}
