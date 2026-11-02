package dev.roland.inventory_management_backend.features.document.facade.workflow;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.common.exception.ApiException;
import dev.roland.inventory_management_backend.common.exception.NotFoundException;
import dev.roland.inventory_management_backend.common.message.NotFoundMessageKey;
import dev.roland.inventory_management_backend.common.service.CurrentCompanyProvider;
import dev.roland.inventory_management_backend.features.company.Company;
import dev.roland.inventory_management_backend.features.document.Document;
import dev.roland.inventory_management_backend.features.document.DocumentExchangeRate;
import dev.roland.inventory_management_backend.features.document.dto.CloseRemaining;
import dev.roland.inventory_management_backend.features.document.dto.DocumentResponse;
import dev.roland.inventory_management_backend.features.document.dto.FulfillmentDraft;
import dev.roland.inventory_management_backend.features.document.dto.FulfillmentLine;
import dev.roland.inventory_management_backend.features.document.dto.FulfillmentPreview;
import dev.roland.inventory_management_backend.features.document.dto.PreviewLine;
import dev.roland.inventory_management_backend.features.document.dto.PreviewResponse;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentRelationType;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentStatus;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentType;
import dev.roland.inventory_management_backend.features.document.mapper.DocumentFulfillmentMapper;
import dev.roland.inventory_management_backend.features.document.mapper.DocumentResponseMapper;
import dev.roland.inventory_management_backend.features.document.message.DocumentApiMessageKey;
import dev.roland.inventory_management_backend.features.document.service.DocumentPostingService;
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
import dev.roland.inventory_management_backend.features.stock_balance.service.StockBalanceService;
import dev.roland.inventory_management_backend.features.unit.Unit;
import dev.roland.inventory_management_backend.features.warehouse.Warehouse;
import dev.roland.inventory_management_backend.features.warehouse.service.WarehouseService;
import edu.umd.cs.findbugs.annotations.NonNull;
import lombok.RequiredArgsConstructor;

/** Coordinates order fulfillment and stock posting. */
@Service
@RequiredArgsConstructor
public class DocumentFulfillmentWorkflowService {
  private final DocumentService documentService;
  private final DocumentLineService lineService;
  private final ProductService productService;
  private final WarehouseService warehouseService;
  private final CurrentCompanyProvider companyProvider;
  private final DocumentPostingService postingService;
  private final DocumentRelationService relationService;
  private final DocumentLineAllocationService allocationService;
  private final StockBalanceService balanceService;
  private final DocumentWorkflowAccessService accessService;
  private final DocumentWorkflowWriterService writer;
  private final DocumentResponseMapper responseMapper;
  private final DocumentDraftWorkflowService draftWorkflowService;

  /**
   * Creates a fulfillment draft from the supplied request.
   *
   * @param request the request value
   * @return the created fulfillment draft request.
   * @throws ApiException when the operation cannot be completed
   */
  @Transactional
  public DocumentResponse createFulfillmentDraft(final FulfillmentDraft request) {
    final Company company = companyProvider.getCurrentCompany();
    if (request.type() != DocumentType.GOODS_RECEIPT
        && request.type() != DocumentType.DELIVERY_NOTE) {
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_TRANSITION);
    }
    if (!accessService.directAllowed(request.type(), accessService.currentActor())) {
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_DIRECT_FULFILLMENT_NOT_ALLOWED);
    }
    final Document source =
        request.sourceDocumentId() == null ? null : accessService.find(request.sourceDocumentId());
    if (source != null && !accessService.isMatchingOrder(source, request.type())) {
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_TRANSITION);
    }
    if (source != null
        && source.getStatus() != DocumentStatus.CONFIRMED
        && source.getStatus() != DocumentStatus.PARTIALLY_COMPLETED) {
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_TRANSITION);
    }
    final Long warehouseId = getWarehouseId(request, source);
    final Document operation = new Document();
    operation.setCompany(company);
    operation.setCreatedByUser(accessService.currentActor());
    operation.setType(request.type());
    operation.setCategory(DocumentWorkflowValues.category(request.type()));
    operation.setStatus(DocumentStatus.DRAFT);
    operation.setDocumentDate(LocalDate.now());
    operation.setLines(new ArrayList<>());
    if (source != null) {
      operation.setPartner(source.getPartner());
      operation.setPartnerNameSnapshot(source.getPartnerNameSnapshot());
      operation.setPartnerTaxNumberSnapshot(source.getPartnerTaxNumberSnapshot());
      operation.setPartnerAddressSnapshot(source.getPartnerAddressSnapshot());
      operation.setCurrency(source.getCurrency());
      operation.setExchangeRate(source.getExchangeRate());
      operation.setExchangeRates(
          DocumentWorkflowValues.safe(source.getExchangeRates()).stream()
              .map(
                  sourceRate -> {
                    final DocumentExchangeRate rate = new DocumentExchangeRate();
                    rate.setDocument(operation);
                    rate.setSourceCurrency(sourceRate.getSourceCurrency());
                    rate.setRate(sourceRate.getRate());
                    return rate;
                  })
              .collect(Collectors.toCollection(ArrayList::new)));
      operation.setReferenceNumber(source.getReferenceNumber());
    }
    if (operation.getCurrency() == null) {
      operation.setCurrency(company.getPreferredCurrency());
    }
    draftWorkflowService.setWarehouse(operation, request.type(), warehouseId, company.getId());
    if (source != null
        && warehouseId.equals(
            request.type() == DocumentType.GOODS_RECEIPT
                ? DocumentWorkflowValues.idOf(source.getTargetWarehouse())
                : DocumentWorkflowValues.idOf(source.getSourceWarehouse()))) {
      if (request.type() == DocumentType.GOODS_RECEIPT) {
        operation.setTargetWarehouseNameSnapshot(source.getTargetWarehouseNameSnapshot());
        operation.setTargetWarehouseCodeSnapshot(source.getTargetWarehouseCodeSnapshot());
      } else {
        operation.setSourceWarehouseNameSnapshot(source.getSourceWarehouseNameSnapshot());
        operation.setSourceWarehouseCodeSnapshot(source.getSourceWarehouseCodeSnapshot());
      }
    }
    final Document saved = documentService.save(operation);
    for (FulfillmentLine input : DocumentWorkflowValues.safe(request.lines())) {
      if (input.quantity() == null || input.quantity().signum() <= 0) {
        continue;
      }
      if (source == null) {
        if (input.productId() == null) {
          continue;
        }
        final Product product =
            productService
                .findByIdAndCompanyId(input.productId(), company.getId())
                .orElseThrow(() -> new NotFoundException(NotFoundMessageKey.PRODUCT));
        final Unit unit = product.getUnit();
        draftWorkflowService.addLine(saved, product, unit, input.quantity(), BigDecimal.ZERO);
      } else {
        if (input.sourceLineId() == null) {
          continue;
        }
        final DocumentLine sourceLine =
            source.getLines().stream()
                .filter(l -> Objects.equals(l.getId(), input.sourceLineId()))
                .findFirst()
                .orElseThrow(
                    () -> new ApiException(DocumentApiMessageKey.DOCUMENT_CROSS_COMPANY_REFERENCE));
        final DocumentLine line =
            draftWorkflowService.copyOperationLine(saved, sourceLine, input.quantity());
        final DocumentLineAllocation allocation = new DocumentLineAllocation();
        allocation.setFulfillmentLine(line);
        allocation.setSourceOrderLine(sourceLine);
        allocation.setQuantity(input.quantity());
        allocationService.save(allocation);
      }
    }
    if (source != null) {
      final DocumentRelation relation = new DocumentRelation();
      relation.setSourceDocument(source);
      relation.setTargetDocument(saved);
      relation.setRelationType(DocumentRelationType.FULFILLS);
      relationService.save(relation);
    }
    writer.addActivity(
        saved, DocumentActivityEvent.CREATED, source == null ? null : "source=" + source.getId());
    return responseMapper.toResponse(documentService.save(saved));
  }

  /**
   * Resolves the warehouse used to fulfill the requested draft.
   *
   * @param request fulfillment draft details
   * @param source source document, when the draft is based on another document
   * @return selected warehouse identifier, or null when none is available warehouse used to fulfill
   *     the requested draft.
   * @throws ApiException when the operation cannot be completed
   */
  @NonNull
  static Long getWarehouseId(final FulfillmentDraft request, final Document source) {
    final Long warehouseId =
        request.warehouseId() != null
            ? request.warehouseId()
            : source == null
                ? null
                : request.type() == DocumentType.GOODS_RECEIPT
                    ? source.getTargetWarehouse() == null
                        ? null
                        : source.getTargetWarehouse().getId()
                    : source.getSourceWarehouse() == null
                        ? null
                        : source.getSourceWarehouse().getId();
    if (warehouseId == null) {
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_LINES);
    }
    return warehouseId;
  }

  /**
   * Previews fulfillment of an order.
   *
   * @param request the request value
   * @return the fulfillment preview
   */
  @Transactional
  public PreviewResponse preview(final FulfillmentPreview request) {
    final Company company = companyProvider.getCurrentCompany();
    final Document source =
        request.sourceDocumentId() == null ? null : accessService.find(request.sourceDocumentId());
    final Document operation =
        request.documentId() == null ? null : accessService.find(request.documentId());
    Long warehouseId = request.warehouseId();
    DocumentType type = operation == null ? request.type() : operation.getType();
    if (source != null) {
      type =
          source.getType() == DocumentType.PURCHASE_ORDER
              ? DocumentType.GOODS_RECEIPT
              : DocumentType.DELIVERY_NOTE;
    }
    if (operation != null && !Objects.equals(operation.getCompany().getId(), company.getId())) {
      throw accessService.notFound();
    }
    if (warehouseId == null && operation != null) {
      warehouseId =
          operation.getType() == DocumentType.GOODS_RECEIPT
              ? DocumentWorkflowValues.idOf(operation.getTargetWarehouse())
              : DocumentWorkflowValues.idOf(operation.getSourceWarehouse());
    }
    final List<PreviewLine> lines = new ArrayList<>();
    if (warehouseId != null) {
      final Warehouse warehouse =
          warehouseService.findEntityByIdAndCompanyId(warehouseId, company.getId());
      for (FulfillmentLine input : DocumentWorkflowValues.safe(request.lines())) {
        final DocumentLine line =
            source == null
                ? operation == null
                    ? null
                    : operation.getLines().stream()
                        .filter(l -> Objects.equals(l.getId(), input.sourceLineId()))
                        .findFirst()
                        .orElse(null)
                : source.getLines().stream()
                    .filter(l -> Objects.equals(l.getId(), input.sourceLineId()))
                    .findFirst()
                    .orElse(null);
        final DocumentLine allocatedSourceLine =
            line == null || source != null
                ? null
                : allocationService.findAllByFulfillmentLineId(line.getId()).stream()
                    .map(DocumentLineAllocation::getSourceOrderLine)
                    .findFirst()
                    .orElse(null);
        final Long productId =
            input.productId() != null
                ? input.productId()
                : line == null ? null : line.getProduct().getId();
        if (productId == null) {
          continue;
        }
        final Product product =
            productService
                .findByIdAndCompanyId(productId, company.getId())
                .orElseThrow(() -> new NotFoundException(NotFoundMessageKey.PRODUCT));
        final BigDecimal operationQty =
            input.quantity() == null && line != null
                ? line.getQuantity()
                : DocumentWorkflowValues.nz(input.quantity());
        final BigDecimal conversionFactor =
            line == null
                ? DocumentWorkflowValues.expectedFactor(product, product.getUnit())
                : DocumentWorkflowValues.nz(line.getConversionFactorSnapshot());
        final BigDecimal stockQty = operationQty.multiply(conversionFactor);
        final BigDecimal delta = type == DocumentType.DELIVERY_NOTE ? stockQty.negate() : stockQty;
        final BigDecimal available =
            balanceService
                .findByWarehouseAndProduct(warehouse, product)
                .map(b -> DocumentWorkflowValues.nz(b.getQuantity()))
                .orElse(BigDecimal.ZERO);
        final BigDecimal cumulativeDelta =
            lines.stream()
                .filter(existing -> Objects.equals(existing.productId(), productId))
                .map(PreviewLine::quantityDelta)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .add(delta);
        final BigDecimal result = available.add(cumulativeDelta);
        BigDecimal shortfall = result.signum() < 0 ? result.abs() : BigDecimal.ZERO;
        String error =
            shortfall.signum() > 0
                ? DocumentApiMessageKey.DOCUMENT_INSUFFICIENT_STOCK.getKey()
                : null;
        final DocumentLine orderLine = source != null ? line : allocatedSourceLine;
        if (operationQty.signum() <= 0) {
          error = DocumentApiMessageKey.DOCUMENT_INVALID_LINES.getKey();
        }
        if (orderLine != null) {
          final BigDecimal remaining =
              DocumentWorkflowValues.nz(orderLine.getQuantity())
                  .subtract(accessService.fulfilledFor(orderLine))
                  .subtract(DocumentWorkflowValues.nz(orderLine.getClosedQuantity()));
          if (operationQty.compareTo(remaining) > 0) {
            shortfall = operationQty.subtract(remaining);
            error = DocumentApiMessageKey.DOCUMENT_INVALID_LINES.getKey();
          }
        }
        if (line != null
            && input.productId() != null
            && !input.productId().equals(line.getProduct().getId())) {
          error = DocumentApiMessageKey.DOCUMENT_INVALID_LINES.getKey();
        }
        lines.add(
            new PreviewLine(
                orderLine == null ? input.sourceLineId() : orderLine.getId(),
                productId,
                line == null || line.getProductNameSnapshot() == null
                    ? product.getName()
                    : line.getProductNameSnapshot(),
                warehouse.getName(),
                available,
                delta,
                result,
                shortfall,
                error));
      }
    }
    return new PreviewResponse(
        warehouseId != null && lines.stream().noneMatch(l -> l.errorMessageKey() != null), lines);
  }

  private void validateRemainingAllocations(
      final List<DocumentLineAllocation> allocations, final Company company) {
    if (!allocations.isEmpty()) {
      final Map<Long, BigDecimal> requested = new HashMap<>();
      allocations.forEach(
          a -> requested.merge(a.getSourceOrderLine().getId(), a.getQuantity(), BigDecimal::add));
      for (DocumentLineAllocation allocation : allocations) {
        final Document source =
            accessService.lock(
                allocation.getSourceOrderLine().getDocument().getId(), company.getId());
        final DocumentLine sourceLine =
            source.getLines().stream()
                .filter(l -> l.getId().equals(allocation.getSourceOrderLine().getId()))
                .findFirst()
                .orElseThrow(() -> new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_LINES));
        final BigDecimal remaining =
            sourceLine
                .getQuantity()
                .subtract(accessService.fulfilledFor(sourceLine))
                .subtract(DocumentWorkflowValues.nz(sourceLine.getClosedQuantity()));
        final BigDecimal requestQty = requested.get(sourceLine.getId());
        if (requestQty.compareTo(remaining) > 0) {
          throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_LINES);
        }
      }
    }
  }

  /**
   * Posts the requested fulfillment operation.
   *
   * @param expectedVersion expected document version
   * @param id document identifier
   * @param idempotencyKey idempotency key
   * @return the posted document response
   * @throws ApiException when the operation cannot be completed
   */
  @Transactional
  public DocumentResponse post(
      final Long id, final String idempotencyKey, final String expectedVersion) {
    if (idempotencyKey == null || idempotencyKey.isBlank()) {
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_IDEMPOTENCY_KEY_REQUIRED);
    }
    final Company company = companyProvider.getCurrentCompany();
    final Document document = accessService.lock(id, company.getId());
    DocumentResponse result = responseMapper.toResponse(document);
    if (!documentService.postingKeyExists(id, idempotencyKey)) {
      accessService.checkVersion(
          document, expectedVersion, () -> responseMapper.toResponse(document));
      if (!accessService.directAllowed(document.getType(), accessService.currentActor())) {
        throw new ApiException(DocumentApiMessageKey.DOCUMENT_DIRECT_FULFILLMENT_NOT_ALLOWED);
      }
      if (document.getStatus() == DocumentStatus.COMPLETED
          || document.getStatus() == DocumentStatus.CANCELLED) {
        throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_TRANSITION);
      }
      if (!accessService.isDirectOperation(document.getType())
          || document.getLines() == null
          || document.getLines().isEmpty()) {
        throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_TRANSITION);
      }
      if (document.getLines().stream()
          .anyMatch(l -> l.getQuantity() == null || l.getQuantity().signum() <= 0)) {
        throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_LINES);
      }
      final List<DocumentLineAllocation> allocations =
          document.getLines().stream()
              .flatMap(l -> allocationService.findAllByFulfillmentLineId(l.getId()).stream())
              .toList();
      validateRemainingAllocations(allocations, company);
      final PreviewResponse preview = preview(DocumentFulfillmentMapper.toPreview(document));
      if (!preview.valid()) {
        final boolean insufficientStock =
            preview.lines().stream()
                .anyMatch(
                    line ->
                        DocumentApiMessageKey.DOCUMENT_INSUFFICIENT_STOCK
                            .getKey()
                            .equals(line.errorMessageKey()));
        throw new ApiException(
            insufficientStock
                ? DocumentApiMessageKey.DOCUMENT_INSUFFICIENT_STOCK
                : DocumentApiMessageKey.DOCUMENT_INVALID_LINES,
            Map.of(
                "lines",
                preview.lines().stream().filter(line -> line.errorMessageKey() != null).toList()));
      }
      writer.allocateNumber(document);
      documentService.savePostingKey(writer.postingKey(document, idempotencyKey));
      for (DocumentLine line : document.getLines()) {
        final List<DocumentLineAllocation> lineAllocations =
            allocationService.findAllByFulfillmentLineId(line.getId());
        for (DocumentLineAllocation allocation : lineAllocations) {
          final DocumentLine sourceLine = allocation.getSourceOrderLine();
          sourceLine.setFulfilledQuantity(
              DocumentWorkflowValues.nz(sourceLine.getFulfilledQuantity())
                  .add(allocation.getQuantity()));
          lineService.save(sourceLine);
        }
      }
      document.setStatus(DocumentStatus.CONFIRMED);
      final Document posted = postingService.complete(document);
      for (DocumentRelation relation :
          relationService.findAllBySourceDocumentIdOrTargetDocumentId(id, id)) {
        final Document source = relation.getSourceDocument();
        if (!source.getId().equals(id)
            && (source.getType() == DocumentType.PURCHASE_ORDER
                || source.getType() == DocumentType.SALES_ORDER)) {
          source.setStatus(
              accessService.hasRemaining(source)
                  ? DocumentStatus.PARTIALLY_COMPLETED
                  : DocumentStatus.COMPLETED);
          documentService.save(source);
        }
      }
      writer.addActivity(posted, DocumentActivityEvent.POSTED, idempotencyKey);
      final Document saved = documentService.save(posted);
      documentService.flush();
      result = responseMapper.toResponse(saved);
    }
    return result;
  }

  /**
   * Confirms a fulfillment document using its expected version.
   *
   * @param expectedVersion expected document version
   * @param id document identifier
   * @return the confirmed document response version.
   * @throws ApiException when the operation cannot be completed
   */
  @Transactional
  public DocumentResponse confirm(final Long id, final String expectedVersion) {
    final Company company = companyProvider.getCurrentCompany();
    final Document document = accessService.lock(id, company.getId());
    accessService.checkVersion(
        document, expectedVersion, () -> responseMapper.toResponse(document));
    if (!accessService.canManageDocument(document, accessService.currentActor())) {
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_TRANSITION);
    }
    if (document.getType() != DocumentType.PURCHASE_ORDER
        && document.getType() != DocumentType.SALES_ORDER) {
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_TRANSITION);
    }
    if (document.getStatus() != DocumentStatus.DRAFT
        && document.getStatus() != DocumentStatus.SENT) {
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_TRANSITION);
    }
    if (document.getLines() == null
        || document.getLines().isEmpty()
        || document.getLines().stream()
            .anyMatch(l -> l.getQuantity() == null || l.getQuantity().signum() <= 0)) {
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_LINES);
    }
    writer.allocateNumber(document);
    document.setStatus(DocumentStatus.CONFIRMED);
    writer.addActivity(document, DocumentActivityEvent.CONFIRMED, null);
    final Document saved = documentService.save(document);
    documentService.flush();
    return responseMapper.toResponse(saved);
  }

  /**
   * Closes any unfulfilled quantity remaining on an order.
   *
   * @param expectedVersion expected document version
   * @param id order identifier
   * @param request remaining quantity closure details
   * @return the updated order response
   * @throws ApiException when the operation cannot be completed
   */
  @Transactional
  public DocumentResponse closeRemaining(
      final Long id, final CloseRemaining request, final String expectedVersion) {
    final Company company = companyProvider.getCurrentCompany();
    final Document order = accessService.lock(id, company.getId());
    accessService.checkVersion(order, expectedVersion, () -> responseMapper.toResponse(order));
    if (!accessService.canManageDocument(order, accessService.currentActor())) {
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_TRANSITION);
    }
    if (request == null || request.reason() == null || request.reason().isBlank()) {
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_REASON_REQUIRED);
    }
    if (order.getType() != DocumentType.PURCHASE_ORDER
        && order.getType() != DocumentType.SALES_ORDER) {
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_TRANSITION);
    }
    if (order.getStatus() != DocumentStatus.CONFIRMED
        && order.getStatus() != DocumentStatus.PARTIALLY_COMPLETED) {
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_TRANSITION);
    }
    for (DocumentLine line : order.getLines()) {
      final BigDecimal remaining =
          DocumentWorkflowValues.nz(line.getQuantity())
              .subtract(accessService.fulfilledFor(line))
              .subtract(DocumentWorkflowValues.nz(line.getClosedQuantity()));
      if (remaining.signum() > 0) {
        line.setClosedQuantity(DocumentWorkflowValues.nz(line.getClosedQuantity()).add(remaining));
        line.setClosureReason(request.reason().trim());
        lineService.save(line);
      }
    }
    order.setStatus(
        accessService.hasRemaining(order)
            ? DocumentStatus.PARTIALLY_COMPLETED
            : DocumentStatus.COMPLETED);
    writer.addActivity(order, DocumentActivityEvent.REMAINDER_CLOSED, request.reason().trim());
    writer.touch(order);
    final Document saved = documentService.save(order);
    documentService.flush();
    return responseMapper.toResponse(saved);
  }
}
