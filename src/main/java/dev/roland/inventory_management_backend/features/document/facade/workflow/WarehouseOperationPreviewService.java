package dev.roland.inventory_management_backend.features.document.facade.workflow;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.common.exception.NotFoundException;
import dev.roland.inventory_management_backend.common.message.NotFoundMessageKey;
import dev.roland.inventory_management_backend.common.service.CurrentCompanyProvider;
import dev.roland.inventory_management_backend.features.company.Company;
import dev.roland.inventory_management_backend.features.document.Document;
import dev.roland.inventory_management_backend.features.document.dto.WarehouseOperationPreview;
import dev.roland.inventory_management_backend.features.document.dto.WarehouseOperationRequest;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentRelationType;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentStatus;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentType;
import dev.roland.inventory_management_backend.features.document.enumeration.ReturnDirection;
import dev.roland.inventory_management_backend.features.document.enumeration.ReturnDisposition;
import dev.roland.inventory_management_backend.features.document.enumeration.WarehouseOperationType;
import dev.roland.inventory_management_backend.features.document.mapper.DocumentResponseMapper;
import dev.roland.inventory_management_backend.features.document.mapper.WarehouseOperationPreviewAssembler;
import dev.roland.inventory_management_backend.features.document.message.DocumentApiMessageKey;
import dev.roland.inventory_management_backend.features.document.utils.DocumentWorkflowValues;
import dev.roland.inventory_management_backend.features.document_line.DocumentLine;
import dev.roland.inventory_management_backend.features.document_line_allocation.DocumentLineAllocation;
import dev.roland.inventory_management_backend.features.document_line_allocation.service.DocumentLineAllocationService;
import dev.roland.inventory_management_backend.features.document_relation.DocumentRelation;
import dev.roland.inventory_management_backend.features.document_relation.service.DocumentRelationService;
import dev.roland.inventory_management_backend.features.product.Product;
import dev.roland.inventory_management_backend.features.product.service.ProductService;
import dev.roland.inventory_management_backend.features.stock_balance.service.StockBalanceService;
import dev.roland.inventory_management_backend.features.stock_movement.StockMovement;
import dev.roland.inventory_management_backend.features.stock_movement.service.StockMovementService;
import dev.roland.inventory_management_backend.features.user.enumeration.UserRole;
import dev.roland.inventory_management_backend.features.warehouse.Warehouse;
import dev.roland.inventory_management_backend.features.warehouse.service.WarehouseService;
import lombok.RequiredArgsConstructor;

/** Builds previews for warehouse document operations. */
@Service
@RequiredArgsConstructor
public class WarehouseOperationPreviewService {
  private final ProductService productService;
  private final WarehouseService warehouseService;
  private final CurrentCompanyProvider companyProvider;
  private final DocumentRelationService relationService;
  private final DocumentLineAllocationService allocationService;
  private final StockBalanceService balanceService;
  private final StockMovementService stockMovementService;
  private final DocumentWorkflowAccessService accessService;
  private final DocumentResponseMapper responseMapper;
  private final WarehouseOperationSupportService warehouseOperationSupportService;
  private final WarehouseTransferPreviewService transferPreviewService;

  /**
   * Builds a preview for the requested warehouse operation.
   *
   * @param id the id value
   * @param operation the operation value
   * @param request the request value
   * @return the resulting value
   */
  @Transactional
  public WarehouseOperationPreview preview(
      final Long id,
      final WarehouseOperationType operation,
      final WarehouseOperationRequest request) {
    final Company company = companyProvider.getCurrentCompany();
    final Document document = id == null ? null : accessService.find(id);
    final List<WarehouseOperationPreview.Line> previewLines = new ArrayList<>();
    final List<String> blockers = new ArrayList<>();
    final List<WarehouseOperationPreview.Movement> movements = new ArrayList<>();
    final List<WarehouseOperationPreview.DocumentRef> relatedReturns = new ArrayList<>();
    String calculationVersion = null;
    if (request == null || request.lines() == null || request.lines().isEmpty()) {
      blockers.add(DocumentApiMessageKey.DOCUMENT_INVALID_LINES.getKey());
    } else if (operation == WarehouseOperationType.STOCK_COUNT_RECONCILE) {
      calculationVersion =
          appendStockCountLines(document, company, request, previewLines, blockers, movements);
    } else if (operation == WarehouseOperationType.TRANSFER_PREVIEW
        || operation == WarehouseOperationType.TRANSFER_RESOLUTION) {
      transferPreviewService.appendLines(
          document, company, operation, request, previewLines, blockers);
    } else {
      if (operation == WarehouseOperationType.RETURN_CREATE) {
        appendReturnLines(company, request, previewLines, blockers);
      } else if (operation == WarehouseOperationType.CORRECTION) {
        appendCorrectionLines(
            document, company, request, previewLines, blockers, movements, relatedReturns);
      } else if (operation == WarehouseOperationType.QUARANTINE_RELEASE) {
        appendQuarantineLines(document, company, request, previewLines, blockers);
      } else if (operation == WarehouseOperationType.REPLACEMENT_DELIVERY) {
        appendReplacementLines(company, request, previewLines, blockers);
      } else {
        WarehouseOperationPreviewAssembler.addUnmappedLines(request, previewLines);
      }
    }
    return WarehouseOperationPreviewAssembler.finish(
        company,
        productService,
        request,
        previewLines,
        blockers,
        calculationVersion,
        movements,
        relatedReturns);
  }

  private void appendReturnLines(
      final Company company,
      final WarehouseOperationRequest request,
      final List<WarehouseOperationPreview.Line> previewLines,
      final List<String> blockers) {
    final Document sourceDocument =
        request.sourceDocumentId() == null ? null : accessService.find(request.sourceDocumentId());
    for (var item : request.lines()) {
      final DocumentLine source =
          item.sourceLineId() == null
              ? null
              : warehouseOperationSupportService.line(item.sourceLineId(), company.getId());
      final BigDecimal max =
          source == null
              ? BigDecimal.ZERO
              : DocumentWorkflowValues.nz(source.getQuantity())
                  .subtract(DocumentWorkflowValues.nz(source.getReturnedQuantity()));
      final BigDecimal quantity = DocumentWorkflowValues.nz(item.quantity());
      final boolean supplierReturn = request.direction() == ReturnDirection.SUPPLIER_RETURN;
      final boolean quarantine =
          item.disposition() == ReturnDisposition.QUARANTINE
              || item.disposition() == ReturnDisposition.DAMAGED;
      final Warehouse originalWarehouse =
          sourceDocument == null
              ? null
              : sourceDocument.getType() == DocumentType.GOODS_RECEIPT
                  ? sourceDocument.getTargetWarehouse()
                  : sourceDocument.getSourceWarehouse();
      final Warehouse targetWarehouse =
          sourceDocument == null
              ? null
              : supplierReturn
                  ? originalWarehouse
                  : quarantine
                      ? warehouseService.ensureQuarantineWarehouse(company)
                      : request.warehouseId() == null
                          ? originalWarehouse
                          : warehouseService.findEntityByIdAndCompanyId(
                              request.warehouseId(), company.getId());
      final BigDecimal quantityDelta = supplierReturn ? quantity.negate() : quantity;
      final BigDecimal currentQuantity =
          source == null || targetWarehouse == null
              ? BigDecimal.ZERO
              : balanceService
                  .findByWarehouseAndProduct(targetWarehouse, source.getProduct())
                  .map(b -> DocumentWorkflowValues.nz(b.getQuantity()))
                  .orElse(BigDecimal.ZERO);
      final boolean valid =
          source != null
              && sourceDocument != null
              && source.getDocument().getId().equals(sourceDocument.getId())
              && sourceDocument.getStatus() == DocumentStatus.COMPLETED
              && (request.direction() == ReturnDirection.CUSTOMER_RETURN
                  ? sourceDocument.getType() == DocumentType.DELIVERY_NOTE
                      && item.disposition() != ReturnDisposition.SUPPLIER_RETURN
                  : request.direction() == ReturnDirection.SUPPLIER_RETURN
                      && sourceDocument.getType() == DocumentType.GOODS_RECEIPT
                      && item.disposition() == ReturnDisposition.SUPPLIER_RETURN)
              && quantity.signum() > 0
              && quantity.compareTo(max) <= 0;
      if (!valid) {
        blockers.add(DocumentApiMessageKey.DOCUMENT_RETURN_QUANTITY_EXCEEDED.getKey());
      }
      if (item.disposition() == null
          || !Set.of(
                  ReturnDisposition.RESTOCK,
                  ReturnDisposition.QUARANTINE,
                  ReturnDisposition.DAMAGED,
                  ReturnDisposition.SUPPLIER_RETURN)
              .contains(item.disposition())) {
        blockers.add(DocumentApiMessageKey.DOCUMENT_INVALID_LINES.getKey());
      }
      previewLines.add(
          WarehouseOperationPreview.Line.builder()
              .sourceLineId(item.sourceLineId())
              .productId(source == null ? item.productId() : source.getProduct().getId())
              .originalQuantity(
                  DocumentWorkflowValues.nz(source == null ? null : source.getQuantity()))
              .returnedQuantity(
                  DocumentWorkflowValues.nz(source == null ? null : source.getReturnedQuantity()))
              .returnableQuantity(max)
              .quantityDelta(quantityDelta)
              .resultingQuantity(currentQuantity.add(quantityDelta))
              .targetWarehouseId(targetWarehouse == null ? null : targetWarehouse.getId())
              .errorMessageKey(
                  valid ? null : DocumentApiMessageKey.DOCUMENT_RETURN_QUANTITY_EXCEEDED.getKey())
              .build());
    }
    if (request.reason() == null || request.reason().isBlank()) {
      blockers.add(DocumentApiMessageKey.DOCUMENT_REASON_REQUIRED.getKey());
    }
    if (request.direction() != ReturnDirection.CUSTOMER_RETURN
        && request.direction() != ReturnDirection.SUPPLIER_RETURN) {
      blockers.add(DocumentApiMessageKey.DOCUMENT_INVALID_LINES.getKey());
    }
    if (sourceDocument != null && sourceDocument.getStatus() != DocumentStatus.COMPLETED) {
      blockers.add(DocumentApiMessageKey.DOCUMENT_INVALID_TRANSITION.getKey());
    }
    if (request.lines().stream()
        .anyMatch(
            line ->
                (request.direction() == ReturnDirection.SUPPLIER_RETURN)
                    != (line.disposition() == ReturnDisposition.SUPPLIER_RETURN))) {
      blockers.add(DocumentApiMessageKey.DOCUMENT_INVALID_LINES.getKey());
    }
    if (sourceDocument != null && sourceDocument.getStatus() != DocumentStatus.COMPLETED) {
      blockers.add(DocumentApiMessageKey.DOCUMENT_INVALID_TRANSITION.getKey());
    }
  }

  private void appendCorrectionLines(
      final Document document,
      final Company company,
      final WarehouseOperationRequest request,
      final List<WarehouseOperationPreview.Line> previewLines,
      final List<String> blockers,
      final List<WarehouseOperationPreview.Movement> movements,
      final List<WarehouseOperationPreview.DocumentRef> relatedReturns) {
    if (document == null || document.getStatus() != DocumentStatus.COMPLETED) {
      blockers.add(DocumentApiMessageKey.DOCUMENT_INVALID_TRANSITION.getKey());
    } else {
      accessService.checkVersion(
          document, request.version(), () -> responseMapper.toResponse(document));
    }
    if (request.operation() != WarehouseOperationType.CORRECT_RECORDING) {
      blockers.add(DocumentApiMessageKey.DOCUMENT_CORRECTION_WORKFLOW_REQUIRED.getKey());
    }
    if (document != null
        && !Set.of(
                DocumentType.GOODS_RECEIPT,
                DocumentType.DELIVERY_NOTE,
                DocumentType.TRANSFER_DELIVERY,
                DocumentType.TRANSFER_RECEIPT)
            .contains(document.getType())) {
      blockers.add(DocumentApiMessageKey.DOCUMENT_INVALID_TRANSITION.getKey());
    }
    if (request.reason() == null || request.reason().isBlank()) {
      blockers.add(DocumentApiMessageKey.DOCUMENT_REASON_REQUIRED.getKey());
    }
    for (var item : request.lines()) {
      final DocumentLine source =
          item.sourceLineId() == null
              ? null
              : warehouseOperationSupportService.line(item.sourceLineId(), company.getId());
      final BigDecimal corrected = DocumentWorkflowValues.nz(item.correctedQuantity());
      final BigDecimal old =
          DocumentWorkflowValues.nz(source == null ? null : source.getQuantity());
      final BigDecimal delta = corrected.subtract(old);
      final BigDecimal returned =
          DocumentWorkflowValues.nz(source == null ? null : source.getReturnedQuantity());
      final boolean valid =
          source != null
              && source.getDocument().getId().equals(document == null ? null : document.getId())
              && corrected.signum() >= 0
              && corrected.compareTo(returned) >= 0;
      if (!valid) {
        blockers.add(DocumentApiMessageKey.DOCUMENT_CORRECTION_BLOCKED.getKey());
      }
      if (source != null && DocumentWorkflowValues.nz(source.getQuantity()).signum() > 0) {
        final BigDecimal fulfillmentDelta =
            corrected.subtract(DocumentWorkflowValues.nz(source.getQuantity()));
        for (DocumentLineAllocation allocation :
            allocationService.findAllByFulfillmentLineId(source.getId())) {
          final DocumentLine orderLine = allocation.getSourceOrderLine();
          final BigDecimal allocationDelta =
              allocation
                  .getQuantity()
                  .multiply(fulfillmentDelta)
                  .divide(DocumentWorkflowValues.nz(source.getQuantity()), 8, RoundingMode.HALF_UP);
          final BigDecimal projected =
              accessService
                  .fulfilledFor(orderLine)
                  .add(allocationDelta)
                  .add(DocumentWorkflowValues.nz(orderLine.getClosedQuantity()));
          if (projected.signum() < 0
              || projected.compareTo(DocumentWorkflowValues.nz(orderLine.getQuantity())) > 0) {
            blockers.add(
                DocumentApiMessageKey.DOCUMENT_CORRECTION_CLOSED_QUANTITY_CONFLICT.getKey());
          }
        }
      }
      BigDecimal quantityDelta = delta;
      BigDecimal resultingQuantity = null;
      if (source != null && document != null && valid) {
        final Map<WarehouseOperationSupportService.StockKey, BigDecimal> stockDeltas =
            warehouseOperationSupportService.correctionStockDeltas(
                document, source, item, company.getId());
        for (var entry : stockDeltas.entrySet()) {
          final Warehouse warehouse =
              warehouseService.findEntityByIdAndCompanyId(
                  entry.getKey().warehouseId(), company.getId());
          final Product product =
              productService
                  .findByIdAndCompanyId(entry.getKey().productId(), company.getId())
                  .orElseThrow(() -> new NotFoundException(NotFoundMessageKey.PRODUCT));
          final BigDecimal current =
              balanceService
                  .findByWarehouseAndProduct(warehouse, product)
                  .map(b -> DocumentWorkflowValues.nz(b.getQuantity()))
                  .orElse(BigDecimal.ZERO);
          final BigDecimal projected = current.add(entry.getValue());
          if (projected.signum() < 0) {
            blockers.add(DocumentApiMessageKey.DOCUMENT_CORRECTION_NEGATIVE_STOCK.getKey());
          }
          final Long correctedWarehouseId =
              item.correctedWarehouseId() == null
                  ? warehouseOperationSupportService.defaultCorrectionWarehouse(document).getId()
                  : item.correctedWarehouseId();
          if (entry
                  .getKey()
                  .productId()
                  .equals(
                      item.correctedProductId() == null
                          ? source.getProduct().getId()
                          : item.correctedProductId())
              && entry.getKey().warehouseId().equals(correctedWarehouseId)) {
            resultingQuantity = projected;
            quantityDelta = entry.getValue();
          }
        }
      }
      previewLines.add(
          WarehouseOperationPreview.Line.builder()
              .sourceLineId(item.sourceLineId())
              .productId(source == null ? item.productId() : source.getProduct().getId())
              .originalQuantity(old)
              .correctedQuantity(corrected)
              .correctedFulfilledQuantity(corrected)
              .closedQuantity(
                  DocumentWorkflowValues.nz(source == null ? null : source.getClosedQuantity()))
              .returnedQuantity(returned)
              .remainingQuantity(
                  corrected.subtract(
                      DocumentWorkflowValues.nz(
                          source == null ? null : source.getClosedQuantity())))
              .quantityDelta(quantityDelta)
              .resultingQuantity(resultingQuantity)
              .targetWarehouseId(item.correctedWarehouseId())
              .errorMessageKey(
                  valid ? null : DocumentApiMessageKey.DOCUMENT_CORRECTION_BLOCKED.getKey())
              .build());
    }
    if (document != null) {
      relationService
          .findAllBySourceDocumentIdOrTargetDocumentId(document.getId(), document.getId())
          .stream()
          .filter(
              r ->
                  r.getRelationType() == DocumentRelationType.RETURNS
                      && r.getSourceDocument().getId().equals(document.getId()))
          .map(DocumentRelation::getTargetDocument)
          .forEach(
              returnDoc ->
                  relatedReturns.add(
                      new WarehouseOperationPreview.DocumentRef(
                          returnDoc.getId(),
                          DocumentWorkflowValues.number(returnDoc),
                          DocumentWorkflowValues.safe(returnDoc.getLines()).stream()
                              .map(DocumentLine::getQuantity)
                              .map(DocumentWorkflowValues::nz)
                              .reduce(BigDecimal.ZERO, BigDecimal::add))));
      final LocalDateTime after =
          document.getCompletedAt() == null ? document.getCreatedAt() : document.getCompletedAt();
      if (after != null) {
        for (var lineRequest : request.lines()) {
          final DocumentLine source =
              warehouseOperationSupportService.line(lineRequest.sourceLineId(), company.getId());
          final Warehouse warehouse =
              document.getType() == DocumentType.GOODS_RECEIPT
                  ? document.getTargetWarehouse()
                  : document.getSourceWarehouse();
          if (warehouse != null) {
            stockMovementService
                .findEffectiveAfter(source.getProduct().getId(), warehouse.getId(), after)
                .forEach(
                    m ->
                        movements.add(
                            new WarehouseOperationPreview.Movement(
                                m.getMovementType().name(),
                                m.getEffectiveAt(),
                                m.getQuantityChange())));
          }
        }
      }
    }
  }

  private void appendQuarantineLines(
      final Document document,
      final Company company,
      final WarehouseOperationRequest request,
      final List<WarehouseOperationPreview.Line> previewLines,
      final List<String> blockers) {
    if (accessService.currentActor().getRole() != UserRole.ADMIN
        && accessService.currentActor().getRole() != UserRole.MANAGER) {
      blockers.add(DocumentApiMessageKey.DOCUMENT_INVALID_TRANSITION.getKey());
    }
    for (var item : DocumentWorkflowValues.safe(request.lines())) {
      final DocumentLine line =
          item.sourceLineId() == null
              ? null
              : warehouseOperationSupportService.line(item.sourceLineId(), company.getId());
      final BigDecimal remaining =
          line == null
              ? BigDecimal.ZERO
              : DocumentWorkflowValues.nz(line.getQuantity())
                  .subtract(DocumentWorkflowValues.nz(line.getReleasedQuantity()));
      final BigDecimal quantity = DocumentWorkflowValues.nz(item.quantity());
      final Warehouse quarantine = warehouseService.ensureQuarantineWarehouse(company);
      final Warehouse target =
          request.targetWarehouseId() == null
              ? null
              : warehouseService.findEntityByIdAndCompanyId(
                  request.targetWarehouseId(), company.getId());
      final BigDecimal quarantineQuantity =
          line == null
              ? BigDecimal.ZERO
              : balanceService
                  .findByWarehouseAndProduct(quarantine, line.getProduct())
                  .map(b -> DocumentWorkflowValues.nz(b.getQuantity()))
                  .orElse(BigDecimal.ZERO);
      final BigDecimal targetQuantity =
          line == null || target == null
              ? BigDecimal.ZERO
              : balanceService
                  .findByWarehouseAndProduct(target, line.getProduct())
                  .map(b -> DocumentWorkflowValues.nz(b.getQuantity()))
                  .orElse(BigDecimal.ZERO);
      final boolean valid =
          document != null
              && document.getType() == DocumentType.RETURN
              && line != null
              && line.getDocument().getId().equals(document.getId())
              && (line.getWorkflowDisposition() == ReturnDisposition.QUARANTINE
                  || line.getWorkflowDisposition() == ReturnDisposition.DAMAGED)
              && quantity.signum() > 0
              && quantity.compareTo(remaining) <= 0
              && quantity.compareTo(quarantineQuantity) <= 0
              && target != null
              && target.getStatus()
                  == dev.roland.inventory_management_backend.features.warehouse.enumeration
                      .WarehouseStatus.ACTIVE
              && target.getType()
                  != dev.roland.inventory_management_backend.features.warehouse.enumeration
                      .WarehouseType.QUARANTINE
              && target.getType()
                  != dev.roland.inventory_management_backend.features.warehouse.enumeration
                      .WarehouseType.TRANSIT;
      if (!valid) {
        blockers.add(DocumentApiMessageKey.DOCUMENT_INVALID_LINES.getKey());
      }
      previewLines.add(
          WarehouseOperationPreview.Line.builder()
              .sourceLineId(item.sourceLineId())
              .productId(line == null ? item.productId() : line.getProduct().getId())
              .sourceQuantity(quarantineQuantity)
              .destinationQuantity(targetQuantity.add(quantity))
              .returnableQuantity(remaining)
              .quantityDelta(quantity)
              .resultingQuantity(targetQuantity.add(quantity))
              .targetWarehouseId(request.targetWarehouseId())
              .errorMessageKey(valid ? null : DocumentApiMessageKey.DOCUMENT_INVALID_LINES.getKey())
              .build());
    }
    if (request.reason() == null || request.reason().isBlank()) {
      blockers.add(DocumentApiMessageKey.DOCUMENT_REASON_REQUIRED.getKey());
    }
  }

  private void appendReplacementLines(
      final Company company,
      final WarehouseOperationRequest request,
      final List<WarehouseOperationPreview.Line> previewLines,
      final List<String> blockers) {
    final Document returned =
        request.returnDocumentId() == null ? null : accessService.find(request.returnDocumentId());
    final Document original =
        request.originalDeliveryId() == null
            ? null
            : accessService.find(request.originalDeliveryId());
    for (var item : DocumentWorkflowValues.safe(request.lines())) {
      final DocumentLine line =
          item.sourceLineId() == null
              ? null
              : warehouseOperationSupportService.line(item.sourceLineId(), company.getId());
      final BigDecimal remaining =
          line == null
              ? BigDecimal.ZERO
              : DocumentWorkflowValues.nz(line.getQuantity())
                  .subtract(DocumentWorkflowValues.nz(line.getReplacedQuantity()));
      final BigDecimal quantity = DocumentWorkflowValues.nz(item.quantity());
      final Warehouse warehouse =
          request.warehouseId() == null
              ? null
              : warehouseService.findEntityByIdAndCompanyId(request.warehouseId(), company.getId());
      final Product product =
          line == null
              ? null
              : item.productId() == null
                  ? line.getProduct()
                  : productService
                      .findByIdAndCompanyId(item.productId(), company.getId())
                      .orElse(null);
      final BigDecimal available =
          warehouse == null || product == null
              ? BigDecimal.ZERO
              : balanceService
                  .findByWarehouseAndProduct(warehouse, product)
                  .map(b -> DocumentWorkflowValues.nz(b.getQuantity()))
                  .orElse(BigDecimal.ZERO);
      final boolean valid =
          returned != null
              && original != null
              && line != null
              && line.getDocument().getId().equals(returned.getId())
              && line.getSourceLine() != null
              && line.getSourceLine().getDocument().getId().equals(original.getId())
              && returned.getWorkflowDirection() == ReturnDirection.CUSTOMER_RETURN
              && quantity.signum() > 0
              && quantity.compareTo(remaining) <= 0
              && quantity.compareTo(available) <= 0
              && warehouse != null
              && warehouse.getStatus()
                  == dev.roland.inventory_management_backend.features.warehouse.enumeration
                      .WarehouseStatus.ACTIVE;
      if (!valid) {
        blockers.add(DocumentApiMessageKey.DOCUMENT_INVALID_LINES.getKey());
      }
      previewLines.add(
          WarehouseOperationPreview.Line.builder()
              .sourceLineId(item.sourceLineId())
              .productId(line == null ? item.productId() : line.getProduct().getId())
              .sourceQuantity(available)
              .returnableQuantity(remaining)
              .quantityDelta(quantity.negate())
              .resultingQuantity(available.subtract(quantity))
              .targetWarehouseId(request.warehouseId())
              .errorMessageKey(valid ? null : DocumentApiMessageKey.DOCUMENT_INVALID_LINES.getKey())
              .build());
    }
  }

  private String appendStockCountLines(
      final Document document,
      final Company company,
      final WarehouseOperationRequest request,
      final List<WarehouseOperationPreview.Line> previewLines,
      final List<String> blockers,
      final List<WarehouseOperationPreview.Movement> movements) {
    String calculationVersion = null;
    if (document == null || document.getType() != DocumentType.STOCK_COUNT) {
      blockers.add(DocumentApiMessageKey.DOCUMENT_INVALID_TRANSITION.getKey());
    } else {
      accessService.checkVersion(
          document, request.version(), () -> responseMapper.toResponse(document));
      final LocalDateTime countedAt =
          request.countedAt() == null ? document.getCountedAt() : request.countedAt();
      if (countedAt == null) {
        blockers.add(DocumentApiMessageKey.DOCUMENT_INVALID_LINES.getKey());
      } else {
        calculationVersion =
            calculateStockCountVersion(
                document, company, request, previewLines, blockers, movements, countedAt);
      }
    }
    return calculationVersion;
  }

  private String calculateStockCountVersion(
      final Document document,
      final Company company,
      final WarehouseOperationRequest request,
      final List<WarehouseOperationPreview.Line> previewLines,
      final List<String> blockers,
      final List<WarehouseOperationPreview.Movement> movements,
      final LocalDateTime countedAt) {
    final List<String> versionParts = new ArrayList<>();
    final Set<Long> countLineIds = new java.util.HashSet<>();
    for (var lineRequest : request.lines()) {
      final DocumentLine countLine =
          warehouseOperationSupportService.line(lineRequest.sourceLineId(), company.getId());
      if (!countLine.getDocument().getId().equals(document.getId())
          || !countLineIds.add(countLine.getId())) {
        blockers.add(DocumentApiMessageKey.DOCUMENT_INVALID_LINES.getKey());
        continue;
      }
      final Long productId = countLine.getProduct().getId();
      final Long warehouseId = document.getSourceWarehouse().getId();
      final List<StockMovement> before =
          stockMovementService.findEffectiveThrough(productId, warehouseId, countedAt);
      final List<StockMovement> after =
          stockMovementService.findEffectiveAfter(productId, warehouseId, countedAt);
      final BigDecimal expected =
          before.stream()
              .map(StockMovement::getQuantityChange)
              .reduce(BigDecimal.ZERO, BigDecimal::add);
      final BigDecimal counted = DocumentWorkflowValues.nz(lineRequest.countedQuantity());
      if (lineRequest.countedQuantity() == null) {
        blockers.add(DocumentApiMessageKey.DOCUMENT_INVALID_LINES.getKey());
      }
      versionParts.add(countLine.getId() + ":" + countedAt + ":" + counted);
      final BigDecimal later =
          after.stream()
              .map(StockMovement::getQuantityChange)
              .reduce(BigDecimal.ZERO, BigDecimal::add);
      final BigDecimal target = counted.add(later);
      final BigDecimal current =
          balanceService
              .findByWarehouseAndProduct(document.getSourceWarehouse(), countLine.getProduct())
              .map(b -> DocumentWorkflowValues.nz(b.getQuantity()))
              .orElse(BigDecimal.ZERO);
      final BigDecimal adjustment = target.subtract(current);
      final BigDecimal variance = counted.subtract(expected);
      previewLines.add(
          WarehouseOperationPreview.Line.builder()
              .sourceLineId(countLine.getId())
              .productId(productId)
              .expectedAtCount(expected)
              .countedQuantity(counted)
              .variance(variance)
              .rebasedTargetQuantity(target)
              .adjustmentQuantity(adjustment)
              .quantityDelta(adjustment)
              .resultingQuantity(current.add(adjustment))
              .targetWarehouseId(warehouseId)
              .build());
      before.forEach(
          m ->
              versionParts.add(m.getId() + ":" + m.getEffectiveAt() + ":" + m.getQuantityChange()));
      after.forEach(
          m -> {
            versionParts.add(m.getId() + ":" + m.getEffectiveAt() + ":" + m.getQuantityChange());
            movements.add(
                new WarehouseOperationPreview.Movement(
                    m.getMovementType().name(), m.getEffectiveAt(), m.getQuantityChange()));
          });
    }
    if (countLineIds.size() != DocumentWorkflowValues.safe(document.getLines()).size()) {
      blockers.add(DocumentApiMessageKey.DOCUMENT_INVALID_LINES.getKey());
    }
    return warehouseOperationSupportService.hashCalculationVersion(versionParts);
  }
}
