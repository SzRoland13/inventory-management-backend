package dev.roland.inventory_management_backend.features.document.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.common.exception.ApiException;
import dev.roland.inventory_management_backend.common.exception.NotFoundException;
import dev.roland.inventory_management_backend.common.message.NotFoundMessageKey;
import dev.roland.inventory_management_backend.common.service.CurrentCompanyProvider;
import dev.roland.inventory_management_backend.features.company.Company;
import dev.roland.inventory_management_backend.features.contact.Contact;
import dev.roland.inventory_management_backend.features.contact.enumeration.ContactType;
import dev.roland.inventory_management_backend.features.contact.service.ContactService;
import dev.roland.inventory_management_backend.features.currency.Currency;
import dev.roland.inventory_management_backend.features.currency.service.CurrencyService;
import dev.roland.inventory_management_backend.features.document.Document;
import dev.roland.inventory_management_backend.features.document.DocumentExchangeRate;
import dev.roland.inventory_management_backend.features.document.dto.DocumentResponse;
import dev.roland.inventory_management_backend.features.document.dto.Draft;
import dev.roland.inventory_management_backend.features.document.dto.DraftUpdate;
import dev.roland.inventory_management_backend.features.document.dto.ExchangeRateDraft;
import dev.roland.inventory_management_backend.features.document.dto.LineDraft;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentStatus;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentType;
import dev.roland.inventory_management_backend.features.document.mapper.DocumentResponseMapper;
import dev.roland.inventory_management_backend.features.document.message.DocumentApiMessageKey;
import dev.roland.inventory_management_backend.features.document.service.DocumentService;
import dev.roland.inventory_management_backend.features.document.utils.DocumentWorkflowValues;
import dev.roland.inventory_management_backend.features.document_activity.DocumentActivityEvent;
import dev.roland.inventory_management_backend.features.document_line.DocumentLine;
import dev.roland.inventory_management_backend.features.document_line.service.DocumentLineService;
import dev.roland.inventory_management_backend.features.partner.Partner;
import dev.roland.inventory_management_backend.features.partner.service.PartnerService;
import dev.roland.inventory_management_backend.features.product.Product;
import dev.roland.inventory_management_backend.features.product.service.ProductService;
import dev.roland.inventory_management_backend.features.unit.Unit;
import dev.roland.inventory_management_backend.features.unit.service.UnitService;
import dev.roland.inventory_management_backend.features.user.enumeration.UserRole;
import dev.roland.inventory_management_backend.features.warehouse.Warehouse;
import dev.roland.inventory_management_backend.features.warehouse.service.WarehouseService;
import lombok.RequiredArgsConstructor;

/** Creates and updates document drafts and their line snapshots. */
@Service
@RequiredArgsConstructor
public class DocumentDraftWorkflowService {
  private final DocumentWorkflowAccessService accessService;
  private final DocumentWorkflowWriterService writer;
  private final DocumentResponseMapper responseMapper;
  private final DocumentService documentService;
  private final DocumentLineService lineService;
  private final PartnerService partnerService;
  private final ContactService contactService;
  private final WarehouseService warehouseService;
  private final CurrentCompanyProvider companyProvider;
  private final CurrencyService currencyService;
  private final ProductService productService;
  private final UnitService unitService;
  private static final List<DocumentType> SUPPORTED_DRAFT_TYPES =
      List.of(
          DocumentType.PURCHASE_ORDER,
          DocumentType.SALES_ORDER,
          DocumentType.GOODS_RECEIPT,
          DocumentType.DELIVERY_NOTE);

  private void validateType(final DocumentType type) {
    if (!SUPPORTED_DRAFT_TYPES.contains(type)) {
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_TRANSITION);
    }
  }

  @Transactional
  public DocumentResponse createDraft(final Draft draft) {
    final Company company = companyProvider.getCurrentCompany();
    validateType(draft.type());
    if (!accessService.canCreateType(draft.type(), accessService.currentActor())) {
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_TRANSITION);
    }
    if (accessService.isDirectOperation(draft.type())
        && !accessService.directAllowed(draft.type(), accessService.currentActor())) {
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_DIRECT_FULFILLMENT_NOT_ALLOWED);
    }
    final Document document = new Document();
    document.setCompany(company);
    document.setType(draft.type());
    document.setCategory(DocumentWorkflowValues.category(draft.type()));
    document.setStatus(DocumentStatus.DRAFT);
    document.setDocumentDate(draft.date() == null ? LocalDate.now() : draft.date());
    document.setNotes(draft.notes());
    document.setLines(new ArrayList<>());
    document.setCreatedByUser(accessService.currentActor());
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
    if (accessService.isDirectOperation(draft.type()) && draft.warehouseId() == null) {
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_LINES);
    }
    final Document saved = documentService.save(document);
    applyLines(saved, draft.lines(), company.getId());
    writer.addActivity(saved, DocumentActivityEvent.CREATED, null);
    final Document result = documentService.save(saved);
    documentService.flush();
    return responseMapper.toResponse(result);
  }

  @Transactional
  public DocumentResponse updateDraft(final Long id, final DraftUpdate update) {
    final Company company = companyProvider.getCurrentCompany();
    final Document document = accessService.lock(id, company.getId());
    if (document.getStatus() != DocumentStatus.DRAFT) {
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_TRANSITION);
    }
    if (accessService.hasFulfillmentSource(document)) {
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_TRANSITION);
    }
    if (!accessService.canManageDocument(document, accessService.currentActor())) {
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_TRANSITION);
    }
    accessService.checkVersion(
        document, update.version(), () -> responseMapper.toResponse(document));
    final Draft draft = update.draft();
    validateType(draft.type());
    if (!accessService.canCreateType(draft.type(), accessService.currentActor())) {
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_TRANSITION);
    }
    if (accessService.isDirectOperation(draft.type())
        && !accessService.directAllowed(draft.type(), accessService.currentActor())) {
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_DIRECT_FULFILLMENT_NOT_ALLOWED);
    }
    document.setType(draft.type());
    setCurrencyAndRates(document, draft, company);
    document.setCategory(DocumentWorkflowValues.category(draft.type()));
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
    if (accessService.isDirectOperation(draft.type()) && draft.warehouseId() == null) {
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_LINES);
    }
    applyLines(document, draft.lines(), company.getId());
    writer.addActivity(document, DocumentActivityEvent.EDITED, null);
    writer.touch(document);
    final Document saved = documentService.save(document);
    documentService.flush();
    return responseMapper.toResponse(saved);
  }

  public void applyLines(
      final Document document, final List<LineDraft> drafts, final Long companyId) {
    final List<DocumentLine> lines = new ArrayList<>();
    for (LineDraft draft : DocumentWorkflowValues.safe(drafts)) {
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
      final BigDecimal quantity = DocumentWorkflowValues.nz(draft.quantity());
      final Currency sourceCurrency =
          draft.sourceCurrencyId() == null
              ? product.getCurrency() == null
                  ? document.getCompany().getPreferredCurrency()
                  : product.getCurrency()
              : currencyService
                  .findById(draft.sourceCurrencyId())
                  .orElseThrow(
                      () -> new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_LINES));
      if (sourceCurrency == null || document.getCurrency() == null) {
        throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_LINES);
      }
      final BigDecimal rate =
          sourceCurrency.getId().equals(document.getCurrency().getId())
              ? BigDecimal.ONE
              : DocumentWorkflowValues.safe(document.getExchangeRates()).stream()
                  .filter(r -> r.getSourceCurrency().getId().equals(sourceCurrency.getId()))
                  .map(DocumentExchangeRate::getRate)
                  .findFirst()
                  .orElseThrow(
                      () -> new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_LINES));
      final BigDecimal sourceUnitPrice =
          draft.sourceUnitPrice() == null
              ? DocumentWorkflowValues.nz(draft.unitPrice())
              : DocumentWorkflowValues.nz(draft.sourceUnitPrice());
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
        DocumentWorkflowValues.safe(document.getExchangeRates()).stream()
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

  public void setCurrencyAndRates(
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
        && accessService.currentActor().getRole() != UserRole.ADMIN) {
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_TRANSITION);
    }
    document.setCurrency(target);
    final Map<Long, DocumentExchangeRate> saved = new HashMap<>();
    for (ExchangeRateDraft entry : DocumentWorkflowValues.safe(draft.exchangeRates())) {
      if (entry == null
          || entry.sourceCurrencyId() == null
          || entry.rate() == null
          || entry.rate().signum() <= 0
          || entry.sourceCurrencyId().equals(target.getId())
          || saved.containsKey(entry.sourceCurrencyId())) {
        throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_LINES);
      }
      final Currency source =
          currencyService
              .findById(entry.sourceCurrencyId())
              .orElseThrow(() -> new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_LINES));
      final DocumentExchangeRate rate =
          saved.get(entry.sourceCurrencyId()) != null
              ? saved.get(entry.sourceCurrencyId())
              : document.getExchangeRates() == null
                  ? null
                  : document.getExchangeRates().stream()
                      .filter(
                          existingRate ->
                              existingRate
                                  .getSourceCurrency()
                                  .getId()
                                  .equals(entry.sourceCurrencyId()))
                      .findFirst()
                      .orElse(null);
      if (rate == null) {
        final DocumentExchangeRate newRate = new DocumentExchangeRate();
        newRate.setDocument(document);
        newRate.setSourceCurrency(source);
        newRate.setRate(entry.rate());
        saved.put(entry.sourceCurrencyId(), newRate);
      } else {
        rate.setSourceCurrency(source);
        rate.setRate(entry.rate());
        saved.put(entry.sourceCurrencyId(), rate);
      }
    }
    if (document.getExchangeRates() == null) {
      document.setExchangeRates(new ArrayList<>());
    }
    final List<DocumentExchangeRate> existing = document.getExchangeRates();
    existing.removeIf(rate -> !saved.containsKey(rate.getSourceCurrency().getId()));
    for (ExchangeRateDraft entry : DocumentWorkflowValues.safe(draft.exchangeRates())) {
      if (entry == null || entry.sourceCurrencyId() == null) {
        continue;
      }
      final DocumentExchangeRate rate = saved.get(entry.sourceCurrencyId());
      if (rate != null && !existing.contains(rate)) {
        existing.add(rate);
      }
    }
    document.setExchangeRate(null);
  }

  public void addLine(
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

  public DocumentLine copyOperationLine(
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
            .unitPrice(DocumentWorkflowValues.nz(source.getUnitPrice()))
            .sourceUnitPrice(DocumentWorkflowValues.nz(source.getSourceUnitPrice()))
            .totalPrice(DocumentWorkflowValues.nz(source.getUnitPrice()).multiply(quantity))
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

  public DocumentLine buildLine(
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

  public BigDecimal expectedFactor(final Product product, final Unit unit) {
    return product.getUnit() != null
            && product.getUnit().getId().equals(unit.getId())
            && product.getSecondaryUnit() != null
        ? product.getSecondaryUnitsPerMainUnit()
        : BigDecimal.ONE;
  }

  public void setWarehouse(
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

  public void snapshotPartner(
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
}
