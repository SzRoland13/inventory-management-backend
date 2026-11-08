package dev.roland.inventory_management_backend.features.document.facade.workflow;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.common.exception.ApiException;
import dev.roland.inventory_management_backend.common.exception.NotFoundException;
import dev.roland.inventory_management_backend.common.message.NotFoundMessageKey;
import dev.roland.inventory_management_backend.common.service.CurrentCompanyProvider;
import dev.roland.inventory_management_backend.features.company.Company;
import dev.roland.inventory_management_backend.features.document.Document;
import dev.roland.inventory_management_backend.features.document.DocumentPostingKey;
import dev.roland.inventory_management_backend.features.document.dto.DocumentResponse;
import dev.roland.inventory_management_backend.features.document.dto.StockCountItemsRequest;
import dev.roland.inventory_management_backend.features.document.dto.WarehouseOperationPreview;
import dev.roland.inventory_management_backend.features.document.dto.WarehouseOperationRequest;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentCategory;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentRelationType;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentStatus;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentType;
import dev.roland.inventory_management_backend.features.document.enumeration.WarehouseOperationType;
import dev.roland.inventory_management_backend.features.document.mapper.DocumentLineMapper;
import dev.roland.inventory_management_backend.features.document.mapper.DocumentResponseMapper;
import dev.roland.inventory_management_backend.features.document.message.DocumentApiMessageKey;
import dev.roland.inventory_management_backend.features.document.service.DocumentService;
import dev.roland.inventory_management_backend.features.document.utils.DocumentWorkflowValues;
import dev.roland.inventory_management_backend.features.document_activity.DocumentActivityEvent;
import dev.roland.inventory_management_backend.features.document_line.DocumentLine;
import dev.roland.inventory_management_backend.features.document_line.service.DocumentLineService;
import dev.roland.inventory_management_backend.features.product.Product;
import dev.roland.inventory_management_backend.features.product.enumeration.ProductStatus;
import dev.roland.inventory_management_backend.features.product.service.ProductService;
import dev.roland.inventory_management_backend.features.stock_movement.enumeration.StockMovementType;
import dev.roland.inventory_management_backend.features.stock_movement.service.StockMovementService;
import dev.roland.inventory_management_backend.features.user.User;
import dev.roland.inventory_management_backend.features.user.enumeration.UserRole;
import dev.roland.inventory_management_backend.features.warehouse.Warehouse;
import dev.roland.inventory_management_backend.features.warehouse.enumeration.WarehouseStatus;
import dev.roland.inventory_management_backend.features.warehouse.enumeration.WarehouseType;
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
  private final WarehouseOperationSupportService warehouseOperationSupportService;
  private final WarehouseOperationPreviewService previewService;
  private final WarehouseOperationRecordService recordService;

  /**
   * Adds products to a draft stock count with their expected and initial counted quantities.
   *
   * @param id the stock count identifier
   * @param request the requested products and current document version
   * @return the updated stock count
   * @throws ApiException when the request is invalid or the operation is not permitted
   */
  @Transactional
  public DocumentResponse addStockCountItems(final Long id, final StockCountItemsRequest request) {
    if (request == null || request.productIds() == null || request.productIds().isEmpty()) {
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_LINES);
    }
    final Company company = companyProvider.getCurrentCompany();
    final User user = accessService.currentActor();
    warehouseOperationSupportService.requireRole(user, UserRole.ADMIN, UserRole.MANAGER);
    final Document count = accessService.lock(id, company.getId());
    if (count.getType() != DocumentType.STOCK_COUNT
        || count.getStatus() != DocumentStatus.DRAFT
        || count.getCountedAt() == null
        || count.getSourceWarehouse() == null) {
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_TRANSITION);
    }
    warehouseOperationSupportService.requireVersion(count, request.version());
    if (count.getLines() == null) {
      count.setLines(new ArrayList<>());
    }

    final Set<Long> existingProductIds = new HashSet<>();
    DocumentWorkflowValues.safe(count.getLines())
        .forEach(line -> existingProductIds.add(line.getProduct().getId()));
    final Set<Long> requestedProductIds = new HashSet<>();
    final Map<Long, BigDecimal> expectedByProduct = new HashMap<>();
    stockMovementService
        .findEffectiveStockThrough(
            company.getId(), count.getSourceWarehouse().getId(), count.getCountedAt())
        .forEach(
            snapshot -> expectedByProduct.put(snapshot.getProductId(), snapshot.getQuantity()));

    for (final Long productId : request.productIds()) {
      if (!requestedProductIds.add(productId) || existingProductIds.contains(productId)) {
        throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_LINES);
      }
      final Product product =
          productService
              .findByIdAndCompanyId(productId, company.getId())
              .orElseThrow(() -> new NotFoundException(NotFoundMessageKey.PRODUCT));
      if (product.getStatus() != ProductStatus.ACTIVE) {
        throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_LINES);
      }
      final var countUnit =
          product.getSecondaryUnit() == null ? product.getUnit() : product.getSecondaryUnit();
      final DocumentLine line =
          DocumentLineMapper.buildLine(count, product, countUnit, BigDecimal.ZERO, BigDecimal.ZERO);
      line.setConversionFactorSnapshot(BigDecimal.ONE);
      line.setExpectedQuantity(expectedByProduct.getOrDefault(productId, BigDecimal.ZERO));
      line.setCountedQuantity(BigDecimal.ZERO);
      count.getLines().add(lineService.save(line));
      existingProductIds.add(productId);
    }
    writer.addActivity(count, DocumentActivityEvent.EDITED, "stock count products added");
    return responseMapper.toResponse(documentService.save(count));
  }

  /**
   * Applies a warehouse operation to a document.
   *
   * @param id document identifier, or null when creating a document
   * @param operation warehouse operation to apply
   * @param request operation details
   * @param suppliedKey optional idempotency key
   * @param suppliedVersion optional expected document version
   * @return the resulting document response
   * @throws ApiException when the operation cannot be completed
   */
  @Transactional
  public DocumentResponse apply(
      final Long id,
      final WarehouseOperationType operation,
      final WarehouseOperationRequest request,
      final String suppliedKey,
      final String suppliedVersion) {
    final Company company = companyProvider.getCurrentCompany();
    final User user = accessService.currentActor();
    if (request == null) {
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_LINES);
    }
    final String key =
        suppliedKey == null || suppliedKey.isBlank() ? request.idempotencyKey() : suppliedKey;
    final String version =
        suppliedVersion == null || suppliedVersion.isBlank() ? request.version() : suppliedVersion;
    Document root = id == null ? null : accessService.lock(id, company.getId());
    DocumentResponse result = null;
    if (root != null && key != null && documentService.postingKeyExists(root.getId(), key)) {
      final Document resultDocument =
          documentService
              .findPostingKey(root.getId(), key)
              .map(DocumentPostingKey::getResultDocument)
              .orElse(null);
      result = responseMapper.toResponse(resultDocument == null ? root : resultDocument);
    }
    if (result == null) {
      if (root != null
          && Set.of(
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
            warehouseService.findEntityByIdAndCompanyId(
                request.sourceWarehouseId(), company.getId());
        final Warehouse destination =
            warehouseService.findEntityByIdAndCompanyId(
                request.destinationWarehouseId(), company.getId());
        final Warehouse transit =
            request.transitWarehouseId() == null
                ? null
                : warehouseService.findEntityByIdAndCompanyId(
                    request.transitWarehouseId(), company.getId());
        if (source.getStatus() != WarehouseStatus.ACTIVE
            || destination.getStatus() != WarehouseStatus.ACTIVE
            || (transit != null && transit.getStatus() != WarehouseStatus.ACTIVE)) {
          throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_LINES);
        }
        if (source.getId().equals(destination.getId())
            || (transit != null
                && (transit.getId().equals(source.getId())
                    || transit.getId().equals(destination.getId())))) {
          throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_LINES);
        }
        if (source.getType() == WarehouseType.TRANSIT
            || source.getType() == WarehouseType.QUARANTINE
            || destination.getType() == WarehouseType.TRANSIT
            || destination.getType() == WarehouseType.QUARANTINE
            || (transit != null && transit.getType() != WarehouseType.TRANSIT)
            || DocumentWorkflowValues.safe(request.lines()).stream()
                .anyMatch(
                    l ->
                        l.productId() == null
                            || DocumentWorkflowValues.nz(l.quantity()).signum() <= 0)) {
          throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_LINES);
        }
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
          final var unit = DocumentWorkflowValues.configuredUnit(product, item.unitId());
          final DocumentLine line =
              DocumentLineMapper.buildLine(
                  root, product, unit, DocumentWorkflowValues.nz(item.quantity()), BigDecimal.ZERO);
          line.setConversionFactorSnapshot(DocumentWorkflowValues.expectedFactor(product, unit));
          root.getLines().add(lineService.save(line));
        }
        writer.addActivity(root, DocumentActivityEvent.CREATED, "transfer draft");
        result = responseMapper.toResponse(documentService.save(root));
      }
      if (operation == WarehouseOperationType.STOCK_COUNT_CREATE) {
        warehouseOperationSupportService.requireRole(user, UserRole.ADMIN, UserRole.MANAGER);
        if (request.countedAt() == null) {
          throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_LINES);
        }
        final Warehouse warehouse =
            warehouseService.findEntityByIdAndCompanyId(request.warehouseId(), company.getId());
        if (warehouse.getStatus() != WarehouseStatus.ACTIVE
            || warehouse.getType() == WarehouseType.TRANSIT
            || warehouse.getType() == WarehouseType.QUARANTINE) {
          throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_LINES);
        }
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
        writer.addActivity(root, DocumentActivityEvent.CREATED, "stock count");
        result = responseMapper.toResponse(documentService.save(root));
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
        if (!preview.valid()) {
          throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_LINES);
        }
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
          final BigDecimal stockQuantity =
              quantity.multiply(DocumentWorkflowValues.operationFactor(source, item.unitId()));
          final BigDecimal sourceQuantity =
              DocumentWorkflowValues.entryQuantity(source, stockQuantity);
          final DocumentLine childLine =
              warehouseOperationSupportService.cloneLine(child, source, quantity);
          DocumentWorkflowValues.applyOperationUnit(childLine, source, item.unitId());
          childLine.setSourceLine(source);
          child.getLines().add(lineService.save(childLine));
          if (operation == WarehouseOperationType.TRANSFER_DISPATCH) {
            writer.saveStockMovement(
                stockMovementService.postMovement(
                    childLine,
                    root.getSourceWarehouse(),
                    stockQuantity.negate(),
                    StockMovementType.TRANSFER_OUT,
                    user,
                    request.effectiveAt()),
                "dispatch");
            final Warehouse transit =
                root.getTransitWarehouse() == null
                    ? root.getTargetWarehouse()
                    : root.getTransitWarehouse();
            writer.saveStockMovement(
                stockMovementService.postMovement(
                    childLine,
                    transit,
                    stockQuantity,
                    StockMovementType.TRANSFER_IN,
                    user,
                    request.effectiveAt()),
                "dispatch");
            source.setFulfilledQuantity(
                DocumentWorkflowValues.nz(source.getFulfilledQuantity()).add(sourceQuantity));
            if (root.getTransitWarehouse() != null) {
              source.setInTransitQuantity(
                  DocumentWorkflowValues.nz(source.getInTransitQuantity()).add(sourceQuantity));
            }
          } else if (operation == WarehouseOperationType.TRANSFER_RECEIPT) {
            final Warehouse transit =
                root.getTransitWarehouse() == null
                    ? root.getSourceWarehouse()
                    : root.getTransitWarehouse();
            if (root.getTransitWarehouse() != null) {
              writer.saveStockMovement(
                  stockMovementService.postMovement(
                      childLine,
                      transit,
                      stockQuantity.negate(),
                      StockMovementType.TRANSFER_OUT,
                      user,
                      request.effectiveAt()),
                  "receipt");
            }
            writer.saveStockMovement(
                stockMovementService.postMovement(
                    childLine,
                    root.getTargetWarehouse(),
                    stockQuantity,
                    StockMovementType.TRANSFER_IN,
                    user,
                    request.effectiveAt()),
                "receipt");
            source.setInTransitQuantity(
                DocumentWorkflowValues.nz(source.getInTransitQuantity()).subtract(sourceQuantity));
          } else {
            final Warehouse transit = root.getTransitWarehouse();
            final WarehouseOperationType resolution =
                request.operation() == null ? WarehouseOperationType.LOSS : request.operation();
            writer.saveStockMovement(
                stockMovementService.postMovement(
                    childLine,
                    transit,
                    stockQuantity.negate(),
                    StockMovementType.TRANSFER_OUT,
                    user,
                    request.effectiveAt()),
                request.reason());
            if (resolution == WarehouseOperationType.REVERSAL) {
              writer.saveStockMovement(
                  stockMovementService.postMovement(
                      childLine,
                      root.getSourceWarehouse(),
                      stockQuantity,
                      StockMovementType.TRANSFER_IN,
                      user,
                      request.effectiveAt()),
                  request.reason());
            }
            if (resolution == WarehouseOperationType.DAMAGE) {
              writer.saveStockMovement(
                  stockMovementService.postMovement(
                      childLine,
                      warehouseOperationSupportService.quarantineWarehouse(company.getId()),
                      stockQuantity,
                      StockMovementType.RETURN_IN,
                      user,
                      request.effectiveAt()),
                  request.reason());
            }
            source.setInTransitQuantity(
                DocumentWorkflowValues.nz(source.getInTransitQuantity()).subtract(sourceQuantity));
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
                        && DocumentWorkflowValues.nz(l.getInTransitQuantity()).signum() == 0)) {
          writer.complete(root);
        } else {
          root.setStatus(DocumentStatus.PARTIALLY_COMPLETED);
        }
        documentService.flush();
        result = responseMapper.toResponse(documentService.save(root));
      }
      if (operation == WarehouseOperationType.RETURN_CREATE) {
        result = recordService.createReturn(request, company, user, key);
      }
      if (operation == WarehouseOperationType.QUARANTINE_RELEASE) {
        assert root != null;
        result = recordService.releaseQuarantine(root, request, company, user, key);
      }
      if (operation == WarehouseOperationType.REPLACEMENT_DELIVERY) {
        result = recordService.createReplacement(request, company, user, key);
      }
      if (operation == WarehouseOperationType.CORRECTION) {
        assert root != null;
        result = recordService.createCorrection(root, request, company, user, key);
      }
      if (operation == WarehouseOperationType.STOCK_COUNT_POST) {
        assert root != null;
        result = recordService.postCount(root, request, company, user, key);
      }
      if (result == null) {
        throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_TRANSITION);
      }
    }
    return result;
  }
}
