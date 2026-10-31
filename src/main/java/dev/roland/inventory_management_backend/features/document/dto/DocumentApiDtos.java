package dev.roland.inventory_management_backend.features.document.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import dev.roland.inventory_management_backend.features.document.enumeration.DocumentAction;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentQueueView;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentType;

/** Request and response contracts for the Release 1 document API. */
public final class DocumentApiDtos {
  private DocumentApiDtos() {}

  public record LineDraft(Long productId, Long unitId, BigDecimal quantity, BigDecimal unitPrice) {}

  public record Draft(
      DocumentType type,
      LocalDate date,
      Long partnerId,
      Long warehouseId,
      String notes,
      List<LineDraft> lines) {}

  public record DraftUpdate(String version, Draft draft) {}

  public record LineResponse(
      Long id,
      Long productId,
      String productName,
      String productCode,
      String unitName,
      BigDecimal orderedQuantity,
      BigDecimal fulfilledQuantity,
      BigDecimal closedQuantity,
      String closureReason,
      BigDecimal remainingQuantity,
      BigDecimal operationQuantity,
      BigDecimal unitPrice,
      Long sourceLineId) {}

  public record DocumentTotals(
      String currencyCode,
      BigDecimal net,
      BigDecimal tax,
      BigDecimal gross,
      BigDecimal roundingAdjustment) {}

  public record RelatedDocument(Long id, String number, DocumentType type, String relationKind) {}

  public record Activity(Long id, String description, String actorName, LocalDateTime occurredAt) {}

  public record FileEntry(
      Long id,
      String fileName,
      String contentType,
      String source,
      String status,
      String language,
      String errorMessage,
      String downloadUrl,
      LocalDateTime createdAt) {}

  public record DocumentResponse(
      Long id,
      DocumentType type,
      String number,
      LocalDate date,
      String ownerName,
      String partnerName,
      String routeName,
      Long partnerId,
      Long warehouseId,
      String notes,
      String currencyCode,
      DocumentTotals totals,
      String status,
      String version,
      List<LineResponse> lines,
      List<DocumentAction> allowedActions,
      List<RelatedDocument> relatedDocuments,
      List<Activity> activity,
      List<FileEntry> files) {}

  public record PageResponse(
      List<DocumentResponse> items, long total, int page, int pageSize, Map<String, Long> counts) {}

  public record Capabilities(
      List<DocumentType> creatableTypes, List<DocumentQueueView> availableQueueViews) {}

  public record FulfillmentLine(Long sourceLineId, Long productId, BigDecimal quantity) {}

  public record FulfillmentDraft(
      Long sourceDocumentId, DocumentType type, Long warehouseId, List<FulfillmentLine> lines) {}

  public record FulfillmentPreview(
      Long sourceDocumentId,
      Long documentId,
      DocumentType type,
      Long warehouseId,
      List<FulfillmentLine> lines) {}

  public record PreviewLine(
      Long sourceLineId,
      Long productId,
      String productName,
      String warehouseName,
      BigDecimal availableQuantity,
      BigDecimal quantityDelta,
      BigDecimal resultingQuantity,
      BigDecimal shortfall,
      String errorMessageKey) {}

  public record PreviewResponse(boolean valid, List<PreviewLine> lines) {}

  public record CloseRemaining(String reason) {}
}
