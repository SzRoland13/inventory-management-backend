package dev.roland.inventory_management_backend.features.document.facade.workflow;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.common.exception.ApiException;
import dev.roland.inventory_management_backend.common.exception.NotFoundException;
import dev.roland.inventory_management_backend.common.message.NotFoundMessageKey;
import dev.roland.inventory_management_backend.features.company.Company;
import dev.roland.inventory_management_backend.features.document.Document;
import dev.roland.inventory_management_backend.features.document.dto.DocumentResponse;
import dev.roland.inventory_management_backend.features.document.dto.WarehouseOperationPreview;
import dev.roland.inventory_management_backend.features.document.dto.WarehouseOperationRequest;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentRelationType;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentType;
import dev.roland.inventory_management_backend.features.document.enumeration.ReturnDirection;
import dev.roland.inventory_management_backend.features.document.enumeration.ReturnDisposition;
import dev.roland.inventory_management_backend.features.document.enumeration.WarehouseOperationType;
import dev.roland.inventory_management_backend.features.document.mapper.DocumentLineMapper;
import dev.roland.inventory_management_backend.features.document.mapper.DocumentResponseMapper;
import dev.roland.inventory_management_backend.features.document.message.DocumentApiMessageKey;
import dev.roland.inventory_management_backend.features.document.service.DocumentService;
import dev.roland.inventory_management_backend.features.document.utils.DocumentWorkflowValues;
import dev.roland.inventory_management_backend.features.document_activity.DocumentActivityEvent;
import dev.roland.inventory_management_backend.features.document_line.DocumentLine;
import dev.roland.inventory_management_backend.features.document_line.service.DocumentLineService;
import dev.roland.inventory_management_backend.features.document_line_allocation.DocumentLineAllocation;
import dev.roland.inventory_management_backend.features.document_line_allocation.service.DocumentLineAllocationService;
import dev.roland.inventory_management_backend.features.document_relation.DocumentRelation;
import dev.roland.inventory_management_backend.features.document_relation.service.DocumentRelationService;
import dev.roland.inventory_management_backend.features.product.Product;
import dev.roland.inventory_management_backend.features.product.service.ProductService;
import dev.roland.inventory_management_backend.features.stock_movement.enumeration.StockMovementType;
import dev.roland.inventory_management_backend.features.stock_movement.service.StockMovementService;
import dev.roland.inventory_management_backend.features.user.User;
import dev.roland.inventory_management_backend.features.user.enumeration.UserRole;
import dev.roland.inventory_management_backend.features.warehouse.Warehouse;
import dev.roland.inventory_management_backend.features.warehouse.service.WarehouseService;
import lombok.RequiredArgsConstructor;

/** Creates linked records for warehouse operations. */
@Service
@RequiredArgsConstructor
public class WarehouseOperationRecordService {
  private final DocumentService documentService;
  private final DocumentLineService lineService;
  private final ProductService productService;
  private final WarehouseService warehouseService;
  private final DocumentRelationService relationService;
  private final DocumentLineAllocationService allocationService;
  private final StockMovementService stockMovementService;
  private final DocumentWorkflowAccessService accessService;
  private final DocumentWorkflowWriterService writer;
  private final DocumentResponseMapper responseMapper;
  private final WarehouseOperationSupportService warehouseOperationSupportService;
  private final WarehouseOperationPreviewService previewService;

  DocumentResponse createReturn(
      final WarehouseOperationRequest request,
      final Company company,
      final User user,
      final String key) {
    warehouseOperationSupportService.requireRole(user, UserRole.ADMIN, UserRole.MANAGER);
    warehouseOperationSupportService.requireReason(request.reason());
    warehouseOperationSupportService.requireKey(key);
    final Document sourceDocument = accessService.lock(request.sourceDocumentId(), company.getId());
    if (documentService.postingKeyExists(sourceDocument.getId(), key)) {
      return relationService
          .findAllBySourceDocumentIdOrTargetDocumentId(
              sourceDocument.getId(), sourceDocument.getId())
          .stream()
          .map(DocumentRelation::getTargetDocument)
          .filter(d -> d.getType() == DocumentType.RETURN)
          .max(java.util.Comparator.comparing(Document::getCreatedAt))
          .map(responseMapper::toResponse)
          .orElseThrow(accessService::notFound);
    }
    final WarehouseOperationPreview preview =
        previewService.preview(
            sourceDocument.getId(), WarehouseOperationType.RETURN_CREATE, request);
    if (!preview.valid()) throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_LINES);
    if (request.direction() == ReturnDirection.CUSTOMER_RETURN
            && sourceDocument.getType() != DocumentType.DELIVERY_NOTE
        || request.direction() == ReturnDirection.SUPPLIER_RETURN
            && sourceDocument.getType() != DocumentType.GOODS_RECEIPT)
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_TRANSITION);
    if (DocumentWorkflowValues.safe(request.lines()).stream()
        .anyMatch(
            line ->
                (request.direction() == ReturnDirection.SUPPLIER_RETURN)
                    != (line.disposition() == ReturnDisposition.SUPPLIER_RETURN)))
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_LINES);
    final Document returned =
        warehouseOperationSupportService.operationDocument(
            sourceDocument, DocumentType.RETURN, request.reason());
    returned.setWorkflowDirection(request.direction());
    for (var item : DocumentWorkflowValues.safe(request.lines())) {
      final DocumentLine source =
          warehouseOperationSupportService.line(item.sourceLineId(), company.getId());
      final BigDecimal quantity = DocumentWorkflowValues.nz(item.quantity());
      source.setReturnedQuantity(
          DocumentWorkflowValues.nz(source.getReturnedQuantity()).add(quantity));
      lineService.save(source);
      final DocumentLine line =
          warehouseOperationSupportService.cloneLine(returned, source, quantity);
      line.setSourceLine(source);
      line.setWorkflowDisposition(item.disposition());
      returned.getLines().add(lineService.save(line));
      final Warehouse originalWarehouse =
          sourceDocument.getType() == DocumentType.GOODS_RECEIPT
              ? sourceDocument.getTargetWarehouse()
              : sourceDocument.getSourceWarehouse();
      final Warehouse target =
          request.warehouseId() == null
              ? originalWarehouse
              : warehouseService.findEntityByIdAndCompanyId(request.warehouseId(), company.getId());
      final boolean quarantine =
          item.disposition() == ReturnDisposition.QUARANTINE
              || item.disposition() == ReturnDisposition.DAMAGED;
      final boolean supplierReturn =
          item.disposition() == ReturnDisposition.SUPPLIER_RETURN
              || request.direction() == ReturnDirection.SUPPLIER_RETURN;
      final Warehouse effectiveWarehouse =
          quarantine
              ? warehouseOperationSupportService.quarantineWarehouse(company.getId())
              : target;
      returned.setTargetWarehouse(effectiveWarehouse);
      returned.setTargetWarehouseCodeSnapshot(effectiveWarehouse.getCode());
      returned.setTargetWarehouseNameSnapshot(effectiveWarehouse.getName());
      if (DocumentWorkflowValues.safe(request.lines()).stream()
              .map(WarehouseOperationRequest.WarehouseOperationLine::disposition)
              .distinct()
              .count()
          == 1) returned.setWorkflowDisposition(item.disposition());
      writer.saveStockMovement(
          stockMovementService.postMovement(
              line,
              effectiveWarehouse,
              supplierReturn ? quantity.negate() : quantity,
              supplierReturn ? StockMovementType.RETURN_OUT : StockMovementType.RETURN_IN,
              user),
          item.reason() == null ? request.reason() : item.reason());
    }
    writer.complete(returned);
    final Document saved = documentService.save(returned);
    writer.relate(sourceDocument, saved, DocumentRelationType.RETURNS);
    writer.consumeKey(sourceDocument, key);
    writer.addActivity(saved, DocumentActivityEvent.POSTED, request.reason());
    return responseMapper.toResponse(saved);
  }

  DocumentResponse createCorrection(
      final Document original,
      final WarehouseOperationRequest request,
      final Company company,
      final User user,
      final String key) {
    warehouseOperationSupportService.requireRole(user, UserRole.ADMIN, UserRole.MANAGER);
    warehouseOperationSupportService.requireKey(key);
    warehouseOperationSupportService.requireReason(request.reason());
    final WarehouseOperationPreview preview =
        previewService.preview(original.getId(), WarehouseOperationType.CORRECTION, request);
    if (!preview.valid()) throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_LINES);
    final Document correction =
        warehouseOperationSupportService.operationDocument(
            original, DocumentType.CORRECTION, request.reason());
    for (var item : DocumentWorkflowValues.safe(request.lines())) {
      final DocumentLine source =
          warehouseOperationSupportService.line(item.sourceLineId(), company.getId());
      final BigDecimal corrected = DocumentWorkflowValues.nz(item.correctedQuantity());
      final BigDecimal oldQuantity = DocumentWorkflowValues.nz(source.getQuantity());
      final boolean inbound =
          original.getType() == DocumentType.GOODS_RECEIPT
              || original.getType() == DocumentType.TRANSFER_RECEIPT;
      final Warehouse primaryWarehouse =
          warehouseOperationSupportService.defaultCorrectionWarehouse(original);
      final Warehouse transferSecondary = original.getTransitWarehouse();
      final Long correctedProductId =
          item.correctedProductId() == null
              ? source.getProduct().getId()
              : item.correctedProductId();
      final Product correctedProduct =
          productService
              .findByIdAndCompanyId(correctedProductId, company.getId())
              .orElseThrow(() -> new NotFoundException(NotFoundMessageKey.PRODUCT));
      final Warehouse correctedPrimary =
          item.correctedWarehouseId() == null
              ? primaryWarehouse
              : warehouseService.findEntityByIdAndCompanyId(
                  item.correctedWarehouseId(), company.getId());
      final List<Warehouse> warehouses = new ArrayList<>();
      final List<BigDecimal> reversalDeltas = new ArrayList<>();
      final List<BigDecimal> correctedDeltas = new ArrayList<>();
      warehouses.add(primaryWarehouse);
      reversalDeltas.add(inbound ? oldQuantity.negate() : oldQuantity);
      correctedDeltas.add(inbound ? corrected : corrected.negate());
      if (original.getType() == DocumentType.TRANSFER_DELIVERY) {
        warehouses.add(transferSecondary);
        reversalDeltas.add(oldQuantity.negate());
        correctedDeltas.add(corrected);
      } else if (original.getType() == DocumentType.TRANSFER_RECEIPT) {
        warehouses.add(transferSecondary);
        reversalDeltas.add(oldQuantity);
        correctedDeltas.add(corrected.negate());
      }
      for (int i = 0; i < warehouses.size(); i++) {
        final BigDecimal reversalDelta = reversalDeltas.get(i);
        final DocumentLine reversalLine =
            warehouseOperationSupportService.cloneLine(correction, source, oldQuantity);
        correction.getLines().add(lineService.save(reversalLine));
        postCorrectionMovement(
            reversalLine, warehouses.get(i), reversalDelta, user, request.reason());
        final BigDecimal correctedDelta = correctedDeltas.get(i);
        final DocumentLine correctedLine =
            DocumentLineMapper.buildLine(
                correction,
                correctedProduct,
                correctedProduct.getUnit(),
                corrected,
                DocumentWorkflowValues.nz(source.getUnitPrice()));
        correctedLine.setSourceLine(source);
        correction.getLines().add(lineService.save(correctedLine));
        if (i == 0 && oldQuantity.signum() > 0) {
          final BigDecimal fulfillmentDelta = corrected.subtract(oldQuantity);
          for (DocumentLineAllocation allocation :
              allocationService.findAllByFulfillmentLineId(source.getId())) {
            final DocumentLineAllocation adjustment = new DocumentLineAllocation();
            adjustment.setFulfillmentLine(correctedLine);
            adjustment.setSourceOrderLine(allocation.getSourceOrderLine());
            adjustment.setQuantity(
                allocation
                    .getQuantity()
                    .multiply(fulfillmentDelta)
                    .divide(oldQuantity, 8, RoundingMode.HALF_UP));
            allocationService.save(adjustment);
          }
        }
        postCorrectionMovement(
            correctedLine,
            i == 0 ? correctedPrimary : warehouses.get(i),
            correctedDelta,
            user,
            request.reason());
      }
    }
    writer.complete(correction);
    final Document saved = documentService.save(correction);
    writer.relate(original, saved, DocumentRelationType.CORRECTS);
    writer.consumeKey(original, key);
    writer.addActivity(saved, DocumentActivityEvent.POSTED, request.reason());
    return responseMapper.toResponse(saved);
  }

  void postCorrectionMovement(
      final DocumentLine line,
      final Warehouse warehouse,
      final BigDecimal delta,
      final User user,
      final String reason) {
    if (warehouse == null || delta.signum() == 0) return;
    writer.saveStockMovement(
        stockMovementService.postMovement(
            line,
            warehouse,
            delta,
            delta.signum() > 0
                ? StockMovementType.ADJUSTMENT_INCREASE
                : StockMovementType.ADJUSTMENT_DECREASE,
            user),
        reason);
  }

  DocumentResponse releaseQuarantine(
      final Document returned,
      final WarehouseOperationRequest request,
      final Company company,
      final User user,
      final String key) {
    warehouseOperationSupportService.requireKey(key);
    warehouseOperationSupportService.requireReason(request.reason());
    warehouseOperationSupportService.requireRole(user, UserRole.ADMIN, UserRole.MANAGER);
    if (returned.getType() != DocumentType.RETURN || request.targetWarehouseId() == null)
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_TRANSITION);
    final Warehouse quarantine =
        warehouseOperationSupportService.quarantineWarehouse(company.getId());
    final Warehouse target =
        warehouseService.findEntityByIdAndCompanyId(request.targetWarehouseId(), company.getId());
    if (target.getType()
            == dev.roland.inventory_management_backend.features.warehouse.enumeration.WarehouseType
                .QUARANTINE
        || target.getType()
            == dev.roland.inventory_management_backend.features.warehouse.enumeration.WarehouseType
                .TRANSIT) throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_LINES);
    final Document release =
        warehouseOperationSupportService.operationDocument(
            returned, DocumentType.STOCK_ADJUSTMENT, request.reason());
    release.setSourceWarehouse(quarantine);
    release.setTargetWarehouse(target);
    for (var item : DocumentWorkflowValues.safe(request.lines())) {
      final DocumentLine returnedLine =
          warehouseOperationSupportService.line(item.sourceLineId(), company.getId());
      if (!returnedLine.getDocument().getId().equals(returned.getId())
          || !(returnedLine.getWorkflowDisposition() == ReturnDisposition.QUARANTINE
              || returnedLine.getWorkflowDisposition() == ReturnDisposition.DAMAGED))
        throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_LINES);
      final BigDecimal quantity = DocumentWorkflowValues.nz(item.quantity());
      final BigDecimal remaining =
          DocumentWorkflowValues.nz(returnedLine.getQuantity())
              .subtract(DocumentWorkflowValues.nz(returnedLine.getReleasedQuantity()));
      if (quantity.signum() <= 0 || quantity.compareTo(remaining) > 0)
        throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_LINES);
      final DocumentLine line =
          warehouseOperationSupportService.cloneLine(release, returnedLine, quantity);
      release.getLines().add(lineService.save(line));
      writer.saveStockMovement(
          stockMovementService.postMovement(
              line, quarantine, quantity.negate(), StockMovementType.RETURN_OUT, user),
          request.reason());
      writer.saveStockMovement(
          stockMovementService.postMovement(
              line, target, quantity, StockMovementType.RETURN_IN, user),
          request.reason());
      returnedLine.setReleasedQuantity(
          DocumentWorkflowValues.nz(returnedLine.getReleasedQuantity()).add(quantity));
      lineService.save(returnedLine);
    }
    writer.complete(release);
    final Document saved = documentService.save(release);
    writer.relate(returned, saved, DocumentRelationType.TRANSFER_STEP);
    writer.consumeKey(returned, key);
    writer.addActivity(saved, DocumentActivityEvent.POSTED, request.reason());
    return responseMapper.toResponse(saved);
  }

  DocumentResponse createReplacement(
      final WarehouseOperationRequest request,
      final Company company,
      final User user,
      final String key) {
    warehouseOperationSupportService.requireRole(user, UserRole.ADMIN, UserRole.MANAGER);
    warehouseOperationSupportService.requireKey(key);
    final Document returned = accessService.lock(request.returnDocumentId(), company.getId());
    final Document original = accessService.find(request.originalDeliveryId());
    if (documentService.postingKeyExists(returned.getId(), key))
      return relationService
          .findAllBySourceDocumentIdOrTargetDocumentId(returned.getId(), returned.getId())
          .stream()
          .map(DocumentRelation::getTargetDocument)
          .filter(d -> d.getType() == DocumentType.DELIVERY_NOTE && d.isNonBillable())
          .max(java.util.Comparator.comparing(Document::getCreatedAt))
          .map(responseMapper::toResponse)
          .orElseThrow(accessService::notFound);
    if (returned.getType() != DocumentType.RETURN
        || original.getType() != DocumentType.DELIVERY_NOTE)
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_TRANSITION);
    final Warehouse warehouse =
        warehouseService.findEntityByIdAndCompanyId(request.warehouseId(), company.getId());
    final WarehouseOperationPreview preview =
        previewService.preview(null, WarehouseOperationType.REPLACEMENT_DELIVERY, request);
    if (!preview.valid()) throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_LINES);
    final Document delivery =
        warehouseOperationSupportService.operationDocument(
            original, DocumentType.DELIVERY_NOTE, "Replacement delivery");
    delivery.setSourceWarehouse(warehouse);
    delivery.setNonBillable(true);
    delivery.setDocumentDate(request.date() == null ? LocalDate.now() : request.date());
    for (var item : DocumentWorkflowValues.safe(request.lines())) {
      final DocumentLine source =
          warehouseOperationSupportService.line(item.sourceLineId(), company.getId());
      final BigDecimal quantity = DocumentWorkflowValues.nz(item.quantity());
      final Product product =
          item.productId() == null
              ? source.getProduct()
              : productService
                  .findByIdAndCompanyId(item.productId(), company.getId())
                  .orElseThrow(() -> new NotFoundException(NotFoundMessageKey.PRODUCT));
      final DocumentLine line =
          DocumentLineMapper.buildLine(
              delivery, product, product.getUnit(), quantity, BigDecimal.ZERO);
      line.setSourceLine(source);
      delivery.getLines().add(lineService.save(line));
      source.setReplacedQuantity(
          DocumentWorkflowValues.nz(source.getReplacedQuantity()).add(quantity));
      lineService.save(source);
      writer.saveStockMovement(
          stockMovementService.postMovement(
              line, warehouse, quantity.negate(), StockMovementType.SALE_OUTBOUND, user),
          "replacement delivery");
    }
    writer.complete(delivery);
    final Document saved = documentService.save(delivery);
    writer.relate(returned, saved, DocumentRelationType.REPLACES);
    writer.relate(original, saved, DocumentRelationType.REPLACES);
    writer.consumeKey(returned, key);
    writer.addActivity(saved, DocumentActivityEvent.POSTED, "non-billable replacement");
    return responseMapper.toResponse(saved);
  }

  DocumentResponse postCount(
      final Document count,
      final WarehouseOperationRequest request,
      final Company company,
      final User user,
      final String key) {
    warehouseOperationSupportService.requireKey(key);
    warehouseOperationSupportService.requireRole(user, UserRole.ADMIN, UserRole.MANAGER);
    if (!Boolean.TRUE.equals(request.confirmed()))
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_TRANSITION);
    final WarehouseOperationPreview preview =
        previewService.preview(
            count.getId(), WarehouseOperationType.STOCK_COUNT_RECONCILE, request);
    if (!preview.valid()
        || request.calculationVersion() == null
        || !request.calculationVersion().equals(preview.calculationVersion()))
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_TRANSITION);
    final Document adjustment =
        warehouseOperationSupportService.operationDocument(
            count, DocumentType.STOCK_ADJUSTMENT, request.reason());
    for (int i = 0; i < request.lines().size(); i++) {
      final var item = request.lines().get(i);
      final DocumentLine countLine =
          warehouseOperationSupportService.line(item.sourceLineId(), company.getId());
      final var result = preview.lines().get(i);
      countLine.setExpectedQuantity(result.expectedAtCount());
      countLine.setCountedQuantity(result.countedQuantity());
      countLine.setVariance(result.variance());
      countLine.setVarianceReason(item.varianceReason());
      lineService.save(countLine);
      final BigDecimal delta = result.adjustmentQuantity();
      if (delta.signum() != 0) {
        final DocumentLine line =
            warehouseOperationSupportService.cloneLine(adjustment, countLine, delta.abs());
        line.setSourceLine(countLine);
        adjustment.getLines().add(lineService.save(line));
        writer.saveStockMovement(
            stockMovementService.postMovement(
                line,
                count.getSourceWarehouse(),
                delta,
                delta.signum() > 0
                    ? StockMovementType.ADJUSTMENT_INCREASE
                    : StockMovementType.ADJUSTMENT_DECREASE,
                user),
            item.varianceReason());
      }
    }
    count.setCalculationVersion(preview.calculationVersion());
    writer.complete(count);
    writer.complete(adjustment);
    final Document saved = documentService.save(count);
    final Document adjustmentSaved = documentService.save(adjustment);
    writer.relate(saved, adjustmentSaved, DocumentRelationType.FULFILLS);
    writer.consumeKey(saved, key);
    writer.addActivity(saved, DocumentActivityEvent.POSTED, "stock count reconciled");
    return responseMapper.toResponse(saved);
  }
}
