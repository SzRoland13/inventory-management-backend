package dev.roland.inventory_management_backend.features.document.facade.workflow;

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

  /**
   * Identifies a product's stock balance in a warehouse.
   *
   * @param productId the productId value
   * @param warehouseId the warehouseId value
   */
  public record StockKey(Long warehouseId, Long productId) {}

  /**
   * Creates a draft operation document based on a parent document.
   *
   * @param parent the parent value
   * @param reason the reason value
   * @param type the type value
   * @return the resulting value
   */
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

  /**
   * Marks the document completed and records its completion time.
   *
   * @param document the document value
   */
  public void complete(final Document document) {
    document.setStatus(DocumentStatus.COMPLETED);
    document.setCompletedAt(LocalDateTime.now());
  }

  /**
   * Copies the supplied line's product and quantities into a new line.
   *
   * @param document the document value
   * @param quantity the quantity value
   * @param source the source value
   * @return the resulting value line.
   */
  public DocumentLine cloneLine(
      final Document document, final DocumentLine source, final BigDecimal quantity) {
    final DocumentLine copy = draftWorkflowService.copyOperationLine(document, source, quantity);
    copy.setSourceLine(source);
    return copy;
  }

  /**
   * Finds a document line and verifies its company ownership.
   *
   * @param companyId the companyId value
   * @param id the id value
   * @return the resulting value
   */
  public DocumentLine line(final Long id, final Long companyId) {
    final DocumentLine line = lineService.findByIdOrThrow(id);
    if (line.getDocument() == null
        || line.getDocument().getCompany() == null
        || !companyId.equals(line.getDocument().getCompany().getId())) {
      throw accessService.notFound();
    }
    return line;
  }

  /**
   * Creates a relation between two documents.
   *
   * @param source the source value
   * @param target the target value
   * @param type the type value
   */
  public void relate(
      final Document source, final Document target, final DocumentRelationType type) {
    writer.relate(source, target, type);
  }

  /**
   * Persists the supplied stock movement.
   *
   * @param movement the movement value
   * @param reason the reason value
   */
  public void stock(final StockMovement movement, final String reason) {
    writer.saveStockMovement(movement, reason);
  }

  /**
   * Persists the idempotency key consumed by the document operation.
   *
   * @param document the document value
   * @param key the key value
   */
  public void consumeKey(final Document document, final String key) {
    writer.consumeKey(document, key);
  }

  /**
   * Rejects a missing or blank idempotency key.
   *
   * @param key the key value
   * @throws ApiException when the operation cannot be completed
   */
  public void requireKey(final String key) {
    if (key == null || key.isBlank()) {
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_IDEMPOTENCY_KEY_REQUIRED);
    }
  }

  /**
   * Validates the expected version of the document.
   *
   * @param document the document value
   * @param version the version value
   */
  public void requireVersion(final Document document, final String version) {
    accessService.checkVersion(document, version, () -> responseMapper.toResponse(document));
  }

  /**
   * Hashes calculation components to create a stable version token.
   *
   * @param components the components value
   * @return the resulting value
   * @throws IllegalStateException when the operation cannot be completed
   */
  public String hashCalculationVersion(final List<String> components) {
    try {
      final MessageDigest digest = MessageDigest.getInstance("SHA-256");
      return HexFormat.of().formatHex(digest.digest(String.join("|", components).getBytes()));
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException(exception);
    }
  }

  /**
   * Checks whether request lines refer to the same source line more than once.
   *
   * @param lines the lines value
   * @return the resulting value than once.
   */
  public boolean hasDuplicateSourceLines(
      final List<WarehouseOperationRequest.WarehouseOperationLine> lines) {
    final Set<Long> ids = new HashSet<>();
    return DocumentWorkflowValues.safe(lines).stream()
        .map(WarehouseOperationRequest.WarehouseOperationLine::sourceLineId)
        .filter(Objects::nonNull)
        .anyMatch(id -> !ids.add(id));
  }

  /**
   * Calculates stock changes caused by correcting a document line.
   *
   * @param companyId the companyId value
   * @param document the document value
   * @param requestLine the requestLine value
   * @param source the source value
   * @return the resulting value line.
   */
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

  /**
   * * * * Adds a nonzero product quantity effect for a warehouse.
   *
   * @param effects the effects value
   * @param productId the productId value
   * @param quantity the quantity value
   * @param warehouse the warehouse value
   */
  public void addEffect(
      final Map<WarehouseOperationSupportService.StockKey, BigDecimal> effects,
      final Warehouse warehouse,
      final Long productId,
      final BigDecimal quantity) {
    if (warehouse != null && quantity.signum() != 0) {
      effects.merge(
          new WarehouseOperationSupportService.StockKey(warehouse.getId(), productId),
          quantity,
          BigDecimal::add);
    }
  }

  /**
   * Returns the primary warehouse affected by a document correction.
   *
   * @param document the document value
   * @return the resulting value
   */
  public Warehouse defaultCorrectionWarehouse(final Document document) {
    return document.getType() == DocumentType.GOODS_RECEIPT
            || document.getType() == DocumentType.TRANSFER_RECEIPT
        ? document.getTargetWarehouse()
        : document.getSourceWarehouse();
  }

  /**
   * Copies a request with the specified operation type.
   *
   * @param operation the operation value
   * @param request the request value
   * @return the resulting value
   */
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

  /**
   * Requires a nonblank reason for the operation.
   *
   * @param reason the reason value
   * @throws ApiException when the operation cannot be completed
   */
  public void requireReason(final String reason) {
    if (reason == null || reason.isBlank()) {
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_REASON_REQUIRED);
    }
  }

  /**
   * Requires the user to have one of the specified roles.
   *
   * @param roles the roles value
   * @param user the user value
   * @throws ApiException when the operation cannot be completed
   */
  public void requireRole(final User user, final UserRole... roles) {
    if (user == null || java.util.Arrays.stream(roles).noneMatch(role -> role == user.getRole())) {
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_TRANSITION);
    }
  }

  /**
   * Returns the quarantine warehouse for the current company.
   *
   * @param companyId the companyId value
   * @return the resulting value
   * @throws ApiException when the operation cannot be completed
   */
  public Warehouse quarantineWarehouse(final Long companyId) {
    final Company company = companyProvider.getCurrentCompany();
    if (!company.getId().equals(companyId)) {
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_CROSS_COMPANY_REFERENCE);
    }
    return warehouseService.ensureQuarantineWarehouse(company);
  }
}
