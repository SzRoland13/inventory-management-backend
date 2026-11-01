package dev.roland.inventory_management_backend.features.document.service.impl;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.common.exception.ApiException;
import dev.roland.inventory_management_backend.common.service.CurrentCompanyProvider;
import dev.roland.inventory_management_backend.features.company.Company;
import dev.roland.inventory_management_backend.features.document.Document;
import dev.roland.inventory_management_backend.features.document.dto.WarehouseOperationRequest;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentRelationType;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentStatus;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentType;
import dev.roland.inventory_management_backend.features.document.enumeration.WarehouseOperationType;
import dev.roland.inventory_management_backend.features.document.mapper.DocumentResponseMapper;
import dev.roland.inventory_management_backend.features.document.message.DocumentApiMessageKey;
import dev.roland.inventory_management_backend.features.document.utils.DocumentWorkflowValues;
import dev.roland.inventory_management_backend.features.document_line.DocumentLine;
import dev.roland.inventory_management_backend.features.document_line.service.DocumentLineService;
import dev.roland.inventory_management_backend.features.stock_movement.StockMovement;
import dev.roland.inventory_management_backend.features.user.User;
import dev.roland.inventory_management_backend.features.user.enumeration.UserRole;
import dev.roland.inventory_management_backend.features.warehouse.Warehouse;
import dev.roland.inventory_management_backend.features.warehouse.service.WarehouseService;
import lombok.RequiredArgsConstructor;

/** Shared persistence helpers for linked warehouse document workflows. */
@Service
@RequiredArgsConstructor
public class WarehouseOperationSupportService {
  private final DocumentWorkflowAccessService accessService;
  private final DocumentWorkflowWriterService writer;
  private final DocumentResponseMapper responseMapper;
  private final DocumentDraftWorkflowService draftWorkflowService;
  private final DocumentLineService lineService;
  private final WarehouseService warehouseService;
  private final CurrentCompanyProvider companyProvider;

  public record StockKey(Long warehouseId, Long productId) {}

  public Document operationDocument(
      final Document parent, final DocumentType type, final String reason) {
    final Document document = new Document();
    document.setCompany(parent.getCompany());
    document.setCreatedByUser(accessService.currentActor());
    document.setType(type);
    document.setCategory(DocumentWorkflowValues.category(type));
    document.setStatus(DocumentStatus.DRAFT);
    document.setDocumentDate(LocalDate.now());
    document.setPartner(parent.getPartner());
    document.setPartnerNameSnapshot(parent.getPartnerNameSnapshot());
    document.setPartnerTaxNumberSnapshot(parent.getPartnerTaxNumberSnapshot());
    document.setPartnerAddressSnapshot(parent.getPartnerAddressSnapshot());
    document.setCurrency(parent.getCurrency());
    document.setExchangeRate(parent.getExchangeRate());
    document.setSourceWarehouse(parent.getSourceWarehouse());
    document.setTargetWarehouse(parent.getTargetWarehouse());
    document.setTransitWarehouse(parent.getTransitWarehouse());
    document.setWorkflowReason(reason);
    document.setLines(new ArrayList<>());
    return document;
  }

  public void complete(final Document document) {
    document.setStatus(DocumentStatus.COMPLETED);
    document.setCompletedAt(LocalDateTime.now());
  }

  public DocumentLine cloneLine(
      final Document document, final DocumentLine source, final BigDecimal quantity) {
    final DocumentLine copy = draftWorkflowService.copyOperationLine(document, source, quantity);
    copy.setSourceLine(source);
    return copy;
  }

  public DocumentLine line(final Long id, final Long companyId) {
    final DocumentLine line = lineService.findByIdOrThrow(id);
    if (line.getDocument() == null
        || line.getDocument().getCompany() == null
        || !companyId.equals(line.getDocument().getCompany().getId())) {
      throw accessService.notFound();
    }
    return line;
  }

  public void relate(
      final Document source, final Document target, final DocumentRelationType type) {
    writer.relate(source, target, type);
  }

  public void stock(final StockMovement movement, final String reason) {
    writer.saveStockMovement(movement, reason);
  }

  public void consumeKey(final Document document, final String key) {
    writer.consumeKey(document, key);
  }

  public void requireKey(final String key) {
    if (key == null || key.isBlank())
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_IDEMPOTENCY_KEY_REQUIRED);
  }

  public void requireVersion(final Document document, final String version) {
    accessService.checkVersion(document, version, () -> responseMapper.toResponse(document));
  }

  public String hashCalculationVersion(final List<String> components) {
    try {
      final MessageDigest digest = MessageDigest.getInstance("SHA-256");
      return HexFormat.of().formatHex(digest.digest(String.join("|", components).getBytes()));
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException(exception);
    }
  }

  public boolean hasDuplicateSourceLines(
      final List<WarehouseOperationRequest.WarehouseOperationLine> lines) {
    final Set<Long> ids = new HashSet<>();
    return DocumentWorkflowValues.safe(lines).stream()
        .map(WarehouseOperationRequest.WarehouseOperationLine::sourceLineId)
        .filter(Objects::nonNull)
        .anyMatch(id -> !ids.add(id));
  }

  public Map<WarehouseOperationSupportService.StockKey, BigDecimal> correctionStockDeltas(
      final Document document,
      final DocumentLine source,
      final WarehouseOperationRequest.WarehouseOperationLine requestLine,
      final Long companyId) {
    final Map<WarehouseOperationSupportService.StockKey, BigDecimal> oldEffects = new HashMap<>();
    final Map<WarehouseOperationSupportService.StockKey, BigDecimal> newEffects = new HashMap<>();
    final Long oldProductId = source.getProduct().getId();
    final Long newProductId =
        requestLine.correctedProductId() == null ? oldProductId : requestLine.correctedProductId();
    final BigDecimal oldQuantity = DocumentWorkflowValues.nz(source.getQuantity());
    final BigDecimal newQuantity = DocumentWorkflowValues.nz(requestLine.correctedQuantity());
    final Warehouse transit = document.getTransitWarehouse();
    final Warehouse oldPrimary = defaultCorrectionWarehouse(document);
    final Warehouse newPrimary =
        requestLine.correctedWarehouseId() == null
            ? oldPrimary
            : warehouseService.findEntityByIdAndCompanyId(
                requestLine.correctedWarehouseId(), companyId);
    switch (document.getType()) {
      case GOODS_RECEIPT -> addEffect(oldEffects, oldPrimary, oldProductId, oldQuantity);
      case DELIVERY_NOTE -> addEffect(oldEffects, oldPrimary, oldProductId, oldQuantity.negate());
      case TRANSFER_DELIVERY -> {
        addEffect(oldEffects, document.getSourceWarehouse(), oldProductId, oldQuantity.negate());
        addEffect(oldEffects, transit, oldProductId, oldQuantity);
      }
      case TRANSFER_RECEIPT -> {
        addEffect(oldEffects, transit, oldProductId, oldQuantity.negate());
        addEffect(oldEffects, document.getTargetWarehouse(), oldProductId, oldQuantity);
      }
      default -> {}
    }
    switch (document.getType()) {
      case GOODS_RECEIPT -> addEffect(newEffects, newPrimary, newProductId, newQuantity);
      case DELIVERY_NOTE -> addEffect(newEffects, newPrimary, newProductId, newQuantity.negate());
      case TRANSFER_DELIVERY -> {
        addEffect(newEffects, newPrimary, newProductId, newQuantity.negate());
        addEffect(newEffects, transit, newProductId, newQuantity);
      }
      case TRANSFER_RECEIPT -> {
        addEffect(newEffects, transit, newProductId, newQuantity.negate());
        addEffect(newEffects, newPrimary, newProductId, newQuantity);
      }
      default -> {}
    }
    final Map<WarehouseOperationSupportService.StockKey, BigDecimal> result = new HashMap<>();
    oldEffects.forEach((key, value) -> result.merge(key, value.negate(), BigDecimal::add));
    newEffects.forEach((key, value) -> result.merge(key, value, BigDecimal::add));
    return result;
  }

  public void addEffect(
      final Map<WarehouseOperationSupportService.StockKey, BigDecimal> effects,
      final Warehouse warehouse,
      final Long productId,
      final BigDecimal quantity) {
    if (warehouse != null && quantity.signum() != 0)
      effects.merge(
          new WarehouseOperationSupportService.StockKey(warehouse.getId(), productId),
          quantity,
          BigDecimal::add);
  }

  public Warehouse defaultCorrectionWarehouse(final Document document) {
    return document.getType() == DocumentType.GOODS_RECEIPT
            || document.getType() == DocumentType.TRANSFER_RECEIPT
        ? document.getTargetWarehouse()
        : document.getSourceWarehouse();
  }

  public WarehouseOperationRequest withOperation(
      final WarehouseOperationRequest request, final WarehouseOperationType operation) {
    return new WarehouseOperationRequest(
        request.documentId(),
        request.sourceDocumentId(),
        request.sourceWarehouseId(),
        request.destinationWarehouseId(),
        request.transitWarehouseId(),
        request.warehouseId(),
        request.targetWarehouseId(),
        request.returnDocumentId(),
        request.originalDeliveryId(),
        request.date(),
        request.countedAt(),
        request.version(),
        request.idempotencyKey(),
        request.calculationVersion(),
        operation,
        request.direction(),
        request.reason(),
        request.confirmed(),
        request.nonBillable(),
        request.lines());
  }

  public void requireReason(final String reason) {
    if (reason == null || reason.isBlank())
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_REASON_REQUIRED);
  }

  public void requireRole(final User user, final UserRole... roles) {
    if (user == null || java.util.Arrays.stream(roles).noneMatch(role -> role == user.getRole()))
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_TRANSITION);
  }

  public Warehouse quarantineWarehouse(final Long companyId) {
    final Company company = companyProvider.getCurrentCompany();
    if (!company.getId().equals(companyId))
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_CROSS_COMPANY_REFERENCE);
    return warehouseService.ensureQuarantineWarehouse(company);
  }
}
