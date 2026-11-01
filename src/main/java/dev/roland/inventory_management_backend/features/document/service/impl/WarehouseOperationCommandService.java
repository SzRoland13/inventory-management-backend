package dev.roland.inventory_management_backend.features.document.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.common.exception.ApiException;
import dev.roland.inventory_management_backend.common.exception.NotFoundException;
import dev.roland.inventory_management_backend.common.message.NotFoundMessageKey;
import dev.roland.inventory_management_backend.common.service.CurrentCompanyProvider;
import dev.roland.inventory_management_backend.features.company.Company;
import dev.roland.inventory_management_backend.features.document.Document;
import dev.roland.inventory_management_backend.features.document.dto.DocumentResponse;
import dev.roland.inventory_management_backend.features.document.dto.WarehouseOperationPreview;
import dev.roland.inventory_management_backend.features.document.dto.WarehouseOperationRequest;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentCategory;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentRelationType;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentStatus;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentType;
import dev.roland.inventory_management_backend.features.document.enumeration.WarehouseOperationType;
import dev.roland.inventory_management_backend.features.document.mapper.DocumentResponseMapper;
import dev.roland.inventory_management_backend.features.document.message.DocumentApiMessageKey;
import dev.roland.inventory_management_backend.features.document.service.DocumentService;
import dev.roland.inventory_management_backend.features.document.utils.DocumentWorkflowValues;
import dev.roland.inventory_management_backend.features.document_activity.DocumentActivityEvent;
import dev.roland.inventory_management_backend.features.document_line.DocumentLine;
import dev.roland.inventory_management_backend.features.document_line.service.DocumentLineService;
import dev.roland.inventory_management_backend.features.product.Product;
import dev.roland.inventory_management_backend.features.product.service.ProductService;
import dev.roland.inventory_management_backend.features.stock_movement.StockMovement;
import dev.roland.inventory_management_backend.features.stock_movement.enumeration.StockMovementType;
import dev.roland.inventory_management_backend.features.stock_movement.service.StockMovementService;
import dev.roland.inventory_management_backend.features.user.User;
import dev.roland.inventory_management_backend.features.user.enumeration.UserRole;
import dev.roland.inventory_management_backend.features.warehouse.Warehouse;
import dev.roland.inventory_management_backend.features.warehouse.service.WarehouseService;
import lombok.RequiredArgsConstructor;

/** Applies warehouse operation commands after preview validation. */
@Service
@RequiredArgsConstructor
public class WarehouseOperationCommandService {
  private final DocumentService documentService;
  private final DocumentLineService lineService;
  private final ProductService productService;
  private final WarehouseService warehouseService;
  private final CurrentCompanyProvider companyProvider;
  private final StockMovementService stockMovementService;
  private final DocumentWorkflowAccessService accessService;
  private final DocumentWorkflowWriterService writer;
  private final DocumentResponseMapper responseMapper;
  private final DocumentDraftWorkflowService draftWorkflowService;
  private final WarehouseOperationSupportService warehouseOperationSupportService;
  private final WarehouseOperationPreviewService previewService;

  private final WarehouseOperationRecordService recordService;

  @Transactional
  public DocumentResponse apply(
      final Long id,
      final WarehouseOperationType operation,
      final WarehouseOperationRequest request,
      final String suppliedKey,
      final String suppliedVersion) {
    final Company company = companyProvider.getCurrentCompany();
    final User user = accessService.currentActor();
    if (request == null) throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_LINES);
    final String key =
        suppliedKey == null || suppliedKey.isBlank() ? request.idempotencyKey() : suppliedKey;
    final String version =
        suppliedVersion == null || suppliedVersion.isBlank() ? request.version() : suppliedVersion;
    Document root = id == null ? null : accessService.lock(id, company.getId());
    if (root != null && key != null && documentService.postingKeyExists(root.getId(), key))
      return responseMapper.toResponse(root);
    if (root != null
        && java.util.Set.of(
                WarehouseOperationType.TRANSFER_DISPATCH,
                WarehouseOperationType.TRANSFER_RECEIPT,
                WarehouseOperationType.TRANSFER_RESOLVE,
                WarehouseOperationType.QUARANTINE_RELEASE,
                WarehouseOperationType.STOCK_COUNT_POST,
                WarehouseOperationType.CORRECTION)
            .contains(operation)) {
      final Document versionedRoot = root;
      accessService.checkVersion(
          versionedRoot, version, () -> responseMapper.toResponse(versionedRoot));
    }
    if (operation == WarehouseOperationType.TRANSFER_DRAFT) {
      warehouseOperationSupportService.requireRole(user, UserRole.ADMIN, UserRole.MANAGER);
      final Warehouse source =
          warehouseService.findEntityByIdAndCompanyId(request.sourceWarehouseId(), company.getId());
      final Warehouse destination =
          warehouseService.findEntityByIdAndCompanyId(
              request.destinationWarehouseId(), company.getId());
      final Warehouse transit =
          request.transitWarehouseId() == null
              ? null
              : warehouseService.findEntityByIdAndCompanyId(
                  request.transitWarehouseId(), company.getId());
      if (source.getStatus()
              != dev.roland.inventory_management_backend.features.warehouse.enumeration
                  .WarehouseStatus.ACTIVE
          || destination.getStatus()
              != dev.roland.inventory_management_backend.features.warehouse.enumeration
                  .WarehouseStatus.ACTIVE
          || (transit != null
              && transit.getStatus()
                  != dev.roland.inventory_management_backend.features.warehouse.enumeration
                      .WarehouseStatus.ACTIVE))
        throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_LINES);
      if (source.getId().equals(destination.getId())
          || (transit != null
              && (transit.getId().equals(source.getId())
                  || transit.getId().equals(destination.getId()))))
        throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_LINES);
      if (source.getType()
              == dev.roland.inventory_management_backend.features.warehouse.enumeration
                  .WarehouseType.TRANSIT
          || source.getType()
              == dev.roland.inventory_management_backend.features.warehouse.enumeration
                  .WarehouseType.QUARANTINE
          || destination.getType()
              == dev.roland.inventory_management_backend.features.warehouse.enumeration
                  .WarehouseType.TRANSIT
          || destination.getType()
              == dev.roland.inventory_management_backend.features.warehouse.enumeration
                  .WarehouseType.QUARANTINE
          || (transit != null
              && transit.getType()
                  != dev.roland.inventory_management_backend.features.warehouse.enumeration
                      .WarehouseType.TRANSIT)
          || DocumentWorkflowValues.safe(request.lines()).stream()
              .anyMatch(
                  l ->
                      l.productId() == null
                          || DocumentWorkflowValues.nz(l.quantity()).signum() <= 0))
        throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_LINES);
      root = new Document();
      root.setCompany(company);
      root.setType(DocumentType.TRANSFER_REQUEST);
      root.setCategory(DocumentCategory.INTERNAL);
      root.setStatus(DocumentStatus.DRAFT);
      root.setDocumentDate(request.date() == null ? LocalDate.now() : request.date());
      root.setCreatedByUser(user);
      root.setSourceWarehouse(source);
      root.setTargetWarehouse(destination);
      root.setTransitWarehouse(transit);
      root.setLines(new ArrayList<>());
      root = documentService.save(root);
      for (var item : DocumentWorkflowValues.safe(request.lines())) {
        final Product product =
            productService
                .findByIdAndCompanyId(item.productId(), company.getId())
                .orElseThrow(() -> new NotFoundException(NotFoundMessageKey.PRODUCT));
        final DocumentLine line =
            draftWorkflowService.buildLine(
                root,
                product,
                product.getUnit(),
                DocumentWorkflowValues.nz(item.quantity()),
                BigDecimal.ZERO);
        root.getLines().add(lineService.save(line));
      }
      writer.addActivity(root, DocumentActivityEvent.CREATED, "transfer draft");
      return responseMapper.toResponse(documentService.save(root));
    }
    if (operation == WarehouseOperationType.STOCK_COUNT_CREATE) {
      warehouseOperationSupportService.requireRole(user, UserRole.ADMIN, UserRole.MANAGER);
      if (request.countedAt() == null || request.lines() == null || request.lines().isEmpty())
        throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_LINES);
      final Warehouse warehouse =
          warehouseService.findEntityByIdAndCompanyId(request.warehouseId(), company.getId());
      if (warehouse.getStatus()
              != dev.roland.inventory_management_backend.features.warehouse.enumeration
                  .WarehouseStatus.ACTIVE
          || warehouse.getType()
              == dev.roland.inventory_management_backend.features.warehouse.enumeration
                  .WarehouseType.TRANSIT
          || warehouse.getType()
              == dev.roland.inventory_management_backend.features.warehouse.enumeration
                  .WarehouseType.QUARANTINE)
        throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_LINES);
      root = new Document();
      root.setCompany(company);
      root.setType(DocumentType.STOCK_COUNT);
      root.setCategory(DocumentCategory.INTERNAL);
      root.setStatus(DocumentStatus.DRAFT);
      root.setDocumentDate(request.countedAt().toLocalDate());
      root.setCountedAt(request.countedAt());
      root.setSourceWarehouse(warehouse);
      root.setWorkflowReason(request.reason());
      root.setCreatedByUser(user);
      root.setLines(new ArrayList<>());
      root = documentService.save(root);
      for (var item : DocumentWorkflowValues.safe(request.lines())) {
        final Product product =
            productService
                .findByIdAndCompanyId(item.productId(), company.getId())
                .orElseThrow(() -> new NotFoundException(NotFoundMessageKey.PRODUCT));
        final DocumentLine line =
            draftWorkflowService.buildLine(
                root, product, product.getUnit(), BigDecimal.ZERO, BigDecimal.ZERO);
        line.setCountedQuantity(DocumentWorkflowValues.nz(item.countedQuantity()));
        line.setExpectedQuantity(
            stockMovementService
                .findEffectiveThrough(product.getId(), warehouse.getId(), root.getCountedAt())
                .stream()
                .map(StockMovement::getQuantityChange)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        root.getLines().add(lineService.save(line));
      }
      writer.addActivity(root, DocumentActivityEvent.CREATED, "stock count");
      return responseMapper.toResponse(documentService.save(root));
    }
    if (operation == WarehouseOperationType.TRANSFER_DISPATCH
        || operation == WarehouseOperationType.TRANSFER_RECEIPT
        || operation == WarehouseOperationType.TRANSFER_RESOLVE) {
      warehouseOperationSupportService.requireKey(key);
      warehouseOperationSupportService.requireRole(user, UserRole.ADMIN, UserRole.MANAGER);
      if (root == null || root.getType() != DocumentType.TRANSFER_REQUEST) {
        throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_TRANSITION);
      }
      final WarehouseOperationType previewKind =
          operation == WarehouseOperationType.TRANSFER_RESOLVE
              ? WarehouseOperationType.TRANSFER_RESOLUTION
              : WarehouseOperationType.TRANSFER_PREVIEW;
      warehouseOperationSupportService.requireVersion(root, version);
      final WarehouseOperationRequest previewRequest =
          warehouseOperationSupportService.withOperation(
              request,
              operation == WarehouseOperationType.TRANSFER_RECEIPT
                  ? WarehouseOperationType.RECEIPT
                  : request.operation());
      final WarehouseOperationPreview preview =
          previewService.preview(root.getId(), previewKind, previewRequest);
      if (!preview.valid()) throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_LINES);
      writer.allocateNumber(root);
      Document child =
          warehouseOperationSupportService.operationDocument(
              root,
              operation == WarehouseOperationType.TRANSFER_RECEIPT
                  ? DocumentType.TRANSFER_RECEIPT
                  : DocumentType.TRANSFER_DELIVERY,
              request.reason());
      for (var item : DocumentWorkflowValues.safe(request.lines())) {
        final DocumentLine source =
            warehouseOperationSupportService.line(item.sourceLineId(), company.getId());
        final BigDecimal quantity = DocumentWorkflowValues.nz(item.quantity());
        final DocumentLine childLine =
            warehouseOperationSupportService.cloneLine(child, source, quantity);
        childLine.setSourceLine(source);
        child.getLines().add(lineService.save(childLine));
        if (operation == WarehouseOperationType.TRANSFER_DISPATCH) {
          writer.saveStockMovement(
              stockMovementService.postMovement(
                  childLine,
                  root.getSourceWarehouse(),
                  quantity.negate(),
                  StockMovementType.TRANSFER_OUT,
                  user),
              "dispatch");
          final Warehouse transit =
              root.getTransitWarehouse() == null
                  ? root.getTargetWarehouse()
                  : root.getTransitWarehouse();
          writer.saveStockMovement(
              stockMovementService.postMovement(
                  childLine, transit, quantity, StockMovementType.TRANSFER_IN, user),
              "dispatch");
          source.setFulfilledQuantity(
              DocumentWorkflowValues.nz(source.getFulfilledQuantity()).add(quantity));
          if (root.getTransitWarehouse() != null)
            source.setInTransitQuantity(
                DocumentWorkflowValues.nz(source.getInTransitQuantity()).add(quantity));
        } else if (operation == WarehouseOperationType.TRANSFER_RECEIPT) {
          final Warehouse transit =
              root.getTransitWarehouse() == null
                  ? root.getSourceWarehouse()
                  : root.getTransitWarehouse();
          if (root.getTransitWarehouse() != null)
            writer.saveStockMovement(
                stockMovementService.postMovement(
                    childLine, transit, quantity.negate(), StockMovementType.TRANSFER_OUT, user),
                "receipt");
          writer.saveStockMovement(
              stockMovementService.postMovement(
                  childLine,
                  root.getTargetWarehouse(),
                  quantity,
                  StockMovementType.TRANSFER_IN,
                  user),
              "receipt");
          source.setInTransitQuantity(
              DocumentWorkflowValues.nz(source.getInTransitQuantity()).subtract(quantity));
        } else {
          final Warehouse transit = root.getTransitWarehouse();
          final WarehouseOperationType resolution =
              request.operation() == null ? WarehouseOperationType.LOSS : request.operation();
          writer.saveStockMovement(
              stockMovementService.postMovement(
                  childLine, transit, quantity.negate(), StockMovementType.TRANSFER_OUT, user),
              request.reason());
          if (resolution == WarehouseOperationType.REVERSAL)
            writer.saveStockMovement(
                stockMovementService.postMovement(
                    childLine,
                    root.getSourceWarehouse(),
                    quantity,
                    StockMovementType.TRANSFER_IN,
                    user),
                request.reason());
          if (resolution == WarehouseOperationType.DAMAGE)
            writer.saveStockMovement(
                stockMovementService.postMovement(
                    childLine,
                    warehouseOperationSupportService.quarantineWarehouse(company.getId()),
                    quantity,
                    StockMovementType.RETURN_IN,
                    user),
                request.reason());
          source.setInTransitQuantity(
              DocumentWorkflowValues.nz(source.getInTransitQuantity()).subtract(quantity));
        }
        lineService.save(source);
      }
      writer.complete(child);
      child = documentService.save(child);
      writer.relate(root, child, DocumentRelationType.TRANSFER_STEP);
      writer.consumeKey(root, key);
      if (DocumentWorkflowValues.safe(root.getLines()).stream()
          .allMatch(
              l ->
                  DocumentWorkflowValues.nz(l.getFulfilledQuantity())
                              .compareTo(DocumentWorkflowValues.nz(l.getQuantity()))
                          >= 0
                      && DocumentWorkflowValues.nz(l.getInTransitQuantity()).signum() == 0))
        writer.complete(root);
      else root.setStatus(DocumentStatus.PARTIALLY_COMPLETED);
      documentService.flush();
      return responseMapper.toResponse(documentService.save(root));
    }
    if (operation == WarehouseOperationType.RETURN_CREATE)
      return recordService.createReturn(request, company, user, key);
    if (operation == WarehouseOperationType.QUARANTINE_RELEASE) {
      assert root != null;
      return recordService.releaseQuarantine(root, request, company, user, key);
    }
    if (operation == WarehouseOperationType.REPLACEMENT_DELIVERY)
      return recordService.createReplacement(request, company, user, key);
    if (operation == WarehouseOperationType.CORRECTION) {
      assert root != null;
      return recordService.createCorrection(root, request, company, user, key);
    }
    if (operation == WarehouseOperationType.STOCK_COUNT_POST)
      return recordService.postCount(root, request, company, user, key);
    throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_TRANSITION);
  }
}
