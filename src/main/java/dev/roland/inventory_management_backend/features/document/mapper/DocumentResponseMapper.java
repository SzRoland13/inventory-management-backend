package dev.roland.inventory_management_backend.features.document.mapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.common.service.ObjectStorageService;
import dev.roland.inventory_management_backend.features.document.Document;
import dev.roland.inventory_management_backend.features.document.dto.Activity;
import dev.roland.inventory_management_backend.features.document.dto.DocumentResponse;
import dev.roland.inventory_management_backend.features.document.dto.DocumentTotals;
import dev.roland.inventory_management_backend.features.document.dto.ExchangeRateResponse;
import dev.roland.inventory_management_backend.features.document.dto.FileEntry;
import dev.roland.inventory_management_backend.features.document.dto.LineResponse;
import dev.roland.inventory_management_backend.features.document.dto.RelatedDocument;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentAction;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentStatus;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentType;
import dev.roland.inventory_management_backend.features.document.enumeration.ReturnDirection;
import dev.roland.inventory_management_backend.features.document.enumeration.ReturnDisposition;
import dev.roland.inventory_management_backend.features.document.facade.workflow.DocumentWorkflowAccessService;
import dev.roland.inventory_management_backend.features.document.utils.DocumentTotalsCalculator;
import dev.roland.inventory_management_backend.features.document.utils.DocumentWorkflowValues;
import dev.roland.inventory_management_backend.features.document_activity.service.DocumentActivityService;
import dev.roland.inventory_management_backend.features.document_file.DocumentFile;
import dev.roland.inventory_management_backend.features.document_file.DocumentFileSource;
import dev.roland.inventory_management_backend.features.document_file.DocumentFileStatus;
import dev.roland.inventory_management_backend.features.document_file.service.DocumentFileService;
import dev.roland.inventory_management_backend.features.document_line.DocumentLine;
import dev.roland.inventory_management_backend.features.document_line_allocation.DocumentLineAllocation;
import dev.roland.inventory_management_backend.features.document_line_allocation.service.DocumentLineAllocationService;
import dev.roland.inventory_management_backend.features.document_relation.service.DocumentRelationService;
import dev.roland.inventory_management_backend.features.media_asset.MediaAsset;
import dev.roland.inventory_management_backend.features.media_usage.MediaUsage;
import dev.roland.inventory_management_backend.features.media_usage.enumeration.MediaEntityType;
import dev.roland.inventory_management_backend.features.media_usage.enumeration.MediaUsageType;
import dev.roland.inventory_management_backend.features.media_usage.service.MediaUsageService;
import dev.roland.inventory_management_backend.features.user.User;
import dev.roland.inventory_management_backend.features.user.enumeration.UserRole;
import lombok.RequiredArgsConstructor;

/** Maps persisted document workflows to API responses and actions. */
@Service
@RequiredArgsConstructor
public class DocumentResponseMapper {
  private final DocumentWorkflowAccessService accessService;
  private final DocumentLineAllocationService allocationService;
  private final DocumentRelationService relationService;
  private final DocumentActivityService activityService;
  private final MediaUsageService mediaUsageService;
  private final DocumentFileService fileService;
  private final ObjectStorageService objectStorage;

  /**
   * Maps a persisted document and its related data to an API response.
   *
   * @param doc the doc value
   * @return the resulting value
   */
  public DocumentResponse toResponse(final Document doc) {
    final List<LineResponse> lines = mapLines(doc);
    final List<RelatedDocument> related = mapRelatedDocuments(doc);
    final List<Activity> activity = mapActivity(doc);
    final List<FileEntry> files = mapFiles(doc);
    final DocumentTotalsCalculator.Totals totals =
        DocumentTotalsCalculator.calculate(DocumentWorkflowValues.safe(doc.getLines()));
    return new DocumentResponse(
        doc.getId(),
        doc.getType(),
        DocumentWorkflowValues.number(doc),
        doc.getDocumentDate(),
        doc.getCreatedByUser() == null ? null : doc.getCreatedByUser().getUsername(),
        doc.getPartnerNameSnapshot(),
        doc.getSourceWarehouseNameSnapshot() != null
            ? doc.getSourceWarehouseNameSnapshot()
            : doc.getTargetWarehouseNameSnapshot(),
        doc.getPartner() == null ? null : doc.getPartner().getId(),
        doc.getSourceWarehouse() != null
            ? doc.getSourceWarehouse().getId()
            : DocumentWorkflowValues.idOf(doc.getTargetWarehouse()),
        doc.getSourceWarehouse() == null ? null : doc.getSourceWarehouse().getId(),
        doc.getTargetWarehouse() == null ? null : doc.getTargetWarehouse().getId(),
        doc.getTransitWarehouse() == null ? null : doc.getTransitWarehouse().getId(),
        doc.getCountedAt(),
        doc.getWorkflowReason(),
        doc.getWorkflowDisposition() == null ? null : doc.getWorkflowDisposition().name(),
        doc.getWorkflowDirection() == null ? null : doc.getWorkflowDirection().name(),
        doc.getCalculationVersion(),
        doc.isNonBillable(),
        doc.getNotes(),
        doc.getCurrency() == null ? null : doc.getCurrency().getCode(),
        doc.getCurrency() == null ? null : doc.getCurrency().getId(),
        DocumentWorkflowValues.safe(doc.getExchangeRates()).stream()
            .map(r -> new ExchangeRateResponse(r.getSourceCurrency().getId(), r.getRate()))
            .toList(),
        new DocumentTotals(
            doc.getCurrency() == null ? null : doc.getCurrency().getCode(),
            totals.net(),
            totals.tax(),
            totals.gross(),
            BigDecimal.ZERO.setScale(2, RoundingMode.UNNECESSARY)),
        doc.getStatus().name(),
        String.valueOf(doc.getVersion() == null ? 0 : doc.getVersion()),
        lines,
        allowedActions(doc),
        related,
        activity,
        files);
  }

  private List<LineResponse> mapLines(final Document doc) {
    return DocumentWorkflowValues.safe(doc.getLines()).stream()
        .map(
            line -> {
              final DocumentLine sourceLine =
                  line.getSourceLine() != null
                      ? line.getSourceLine()
                      : allocationService.findAllByFulfillmentLineId(line.getId()).stream()
                          .map(DocumentLineAllocation::getSourceOrderLine)
                          .findFirst()
                          .orElse(null);
              final DocumentLine totalsLine = sourceLine == null ? line : sourceLine;
              final DocumentLine quantitySourceLine =
                  line.getSourceLine() == null ? line : line.getSourceLine();
              final BigDecimal ordered = DocumentWorkflowValues.nz(totalsLine.getQuantity());
              final BigDecimal fulfilled = accessService.fulfilledFor(totalsLine);
              final BigDecimal closed = DocumentWorkflowValues.nz(totalsLine.getClosedQuantity());
              return new LineResponse(
                  line.getId(),
                  line.getProduct().getId(),
                  line.getProductNameSnapshot(),
                  line.getProductSkuSnapshot(),
                  line.getUnitNameSnapshot(),
                  ordered,
                  fulfilled,
                  closed,
                  totalsLine.getClosureReason(),
                  ordered.subtract(fulfilled).subtract(closed),
                  isDirectOperation(doc.getType())
                      ? DocumentWorkflowValues.nz(line.getQuantity())
                      : sourceLine == null ? null : DocumentWorkflowValues.nz(line.getQuantity()),
                  line.getUnitPrice(),
                  line.getSourceUnitPrice(),
                  line.getSourceCurrency() == null ? null : line.getSourceCurrency().getId(),
                  line.getSourceLine() != null
                      ? line.getSourceLine().getId()
                      : sourceLine == null ? null : sourceLine.getId(),
                  DocumentWorkflowValues.nz(quantitySourceLine.getReturnedQuantity()),
                  DocumentWorkflowValues.nz(quantitySourceLine.getQuantity())
                      .subtract(
                          DocumentWorkflowValues.nz(quantitySourceLine.getReturnedQuantity())),
                  DocumentWorkflowValues.nz(quantitySourceLine.getInTransitQuantity()),
                  line.getExpectedQuantity(),
                  line.getCountedQuantity(),
                  line.getVariance());
            })
        .toList();
  }

  private List<RelatedDocument> mapRelatedDocuments(final Document doc) {
    return relationService
        .findAllBySourceDocumentIdOrTargetDocumentId(doc.getId(), doc.getId())
        .stream()
        .map(
            r -> {
              final Document d =
                  r.getSourceDocument().getId().equals(doc.getId())
                      ? r.getTargetDocument()
                      : r.getSourceDocument();
              return new RelatedDocument(
                  d.getId(),
                  DocumentWorkflowValues.number(d),
                  d.getType(),
                  r.getRelationType().name());
            })
        .distinct()
        .toList();
  }

  private List<Activity> mapActivity(final Document doc) {
    return activityService.findAllByDocumentIdOrderByOccurredAtAsc(doc.getId()).stream()
        .map(
            a ->
                new Activity(
                    a.getId(),
                    a.getEventType() + (a.getDetails() == null ? "" : ": " + a.getDetails()),
                    a.getActor() == null ? "System" : a.getActor().getUsername(),
                    a.getOccurredAt()))
        .toList();
  }

  private List<FileEntry> mapFiles(final Document doc) {
    final List<FileEntry> files = new ArrayList<>();
    for (MediaUsage usage :
        mediaUsageService.findAllByEntityTypeAndEntityIdAndUsageTypeOrderBySortOrderAsc(
            MediaEntityType.DOCUMENT, doc.getId(), MediaUsageType.DOCUMENT_FILE)) {
      final MediaAsset asset = usage.getMediaAsset();
      files.add(
          new FileEntry(
              asset.getId(),
              asset.getFilename(),
              asset.getMimeType(),
              DocumentFileSource.UPLOAD.name(),
              null,
              null,
              null,
              objectStorage.generatePresignedGetUrl(asset.getObjectPath()).getUrl(),
              asset.getCreatedAt()));
    }
    for (DocumentFile file : fileService.findAllByDocumentIdOrderByCreatedAtDesc(doc.getId())) {
      if (file.getSource() != DocumentFileSource.GENERATED_PDF) {
        continue;
      }
      final MediaAsset asset = file.getMediaAsset();
      files.add(
          new FileEntry(
              file.getId(),
              asset == null ? "document.pdf" : asset.getFilename(),
              asset == null ? "application/pdf" : asset.getMimeType(),
              file.getSource().name(),
              file.getStatus() == null ? null : file.getStatus().name(),
              file.getLanguageTag(),
              file.getErrorMessage(),
              asset == null
                  ? null
                  : objectStorage.generatePresignedGetUrl(asset.getObjectPath()).getUrl(),
              file.getCreatedAt()));
    }
    return files;
  }

  private boolean isSupportedDraftType(final DocumentType type) {
    return type == DocumentType.PURCHASE_ORDER
        || type == DocumentType.SALES_ORDER
        || type == DocumentType.GOODS_RECEIPT
        || type == DocumentType.DELIVERY_NOTE;
  }

  private boolean isDirectOperation(final DocumentType type) {
    return type == DocumentType.GOODS_RECEIPT || type == DocumentType.DELIVERY_NOTE;
  }

  /**
   * Performs the allowedActions operation.
   *
   * @param doc the doc value
   * @return the resulting value
   */
  List<DocumentAction> allowedActions(final Document doc) {
    final List<DocumentAction> actions = new ArrayList<>();
    final User user = accessService.currentActor();
    if (doc.getStatus() == DocumentStatus.DRAFT
        && accessService.canManageDocument(doc, user)
        && isSupportedDraftType(doc.getType())
        && !accessService.hasFulfillmentSource(doc)) {
      actions.add(DocumentAction.EDIT);
    }
    if (doc.getStatus() == DocumentStatus.DRAFT
        && accessService.canManageDocument(doc, user)
        && (doc.getType() == DocumentType.PURCHASE_ORDER
            || doc.getType() == DocumentType.SALES_ORDER)) {
      actions.add(DocumentAction.CONFIRM);
    }
    if ((doc.getType() == DocumentType.GOODS_RECEIPT || doc.getType() == DocumentType.DELIVERY_NOTE)
        && doc.getStatus() == DocumentStatus.DRAFT
        && accessService.directAllowed(doc.getType(), user)) {
      actions.add(DocumentAction.POST);
    }
    if (doc.getType() == DocumentType.PURCHASE_ORDER
        && (doc.getStatus() == DocumentStatus.CONFIRMED
            || doc.getStatus() == DocumentStatus.PARTIALLY_COMPLETED)
        && accessService.hasRemaining(doc)
        && accessService.canManageDocument(doc, user)
        && accessService.directAllowed(DocumentType.GOODS_RECEIPT, user)) {
      actions.add(DocumentAction.POST);
    }
    if (doc.getType() == DocumentType.SALES_ORDER
        && (doc.getStatus() == DocumentStatus.CONFIRMED
            || doc.getStatus() == DocumentStatus.PARTIALLY_COMPLETED)
        && accessService.hasRemaining(doc)
        && accessService.canManageDocument(doc, user)
        && accessService.directAllowed(DocumentType.DELIVERY_NOTE, user)) {
      actions.add(DocumentAction.POST);
    }
    if (doc.getStatus() != DocumentStatus.CANCELLED && accessService.canManageDocument(doc, user)) {
      actions.add(DocumentAction.UPLOAD_FILE);
    }
    if (doc.getStatus() != DocumentStatus.DRAFT
        && doc.getStatus() != DocumentStatus.CANCELLED
        && accessService.canManageDocument(doc, user)) {
      actions.add(DocumentAction.GENERATE_PDF);
      final boolean latestPdfFailed =
          fileService.findAllByDocumentIdOrderByCreatedAtDesc(doc.getId()).stream()
              .filter(file -> file.getSource() == DocumentFileSource.GENERATED_PDF)
              .findFirst()
              .map(file -> file.getStatus() == DocumentFileStatus.FAILED)
              .orElse(false);
      if (latestPdfFailed) {
        actions.add(DocumentAction.RETRY_PDF);
      }
    }
    if ((doc.getType() == DocumentType.PURCHASE_ORDER || doc.getType() == DocumentType.SALES_ORDER)
        && (doc.getStatus() == DocumentStatus.CONFIRMED
            || doc.getStatus() == DocumentStatus.PARTIALLY_COMPLETED)
        && accessService.canManageDocument(doc, user)) {
      actions.add(DocumentAction.CLOSE_REMAINING);
    }
    if (user != null && user.getRole() != UserRole.SALES) {
      if (doc.getType() == DocumentType.TRANSFER_REQUEST
          && doc.getStatus() != DocumentStatus.COMPLETED
          && doc.getStatus() != DocumentStatus.CANCELLED) {
        if ((doc.getStatus() == DocumentStatus.DRAFT
                || doc.getStatus() == DocumentStatus.PARTIALLY_COMPLETED)
            && DocumentWorkflowValues.safe(doc.getLines()).stream()
                .anyMatch(
                    l ->
                        DocumentWorkflowValues.nz(l.getFulfilledQuantity())
                                .compareTo(DocumentWorkflowValues.nz(l.getQuantity()))
                            < 0)) {
          actions.add(DocumentAction.DISPATCH);
        }
        if (DocumentWorkflowValues.safe(doc.getLines()).stream()
            .anyMatch(l -> DocumentWorkflowValues.nz(l.getInTransitQuantity()).signum() > 0)) {
          actions.add(DocumentAction.RECEIVE_TRANSFER);
          actions.add(DocumentAction.RESOLVE_TRANSIT);
        }
      }
      if ((doc.getType() == DocumentType.DELIVERY_NOTE
              || doc.getType() == DocumentType.GOODS_RECEIPT)
          && doc.getStatus() == DocumentStatus.COMPLETED
          && DocumentWorkflowValues.safe(doc.getLines()).stream()
              .anyMatch(
                  l ->
                      DocumentWorkflowValues.nz(l.getQuantity())
                              .compareTo(DocumentWorkflowValues.nz(l.getReturnedQuantity()))
                          > 0)) {
        actions.add(DocumentAction.CREATE_RETURN);
      }
      if (doc.getType() == DocumentType.STOCK_COUNT && doc.getStatus() == DocumentStatus.DRAFT) {
        actions.add(DocumentAction.RECONCILE);
      }
      if (doc.getStatus() == DocumentStatus.COMPLETED
          && (doc.getType() == DocumentType.DELIVERY_NOTE
              || doc.getType() == DocumentType.GOODS_RECEIPT
              || doc.getType() == DocumentType.TRANSFER_DELIVERY
              || doc.getType() == DocumentType.TRANSFER_RECEIPT)) {
        actions.add(DocumentAction.CORRECT);
      }
      if (doc.getType() == DocumentType.RETURN
          && DocumentWorkflowValues.safe(doc.getLines()).stream()
              .anyMatch(
                  l ->
                      (l.getWorkflowDisposition() == ReturnDisposition.QUARANTINE
                              || l.getWorkflowDisposition() == ReturnDisposition.DAMAGED)
                          && DocumentWorkflowValues.nz(l.getReleasedQuantity())
                                  .compareTo(DocumentWorkflowValues.nz(l.getQuantity()))
                              < 0)) {
        actions.add(DocumentAction.RELEASE_QUARANTINE);
      }
    }
    if (user != null
        && user.getRole() != UserRole.SALES
        && doc.getType() == DocumentType.RETURN
        && doc.getWorkflowDirection() == ReturnDirection.CUSTOMER_RETURN) {
      actions.add(DocumentAction.CREATE_REPLACEMENT_DELIVERY);
    }
    return actions;
  }
}
