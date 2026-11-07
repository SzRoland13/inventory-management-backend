package dev.roland.inventory_management_backend.features.document.facade.workflow;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.common.exception.ApiException;
import dev.roland.inventory_management_backend.common.service.CurrentCompanyProvider;
import dev.roland.inventory_management_backend.features.company.Company;
import dev.roland.inventory_management_backend.features.currency.Currency;
import dev.roland.inventory_management_backend.features.currency.service.CurrencyService;
import dev.roland.inventory_management_backend.features.document.Document;
import dev.roland.inventory_management_backend.features.document.dto.CommercialDecision;
import dev.roland.inventory_management_backend.features.document.dto.CommercialDraft;
import dev.roland.inventory_management_backend.features.document.dto.CommercialDraftUpdate;
import dev.roland.inventory_management_backend.features.document.dto.CommercialLineDraft;
import dev.roland.inventory_management_backend.features.document.dto.CommercialPreviewLine;
import dev.roland.inventory_management_backend.features.document.dto.CommercialPreviewRequest;
import dev.roland.inventory_management_backend.features.document.dto.CommercialPreviewResponse;
import dev.roland.inventory_management_backend.features.document.dto.DocumentResponse;
import dev.roland.inventory_management_backend.features.document.dto.DocumentTotals;
import dev.roland.inventory_management_backend.features.document.dto.SettlementAssignment;
import dev.roland.inventory_management_backend.features.document.dto.SettlementOwner;
import dev.roland.inventory_management_backend.features.document.enumeration.CommercialApprovalStatus;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentCategory;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentRelationType;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentStatus;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentType;
import dev.roland.inventory_management_backend.features.document.mapper.DocumentResponseMapper;
import dev.roland.inventory_management_backend.features.document.message.DocumentApiMessageKey;
import dev.roland.inventory_management_backend.features.document.service.DocumentService;
import dev.roland.inventory_management_backend.features.document.utils.DocumentTotalsCalculator;
import dev.roland.inventory_management_backend.features.document.utils.DocumentWorkflowValues;
import dev.roland.inventory_management_backend.features.document_activity.DocumentActivityEvent;
import dev.roland.inventory_management_backend.features.document_activity.service.DocumentActivityService;
import dev.roland.inventory_management_backend.features.document_line.DocumentLine;
import dev.roland.inventory_management_backend.features.document_line.service.DocumentLineService;
import dev.roland.inventory_management_backend.features.document_relation.DocumentRelation;
import dev.roland.inventory_management_backend.features.document_relation.service.DocumentRelationService;
import dev.roland.inventory_management_backend.features.user.User;
import dev.roland.inventory_management_backend.features.user.enumeration.UserRole;
import dev.roland.inventory_management_backend.features.user.enumeration.UserStatus;
import dev.roland.inventory_management_backend.features.user.service.UserService;
import lombok.RequiredArgsConstructor;

/** Implements company-scoped commercial document creation, matching, approval, and finalization. */
@Service
@RequiredArgsConstructor
public class CommercialDocumentWorkflowService {
  private final DocumentService documents;
  private final DocumentLineService lines;
  private final DocumentRelationService relations;
  private final DocumentActivityService activity;
  private final DocumentWorkflowAccessService access;
  private final DocumentWorkflowWriterService writer;
  private final DocumentResponseMapper responseMapper;
  private final CurrentCompanyProvider companyProvider;
  private final CurrencyService currencyService;
  private final UserService userService;

  /**
   * Calculates source balances and monetary totals without persisting a document.
   *
   * @param previewRequest proposed draft and optional draft excluded from availability checks
   * @return calculated totals and per-source-line balances
   */
  @Transactional
  public CommercialPreviewResponse preview(final CommercialPreviewRequest previewRequest) {
    final CommercialDraft request = previewRequest.draft();
    final Company company = companyProvider.getCurrentCompany();
    final User actor = access.currentActor();
    if (actor == null || !access.canCreateType(request.type(), actor)) {
      throw invalid();
    }
    final Long excludedId = previewRequest.excludeDocumentId();
    if (excludedId != null) {
      final Document excluded = access.lock(excludedId, company.getId());
      if (excluded.getStatus() != DocumentStatus.DRAFT
          || excluded.getType() != request.type()
          || !access.canManageDocument(excluded, actor)) {
        throw invalid();
      }
    }
    final List<Long> sourceIds = request.sourceDocumentIds().stream().distinct().sorted().toList();
    final boolean credit = request.type() == DocumentType.FINANCIAL_CREDIT;
    if (sourceIds.size() != request.sourceDocumentIds().size()
        || (credit && sourceIds.size() != 1)) {
      throw invalid();
    }
    final List<Document> sources =
        sourceIds.stream().map(id -> access.lock(id, company.getId())).toList();
    validateSources(request, sources, company.getId());
    final Currency currency =
        currencyService
            .findById(request.currencyId())
            .orElseThrow(() -> new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_LINES));
    if (sources.stream()
        .anyMatch(
            source ->
                source.getCurrency() == null
                    || !source.getCurrency().getId().equals(currency.getId()))) {
      throw invalid();
    }
    final Map<Long, DocumentLine> sourceLines = new HashMap<>();
    sources.forEach(
        source ->
            DocumentWorkflowValues.safe(source.getLines())
                .forEach(line -> sourceLines.put(line.getId(), line)));
    final Set<Long> seen = new HashSet<>();
    final List<CommercialPreviewLine> balances = new ArrayList<>();
    final List<DocumentLine> previewLines = new ArrayList<>();
    boolean valid = true;
    boolean discrepancy = false;
    final Document previewDocument = new Document();
    previewDocument.setCurrency(currency);
    for (CommercialLineDraft draftLine : request.lines()) {
      final DocumentLine sourceLine = sourceLines.get(draftLine.sourceLineId());
      if (sourceLine == null || !seen.add(sourceLine.getId())) {
        throw invalid();
      }
      final BigDecimal quantity = DocumentWorkflowValues.nz(draftLine.quantity());
      final BigDecimal sourceQuantity = DocumentWorkflowValues.nz(sourceLine.getQuantity());
      final BigDecimal billedQuantity =
          credit ? BigDecimal.ZERO : billedQuantity(sourceLine, company.getId(), excludedId);
      final BigDecimal remainingQuantity =
          sourceQuantity.subtract(billedQuantity).max(BigDecimal.ZERO);
      final CreditBalance creditBalance =
          credit ? creditBalance(sourceLine, company.getId(), excludedId) : CreditBalance.empty();
      final BigDecimal remainingCreditQuantity =
          sourceQuantity.subtract(creditBalance.quantity()).max(BigDecimal.ZERO);
      final BigDecimal remainingCreditNet =
          sourceQuantity
              .multiply(DocumentWorkflowValues.nz(sourceLine.getUnitPrice()))
              .subtract(creditBalance.net())
              .max(BigDecimal.ZERO);
      final BigDecimal price =
          draftLine.unitPrice() == null
              ? DocumentWorkflowValues.nz(sourceLine.getUnitPrice())
              : draftLine.unitPrice();
      final BigDecimal vat =
          draftLine.vatRate() == null
              ? DocumentWorkflowValues.nz(sourceLine.getVatRateSnapshot())
              : draftLine.vatRate();
      validatePriceAndVat(price, vat);
      final BigDecimal requestedNet =
          draftLine.amount() == null ? price.multiply(quantity) : draftLine.amount();
      if (quantity.signum() < 0
          || (!credit && quantity.signum() == 0)
          || (credit
              && quantity.signum() == 0
              && DocumentWorkflowValues.nz(draftLine.amount()).signum() <= 0)
          || requestedNet.signum() < 0
          || (!credit && quantity.compareTo(remainingQuantity) > 0)
          || (credit
              && (quantity.compareTo(remainingCreditQuantity) > 0
                  || requestedNet.compareTo(remainingCreditNet) > 0))) {
        valid = false;
      }
      if (!credit) {
        discrepancy |=
            price.compareTo(DocumentWorkflowValues.nz(sourceLine.getUnitPrice())) != 0
                || vat.compareTo(DocumentWorkflowValues.nz(sourceLine.getVatRateSnapshot())) != 0;
      }
      balances.add(
          new CommercialPreviewLine(
              sourceLine.getId(),
              sourceQuantity,
              billedQuantity,
              remainingQuantity,
              creditBalance.quantity(),
              remainingCreditQuantity,
              creditBalance.net(),
              remainingCreditNet,
              quantity,
              requestedNet));
      previewLines.add(
          cloneCommercialLine(
              previewDocument,
              sourceLine,
              quantity,
              price,
              vat,
              credit ? draftLine.amount() : null));
    }
    if (previewLines.isEmpty()) {
      throw invalid();
    }
    final DocumentTotalsCalculator.Totals calculated =
        DocumentTotalsCalculator.calculate(previewLines);
    return new CommercialPreviewResponse(
        balances,
        new DocumentTotals(
            currency.getCode(),
            calculated.net(),
            calculated.tax(),
            calculated.gross(),
            BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)),
        discrepancy,
        valid);
  }

  private BigDecimal billedQuantity(
      final DocumentLine sourceLine, final Long companyId, final Long excludedDocumentId) {
    BigDecimal allocated = BigDecimal.ZERO;
    for (Document document : documents.findAllByCompanyIdOrderByUpdatedAtDesc(companyId)) {
      if (excludedDocumentId != null && excludedDocumentId.equals(document.getId())) {
        continue;
      }
      if (document.getType() != DocumentType.SUPPLIER_BILL
          && document.getType() != DocumentType.CUSTOMER_INVOICE) {
        continue;
      }
      for (DocumentLine line : DocumentWorkflowValues.safe(document.getLines())) {
        if (line.getSourceLine() != null
            && line.getSourceLine().getId().equals(sourceLine.getId())) {
          allocated = allocated.add(DocumentWorkflowValues.nz(line.getQuantity()));
        }
      }
    }
    return allocated;
  }

  private CreditBalance creditBalance(
      final DocumentLine sourceLine, final Long companyId, final Long excludedDocumentId) {
    BigDecimal creditedQuantity = BigDecimal.ZERO;
    BigDecimal creditedNet = BigDecimal.ZERO;
    for (Document document : documents.findAllByCompanyIdOrderByUpdatedAtDesc(companyId)) {
      if (excludedDocumentId != null && excludedDocumentId.equals(document.getId())) {
        continue;
      }
      if (document.getType() != DocumentType.FINANCIAL_CREDIT) {
        continue;
      }
      for (DocumentLine line : DocumentWorkflowValues.safe(document.getLines())) {
        if (line.getSourceLine() != null
            && line.getSourceLine().getId().equals(sourceLine.getId())) {
          creditedQuantity = creditedQuantity.add(DocumentWorkflowValues.nz(line.getQuantity()));
          creditedNet =
              creditedNet.add(
                  line.getCommercialNetAmountOverride() == null
                      ? DocumentWorkflowValues.nz(line.getUnitPrice())
                          .multiply(DocumentWorkflowValues.nz(line.getQuantity()))
                      : line.getCommercialNetAmountOverride());
        }
      }
    }
    return new CreditBalance(creditedQuantity, creditedNet);
  }

  private record CreditBalance(BigDecimal quantity, BigDecimal net) {
    private static CreditBalance empty() {
      return new CreditBalance(BigDecimal.ZERO, BigDecimal.ZERO);
    }
  }

  /**
   * Creates a commercial document draft from source documents.
   *
   * @param request commercial draft data
   * @return created document
   */
  @Transactional
  public DocumentResponse createDraft(final CommercialDraft request) {
    final Company company = companyProvider.getCurrentCompany();
    final User actor = access.currentActor();
    if (actor == null || !access.canCreateType(request.type(), actor)) {
      throw invalid();
    }
    final boolean credit = request.type() == DocumentType.FINANCIAL_CREDIT;
    if (!credit
        && request.type() != DocumentType.SUPPLIER_BILL
        && request.type() != DocumentType.CUSTOMER_INVOICE) {
      throw invalid();
    }
    final List<Long> sourceIds = request.sourceDocumentIds().stream().distinct().sorted().toList();
    if (sourceIds.size() != request.sourceDocumentIds().size()
        || (credit && sourceIds.size() != 1)) {
      throw invalid();
    }
    final List<Document> sources =
        sourceIds.stream().map(id -> access.lock(id, company.getId())).toList();
    validateSources(request, sources, company.getId());
    final Currency currency =
        currencyService
            .findById(request.currencyId())
            .orElseThrow(() -> new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_LINES));
    if (sources.stream()
        .anyMatch(
            s -> s.getCurrency() == null || !s.getCurrency().getId().equals(currency.getId()))) {
      throw invalid();
    }
    final Map<Long, DocumentLine> sourceLines = new HashMap<>();
    sources.forEach(
        source ->
            DocumentWorkflowValues.safe(source.getLines())
                .forEach(line -> sourceLines.put(line.getId(), line)));
    final Document document = new Document();
    document.setCompany(company);
    document.setCreatedByUser(actor);
    document.setType(request.type());
    document.setCategory(
        request.type() == DocumentType.FINANCIAL_CREDIT
            ? sources.getFirst().getCategory()
            : request.type() == DocumentType.SUPPLIER_BILL
                ? DocumentCategory.PROCUREMENT
                : DocumentCategory.SALES);
    document.setStatus(DocumentStatus.DRAFT);
    document.setDocumentDate(request.date());
    document.setCurrency(currency);
    document.setReferenceNumber(request.referenceNumber());
    document.setWorkflowReason(request.reason());
    document.setPaymentTermsSnapshot(sources.getFirst().getPaymentTermsSnapshot());
    final Document partnerSource = sources.getFirst();
    document.setPartner(partnerSource.getPartner());
    document.setPartnerNameSnapshot(partnerSource.getPartnerNameSnapshot());
    document.setPartnerTaxNumberSnapshot(partnerSource.getPartnerTaxNumberSnapshot());
    document.setPartnerAddressSnapshot(partnerSource.getPartnerAddressSnapshot());
    document.setExchangeRates(new ArrayList<>());
    document.setLines(new ArrayList<>());
    final Set<Long> seenSourceLines = new HashSet<>();
    boolean discrepancy = false;
    for (CommercialLineDraft draftLine : request.lines()) {
      final DocumentLine sourceLine = sourceLines.get(draftLine.sourceLineId());
      if (sourceLine == null || !seenSourceLines.add(sourceLine.getId())) {
        throw invalid();
      }
      final BigDecimal quantity = DocumentWorkflowValues.nz(draftLine.quantity());
      if (quantity.signum() < 0
          || (!credit && quantity.signum() == 0)
          || (credit
              && quantity.signum() == 0
              && DocumentWorkflowValues.nz(draftLine.amount()).signum() <= 0)) {
        throw invalid();
      }
      if (credit) {
        validateCreditAvailability(
            sourceLine, quantity, draftLine.unitPrice(), draftLine.amount(), company.getId(), null);
      } else {
        validateBillingAvailability(sourceLine, quantity, company.getId(), null);
      }
      final BigDecimal price =
          draftLine.unitPrice() == null
              ? DocumentWorkflowValues.nz(sourceLine.getUnitPrice())
              : draftLine.unitPrice();
      final BigDecimal vat =
          draftLine.vatRate() == null
              ? DocumentWorkflowValues.nz(sourceLine.getVatRateSnapshot())
              : draftLine.vatRate();
      validatePriceAndVat(price, vat);
      if (!credit) {
        discrepancy |=
            price.compareTo(DocumentWorkflowValues.nz(sourceLine.getUnitPrice())) != 0
                || vat.compareTo(DocumentWorkflowValues.nz(sourceLine.getVatRateSnapshot())) != 0;
      }
      final DocumentLine line =
          cloneCommercialLine(
              document, sourceLine, quantity, price, vat, credit ? draftLine.amount() : null);
      document.getLines().add(line);
    }
    if (document.getLines().isEmpty()) {
      throw invalid();
    }
    document.setApprovalRequired(discrepancy);
    document.setApprovalStatus(
        discrepancy ? CommercialApprovalStatus.PENDING : CommercialApprovalStatus.NOT_REQUIRED);
    final Document saved = documents.save(document);
    for (DocumentLine line : saved.getLines()) {
      lines.save(line);
    }
    for (Document source : sources) {
      final DocumentRelation relation = new DocumentRelation();
      relation.setSourceDocument(source);
      relation.setTargetDocument(saved);
      relation.setRelationType(
          credit ? DocumentRelationType.FINANCIAL_CREDIT : DocumentRelationType.CREATED_FROM);
      relations.save(relation);
    }
    writer.addActivity(
        saved, DocumentActivityEvent.CREATED, "Commercial draft created from source documents");
    documents.flush();
    return responseMapper.toResponse(saved);
  }

  /**
   * Updates a commercial draft and resets any prior approval.
   *
   * @param id document identifier
   * @param update updated draft data
   * @return updated document
   */
  @Transactional
  public DocumentResponse updateDraft(final Long id, final CommercialDraftUpdate update) {
    final Company company = companyProvider.getCurrentCompany();
    final Document document = access.lock(id, company.getId());
    if (document.getStatus() != DocumentStatus.DRAFT
        || !access.canManageDocument(document, access.currentActor())
        || update.draft().type() != document.getType()) {
      throw invalid();
    }
    access.checkVersion(
        document, String.valueOf(update.version()), () -> responseMapper.toResponse(document));
    final List<Long> sourceIds =
        update.draft().sourceDocumentIds().stream().distinct().sorted().toList();
    if (sourceIds.size() != update.draft().sourceDocumentIds().size()
        || document.getCurrency() == null
        || !document.getCurrency().getId().equals(update.draft().currencyId())) {
      throw invalid();
    }
    final List<Document> sources =
        sourceIds.stream().map(sourceId -> access.lock(sourceId, company.getId())).toList();
    final Set<Long> linkedSourceIds =
        relations.findAllBySourceDocumentIdOrTargetDocumentId(id, id).stream()
            .filter(relation -> relation.getTargetDocument().getId().equals(id))
            .filter(
                relation ->
                    relation.getRelationType() == DocumentRelationType.CREATED_FROM
                        || relation.getRelationType() == DocumentRelationType.FINANCIAL_CREDIT)
            .map(relation -> relation.getSourceDocument().getId())
            .collect(java.util.stream.Collectors.toSet());
    if (!linkedSourceIds.equals(new HashSet<>(sourceIds))) {
      throw invalid();
    }
    validateSources(update.draft(), sources, company.getId());
    final Map<Long, DocumentLine> sourceLines = new HashMap<>();
    sources.forEach(
        source ->
            DocumentWorkflowValues.safe(source.getLines())
                .forEach(line -> sourceLines.put(line.getId(), line)));
    final List<DocumentLine> updatedLines = new ArrayList<>();
    final Set<Long> seenLineIds = new HashSet<>();
    boolean discrepancy = false;
    final boolean credit = document.getType() == DocumentType.FINANCIAL_CREDIT;
    for (CommercialLineDraft lineDraft : update.draft().lines()) {
      final DocumentLine sourceLine = sourceLines.get(lineDraft.sourceLineId());
      if (sourceLine == null || !seenLineIds.add(sourceLine.getId())) {
        throw invalid();
      }
      final BigDecimal quantity = DocumentWorkflowValues.nz(lineDraft.quantity());
      if (quantity.signum() < 0
          || (!credit && quantity.signum() == 0)
          || (credit
              && quantity.signum() == 0
              && DocumentWorkflowValues.nz(lineDraft.amount()).signum() <= 0)) {
        throw invalid();
      }
      if (credit) {
        validateCreditAvailability(
            sourceLine, quantity, lineDraft.unitPrice(), lineDraft.amount(), company.getId(), id);
      } else {
        validateBillingAvailability(sourceLine, quantity, company.getId(), id);
      }
      final BigDecimal price =
          lineDraft.unitPrice() == null
              ? DocumentWorkflowValues.nz(sourceLine.getUnitPrice())
              : lineDraft.unitPrice();
      final BigDecimal vat =
          lineDraft.vatRate() == null
              ? DocumentWorkflowValues.nz(sourceLine.getVatRateSnapshot())
              : lineDraft.vatRate();
      validatePriceAndVat(price, vat);
      if (!credit) {
        discrepancy |=
            price.compareTo(DocumentWorkflowValues.nz(sourceLine.getUnitPrice())) != 0
                || vat.compareTo(DocumentWorkflowValues.nz(sourceLine.getVatRateSnapshot())) != 0;
      }
      updatedLines.add(
          cloneCommercialLine(
              document, sourceLine, quantity, price, vat, credit ? lineDraft.amount() : null));
    }
    if (updatedLines.isEmpty()) {
      throw invalid();
    }
    document.setLines(updatedLines);
    document.setDocumentDate(update.draft().date());
    document.setReferenceNumber(update.draft().referenceNumber());
    document.setWorkflowReason(update.draft().reason());
    document.setApprovalRequired(discrepancy);
    document.setApprovalStatus(
        discrepancy ? CommercialApprovalStatus.PENDING : CommercialApprovalStatus.NOT_REQUIRED);
    document.setApprovalComment(null);
    document.setApprovedByUser(null);
    document.setApprovedAt(null);
    writer.addActivity(
        document, DocumentActivityEvent.EDITED, "Commercial draft edited; approval reset");
    final Document saved = documents.save(document);
    documents.flush();
    return responseMapper.toResponse(saved);
  }

  /**
   * Records an approval or rejection decision.
   *
   * @param id document identifier
   * @param decision decision data
   * @param approve whether the decision approves
   * @return updated document
   */
  @Transactional
  public DocumentResponse decide(
      final Long id, final CommercialDecision decision, final boolean approve) {
    final Document document = access.lock(id, companyProvider.getCurrentCompany().getId());
    final User actor = access.currentActor();
    requireManager(actor);
    access.checkVersion(
        document, String.valueOf(decision.version()), () -> responseMapper.toResponse(document));
    if (isNotCommercialInvoice(document)
        || document.getStatus() != DocumentStatus.DRAFT
        || !document.isApprovalRequired()
        || document.getApprovalStatus() != CommercialApprovalStatus.PENDING) {
      throw invalid();
    }
    document.setApprovalStatus(
        approve ? CommercialApprovalStatus.APPROVED : CommercialApprovalStatus.REJECTED);
    document.setApprovalComment(decision.comment().trim());
    document.setApprovedByUser(actor);
    document.setApprovedAt(LocalDateTime.now());
    writer.addActivity(
        document,
        approve ? DocumentActivityEvent.APPROVED : DocumentActivityEvent.REJECTED,
        decision.comment().trim());
    documents.save(document);
    documents.flush();
    return responseMapper.toResponse(document);
  }

  /**
   * Finalizes an approved commercial document.
   *
   * @param id document identifier
   * @param version expected document version
   * @return finalized document
   * @throws ApiException if the actor cannot finalize or approval is pending
   */
  @Transactional
  public DocumentResponse finalizeDocument(final Long id, final String version) {
    final Document document = access.lock(id, companyProvider.getCurrentCompany().getId());
    final User actor = access.currentActor();
    if (!access.canManageDocument(document, actor)
        || document.getStatus() != DocumentStatus.DRAFT
        || (isNotCommercialInvoice(document)
            && document.getType() != DocumentType.FINANCIAL_CREDIT)) {
      throw invalid();
    }
    access.checkVersion(document, version, () -> responseMapper.toResponse(document));
    if (document.isApprovalRequired()
        && document.getApprovalStatus() != CommercialApprovalStatus.APPROVED) {
      throw new ApiException(DocumentApiMessageKey.COMMERCIAL_APPROVAL_REQUIRED);
    }
    writer.allocateNumber(document);
    document.setStatus(
        document.getType() == DocumentType.SUPPLIER_BILL
            ? DocumentStatus.RECEIVED
            : document.getType() == DocumentType.CUSTOMER_INVOICE
                ? DocumentStatus.ISSUED
                : DocumentStatus.COMPLETED);
    document.setCompletedAt(LocalDateTime.now());
    writer.addActivity(document, DocumentActivityEvent.FINALIZED, document.getStatus().name());
    documents.save(document);
    documents.flush();
    return responseMapper.toResponse(document);
  }

  /**
   * Assigns the settlement owner after a source correction.
   *
   * @param id document identifier
   * @param assignment owner assignment data
   * @return updated document
   */
  @Transactional
  public DocumentResponse assignSettlementOwner(
      final Long id, final SettlementAssignment assignment) {
    final Document document = access.lock(id, companyProvider.getCurrentCompany().getId());
    final User actor = access.currentActor();
    requireManager(actor);
    access.checkVersion(
        document, String.valueOf(assignment.version()), () -> responseMapper.toResponse(document));
    if (!document.isFinancialSettlementRequired()) {
      throw invalid();
    }
    final User owner = userService.findByIdOrThrow(assignment.ownerId());
    if (owner == null
        || owner.getCompany() == null
        || owner.getStatus() != UserStatus.ACTIVE
        || !owner.getCompany().getId().equals(companyProvider.getCurrentCompany().getId())) {
      throw invalid();
    }
    document.setFinancialSettlementOwner(owner);
    writer.addActivity(document, DocumentActivityEvent.SETTLEMENT_ASSIGNED, owner.getUsername());
    documents.save(document);
    documents.flush();
    return responseMapper.toResponse(document);
  }

  /**
   * Lists active settlement owners for the current company.
   *
   * @return active settlement owners
   */
  public List<SettlementOwner> settlementOwners() {
    requireManager(access.currentActor());
    return userService.findAllByCompanyId(companyProvider.getCurrentCompany().getId()).stream()
        .filter(user -> user.getStatus() == UserStatus.ACTIVE)
        .map(user -> new SettlementOwner(user.getId(), user.getUsername(), user.getRole()))
        .toList();
  }

  private void validateSources(
      final CommercialDraft request, final List<Document> sources, final Long companyId) {
    final DocumentType type = request.type();
    if (sources.isEmpty()) {
      throw invalid();
    }
    if (type == DocumentType.FINANCIAL_CREDIT) {
      final Document source = sources.getFirst();
      if (!isFinalInvoice(source)) {
        throw invalid();
      }
      return;
    }
    final DocumentType expected =
        type == DocumentType.SUPPLIER_BILL
            ? DocumentType.GOODS_RECEIPT
            : DocumentType.DELIVERY_NOTE;
    final Document first = sources.getFirst();
    for (Document source : sources) {
      if (source.getCompany() == null
          || !companyId.equals(source.getCompany().getId())
          || source.getType() != expected
          || source.getStatus() != DocumentStatus.COMPLETED
          || source.isNonBillable()
          || differentId(first.getPartner(), source.getPartner())
          || differentId(first.getCurrency(), source.getCurrency())
          || !java.util.Objects.equals(
              first.getPaymentTermsSnapshot(), source.getPaymentTermsSnapshot())) {
        throw new ApiException(DocumentApiMessageKey.COMMERCIAL_SOURCE_INELIGIBLE);
      }
    }
  }

  private void validateBillingAvailability(
      final DocumentLine sourceLine,
      final BigDecimal quantity,
      final Long companyId,
      final Long excludedDocumentId) {
    BigDecimal allocated = BigDecimal.ZERO;
    for (Document document : documents.findAllByCompanyIdOrderByUpdatedAtDesc(companyId)) {
      if (excludedDocumentId != null && excludedDocumentId.equals(document.getId())) {
        continue;
      }
      if (document.getType() != DocumentType.SUPPLIER_BILL
          && document.getType() != DocumentType.CUSTOMER_INVOICE) {
        continue;
      }
      for (DocumentLine line : DocumentWorkflowValues.safe(document.getLines())) {
        if (line.getSourceLine() != null
            && line.getSourceLine().getId().equals(sourceLine.getId())) {
          allocated = allocated.add(DocumentWorkflowValues.nz(line.getQuantity()));
        }
      }
    }
    if (allocated.add(quantity).compareTo(DocumentWorkflowValues.nz(sourceLine.getQuantity()))
        > 0) {
      throw new ApiException(DocumentApiMessageKey.COMMERCIAL_OVERBILLING);
    }
  }

  private void validateCreditAvailability(
      final DocumentLine sourceLine,
      final BigDecimal quantity,
      final BigDecimal requestedPrice,
      final BigDecimal requestedAmount,
      final Long companyId,
      final Long excludedDocumentId) {
    final CreditBalance balance = creditBalance(sourceLine, companyId, excludedDocumentId);
    final BigDecimal creditedQuantity = balance.quantity();
    final BigDecimal creditedNet = balance.net();
    final BigDecimal unitPrice =
        requestedPrice == null
            ? DocumentWorkflowValues.nz(sourceLine.getUnitPrice())
            : requestedPrice;
    final BigDecimal sourceQuantity = DocumentWorkflowValues.nz(sourceLine.getQuantity());
    final BigDecimal availableNet =
        sourceQuantity
            .multiply(DocumentWorkflowValues.nz(sourceLine.getUnitPrice()))
            .subtract(creditedNet);
    final BigDecimal amount =
        requestedAmount == null ? unitPrice.multiply(quantity) : requestedAmount;
    if (amount.signum() < 0
        || creditedQuantity.add(quantity).compareTo(sourceQuantity) > 0
        || amount.compareTo(availableNet) > 0) {
      throw new ApiException(DocumentApiMessageKey.COMMERCIAL_CREDIT_LIMIT);
    }
  }

  private DocumentLine cloneCommercialLine(
      final Document document,
      final DocumentLine source,
      final BigDecimal quantity,
      final BigDecimal price,
      final BigDecimal vat,
      final BigDecimal netAmountOverride) {
    final DocumentLine line = new DocumentLine();
    line.setDocument(document);
    line.setSourceLine(source);
    line.setProduct(source.getProduct());
    line.setUnitSnapshot(source.getUnitSnapshot());
    line.setUnitCodeSnapshot(source.getUnitCodeSnapshot());
    line.setUnitNameSnapshot(source.getUnitNameSnapshot());
    line.setConversionFactorSnapshot(source.getConversionFactorSnapshot());
    line.setQuantity(quantity);
    line.setFulfilledQuantity(BigDecimal.ZERO);
    line.setClosedQuantity(BigDecimal.ZERO);
    line.setReturnedQuantity(BigDecimal.ZERO);
    line.setReleasedQuantity(BigDecimal.ZERO);
    line.setReplacedQuantity(BigDecimal.ZERO);
    line.setInTransitQuantity(BigDecimal.ZERO);
    line.setUnitPrice(price);
    line.setSourceUnitPrice(
        source.getSourceUnitPrice() == null ? price : source.getSourceUnitPrice());
    line.setTotalPrice(netAmountOverride == null ? price.multiply(quantity) : netAmountOverride);
    line.setCommercialNetAmountOverride(netAmountOverride);
    line.setCurrency(document.getCurrency());
    line.setSourceCurrency(source.getSourceCurrency());
    line.setExchangeRateSnapshot(source.getExchangeRateSnapshot());
    line.setProductNameSnapshot(source.getProductNameSnapshot());
    line.setProductSkuSnapshot(source.getProductSkuSnapshot());
    line.setVatRateSnapshot(vat);
    return line;
  }

  private boolean differentId(final Object first, final Object second) {
    boolean different = true;
    if (first == null || second == null) {
      different = first != second;
    } else if (first instanceof dev.roland.inventory_management_backend.features.partner.Partner a
        && second instanceof dev.roland.inventory_management_backend.features.partner.Partner b) {
      different = !a.getId().equals(b.getId());
    } else if (first instanceof Currency a && second instanceof Currency b) {
      different = !a.getId().equals(b.getId());
    }
    return different;
  }

  private boolean isFinalInvoice(final Document document) {
    return (document.getType() == DocumentType.SUPPLIER_BILL
            && document.getStatus() == DocumentStatus.RECEIVED)
        || (document.getType() == DocumentType.CUSTOMER_INVOICE
            && document.getStatus() == DocumentStatus.ISSUED);
  }

  private void validatePriceAndVat(final BigDecimal price, final BigDecimal vat) {
    if (price.signum() < 0 || vat.signum() < 0 || vat.compareTo(new BigDecimal("99.999")) > 0) {
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_LINES);
    }
  }

  private boolean isNotCommercialInvoice(final Document document) {
    return document.getType() != DocumentType.SUPPLIER_BILL
        && document.getType() != DocumentType.CUSTOMER_INVOICE;
  }

  private void requireManager(final User user) {
    if (user == null || (user.getRole() != UserRole.ADMIN && user.getRole() != UserRole.MANAGER)) {
      throw invalid();
    }
  }

  private ApiException invalid() {
    return new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_TRANSITION);
  }
}
