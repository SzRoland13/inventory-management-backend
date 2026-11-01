package dev.roland.inventory_management_backend.features.document.facade.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.transaction.Transactional;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import dev.roland.inventory_management_backend.common.async.AsyncJobType;
import dev.roland.inventory_management_backend.common.async.OutboxService;
import dev.roland.inventory_management_backend.common.exception.ApiException;
import dev.roland.inventory_management_backend.common.exception.NotFoundException;
import dev.roland.inventory_management_backend.common.message.NotFoundMessageKey;
import dev.roland.inventory_management_backend.common.service.CurrentCompanyProvider;
import dev.roland.inventory_management_backend.common.service.ObjectStorageService;
import dev.roland.inventory_management_backend.features.auth.CustomUserDetails;
import dev.roland.inventory_management_backend.features.company.Company;
import dev.roland.inventory_management_backend.features.contact.Contact;
import dev.roland.inventory_management_backend.features.contact.enumeration.ContactType;
import dev.roland.inventory_management_backend.features.contact.service.ContactService;
import dev.roland.inventory_management_backend.features.currency.Currency;
import dev.roland.inventory_management_backend.features.currency.service.CurrencyService;
import dev.roland.inventory_management_backend.features.document.Document;
import dev.roland.inventory_management_backend.features.document.DocumentExchangeRate;
import dev.roland.inventory_management_backend.features.document.DocumentPostingKey;
import dev.roland.inventory_management_backend.features.document.dto.Activity;
import dev.roland.inventory_management_backend.features.document.dto.Capabilities;
import dev.roland.inventory_management_backend.features.document.dto.CloseRemaining;
import dev.roland.inventory_management_backend.features.document.dto.DocumentResponse;
import dev.roland.inventory_management_backend.features.document.dto.DocumentTotals;
import dev.roland.inventory_management_backend.features.document.dto.Draft;
import dev.roland.inventory_management_backend.features.document.dto.DraftUpdate;
import dev.roland.inventory_management_backend.features.document.dto.ExchangeRateDraft;
import dev.roland.inventory_management_backend.features.document.dto.ExchangeRateResponse;
import dev.roland.inventory_management_backend.features.document.dto.FileEntry;
import dev.roland.inventory_management_backend.features.document.dto.FulfillmentDraft;
import dev.roland.inventory_management_backend.features.document.dto.FulfillmentLine;
import dev.roland.inventory_management_backend.features.document.dto.FulfillmentPreview;
import dev.roland.inventory_management_backend.features.document.dto.LineDraft;
import dev.roland.inventory_management_backend.features.document.dto.LineResponse;
import dev.roland.inventory_management_backend.features.document.dto.PageResponse;
import dev.roland.inventory_management_backend.features.document.dto.PreviewLine;
import dev.roland.inventory_management_backend.features.document.dto.PreviewResponse;
import dev.roland.inventory_management_backend.features.document.dto.RelatedDocument;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentAction;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentCategory;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentQueueView;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentRelationType;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentStatus;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentType;
import dev.roland.inventory_management_backend.features.document.exception.DocumentConflictException;
import dev.roland.inventory_management_backend.features.document.facade.DocumentWorkflowFacade;
import dev.roland.inventory_management_backend.features.document.message.DocumentApiMessageKey;
import dev.roland.inventory_management_backend.features.document.service.DocumentPostingService;
import dev.roland.inventory_management_backend.features.document.service.DocumentService;
import dev.roland.inventory_management_backend.features.document.utils.DocumentTotalsCalculator;
import dev.roland.inventory_management_backend.features.document_activity.DocumentActivity;
import dev.roland.inventory_management_backend.features.document_activity.DocumentActivityEvent;
import dev.roland.inventory_management_backend.features.document_activity.service.DocumentActivityService;
import dev.roland.inventory_management_backend.features.document_file.DocumentFile;
import dev.roland.inventory_management_backend.features.document_file.DocumentFileSource;
import dev.roland.inventory_management_backend.features.document_file.DocumentFileStatus;
import dev.roland.inventory_management_backend.features.document_file.service.DocumentFileService;
import dev.roland.inventory_management_backend.features.document_line.DocumentLine;
import dev.roland.inventory_management_backend.features.document_line.service.DocumentLineService;
import dev.roland.inventory_management_backend.features.document_line_allocation.DocumentLineAllocation;
import dev.roland.inventory_management_backend.features.document_line_allocation.service.DocumentLineAllocationService;
import dev.roland.inventory_management_backend.features.document_prefix.DocumentPrefix;
import dev.roland.inventory_management_backend.features.document_prefix.service.DocumentPrefixService;
import dev.roland.inventory_management_backend.features.document_relation.DocumentRelation;
import dev.roland.inventory_management_backend.features.document_relation.service.DocumentRelationService;
import dev.roland.inventory_management_backend.features.document_sequence.DocumentSequence;
import dev.roland.inventory_management_backend.features.document_sequence.service.DocumentSequenceService;
import dev.roland.inventory_management_backend.features.media_asset.MediaAsset;
import dev.roland.inventory_management_backend.features.media_asset.service.MediaAssetService;
import dev.roland.inventory_management_backend.features.media_usage.MediaUsage;
import dev.roland.inventory_management_backend.features.media_usage.enumeration.MediaEntityType;
import dev.roland.inventory_management_backend.features.media_usage.enumeration.MediaUsageType;
import dev.roland.inventory_management_backend.features.media_usage.service.MediaUsageService;
import dev.roland.inventory_management_backend.features.partner.Partner;
import dev.roland.inventory_management_backend.features.partner.service.PartnerService;
import dev.roland.inventory_management_backend.features.product.Product;
import dev.roland.inventory_management_backend.features.product.service.ProductService;
import dev.roland.inventory_management_backend.features.stock_balance.service.StockBalanceService;
import dev.roland.inventory_management_backend.features.unit.Unit;
import dev.roland.inventory_management_backend.features.unit.service.UnitService;
import dev.roland.inventory_management_backend.features.user.User;
import dev.roland.inventory_management_backend.features.user.enumeration.UserRole;
import dev.roland.inventory_management_backend.features.user.service.UserService;
import dev.roland.inventory_management_backend.features.warehouse.Warehouse;
import dev.roland.inventory_management_backend.features.warehouse.service.WarehouseService;
import edu.umd.cs.findbugs.annotations.NonNull;
import lombok.RequiredArgsConstructor;

/** Coordinates company-scoped document workflows. */
@Service
@RequiredArgsConstructor
public class DocumentWorkflowFacadeImpl implements DocumentWorkflowFacade {
  private static final List<DocumentType> RELEASE_ONE_TYPES =
      List.of(
          DocumentType.PURCHASE_ORDER,
          DocumentType.SALES_ORDER,
          DocumentType.GOODS_RECEIPT,
          DocumentType.DELIVERY_NOTE);
  private static final List<DocumentQueueView> VIEWS = List.of(DocumentQueueView.values());

  private final DocumentService documentService;
  private final CurrencyService currencyService;
  private final DocumentLineService lineService;
  private final ProductService productService;
  private final UnitService unitService;
  private final PartnerService partnerService;
  private final ContactService contactService;
  private final WarehouseService warehouseService;
  private final CurrentCompanyProvider companyProvider;
  private final UserService userService;
  private final DocumentPostingService postingService;
  private final DocumentRelationService relationService;
  private final DocumentLineAllocationService allocationService;
  private final DocumentActivityService activityService;
  private final DocumentFileService fileService;
  private final DocumentPrefixService prefixService;
  private final DocumentSequenceService sequenceService;
  private final StockBalanceService balanceService;
  private final MediaAssetService mediaAssetService;
  private final MediaUsageService mediaUsageService;
  private final ObjectStorageService objectStorage;
  private final OutboxService outboxService;

  @Override
  public Capabilities capabilities() {
    final User user = actor();
    return new Capabilities(
        RELEASE_ONE_TYPES.stream().filter(type -> canCreateType(type, user)).toList(), VIEWS);
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
    final User actor = actor();
    final List<Document> all =
        documentService.findAllByCompanyIdOrderByUpdatedAtDesc(company.getId());
    final Map<String, Long> counts = new HashMap<>();
    VIEWS.forEach(
        queueView ->
            counts.put(
                queueView.name(),
                all.stream().filter(d -> matchesView(d, queueView, actor)).count()));
    final String normalizedSearch = search == null ? "" : search.trim().toLowerCase();
    final List<Document> selected =
        all.stream()
            .filter(d -> matchesView(d, selectedView, actor))
            .filter(
                d ->
                    normalizedSearch.isEmpty()
                        || number(d).toLowerCase().contains(normalizedSearch)
                        || (d.getPartnerNameSnapshot() != null
                            && d.getPartnerNameSnapshot().toLowerCase().contains(normalizedSearch)))
            .toList();
    final int from = Math.min(page * pageSize, selected.size());
    final List<DocumentResponse> items =
        selected.subList(from, Math.min(from + pageSize, selected.size())).stream()
            .map(this::toResponse)
            .toList();
    return new PageResponse(items, selected.size(), page, pageSize, counts);
  }

  @Transactional
  @Override
  public DocumentResponse get(final Long id) {
    return toResponse(find(id));
  }

  @Transactional
  @Override
  public DocumentResponse createDraft(final Draft draft) {
    final Company company = companyProvider.getCurrentCompany();
    validateType(draft.type());
    if (!canCreateType(draft.type(), actor())) {
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_TRANSITION);
    }
    if (isDirectOperation(draft.type()) && !directAllowed(draft.type(), actor())) {
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_DIRECT_FULFILLMENT_NOT_ALLOWED);
    }
    final Document document = new Document();
    document.setCompany(company);
    document.setType(draft.type());
    document.setCategory(category(draft.type()));
    document.setStatus(DocumentStatus.DRAFT);
    document.setDocumentDate(draft.date() == null ? LocalDate.now() : draft.date());
    document.setNotes(draft.notes());
    document.setLines(new ArrayList<>());
    document.setCreatedByUser(actor());
    setCurrencyAndRates(document, draft, company);
    if (draft.partnerId() != null) {
      final Partner partner =
          partnerService
              .findActive(draft.partnerId(), company.getId())
              .orElseThrow(() -> new NotFoundException(NotFoundMessageKey.PARTNER));
      document.setPartner(partner);
      document.setPartnerNameSnapshot(partner.getName());
      snapshotPartner(document, partner, company.getId());
    }
    if (draft.warehouseId() != null) {
      setWarehouse(document, draft.type(), draft.warehouseId(), company.getId());
    }
    if (isDirectOperation(draft.type()) && draft.warehouseId() == null) {
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_LINES);
    }
    final Document saved = documentService.save(document);
    applyLines(saved, draft.lines(), company.getId());
    addActivity(saved, DocumentActivityEvent.CREATED, null);
    final Document result = documentService.save(saved);
    documentService.flush();
    return toResponse(result);
  }

  @Transactional
  @Override
  public DocumentResponse updateDraft(final Long id, final DraftUpdate update) {
    final Company company = companyProvider.getCurrentCompany();
    final Document document = lock(id, company.getId());
    if (document.getStatus() != DocumentStatus.DRAFT) {
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_TRANSITION);
    }
    if (hasFulfillmentSource(document)) {
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_TRANSITION);
    }
    if (!canManageDocument(document, actor())) {
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_TRANSITION);
    }
    checkVersion(document, update.version());
    final Draft draft = update.draft();
    validateType(draft.type());
    if (!canCreateType(draft.type(), actor())) {
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_TRANSITION);
    }
    if (isDirectOperation(draft.type()) && !directAllowed(draft.type(), actor())) {
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_DIRECT_FULFILLMENT_NOT_ALLOWED);
    }
    document.setType(draft.type());
    setCurrencyAndRates(document, draft, company);
    document.setCategory(category(draft.type()));
    document.setDocumentDate(draft.date() == null ? document.getDocumentDate() : draft.date());
    document.setNotes(draft.notes());
    document.setPartner(null);
    document.setPartnerNameSnapshot(null);
    document.setPartnerTaxNumberSnapshot(null);
    document.setPartnerAddressSnapshot(null);
    if (draft.partnerId() != null) {
      final Partner partner =
          partnerService
              .findActive(draft.partnerId(), company.getId())
              .orElseThrow(() -> new NotFoundException(NotFoundMessageKey.PARTNER));
      document.setPartner(partner);
      document.setPartnerNameSnapshot(partner.getName());
      snapshotPartner(document, partner, company.getId());
    }
    document.setSourceWarehouse(null);
    document.setTargetWarehouse(null);
    document.setSourceWarehouseCodeSnapshot(null);
    document.setSourceWarehouseNameSnapshot(null);
    document.setTargetWarehouseCodeSnapshot(null);
    document.setTargetWarehouseNameSnapshot(null);
    if (draft.warehouseId() != null) {
      setWarehouse(document, draft.type(), draft.warehouseId(), company.getId());
    }
    if (isDirectOperation(draft.type()) && draft.warehouseId() == null) {
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_LINES);
    }
    applyLines(document, draft.lines(), company.getId());
    addActivity(document, DocumentActivityEvent.EDITED, null);
    touch(document);
    final Document saved = documentService.save(document);
    documentService.flush();
    return toResponse(saved);
  }

  @Transactional
  @Override
  public DocumentResponse createFulfillmentDraft(final FulfillmentDraft request) {
    final Company company = companyProvider.getCurrentCompany();
    if (request.type() != DocumentType.GOODS_RECEIPT
        && request.type() != DocumentType.DELIVERY_NOTE) {
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_TRANSITION);
    }
    if (!directAllowed(request.type(), actor())) {
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_DIRECT_FULFILLMENT_NOT_ALLOWED);
    }
    final Document source =
        request.sourceDocumentId() == null ? null : find(request.sourceDocumentId());
    if (source != null && !isMatchingOrder(source, request.type())) {
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
    operation.setCreatedByUser(actor());
    operation.setType(request.type());
    operation.setCategory(category(request.type()));
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
          safe(source.getExchangeRates()).stream()
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
    setWarehouse(operation, request.type(), warehouseId, company.getId());
    if (source != null
        && warehouseId.equals(
            request.type() == DocumentType.GOODS_RECEIPT
                ? idOf(source.getTargetWarehouse())
                : idOf(source.getSourceWarehouse()))) {
      if (request.type() == DocumentType.GOODS_RECEIPT) {
        operation.setTargetWarehouseNameSnapshot(source.getTargetWarehouseNameSnapshot());
        operation.setTargetWarehouseCodeSnapshot(source.getTargetWarehouseCodeSnapshot());
      } else {
        operation.setSourceWarehouseNameSnapshot(source.getSourceWarehouseNameSnapshot());
        operation.setSourceWarehouseCodeSnapshot(source.getSourceWarehouseCodeSnapshot());
      }
    }
    final Document saved = documentService.save(operation);
    for (FulfillmentLine input : safe(request.lines())) {
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
        addLine(saved, product, unit, input.quantity(), BigDecimal.ZERO);
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
        final DocumentLine line = copyOperationLine(saved, sourceLine, input.quantity());
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
    addActivity(
        saved, DocumentActivityEvent.CREATED, source == null ? null : "source=" + source.getId());
    return toResponse(documentService.save(saved));
  }

  @NonNull
  private static Long getWarehouseId(final FulfillmentDraft request, final Document source) {
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

  @Transactional
  @Override
  public PreviewResponse preview(final FulfillmentPreview request) {
    final Company company = companyProvider.getCurrentCompany();
    final Document source =
        request.sourceDocumentId() == null ? null : find(request.sourceDocumentId());
    final Document operation = request.documentId() == null ? null : find(request.documentId());
    Long warehouseId = request.warehouseId();
    DocumentType type = operation == null ? request.type() : operation.getType();
    if (source != null) {
      type =
          source.getType() == DocumentType.PURCHASE_ORDER
              ? DocumentType.GOODS_RECEIPT
              : DocumentType.DELIVERY_NOTE;
    }
    if (operation != null && !Objects.equals(operation.getCompany().getId(), company.getId())) {
      throw notFound();
    }
    if (warehouseId == null && operation != null) {
      warehouseId =
          operation.getType() == DocumentType.GOODS_RECEIPT
              ? idOf(operation.getTargetWarehouse())
              : idOf(operation.getSourceWarehouse());
    }
    final List<PreviewLine> lines = new ArrayList<>();
    if (warehouseId != null) {
      final Warehouse warehouse =
          warehouseService.findEntityByIdAndCompanyId(warehouseId, company.getId());
      for (FulfillmentLine input : safe(request.lines())) {
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
            input.quantity() == null && line != null ? line.getQuantity() : nz(input.quantity());
        final BigDecimal conversionFactor =
            line == null
                ? expectedFactor(product, product.getUnit())
                : nz(line.getConversionFactorSnapshot());
        final BigDecimal stockQty = operationQty.multiply(conversionFactor);
        final BigDecimal delta = type == DocumentType.DELIVERY_NOTE ? stockQty.negate() : stockQty;
        final BigDecimal available =
            balanceService
                .findByWarehouseAndProduct(warehouse, product)
                .map(b -> nz(b.getQuantity()))
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
              nz(orderLine.getQuantity())
                  .subtract(fulfilledFor(orderLine))
                  .subtract(nz(orderLine.getClosedQuantity()));
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

  @Transactional
  @Override
  public DocumentResponse post(
      final Long id, final String idempotencyKey, final String expectedVersion) {
    if (idempotencyKey == null || idempotencyKey.isBlank()) {
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_IDEMPOTENCY_KEY_REQUIRED);
    }
    final Company company = companyProvider.getCurrentCompany();
    final Document document = lock(id, company.getId());
    DocumentResponse result = toResponse(document);
    if (!documentService.postingKeyExists(id, idempotencyKey)) {
      checkVersion(document, expectedVersion);
      if (!directAllowed(document.getType(), actor())) {
        throw new ApiException(DocumentApiMessageKey.DOCUMENT_DIRECT_FULFILLMENT_NOT_ALLOWED);
      }
      if (document.getStatus() == DocumentStatus.COMPLETED
          || document.getStatus() == DocumentStatus.CANCELLED) {
        throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_TRANSITION);
      }
      if (!isDirectOperation(document.getType())
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
      if (!allocations.isEmpty()) {
        final Map<Long, BigDecimal> requested = new HashMap<>();
        allocations.forEach(
            a -> requested.merge(a.getSourceOrderLine().getId(), a.getQuantity(), BigDecimal::add));
        for (DocumentLineAllocation allocation : allocations) {
          final Document source =
              lock(allocation.getSourceOrderLine().getDocument().getId(), company.getId());
          final DocumentLine sourceLine =
              source.getLines().stream()
                  .filter(l -> l.getId().equals(allocation.getSourceOrderLine().getId()))
                  .findFirst()
                  .orElseThrow(
                      () -> new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_LINES));
          final BigDecimal remaining =
              sourceLine
                  .getQuantity()
                  .subtract(fulfilledFor(sourceLine))
                  .subtract(nz(sourceLine.getClosedQuantity()));
          final BigDecimal requestQty = requested.get(sourceLine.getId());
          if (requestQty.compareTo(remaining) > 0) {
            throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_LINES);
          }
        }
      }
      final PreviewResponse preview =
          preview(
              new FulfillmentPreview(
                  null,
                  id,
                  document.getType(),
                  document.getType() == DocumentType.GOODS_RECEIPT
                      ? idOf(document.getTargetWarehouse())
                      : idOf(document.getSourceWarehouse()),
                  document.getLines().stream()
                      .map(
                          l ->
                              new FulfillmentLine(
                                  l.getId(), l.getProduct().getId(), l.getQuantity()))
                      .toList()));
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
      allocateNumber(document);
      documentService.savePostingKey(postingKey(document, idempotencyKey));
      for (DocumentLine line : document.getLines()) {
        final List<DocumentLineAllocation> lineAllocations =
            allocationService.findAllByFulfillmentLineId(line.getId());
        for (DocumentLineAllocation allocation : lineAllocations) {
          final DocumentLine sourceLine = allocation.getSourceOrderLine();
          sourceLine.setFulfilledQuantity(
              nz(sourceLine.getFulfilledQuantity()).add(allocation.getQuantity()));
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
              remaining(source) ? DocumentStatus.PARTIALLY_COMPLETED : DocumentStatus.COMPLETED);
          documentService.save(source);
        }
      }
      addActivity(posted, DocumentActivityEvent.POSTED, idempotencyKey);
      final Document saved = documentService.save(posted);
      documentService.flush();
      result = toResponse(saved);
    }
    return result;
  }

  @Transactional
  @Override
  public DocumentResponse confirm(final Long id, final String expectedVersion) {
    final Company company = companyProvider.getCurrentCompany();
    final Document document = lock(id, company.getId());
    checkVersion(document, expectedVersion);
    if (!canManageDocument(document, actor())) {
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
    allocateNumber(document);
    document.setStatus(DocumentStatus.CONFIRMED);
    addActivity(document, DocumentActivityEvent.CONFIRMED, null);
    final Document saved = documentService.save(document);
    documentService.flush();
    return toResponse(saved);
  }

  @Transactional
  @Override
  public DocumentResponse closeRemaining(
      final Long id, final CloseRemaining request, final String expectedVersion) {
    final Company company = companyProvider.getCurrentCompany();
    final Document order = lock(id, company.getId());
    checkVersion(order, expectedVersion);
    if (!canManageDocument(order, actor())) {
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
          nz(line.getQuantity())
              .subtract(fulfilledFor(line))
              .subtract(nz(line.getClosedQuantity()));
      if (remaining.signum() > 0) {
        line.setClosedQuantity(nz(line.getClosedQuantity()).add(remaining));
        line.setClosureReason(request.reason().trim());
        lineService.save(line);
      }
    }
    order.setStatus(
        remaining(order) ? DocumentStatus.PARTIALLY_COMPLETED : DocumentStatus.COMPLETED);
    addActivity(order, DocumentActivityEvent.REMAINDER_CLOSED, request.reason().trim());
    touch(order);
    final Document saved = documentService.save(order);
    documentService.flush();
    return toResponse(saved);
  }

  @Transactional
  @Override
  public DocumentResponse uploadFile(final Long id, final MultipartFile file) {
    final Document document = find(id);
    if (!canManageDocument(document, actor())) {
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_TRANSITION);
    }
    if (document.getStatus() == DocumentStatus.CANCELLED) {
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_TRANSITION);
    }
    if (file == null || file.isEmpty()) {
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_LINES);
    }
    try {
      final String filename = file.getOriginalFilename();
      final String original =
          filename == null ? "upload" : filename.replaceAll("[^a-zA-Z0-9._-]", "_");
      final String objectName = UUID.randomUUID() + "-" + original;
      final String path =
          objectStorage.generateSolidObjectPath(
              objectName, MediaEntityType.DOCUMENT, document.getId(), MediaUsageType.DOCUMENT_FILE);
      objectStorage.upload(
          path,
          file.getSize(),
          file.getContentType() == null ? "application/octet-stream" : file.getContentType(),
          file.getInputStream());
      final MediaAsset asset =
          mediaAssetService.save(
              MediaAsset.builder()
                  .objectPath(path)
                  .filename(original)
                  .mimeType(
                      file.getContentType() == null
                          ? "application/octet-stream"
                          : file.getContentType())
                  .fileSize(file.getSize())
                  .uploadedBy(actor())
                  .build());
      final MediaUsage usage =
          MediaUsage.builder()
              .mediaAsset(asset)
              .entityType(MediaEntityType.DOCUMENT)
              .entityId(id)
              .usageType(MediaUsageType.DOCUMENT_FILE)
              .build();
      mediaUsageService.save(usage);
      final DocumentFile entry = new DocumentFile();
      entry.setDocument(document);
      entry.setMediaAsset(asset);
      entry.setSource(DocumentFileSource.UPLOAD);
      fileService.save(entry);
      addActivity(document, DocumentActivityEvent.FILE_UPLOADED, original);
      return toResponse(document);
    } catch (IOException ex) {
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_LINES);
    }
  }

  @Transactional
  @Override
  public DocumentResponse generatePdf(
      final Long id, final String idempotencyKey, final String language) {
    return queuePdf(id, idempotencyKey, normalizePdfLanguage(language));
  }

  @Transactional
  @Override
  public DocumentResponse retryPdf(final Long id, final String idempotencyKey) {
    return queuePdf(id, idempotencyKey, null);
  }

  private DocumentResponse queuePdf(
      final Long id, final String idempotencyKey, final String requestedLanguage) {
    final Company company = companyProvider.getCurrentCompany();
    final Document document = lock(id, company.getId());
    if (!canManageDocument(document, actor())) {
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_TRANSITION);
    }
    if (document.getStatus() == DocumentStatus.DRAFT
        || document.getStatus() == DocumentStatus.CANCELLED) {
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_TRANSITION);
    }
    final String key =
        idempotencyKey == null || idempotencyKey.isBlank()
            ? UUID.randomUUID().toString()
            : idempotencyKey;
    final boolean duplicateRequest =
        idempotencyKey != null
            && fileService.findByDocumentIdAndIdempotencyKey(id, idempotencyKey).isPresent();
    final boolean busy =
        fileService.existsByDocumentIdAndSourceAndStatusIn(
            id,
            DocumentFileSource.GENERATED_PDF,
            List.of(DocumentFileStatus.QUEUED, DocumentFileStatus.PROCESSING));
    if (!duplicateRequest && !busy) {
      final List<DocumentFile> prior =
          fileService.findAllByDocumentIdOrderByCreatedAtDesc(id).stream()
              .filter(f -> f.getSource() == DocumentFileSource.GENERATED_PDF)
              .toList();
      final String languageTag =
          requestedLanguage == null
              ? prior.stream()
                  .map(DocumentFile::getLanguageTag)
                  .filter(Objects::nonNull)
                  .findFirst()
                  .orElse("en")
              : requestedLanguage;
      final DocumentFile attempt = new DocumentFile();
      attempt.setDocument(document);
      attempt.setSource(DocumentFileSource.GENERATED_PDF);
      attempt.setStatus(DocumentFileStatus.QUEUED);
      attempt.setLanguageTag(languageTag);
      attempt.setIdempotencyKey(key);
      attempt.setAttempt(prior.size() + 1);
      if (!prior.isEmpty()) {
        attempt.setParentFile(prior.getFirst());
      }
      final DocumentFile saved = fileService.save(attempt);
      outboxService.enqueue(
          AsyncJobType.DOCUMENT_PDF,
          Map.of("documentId", id, "fileId", saved.getId(), "idempotencyKey", key),
          company.getId(),
          id);
      addActivity(document, DocumentActivityEvent.PDF_QUEUED, String.valueOf(saved.getId()));
    }
    return toResponse(document);
  }

  private String normalizePdfLanguage(final String language) {
    final String languageTag =
        language == null || language.isBlank()
            ? "en"
            : Locale.forLanguageTag(language.trim()).getLanguage();
    if (!"en".equals(languageTag) && !"hu".equals(languageTag)) {
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_PDF_LANGUAGE);
    }
    return languageTag;
  }

  private void applyLines(
      final Document document, final List<LineDraft> drafts, final Long companyId) {
    final List<DocumentLine> lines = new ArrayList<>();
    for (LineDraft draft : safe(drafts)) {
      if (draft == null || draft.productId() == null || draft.unitId() == null) {
        continue;
      }
      final Product product =
          productService
              .findByIdAndCompanyId(draft.productId(), companyId)
              .orElseThrow(() -> new NotFoundException(NotFoundMessageKey.PRODUCT));
      final Unit unit =
          unitService
              .findById(draft.unitId())
              .orElseThrow(() -> new NotFoundException(NotFoundMessageKey.UNIT));
      if (unit.getCompany() != null && !companyId.equals(unit.getCompany().getId())) {
        throw new ApiException(DocumentApiMessageKey.DOCUMENT_CROSS_COMPANY_REFERENCE);
      }
      final BigDecimal quantity = nz(draft.quantity());
      final Currency sourceCurrency =
          product.getCurrency() == null
              ? document.getCompany().getPreferredCurrency()
              : product.getCurrency();
      if (sourceCurrency == null || document.getCurrency() == null) {
        throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_LINES);
      }
      final BigDecimal rate =
          sourceCurrency.getId().equals(document.getCurrency().getId())
              ? BigDecimal.ONE
              : safe(document.getExchangeRates()).stream()
                  .filter(r -> r.getSourceCurrency().getId().equals(sourceCurrency.getId()))
                  .map(DocumentExchangeRate::getRate)
                  .findFirst()
                  .orElseThrow(
                      () -> new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_LINES));
      final BigDecimal sourceUnitPrice =
          draft.sourceUnitPrice() == null ? nz(draft.unitPrice()) : nz(draft.sourceUnitPrice());
      final DocumentLine line =
          buildLine(document, product, unit, quantity, sourceUnitPrice.multiply(rate));
      line.setCurrency(document.getCurrency());
      line.setSourceCurrency(sourceCurrency);
      line.setExchangeRateSnapshot(rate);
      line.setSourceUnitPrice(sourceUnitPrice);
      lines.add(line);
    }
    final Set<Long> requiredRates =
        lines.stream()
            .map(DocumentLine::getSourceCurrency)
            .map(Currency::getId)
            .filter(id -> !id.equals(document.getCurrency().getId()))
            .collect(Collectors.toSet());
    final Set<Long> submittedRates =
        safe(document.getExchangeRates()).stream()
            .map(r -> r.getSourceCurrency().getId())
            .collect(Collectors.toSet());
    if (!requiredRates.equals(submittedRates)) {
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_LINES);
    }
    if (document.getLines() == null) {
      document.setLines(new ArrayList<>());
    }
    document.getLines().clear();
    document.getLines().addAll(lines);
  }

  private void setCurrencyAndRates(
      final Document document, final Draft draft, final Company company) {
    final Long requested = draft.currencyId();
    final Currency target =
        requested == null
            ? company.getPreferredCurrency()
            : currencyService
                .findById(requested)
                .orElseThrow(() -> new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_LINES));
    if (target == null) {
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_LINES);
    }
    if (!target
            .getId()
            .equals(
                company.getPreferredCurrency() == null
                    ? null
                    : company.getPreferredCurrency().getId())
        && actor().getRole() != UserRole.ADMIN) {
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_TRANSITION);
    }
    document.setCurrency(target);
    final List<DocumentExchangeRate> saved = new ArrayList<>();
    for (ExchangeRateDraft entry : safe(draft.exchangeRates())) {
      if (entry == null
          || entry.sourceCurrencyId() == null
          || entry.rate() == null
          || entry.rate().signum() <= 0
          || entry.sourceCurrencyId().equals(target.getId())
          || saved.stream()
              .anyMatch(r -> r.getSourceCurrency().getId().equals(entry.sourceCurrencyId()))) {
        throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_LINES);
      }
      final Currency source =
          currencyService
              .findById(entry.sourceCurrencyId())
              .orElseThrow(() -> new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_LINES));
      final DocumentExchangeRate rate = new DocumentExchangeRate();
      rate.setDocument(document);
      rate.setSourceCurrency(source);
      rate.setRate(entry.rate());
      saved.add(rate);
    }
    document.setExchangeRates(saved);
    document.setExchangeRate(null);
  }

  private void addLine(
      final Document doc,
      final Product product,
      final Unit unit,
      final BigDecimal quantity,
      final BigDecimal price) {
    final DocumentLine line = buildLine(doc, product, unit, quantity, price);
    line.setSourceUnitPrice(price);
    line.setCurrency(doc.getCurrency());
    line.setSourceCurrency(doc.getCurrency());
    line.setExchangeRateSnapshot(BigDecimal.ONE);
    doc.getLines().add(line);
    lineService.save(line);
  }

  private DocumentLine copyOperationLine(
      final Document doc, final DocumentLine source, final BigDecimal quantity) {
    final DocumentLine line =
        DocumentLine.builder()
            .document(doc)
            .product(source.getProduct())
            .unitSnapshot(source.getUnitSnapshot())
            .unitCodeSnapshot(source.getUnitCodeSnapshot())
            .unitNameSnapshot(source.getUnitNameSnapshot())
            .conversionFactorSnapshot(source.getConversionFactorSnapshot())
            .quantity(quantity)
            .fulfilledQuantity(BigDecimal.ZERO)
            .unitPrice(nz(source.getUnitPrice()))
            .sourceUnitPrice(nz(source.getSourceUnitPrice()))
            .totalPrice(nz(source.getUnitPrice()).multiply(quantity))
            .currency(source.getCurrency())
            .sourceCurrency(source.getSourceCurrency())
            .exchangeRateSnapshot(source.getExchangeRateSnapshot())
            .productNameSnapshot(source.getProductNameSnapshot())
            .productSkuSnapshot(source.getProductSkuSnapshot())
            .vatRateSnapshot(source.getVatRateSnapshot())
            .build();
    doc.getLines().add(line);
    lineService.save(line);

    return line;
  }

  private DocumentLine buildLine(
      final Document doc,
      final Product product,
      final Unit unit,
      final BigDecimal quantity,
      final BigDecimal price) {
    return DocumentLine.builder()
        .document(doc)
        .product(product)
        .unitSnapshot(unit)
        .unitCodeSnapshot(unit.getCode())
        .unitNameSnapshot(unit.getName())
        .conversionFactorSnapshot(expectedFactor(product, unit))
        .quantity(quantity)
        .fulfilledQuantity(BigDecimal.ZERO)
        .unitPrice(price)
        .sourceUnitPrice(price)
        .totalPrice(price.multiply(quantity))
        .productNameSnapshot(product.getName())
        .productSkuSnapshot(product.getSku())
        .vatRateSnapshot(product.getVatRate())
        .build();
  }

  private BigDecimal expectedFactor(final Product product, final Unit unit) {
    return product.getUnit() != null
            && product.getUnit().getId().equals(unit.getId())
            && product.getSecondaryUnit() != null
        ? product.getSecondaryUnitsPerMainUnit()
        : BigDecimal.ONE;
  }

  private void setWarehouse(
      final Document document,
      final DocumentType type,
      final Long warehouseId,
      final Long companyId) {
    final Warehouse warehouse = warehouseService.findEntityByIdAndCompanyId(warehouseId, companyId);
    if (type == DocumentType.GOODS_RECEIPT || type == DocumentType.PURCHASE_ORDER) {
      document.setTargetWarehouse(warehouse);
      document.setTargetWarehouseCodeSnapshot(warehouse.getCode());
      document.setTargetWarehouseNameSnapshot(warehouse.getName());
    } else if (type == DocumentType.DELIVERY_NOTE || type == DocumentType.SALES_ORDER) {
      document.setSourceWarehouse(warehouse);
      document.setSourceWarehouseCodeSnapshot(warehouse.getCode());
      document.setSourceWarehouseNameSnapshot(warehouse.getName());
    }
  }

  private void snapshotPartner(
      final Document document, final Partner partner, final Long companyId) {
    document.setPartnerTaxNumberSnapshot(partner.getTaxNumber());
    final Contact address =
        contactService.list(partner.getId(), companyId).stream()
            .filter(c -> c.getType() == ContactType.BILLING || c.getType() == ContactType.BOTH)
            .findFirst()
            .orElseGet(
                () ->
                    contactService.list(partner.getId(), companyId).stream()
                        .findFirst()
                        .orElse(null));
    if (address != null) {
      document.setPartnerAddressSnapshot(
          String.join(
              "\n",
              Stream.of(
                      address.getAddressLine1(),
                      address.getAddressLine2(),
                      address.getCity(),
                      address.getCountry())
                  .filter(Objects::nonNull)
                  .filter(s -> !s.isBlank())
                  .toList()));
    }
  }

  private DocumentResponse toResponse(final Document doc) {
    final List<LineResponse> lines =
        safe(doc.getLines()).stream()
            .map(
                line -> {
                  final DocumentLine sourceLine =
                      allocationService.findAllByFulfillmentLineId(line.getId()).stream()
                          .map(DocumentLineAllocation::getSourceOrderLine)
                          .findFirst()
                          .orElse(null);
                  final DocumentLine totalsLine = sourceLine == null ? line : sourceLine;
                  final BigDecimal ordered = nz(totalsLine.getQuantity());
                  final BigDecimal fulfilled = fulfilledFor(totalsLine);
                  final BigDecimal closed = nz(totalsLine.getClosedQuantity());
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
                          ? nz(line.getQuantity())
                          : sourceLine == null ? null : nz(line.getQuantity()),
                      line.getUnitPrice(),
                      line.getSourceUnitPrice(),
                      sourceLine == null ? null : sourceLine.getId());
                })
            .toList();
    final List<RelatedDocument> related =
        relationService
            .findAllBySourceDocumentIdOrTargetDocumentId(doc.getId(), doc.getId())
            .stream()
            .map(
                r ->
                    r.getSourceDocument().getId().equals(doc.getId())
                        ? r.getTargetDocument()
                        : r.getSourceDocument())
            .distinct()
            .map(
                d ->
                    new RelatedDocument(
                        d.getId(), number(d), d.getType(), DocumentRelationType.FULFILLS.name()))
            .toList();
    final List<Activity> activity =
        activityService.findAllByDocumentIdOrderByOccurredAtAsc(doc.getId()).stream()
            .map(
                a ->
                    new Activity(
                        a.getId(),
                        a.getEventType() + (a.getDetails() == null ? "" : ": " + a.getDetails()),
                        a.getActor() == null ? "System" : a.getActor().getUsername(),
                        a.getOccurredAt()))
            .toList();
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
    final DocumentTotalsCalculator.Totals totals =
        DocumentTotalsCalculator.calculate(safe(doc.getLines()));
    return new DocumentResponse(
        doc.getId(),
        doc.getType(),
        number(doc),
        doc.getDocumentDate(),
        doc.getCreatedByUser() == null ? null : doc.getCreatedByUser().getUsername(),
        doc.getPartnerNameSnapshot(),
        doc.getSourceWarehouseNameSnapshot() != null
            ? doc.getSourceWarehouseNameSnapshot()
            : doc.getTargetWarehouseNameSnapshot(),
        doc.getPartner() == null ? null : doc.getPartner().getId(),
        doc.getSourceWarehouse() != null
            ? doc.getSourceWarehouse().getId()
            : idOf(doc.getTargetWarehouse()),
        doc.getNotes(),
        doc.getCurrency() == null ? null : doc.getCurrency().getCode(),
        doc.getCurrency() == null ? null : doc.getCurrency().getId(),
        safe(doc.getExchangeRates()).stream()
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

  private List<DocumentAction> allowedActions(final Document doc) {
    final List<DocumentAction> actions = new ArrayList<>();
    final User user = actor();
    if (doc.getStatus() == DocumentStatus.DRAFT
        && canManageDocument(doc, user)
        && !hasFulfillmentSource(doc)) {
      actions.add(DocumentAction.EDIT);
    }
    if (doc.getStatus() == DocumentStatus.DRAFT
        && canManageDocument(doc, user)
        && (doc.getType() == DocumentType.PURCHASE_ORDER
            || doc.getType() == DocumentType.SALES_ORDER)) {
      actions.add(DocumentAction.CONFIRM);
    }
    if ((doc.getType() == DocumentType.GOODS_RECEIPT || doc.getType() == DocumentType.DELIVERY_NOTE)
        && doc.getStatus() == DocumentStatus.DRAFT
        && directAllowed(doc.getType(), user)) {
      actions.add(DocumentAction.POST);
    }
    if (doc.getStatus() != DocumentStatus.CANCELLED && canManageDocument(doc, user)) {
      actions.add(DocumentAction.UPLOAD_FILE);
    }
    if (doc.getStatus() != DocumentStatus.DRAFT
        && doc.getStatus() != DocumentStatus.CANCELLED
        && canManageDocument(doc, user)) {
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
        && canManageDocument(doc, user)) {
      actions.add(DocumentAction.CLOSE_REMAINING);
    }
    return actions;
  }

  private void allocateNumber(final Document document) {
    if (document.getDocumentNumber() != null) {
      return;
    }
    final Long companyId = document.getCompany().getId();
    final int year = document.getDocumentDate().getYear();
    final DocumentPrefix prefix =
        prefixService
            .findFirstByCompanyIdAndDocumentTypeOrderByIdAsc(companyId, document.getType())
            .orElseGet(
                () ->
                    prefixService.save(
                        DocumentPrefix.builder()
                            .company(document.getCompany())
                            .documentType(document.getType())
                            .prefix(document.getType().getDefaultPrefix())
                            .build()));
    final String prefixText = prefix.getPrefix();
    sequenceService.ensureSequenceExists(companyId, prefix.getId(), year);
    final DocumentSequence sequence =
        sequenceService
            .lockByScope(companyId, prefix.getId(), year)
            .orElseThrow(
                () -> new ApiException(DocumentApiMessageKey.DOCUMENT_SEQUENCE_UNAVAILABLE));
    sequence.setCounter(sequence.getCounter() + 1);
    document.setDocumentSequence(sequence);
    document.setDocumentNumber(
        prefixText + "-" + year + "-" + String.format("%05d", sequence.getCounter()));
  }

  private void addActivity(
      final Document doc, final DocumentActivityEvent type, final String details) {
    final DocumentActivity event = new DocumentActivity();
    event.setDocument(doc);
    event.setActor(actor());
    event.setEventType(type);
    event.setDetails(details);
    activityService.save(event);
  }

  private void touch(final Document document) {
    document.setUpdatedAt(LocalDateTime.now());
  }

  private DocumentPostingKey postingKey(final Document document, final String key) {
    final DocumentPostingKey value = new DocumentPostingKey();
    value.setDocument(document);
    value.setIdempotencyKey(key);
    return value;
  }

  private DocumentQueueView queueView(final String view) {
    return view == null || view.isBlank()
        ? DocumentQueueView.ALL
        : VIEWS.stream()
            .filter(queueView -> queueView.name().equals(view))
            .findFirst()
            .orElseThrow(() -> new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_QUEUE_VIEW));
  }

  private boolean matchesView(final Document d, final DocumentQueueView view, final User actor) {
    return switch (view) {
      case MY_DRAFTS -> d.getStatus() == DocumentStatus.DRAFT
          && d.getCreatedByUser() != null
          && d.getCreatedByUser().getId().equals(actor.getId());
      case READY_TO_RECEIVE -> d.getType() == DocumentType.PURCHASE_ORDER
          && (d.getStatus() == DocumentStatus.CONFIRMED
              || d.getStatus() == DocumentStatus.PARTIALLY_COMPLETED)
          && remaining(d);
      case READY_TO_SHIP -> d.getType() == DocumentType.SALES_ORDER
          && (d.getStatus() == DocumentStatus.CONFIRMED
              || d.getStatus() == DocumentStatus.PARTIALLY_COMPLETED)
          && remaining(d);
      case PARTIALLY_FULFILLED -> d.getStatus() == DocumentStatus.PARTIALLY_COMPLETED;
      case ALL -> true;
    };
  }

  private boolean remaining(final Document d) {
    return safe(d.getLines()).stream()
        .anyMatch(
            l -> nz(l.getQuantity()).compareTo(fulfilledFor(l).add(nz(l.getClosedQuantity()))) > 0);
  }

  private BigDecimal fulfilledFor(final DocumentLine line) {
    return line.getId() == null
        ? nz(line.getFulfilledQuantity())
        : nz(allocationService.sumPostedAllocated(line.getId(), DocumentStatus.COMPLETED));
  }

  private boolean isMatchingOrder(final Document source, final DocumentType operation) {
    return operation == DocumentType.GOODS_RECEIPT
        ? source.getType() == DocumentType.PURCHASE_ORDER
        : source.getType() == DocumentType.SALES_ORDER;
  }

  private boolean hasFulfillmentSource(final Document document) {
    return relationService
        .findAllBySourceDocumentIdOrTargetDocumentId(document.getId(), document.getId())
        .stream()
        .anyMatch(
            relation ->
                relation.getRelationType() == DocumentRelationType.FULFILLS
                    && relation.getTargetDocument().getId().equals(document.getId()));
  }

  private boolean isDirectOperation(final DocumentType type) {
    return type == DocumentType.GOODS_RECEIPT || type == DocumentType.DELIVERY_NOTE;
  }

  private boolean directAllowed(final DocumentType type, final User user) {
    return user != null
        && (user.getRole() == UserRole.ADMIN
            || user.getRole() == UserRole.MANAGER
            || (type == DocumentType.DELIVERY_NOTE && user.getRole() == UserRole.SALES));
  }

  private boolean canCreateType(final DocumentType type, final User user) {
    return user != null
        && (user.getRole() == UserRole.ADMIN
            || user.getRole() == UserRole.MANAGER
            || (user.getRole() == UserRole.SALES
                && (type == DocumentType.SALES_ORDER || type == DocumentType.DELIVERY_NOTE)));
  }

  private boolean canManageDocument(final Document document, final User user) {
    return user != null
        && (user.getRole() == UserRole.ADMIN
            || user.getRole() == UserRole.MANAGER
            || (document.getCreatedByUser() != null
                && document.getCreatedByUser().getId().equals(user.getId())));
  }

  private void checkVersion(final Document document, final String suppliedVersion) {
    if (suppliedVersion == null || suppliedVersion.isBlank()) {
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_VERSION_REQUIRED);
    }
    String version = suppliedVersion.trim();
    if (version.startsWith("W/")) {
      version = version.substring(2);
    }
    version = version.replace("\"", "");
    if (!Objects.equals(String.valueOf(document.getVersion()), version)) {
      throw new DocumentConflictException(toResponse(document));
    }
  }

  private DocumentCategory category(final DocumentType type) {
    return type == DocumentType.PURCHASE_ORDER || type == DocumentType.GOODS_RECEIPT
        ? DocumentCategory.PROCUREMENT
        : DocumentCategory.SALES;
  }

  private String number(final Document d) {
    return d.getDocumentNumber() == null
        ? d.getReferenceNumber() == null ? "" : d.getReferenceNumber()
        : d.getDocumentNumber();
  }

  private Long idOf(final Warehouse w) {
    return w == null ? null : w.getId();
  }

  private BigDecimal nz(final BigDecimal value) {
    return value == null ? BigDecimal.ZERO : value;
  }

  private <T> List<T> safe(final List<T> values) {
    return values == null ? List.of() : values;
  }

  private void validateType(final DocumentType type) {
    if (!RELEASE_ONE_TYPES.contains(type)) {
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_TRANSITION);
    }
  }

  private User actor() {
    final var authentication = SecurityContextHolder.getContext().getAuthentication();
    User result = null;
    if (authentication != null
        && authentication.getPrincipal() instanceof CustomUserDetails principal) {
      result = userService.findByIdOrThrow(principal.getUserId());
    }
    return result;
  }

  private Document find(final Long id) {
    return documentService
        .findByIdAndCompanyId(id, companyProvider.getCurrentCompany().getId())
        .orElseThrow(this::notFound);
  }

  private Document lock(final Long id, final Long companyId) {
    return documentService.lockByIdAndCompanyId(id, companyId).orElseThrow(this::notFound);
  }

  private NotFoundException notFound() {
    return new NotFoundException(NotFoundMessageKey.DOCUMENT);
  }
}
