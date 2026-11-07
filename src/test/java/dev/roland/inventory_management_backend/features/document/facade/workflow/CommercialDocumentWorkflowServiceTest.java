package dev.roland.inventory_management_backend.features.document.facade.workflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
import dev.roland.inventory_management_backend.features.document.dto.SettlementAssignment;
import dev.roland.inventory_management_backend.features.document.enumeration.CommercialApprovalStatus;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentCategory;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentRelationType;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentStatus;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentType;
import dev.roland.inventory_management_backend.features.document.mapper.DocumentResponseMapper;
import dev.roland.inventory_management_backend.features.document.repository.DocumentRepository;
import dev.roland.inventory_management_backend.features.document.service.DocumentService;
import dev.roland.inventory_management_backend.features.document_activity.service.DocumentActivityService;
import dev.roland.inventory_management_backend.features.document_line.DocumentLine;
import dev.roland.inventory_management_backend.features.document_line.service.DocumentLineService;
import dev.roland.inventory_management_backend.features.document_relation.DocumentRelation;
import dev.roland.inventory_management_backend.features.document_relation.service.DocumentRelationService;
import dev.roland.inventory_management_backend.features.partner.Partner;
import dev.roland.inventory_management_backend.features.user.User;
import dev.roland.inventory_management_backend.features.user.enumeration.UserRole;
import dev.roland.inventory_management_backend.features.user.enumeration.UserStatus;
import dev.roland.inventory_management_backend.features.user.service.UserService;

@ExtendWith(MockitoExtension.class)
class CommercialDocumentWorkflowServiceTest {
  private static final Long COMPANY_ID = 4L;
  private static final Long SOURCE_ID = 10L;
  private static final Long SOURCE_LINE_ID = 11L;
  private static final Long COMMERCIAL_ID = 20L;

  @Mock private DocumentService documents;
  @Mock private DocumentLineService lines;
  @Mock private DocumentRelationService relations;
  @Mock private DocumentActivityService activity;
  @Mock private DocumentWorkflowAccessService access;
  @Mock private DocumentWorkflowWriterService writer;
  @Mock private DocumentResponseMapper responseMapper;
  @Mock private CurrentCompanyProvider companyProvider;
  @Mock private CurrencyService currencyService;
  @Mock private UserService userService;

  private CommercialDocumentWorkflowService workflow;
  private Company company;
  private Currency currency;
  private User manager;
  private Document source;
  private DocumentLine sourceLine;
  private final AtomicReference<Document> savedDocument = new AtomicReference<>();

  @BeforeEach
  void setUp() {
    workflow =
        new CommercialDocumentWorkflowService(
            documents,
            lines,
            relations,
            activity,
            access,
            writer,
            responseMapper,
            companyProvider,
            currencyService,
            userService);
    company = Company.builder().id(COMPANY_ID).name("Example").build();
    currency = Currency.builder().id(1L).code("EUR").name("Euro").build();
    manager =
        User.builder()
            .id(5L)
            .username("manager")
            .role(UserRole.MANAGER)
            .status(UserStatus.ACTIVE)
            .company(company)
            .build();
    sourceLine = new DocumentLine();
    sourceLine.setId(SOURCE_LINE_ID);
    sourceLine.setQuantity(new BigDecimal("5"));
    sourceLine.setUnitPrice(new BigDecimal("10"));
    sourceLine.setVatRateSnapshot(new BigDecimal("27"));
    source = new Document();
    source.setId(SOURCE_ID);
    source.setCompany(company);
    source.setCurrency(currency);
    source.setType(DocumentType.GOODS_RECEIPT);
    source.setStatus(DocumentStatus.COMPLETED);
    source.setLines(List.of(sourceLine));
    lenient().when(companyProvider.getCurrentCompany()).thenReturn(company);
    lenient().when(access.currentActor()).thenReturn(manager);
    lenient().when(access.canCreateType(any(), any())).thenReturn(true);
    lenient().when(access.canManageDocument(any(), any())).thenReturn(true);
    lenient().when(access.lock(SOURCE_ID, COMPANY_ID)).thenReturn(source);
    lenient().when(currencyService.findById(1L)).thenReturn(Optional.of(currency));
    lenient()
        .when(documents.findAllByCompanyIdOrderByUpdatedAtDesc(COMPANY_ID))
        .thenReturn(List.of());
    lenient()
        .when(documents.save(any(Document.class)))
        .thenAnswer(
            invocation -> {
              Document document = invocation.getArgument(0);
              if (document.getId() == null) {
                document.setId(COMMERCIAL_ID);
              }
              savedDocument.set(document);
              return document;
            });
  }

  @Test
  void createsDraftAndRequiresApprovalWhenPriceOrVatDiffers() {
    workflow.createDraft(
        draft(DocumentType.SUPPLIER_BILL, new BigDecimal("8"), new BigDecimal("5"), null));

    assertEquals(CommercialApprovalStatus.PENDING, savedDocument.get().getApprovalStatus());
    assertEquals(new BigDecimal("8"), savedDocument.get().getLines().getFirst().getUnitPrice());
    assertEquals(SOURCE_LINE_ID, savedDocument.get().getLines().getFirst().getSourceLine().getId());
  }

  @Test
  void approvalAllowsSupplierBillFinalization() {
    workflow.createDraft(draft(DocumentType.SUPPLIER_BILL, new BigDecimal("8"), null, null));
    Document document = savedDocument.get();
    lenient().when(access.lock(COMMERCIAL_ID, COMPANY_ID)).thenReturn(document);

    workflow.decide(COMMERCIAL_ID, new CommercialDecision(0L, "Reviewed"), true);
    workflow.finalizeDocument(COMMERCIAL_ID, "0");

    assertEquals(CommercialApprovalStatus.APPROVED, document.getApprovalStatus());
    assertEquals(DocumentStatus.RECEIVED, document.getStatus());
  }

  @Test
  void exactMatchDoesNotRequireApprovalAndCustomerInvoiceFinalizesAsIssued() {
    source.setType(DocumentType.DELIVERY_NOTE);
    workflow.createDraft(draft(DocumentType.CUSTOMER_INVOICE, null, null, null));
    Document invoice = savedDocument.get();

    assertEquals(CommercialApprovalStatus.NOT_REQUIRED, invoice.getApprovalStatus());
    lenient().when(access.lock(COMMERCIAL_ID, COMPANY_ID)).thenReturn(invoice);
    workflow.finalizeDocument(COMMERCIAL_ID, "0");

    assertEquals(DocumentStatus.ISSUED, invoice.getStatus());
  }

  @Test
  void updateResetsApprovalAndUpdatesCommercialValues() {
    workflow.createDraft(draft(DocumentType.SUPPLIER_BILL, null, null, null));
    Document bill = savedDocument.get();
    DocumentRelation relation =
        DocumentRelation.builder()
            .sourceDocument(source)
            .targetDocument(bill)
            .relationType(DocumentRelationType.CREATED_FROM)
            .build();
    lenient().when(access.lock(COMMERCIAL_ID, COMPANY_ID)).thenReturn(bill);
    when(relations.findAllBySourceDocumentIdOrTargetDocumentId(COMMERCIAL_ID, COMMERCIAL_ID))
        .thenReturn(List.of(relation));

    workflow.updateDraft(
        COMMERCIAL_ID,
        new CommercialDraftUpdate(
            0L, draft(DocumentType.SUPPLIER_BILL, new BigDecimal("10"), null, null)));

    assertEquals(CommercialApprovalStatus.NOT_REQUIRED, bill.getApprovalStatus());
    assertEquals("EXT-1", bill.getReferenceNumber());
  }

  @Test
  void allowsConsolidatingReceiptsWithMatchingPartnerCurrencyAndPaymentTerms() {
    Partner partner = Partner.builder().id(9L).name("Supplier").company(company).build();
    source.setPartner(partner);
    source.setPaymentTermsSnapshot("Net 30");
    Document secondSource = new Document();
    secondSource.setId(12L);
    secondSource.setCompany(company);
    secondSource.setCurrency(currency);
    secondSource.setPartner(partner);
    secondSource.setPaymentTermsSnapshot("Net 30");
    secondSource.setType(DocumentType.GOODS_RECEIPT);
    secondSource.setStatus(DocumentStatus.COMPLETED);
    DocumentLine secondLine = new DocumentLine();
    secondLine.setId(13L);
    secondLine.setQuantity(new BigDecimal("3"));
    secondLine.setUnitPrice(new BigDecimal("4"));
    secondLine.setVatRateSnapshot(new BigDecimal("27"));
    secondSource.setLines(List.of(secondLine));
    lenient().when(access.lock(12L, COMPANY_ID)).thenReturn(secondSource);
    CommercialDraft request =
        new CommercialDraft(
            DocumentType.SUPPLIER_BILL,
            List.of(SOURCE_ID, 12L),
            LocalDate.now(),
            1L,
            "Net 30",
            null,
            null,
            null,
            List.of(
                new CommercialLineDraft(SOURCE_LINE_ID, BigDecimal.ONE, null, null, null),
                new CommercialLineDraft(13L, BigDecimal.ONE, null, null, null)));

    workflow.createDraft(request);

    assertEquals(2, savedDocument.get().getLines().size());
  }

  @Test
  void locksSourceDocumentsInStableOrderBeforeCheckingExistingBillingAllocations() {
    final Document secondSource = secondReceipt();
    secondSource.setCurrency(currency);
    lenient().when(access.lock(12L, COMPANY_ID)).thenReturn(secondSource);
    final CommercialDraft draft = twoSourceDraft();

    workflow.createDraft(
        new CommercialDraft(
            draft.type(),
            List.of(12L, SOURCE_ID),
            draft.date(),
            draft.currencyId(),
            draft.paymentTerms(),
            draft.reason(),
            draft.referenceNumber(),
            draft.settlementOwnerId(),
            draft.lines()));

    final InOrder order = inOrder(access, documents);
    order.verify(access).lock(SOURCE_ID, COMPANY_ID);
    order.verify(access).lock(12L, COMPANY_ID);
    order.verify(documents, times(2)).findAllByCompanyIdOrderByUpdatedAtDesc(COMPANY_ID);
  }

  @Test
  void commercialDraftCreationUsesTransactionalPessimisticSourceLocks() throws Exception {
    final var lockMethod =
        DocumentRepository.class.getMethod("lockByIdAndCompanyId", Long.class, Long.class);
    final var createDraftMethod =
        CommercialDocumentWorkflowService.class.getMethod("createDraft", CommercialDraft.class);

    assertEquals(
        LockModeType.PESSIMISTIC_WRITE,
        lockMethod.getAnnotation(org.springframework.data.jpa.repository.Lock.class).value());
    org.junit.jupiter.api.Assertions.assertNotNull(
        createDraftMethod.getAnnotation(Transactional.class));
  }

  @Test
  void updatesAmountOnlyCreditDraft() {
    source.setType(DocumentType.CUSTOMER_INVOICE);
    source.setStatus(DocumentStatus.ISSUED);
    workflow.createDraft(draft(DocumentType.FINANCIAL_CREDIT, null, null, new BigDecimal("12")));
    Document credit = savedDocument.get();
    DocumentRelation relation =
        DocumentRelation.builder()
            .sourceDocument(source)
            .targetDocument(credit)
            .relationType(DocumentRelationType.FINANCIAL_CREDIT)
            .build();
    lenient().when(access.lock(COMMERCIAL_ID, COMPANY_ID)).thenReturn(credit);
    when(relations.findAllBySourceDocumentIdOrTargetDocumentId(COMMERCIAL_ID, COMMERCIAL_ID))
        .thenReturn(List.of(relation));

    workflow.updateDraft(
        COMMERCIAL_ID,
        new CommercialDraftUpdate(
            0L, draft(DocumentType.FINANCIAL_CREDIT, null, null, new BigDecimal("15"))));

    assertEquals(
        new BigDecimal("15"), credit.getLines().getFirst().getCommercialNetAmountOverride());
  }

  @Test
  void updateWithPriceDifferenceRequiresNewApproval() {
    workflow.createDraft(draft(DocumentType.SUPPLIER_BILL, null, null, null));
    Document bill = savedDocument.get();
    DocumentRelation relation =
        DocumentRelation.builder()
            .sourceDocument(source)
            .targetDocument(bill)
            .relationType(DocumentRelationType.CREATED_FROM)
            .build();
    lenient().when(access.lock(COMMERCIAL_ID, COMPANY_ID)).thenReturn(bill);
    when(relations.findAllBySourceDocumentIdOrTargetDocumentId(COMMERCIAL_ID, COMMERCIAL_ID))
        .thenReturn(List.of(relation));

    workflow.updateDraft(
        COMMERCIAL_ID,
        new CommercialDraftUpdate(
            0L, draft(DocumentType.SUPPLIER_BILL, new BigDecimal("9"), null, null)));

    assertEquals(CommercialApprovalStatus.PENDING, bill.getApprovalStatus());
  }

  @Test
  void finalizesFinancialCreditAsCompleted() {
    source.setType(DocumentType.CUSTOMER_INVOICE);
    source.setStatus(DocumentStatus.ISSUED);
    workflow.createDraft(draft(DocumentType.FINANCIAL_CREDIT, null, null, new BigDecimal("12")));
    Document credit = savedDocument.get();
    lenient().when(access.lock(COMMERCIAL_ID, COMPANY_ID)).thenReturn(credit);

    workflow.finalizeDocument(COMMERCIAL_ID, "0");

    assertEquals(DocumentStatus.COMPLETED, credit.getStatus());
  }

  @Test
  void rejectsDraftUpdatesWithChangedTypeOrSourceLinks() {
    workflow.createDraft(draft(DocumentType.SUPPLIER_BILL, null, null, null));
    Document bill = savedDocument.get();
    lenient().when(access.lock(COMMERCIAL_ID, COMPANY_ID)).thenReturn(bill);
    assertThrows(
        ApiException.class,
        () ->
            workflow.updateDraft(
                COMMERCIAL_ID,
                new CommercialDraftUpdate(
                    0L, draft(DocumentType.CUSTOMER_INVOICE, null, null, null))));
    assertThrows(
        ApiException.class,
        () ->
            workflow.updateDraft(
                COMMERCIAL_ID,
                new CommercialDraftUpdate(
                    0L, draft(DocumentType.SUPPLIER_BILL, null, null, null))));
  }

  @Test
  void rejectsInvalidSourceTypeAndStatus() {
    source.setType(DocumentType.SALES_ORDER);
    assertThrows(
        ApiException.class,
        () -> workflow.createDraft(draft(DocumentType.SUPPLIER_BILL, null, null, null)));
    source.setType(DocumentType.GOODS_RECEIPT);
    source.setStatus(DocumentStatus.DRAFT);
    assertThrows(
        ApiException.class,
        () -> workflow.createDraft(draft(DocumentType.SUPPLIER_BILL, null, null, null)));
  }

  @Test
  void rejectsConsolidatedSourcesWithDifferentCompanyPartnerCurrencyOrTerms() {
    Document second = secondReceipt();
    lenient().when(access.lock(12L, COMPANY_ID)).thenReturn(second);
    second.setCompany(Company.builder().id(99L).build());
    assertThrows(ApiException.class, () -> workflow.createDraft(twoSourceDraft()));
    second.setCompany(company);

    Partner firstPartner = Partner.builder().id(30L).name("A").company(company).build();
    Partner secondPartner = Partner.builder().id(31L).name("B").company(company).build();
    source.setPartner(firstPartner);
    second.setPartner(secondPartner);
    assertThrows(ApiException.class, () -> workflow.createDraft(twoSourceDraft()));

    second.setPartner(firstPartner);
    Currency otherCurrency = Currency.builder().id(2L).code("USD").name("Dollar").build();
    second.setCurrency(otherCurrency);
    assertThrows(ApiException.class, () -> workflow.createDraft(twoSourceDraft()));

    second.setCurrency(currency);
    source.setPaymentTermsSnapshot("Net 10");
    second.setPaymentTermsSnapshot("Net 30");
    assertThrows(ApiException.class, () -> workflow.createDraft(twoSourceDraft()));
  }

  @Test
  void rejectsInvalidCreditQuantityAndDuplicateSourceLine() {
    source.setType(DocumentType.CUSTOMER_INVOICE);
    source.setStatus(DocumentStatus.ISSUED);
    assertThrows(
        ApiException.class,
        () ->
            workflow.createDraft(
                new CommercialDraft(
                    DocumentType.FINANCIAL_CREDIT,
                    List.of(SOURCE_ID),
                    LocalDate.now(),
                    1L,
                    null,
                    null,
                    null,
                    null,
                    List.of(
                        new CommercialLineDraft(
                            SOURCE_LINE_ID, BigDecimal.ZERO, null, null, BigDecimal.ZERO)))));
    assertThrows(
        ApiException.class,
        () ->
            workflow.createDraft(
                new CommercialDraft(
                    DocumentType.SUPPLIER_BILL,
                    List.of(SOURCE_ID),
                    LocalDate.now(),
                    1L,
                    null,
                    null,
                    null,
                    null,
                    List.of(
                        new CommercialLineDraft(SOURCE_LINE_ID, BigDecimal.ONE, null, null, null),
                        new CommercialLineDraft(
                            SOURCE_LINE_ID, BigDecimal.ONE, null, null, null)))));
  }

  @Test
  void rejectsSettlementAssignmentBySalesAndForSuspendedOwner() {
    Document bill = new Document();
    bill.setId(COMMERCIAL_ID);
    bill.setCompany(company);
    bill.setFinancialSettlementRequired(true);
    lenient().when(access.lock(COMMERCIAL_ID, COMPANY_ID)).thenReturn(bill);
    manager.setRole(UserRole.SALES);
    assertThrows(
        ApiException.class,
        () -> workflow.assignSettlementOwner(COMMERCIAL_ID, new SettlementAssignment(0L, 6L)));

    manager.setRole(UserRole.MANAGER);
    User suspended = User.builder().id(6L).status(UserStatus.SUSPENDED).company(company).build();
    when(userService.findByIdOrThrow(6L)).thenReturn(suspended);
    assertThrows(
        ApiException.class,
        () -> workflow.assignSettlementOwner(COMMERCIAL_ID, new SettlementAssignment(0L, 6L)));

    User otherCompany =
        User.builder()
            .id(7L)
            .status(UserStatus.ACTIVE)
            .role(UserRole.MANAGER)
            .company(Company.builder().id(99L).build())
            .build();
    when(userService.findByIdOrThrow(7L)).thenReturn(otherCompany);
    assertThrows(
        ApiException.class,
        () -> workflow.assignSettlementOwner(COMMERCIAL_ID, new SettlementAssignment(0L, 7L)));

    User noCompany = User.builder().id(8L).status(UserStatus.ACTIVE).role(UserRole.MANAGER).build();
    when(userService.findByIdOrThrow(8L)).thenReturn(noCompany);
    assertThrows(
        ApiException.class,
        () -> workflow.assignSettlementOwner(COMMERCIAL_ID, new SettlementAssignment(0L, 8L)));
  }

  @Test
  void existingCreditAllocationsReduceAvailableCreditAmount() {
    source.setType(DocumentType.CUSTOMER_INVOICE);
    source.setStatus(DocumentStatus.ISSUED);
    Document existing = new Document();
    existing.setId(40L);
    existing.setType(DocumentType.FINANCIAL_CREDIT);
    DocumentLine allocated = new DocumentLine();
    allocated.setSourceLine(sourceLine);
    allocated.setQuantity(BigDecimal.ONE);
    allocated.setUnitPrice(new BigDecimal("10"));
    allocated.setCommercialNetAmountOverride(new BigDecimal("20"));
    existing.setLines(List.of(allocated));
    when(documents.findAllByCompanyIdOrderByUpdatedAtDesc(COMPANY_ID))
        .thenReturn(List.of(existing, existingBill(BigDecimal.ONE)));

    assertThrows(
        ApiException.class,
        () ->
            workflow.createDraft(
                draft(DocumentType.FINANCIAL_CREDIT, null, null, new BigDecimal("31"))));
  }

  @Test
  void rejectsBillingAboveRemainingSourceQuantity() {
    when(documents.findAllByCompanyIdOrderByUpdatedAtDesc(COMPANY_ID))
        .thenReturn(List.of(existingBill(new BigDecimal("5"))));

    assertThrows(
        ApiException.class,
        () -> workflow.createDraft(draft(DocumentType.SUPPLIER_BILL, null, null, null)));
  }

  @Test
  void amountOnlyCreditUsesExplicitAmountAndCopiesInvoiceCategory() {
    source.setType(DocumentType.CUSTOMER_INVOICE);
    source.setStatus(DocumentStatus.ISSUED);
    source.setCategory(DocumentCategory.SALES);

    workflow.createDraft(draft(DocumentType.FINANCIAL_CREDIT, null, null, new BigDecimal("12")));

    Document credit = savedDocument.get();
    assertEquals(DocumentType.FINANCIAL_CREDIT, credit.getType());
    assertEquals(source.getCategory(), credit.getCategory());
    assertEquals(
        new BigDecimal("12"), credit.getLines().getFirst().getCommercialNetAmountOverride());
  }

  @Test
  void rejectsCreditAboveRemainingAmountAndCreditFromDraftInvoice() {
    source.setType(DocumentType.CUSTOMER_INVOICE);
    source.setStatus(DocumentStatus.ISSUED);
    assertThrows(
        ApiException.class,
        () ->
            workflow.createDraft(
                draft(DocumentType.FINANCIAL_CREDIT, null, null, new BigDecimal("51"))));

    source.setStatus(DocumentStatus.DRAFT);
    assertThrows(
        ApiException.class,
        () ->
            workflow.createDraft(
                draft(DocumentType.FINANCIAL_CREDIT, null, null, new BigDecimal("1"))));
  }

  @Test
  void rejectsInvalidPriceAndVat() {
    assertThrows(
        ApiException.class,
        () ->
            workflow.createDraft(
                draft(DocumentType.SUPPLIER_BILL, new BigDecimal("-1"), null, null)));
    assertThrows(
        ApiException.class,
        () ->
            workflow.createDraft(
                draft(DocumentType.SUPPLIER_BILL, null, new BigDecimal("100"), null)));
    assertThrows(
        ApiException.class,
        () ->
            workflow.createDraft(
                draft(DocumentType.SUPPLIER_BILL, null, new BigDecimal("-1"), null)));
  }

  @Test
  void rejectsBillingCurrencyThatDiffersFromItsSourceDocument() {
    Currency otherCurrency = Currency.builder().id(2L).code("USD").name("Dollar").build();
    when(currencyService.findById(2L)).thenReturn(Optional.of(otherCurrency));
    CommercialDraft mismatchedCurrency =
        new CommercialDraft(
            DocumentType.SUPPLIER_BILL,
            List.of(SOURCE_ID),
            LocalDate.now(),
            2L,
            null,
            null,
            null,
            null,
            List.of(new CommercialLineDraft(SOURCE_LINE_ID, BigDecimal.ONE, null, null, null)));

    assertThrows(ApiException.class, () -> workflow.createDraft(mismatchedCurrency));
  }

  @Test
  void rejectsCreationWhenRoleTypeSourceOrLineIsInvalid() {
    when(access.canCreateType(any(), any())).thenReturn(false);
    assertThrows(
        ApiException.class,
        () -> workflow.createDraft(draft(DocumentType.SUPPLIER_BILL, null, null, null)));
    when(access.canCreateType(any(), any())).thenReturn(true);
    when(currencyService.findById(99L)).thenReturn(Optional.empty());
    assertThrows(
        ApiException.class,
        () ->
            workflow.createDraft(
                new CommercialDraft(
                    DocumentType.SUPPLIER_BILL,
                    List.of(SOURCE_ID),
                    LocalDate.now(),
                    99L,
                    null,
                    null,
                    null,
                    null,
                    List.of(
                        new CommercialLineDraft(
                            SOURCE_LINE_ID, BigDecimal.ONE, null, null, null)))));
    assertThrows(
        ApiException.class,
        () -> workflow.createDraft(draft(DocumentType.SALES_ORDER, null, null, null)));
    assertThrows(
        ApiException.class,
        () ->
            workflow.createDraft(
                new CommercialDraft(
                    DocumentType.SUPPLIER_BILL,
                    List.of(SOURCE_ID, SOURCE_ID),
                    LocalDate.now(),
                    1L,
                    null,
                    null,
                    null,
                    null,
                    List.of(
                        new CommercialLineDraft(
                            SOURCE_LINE_ID, BigDecimal.ONE, null, null, null)))));
    assertThrows(
        ApiException.class,
        () ->
            workflow.createDraft(
                new CommercialDraft(
                    DocumentType.SUPPLIER_BILL,
                    List.of(SOURCE_ID),
                    LocalDate.now(),
                    1L,
                    null,
                    null,
                    null,
                    null,
                    List.of(new CommercialLineDraft(999L, BigDecimal.ONE, null, null, null)))));
    source.setNonBillable(true);
    assertThrows(
        ApiException.class,
        () -> workflow.createDraft(draft(DocumentType.SUPPLIER_BILL, null, null, null)));
  }

  @Test
  void rejectsMissingActorCurrencyEmptyLinesAndInvalidQuantities() {
    when(access.currentActor()).thenReturn(null);
    assertThrows(
        ApiException.class,
        () -> workflow.createDraft(draft(DocumentType.SUPPLIER_BILL, null, null, null)));
    when(access.currentActor()).thenReturn(manager);

    assertThrows(
        ApiException.class,
        () ->
            workflow.createDraft(
                new CommercialDraft(
                    DocumentType.FINANCIAL_CREDIT,
                    List.of(SOURCE_ID, 12L),
                    LocalDate.now(),
                    1L,
                    null,
                    null,
                    null,
                    null,
                    List.of(
                        new CommercialLineDraft(
                            SOURCE_LINE_ID, BigDecimal.ONE, null, null, null)))));

    source.setCurrency(null);
    assertThrows(
        ApiException.class,
        () -> workflow.createDraft(draft(DocumentType.SUPPLIER_BILL, null, null, null)));
    source.setCurrency(currency);

    assertThrows(
        ApiException.class,
        () ->
            workflow.createDraft(
                new CommercialDraft(
                    DocumentType.SUPPLIER_BILL,
                    List.of(SOURCE_ID),
                    LocalDate.now(),
                    1L,
                    null,
                    null,
                    null,
                    null,
                    List.of())));
    assertThrows(
        ApiException.class,
        () ->
            workflow.createDraft(
                new CommercialDraft(
                    DocumentType.SUPPLIER_BILL,
                    List.of(SOURCE_ID),
                    LocalDate.now(),
                    1L,
                    null,
                    null,
                    null,
                    null,
                    List.of(
                        new CommercialLineDraft(
                            SOURCE_LINE_ID, BigDecimal.ZERO, null, null, null)))));
    assertThrows(
        ApiException.class,
        () ->
            workflow.createDraft(
                new CommercialDraft(
                    DocumentType.SUPPLIER_BILL,
                    List.of(SOURCE_ID),
                    LocalDate.now(),
                    1L,
                    null,
                    null,
                    null,
                    null,
                    List.of(
                        new CommercialLineDraft(
                            SOURCE_LINE_ID, new BigDecimal("-1"), null, null, null)))));
    assertThrows(
        ApiException.class,
        () ->
            workflow.createDraft(
                new CommercialDraft(
                    DocumentType.SUPPLIER_BILL,
                    List.of(),
                    LocalDate.now(),
                    1L,
                    null,
                    null,
                    null,
                    null,
                    List.of(
                        new CommercialLineDraft(
                            SOURCE_LINE_ID, BigDecimal.ONE, null, null, null)))));
  }

  @Test
  void updateRequiresDraftManagePermissionAndMatchingCurrency() {
    workflow.createDraft(draft(DocumentType.SUPPLIER_BILL, null, null, null));
    Document bill = savedDocument.get();
    lenient().when(access.lock(COMMERCIAL_ID, COMPANY_ID)).thenReturn(bill);
    CommercialDraftUpdate update =
        new CommercialDraftUpdate(0L, draft(DocumentType.SUPPLIER_BILL, null, null, null));

    bill.setStatus(DocumentStatus.CANCELLED);
    assertThrows(ApiException.class, () -> workflow.updateDraft(COMMERCIAL_ID, update));
    bill.setStatus(DocumentStatus.DRAFT);
    when(access.canManageDocument(bill, manager)).thenReturn(false);
    assertThrows(ApiException.class, () -> workflow.updateDraft(COMMERCIAL_ID, update));
    when(access.canManageDocument(bill, manager)).thenReturn(true);
    bill.setCurrency(null);
    assertThrows(ApiException.class, () -> workflow.updateDraft(COMMERCIAL_ID, update));
  }

  @Test
  void rejectsCreditQuantityAboveSourceAndNegativeAmount() {
    source.setType(DocumentType.SUPPLIER_BILL);
    source.setStatus(DocumentStatus.RECEIVED);
    assertThrows(
        ApiException.class,
        () ->
            workflow.createDraft(
                new CommercialDraft(
                    DocumentType.FINANCIAL_CREDIT,
                    List.of(SOURCE_ID),
                    LocalDate.now(),
                    1L,
                    null,
                    null,
                    null,
                    null,
                    List.of(
                        new CommercialLineDraft(
                            SOURCE_LINE_ID, new BigDecimal("6"), null, null, null)))));
    assertThrows(
        ApiException.class,
        () ->
            workflow.createDraft(
                draft(DocumentType.FINANCIAL_CREDIT, null, null, new BigDecimal("-1"))));
  }

  @Test
  void financialCreditMustUseExactlyOneUniqueSourceDocument() {
    source.setType(DocumentType.CUSTOMER_INVOICE);
    source.setStatus(DocumentStatus.ISSUED);
    assertThrows(
        ApiException.class,
        () ->
            workflow.createDraft(
                new CommercialDraft(
                    DocumentType.FINANCIAL_CREDIT,
                    List.of(SOURCE_ID, 12L),
                    LocalDate.now(),
                    1L,
                    null,
                    null,
                    null,
                    null,
                    List.of(
                        new CommercialLineDraft(
                            SOURCE_LINE_ID, BigDecimal.ONE, null, null, null)))));
  }

  @Test
  void rejectsApprovalForNonManagerAndPreventsFinalizationUntilApproved() {
    workflow.createDraft(draft(DocumentType.SUPPLIER_BILL, new BigDecimal("8"), null, null));
    Document bill = savedDocument.get();
    lenient().when(access.lock(COMMERCIAL_ID, COMPANY_ID)).thenReturn(bill);
    assertThrows(ApiException.class, () -> workflow.finalizeDocument(COMMERCIAL_ID, "0"));
    manager.setRole(UserRole.SALES);
    assertThrows(
        ApiException.class,
        () -> workflow.decide(COMMERCIAL_ID, new CommercialDecision(0L, "Rejected"), false));
  }

  @Test
  void recordsRejectionAndRejectsInvalidSettlementAssignment() {
    workflow.createDraft(draft(DocumentType.SUPPLIER_BILL, new BigDecimal("8"), null, null));
    Document bill = savedDocument.get();
    lenient().when(access.lock(COMMERCIAL_ID, COMPANY_ID)).thenReturn(bill);
    workflow.decide(COMMERCIAL_ID, new CommercialDecision(0L, "Incorrect price"), false);
    assertEquals(CommercialApprovalStatus.REJECTED, bill.getApprovalStatus());
    assertThrows(ApiException.class, () -> workflow.finalizeDocument(COMMERCIAL_ID, "0"));

    bill.setFinancialSettlementRequired(false);
    assertThrows(
        ApiException.class,
        () -> workflow.assignSettlementOwner(COMMERCIAL_ID, new SettlementAssignment(0L, 5L)));
  }

  @Test
  void settlementOwnersOnlyIncludesActiveCompanyUsers() {
    User inactive = User.builder().id(6L).username("inactive").status(UserStatus.SUSPENDED).build();
    when(userService.findAllByCompanyId(COMPANY_ID)).thenReturn(List.of(manager, inactive));

    assertEquals(1, workflow.settlementOwners().size());
    manager.setRole(UserRole.ADMIN);
    assertEquals(1, workflow.settlementOwners().size());
  }

  @Test
  void rejectsSettlementOwnerLookupAndCommercialFinalizationForSalesOrInvalidState() {
    manager.setRole(UserRole.SALES);
    assertThrows(ApiException.class, () -> workflow.settlementOwners());
    manager.setRole(UserRole.MANAGER);
    when(access.currentActor()).thenReturn(null);
    assertThrows(ApiException.class, () -> workflow.settlementOwners());
    when(access.currentActor()).thenReturn(manager);

    Document invalidDocument = new Document();
    invalidDocument.setId(COMMERCIAL_ID);
    invalidDocument.setType(DocumentType.SALES_ORDER);
    invalidDocument.setStatus(DocumentStatus.DRAFT);
    lenient().when(access.lock(COMMERCIAL_ID, COMPANY_ID)).thenReturn(invalidDocument);
    when(access.canManageDocument(invalidDocument, manager)).thenReturn(false);
    assertThrows(ApiException.class, () -> workflow.finalizeDocument(COMMERCIAL_ID, "0"));
    when(access.canManageDocument(invalidDocument, manager)).thenReturn(true);
    assertThrows(ApiException.class, () -> workflow.finalizeDocument(COMMERCIAL_ID, "0"));
    assertThrows(
        ApiException.class,
        () -> workflow.decide(COMMERCIAL_ID, new CommercialDecision(0L, "No approval"), true));
  }

  @Test
  void settlementAssignmentRequiresEligibleCompanyOwner() {
    Document document = new Document();
    document.setId(COMMERCIAL_ID);
    document.setCompany(company);
    document.setStatus(DocumentStatus.RECEIVED);
    document.setType(DocumentType.SUPPLIER_BILL);
    document.setFinancialSettlementRequired(true);
    lenient().when(access.lock(COMMERCIAL_ID, COMPANY_ID)).thenReturn(document);
    when(userService.findByIdOrThrow(manager.getId())).thenReturn(manager);

    workflow.assignSettlementOwner(COMMERCIAL_ID, new SettlementAssignment(0L, manager.getId()));

    assertEquals(manager, document.getFinancialSettlementOwner());
  }

  private CommercialDraft draft(
      final DocumentType type,
      final BigDecimal unitPrice,
      final BigDecimal vatRate,
      final BigDecimal amount) {
    return new CommercialDraft(
        type,
        List.of(SOURCE_ID),
        LocalDate.of(2026, 11, 1),
        1L,
        null,
        "Correction",
        "EXT-1",
        null,
        List.of(
            new CommercialLineDraft(
                SOURCE_LINE_ID,
                amount == null ? BigDecimal.ONE : BigDecimal.ZERO,
                unitPrice,
                vatRate,
                amount)));
  }

  private Document existingBill(final BigDecimal quantity) {
    Document bill = new Document();
    bill.setId(30L);
    bill.setType(DocumentType.SUPPLIER_BILL);
    DocumentLine billed = new DocumentLine();
    billed.setSourceLine(sourceLine);
    billed.setQuantity(quantity);
    bill.setLines(List.of(billed));
    return bill;
  }

  private Document secondReceipt() {
    Document second = new Document();
    second.setId(12L);
    second.setCompany(company);
    second.setCurrency(currency);
    second.setType(DocumentType.GOODS_RECEIPT);
    second.setStatus(DocumentStatus.COMPLETED);
    DocumentLine line = new DocumentLine();
    line.setId(13L);
    line.setQuantity(new BigDecimal("3"));
    line.setUnitPrice(new BigDecimal("4"));
    line.setVatRateSnapshot(new BigDecimal("27"));
    second.setLines(List.of(line));
    return second;
  }

  private CommercialDraft twoSourceDraft() {
    return new CommercialDraft(
        DocumentType.SUPPLIER_BILL,
        List.of(SOURCE_ID, 12L),
        LocalDate.now(),
        1L,
        null,
        null,
        null,
        null,
        List.of(
            new CommercialLineDraft(SOURCE_LINE_ID, BigDecimal.ONE, null, null, null),
            new CommercialLineDraft(13L, BigDecimal.ONE, null, null, null)));
  }
}
