package dev.roland.inventory_management_backend.features.document.facade.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import dev.roland.inventory_management_backend.common.async.OutboxService;
import dev.roland.inventory_management_backend.common.exception.ApiException;
import dev.roland.inventory_management_backend.common.service.CurrentCompanyProvider;
import dev.roland.inventory_management_backend.common.service.ObjectStorageService;
import dev.roland.inventory_management_backend.features.auth.CustomUserDetails;
import dev.roland.inventory_management_backend.features.company.Company;
import dev.roland.inventory_management_backend.features.contact.service.ContactService;
import dev.roland.inventory_management_backend.features.currency.Currency;
import dev.roland.inventory_management_backend.features.currency.service.CurrencyService;
import dev.roland.inventory_management_backend.features.document.Document;
import dev.roland.inventory_management_backend.features.document.DocumentExchangeRate;
import dev.roland.inventory_management_backend.features.document.dto.CloseRemaining;
import dev.roland.inventory_management_backend.features.document.dto.Draft;
import dev.roland.inventory_management_backend.features.document.dto.DraftUpdate;
import dev.roland.inventory_management_backend.features.document.dto.ExchangeRateDraft;
import dev.roland.inventory_management_backend.features.document.dto.FulfillmentDraft;
import dev.roland.inventory_management_backend.features.document.dto.FulfillmentLine;
import dev.roland.inventory_management_backend.features.document.dto.FulfillmentPreview;
import dev.roland.inventory_management_backend.features.document.dto.LineDraft;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentStatus;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentType;
import dev.roland.inventory_management_backend.features.document.facade.workflow.DocumentDraftWorkflowService;
import dev.roland.inventory_management_backend.features.document.facade.workflow.DocumentFileWorkflowService;
import dev.roland.inventory_management_backend.features.document.facade.workflow.DocumentFulfillmentWorkflowService;
import dev.roland.inventory_management_backend.features.document.facade.workflow.DocumentWorkflowAccessService;
import dev.roland.inventory_management_backend.features.document.facade.workflow.DocumentWorkflowWriterService;
import dev.roland.inventory_management_backend.features.document.facade.workflow.WarehouseOperationCommandService;
import dev.roland.inventory_management_backend.features.document.facade.workflow.WarehouseOperationPreviewService;
import dev.roland.inventory_management_backend.features.document.facade.workflow.WarehouseOperationRecordService;
import dev.roland.inventory_management_backend.features.document.facade.workflow.WarehouseOperationSupportService;
import dev.roland.inventory_management_backend.features.document.facade.workflow.WarehouseTransferPreviewService;
import dev.roland.inventory_management_backend.features.document.mapper.DocumentResponseMapper;
import dev.roland.inventory_management_backend.features.document.message.DocumentApiMessageKey;
import dev.roland.inventory_management_backend.features.document.service.DocumentPostingService;
import dev.roland.inventory_management_backend.features.document.service.DocumentService;
import dev.roland.inventory_management_backend.features.document_activity.service.DocumentActivityService;
import dev.roland.inventory_management_backend.features.document_file.DocumentFile;
import dev.roland.inventory_management_backend.features.document_file.DocumentFileStatus;
import dev.roland.inventory_management_backend.features.document_file.service.DocumentFileService;
import dev.roland.inventory_management_backend.features.document_line.service.DocumentLineService;
import dev.roland.inventory_management_backend.features.document_line_allocation.service.DocumentLineAllocationService;
import dev.roland.inventory_management_backend.features.document_prefix.DocumentPrefix;
import dev.roland.inventory_management_backend.features.document_prefix.service.DocumentPrefixService;
import dev.roland.inventory_management_backend.features.document_relation.service.DocumentRelationService;
import dev.roland.inventory_management_backend.features.document_sequence.DocumentSequence;
import dev.roland.inventory_management_backend.features.document_sequence.service.DocumentSequenceService;
import dev.roland.inventory_management_backend.features.media_asset.service.MediaAssetService;
import dev.roland.inventory_management_backend.features.media_usage.enumeration.MediaEntityType;
import dev.roland.inventory_management_backend.features.media_usage.enumeration.MediaUsageType;
import dev.roland.inventory_management_backend.features.media_usage.service.MediaUsageService;
import dev.roland.inventory_management_backend.features.partner.service.PartnerService;
import dev.roland.inventory_management_backend.features.product.Product;
import dev.roland.inventory_management_backend.features.product.service.ProductService;
import dev.roland.inventory_management_backend.features.stock_balance.service.StockBalanceService;
import dev.roland.inventory_management_backend.features.stock_movement.service.StockMovementService;
import dev.roland.inventory_management_backend.features.unit.Unit;
import dev.roland.inventory_management_backend.features.unit.service.UnitService;
import dev.roland.inventory_management_backend.features.user.User;
import dev.roland.inventory_management_backend.features.user.enumeration.UserRole;
import dev.roland.inventory_management_backend.features.user.enumeration.UserStatus;
import dev.roland.inventory_management_backend.features.user.service.UserService;
import dev.roland.inventory_management_backend.features.warehouse.Warehouse;
import dev.roland.inventory_management_backend.features.warehouse.service.WarehouseService;

@ExtendWith(MockitoExtension.class)
class DocumentWorkflowFacadeImplTest {
  private static final Long COMPANY_ID = 9L;
  private static final Long USER_ID = 12L;

  @Mock private DocumentService documentService;
  @Mock private DocumentLineService lineService;
  @Mock private ProductService productService;
  @Mock private UnitService unitService;
  @Mock private PartnerService partnerService;
  @Mock private ContactService contactService;
  @Mock private CurrencyService currencyService;
  @Mock private WarehouseService warehouseService;
  @Mock private CurrentCompanyProvider companyProvider;
  @Mock private UserService userService;
  @Mock private DocumentPostingService postingService;
  @Mock private DocumentRelationService relationService;
  @Mock private DocumentLineAllocationService allocationService;
  @Mock private DocumentActivityService activityService;
  @Mock private DocumentFileService fileService;
  @Mock private DocumentPrefixService prefixService;
  @Mock private DocumentSequenceService sequenceService;
  @Mock private StockBalanceService balanceService;
  @Mock private MediaAssetService mediaAssetService;
  @Mock private MediaUsageService mediaUsageService;
  @Mock private ObjectStorageService objectStorage;
  @Mock private OutboxService outboxService;
  @Mock private StockMovementService stockMovementService;

  private DocumentWorkflowFacadeImpl facade;

  private Company company;
  private User user;
  private Currency preferredCurrency;

  @BeforeEach
  void setUp() {
    var documentWorkflowAccessService =
        new DocumentWorkflowAccessService(
            documentService, companyProvider, userService, allocationService, relationService);
    var documentResponseMapper =
        new DocumentResponseMapper(
            documentWorkflowAccessService,
            allocationService,
            relationService,
            activityService,
            mediaUsageService,
            fileService,
            objectStorage);
    var documentWorkflowWriterService =
        new DocumentWorkflowWriterService(
            activityService,
            documentWorkflowAccessService,
            documentService,
            prefixService,
            sequenceService,
            relationService,
            stockMovementService);
    var documentDraftWorkflowService =
        new DocumentDraftWorkflowService(
            documentWorkflowAccessService,
            documentWorkflowWriterService,
            documentResponseMapper,
            documentService,
            lineService,
            partnerService,
            contactService,
            warehouseService,
            companyProvider,
            currencyService,
            productService,
            unitService);
    var warehouseOperationSupportService =
        new WarehouseOperationSupportService(
            documentWorkflowAccessService,
            documentWorkflowWriterService,
            documentResponseMapper,
            documentDraftWorkflowService,
            lineService,
            warehouseService,
            companyProvider);
    var warehouseTransferPreviewService =
        new WarehouseTransferPreviewService(balanceService, warehouseOperationSupportService);
    var warehouseOperationPreviewService =
        new WarehouseOperationPreviewService(
            productService,
            warehouseService,
            companyProvider,
            relationService,
            allocationService,
            balanceService,
            stockMovementService,
            documentWorkflowAccessService,
            documentResponseMapper,
            warehouseOperationSupportService,
            warehouseTransferPreviewService);
    var warehouseOperationRecordService =
        new WarehouseOperationRecordService(
            documentService,
            lineService,
            productService,
            warehouseService,
            relationService,
            allocationService,
            stockMovementService,
            documentWorkflowAccessService,
            documentWorkflowWriterService,
            documentResponseMapper,
            warehouseOperationSupportService,
            warehouseOperationPreviewService);
    var warehouseOperationCommandService =
        new WarehouseOperationCommandService(
            documentService,
            lineService,
            productService,
            warehouseService,
            companyProvider,
            stockMovementService,
            documentWorkflowAccessService,
            documentWorkflowWriterService,
            documentResponseMapper,
            warehouseOperationSupportService,
            warehouseOperationPreviewService,
            warehouseOperationRecordService);
    var documentFulfillmentWorkflowService =
        new DocumentFulfillmentWorkflowService(
            documentService,
            lineService,
            productService,
            warehouseService,
            companyProvider,
            postingService,
            relationService,
            allocationService,
            balanceService,
            documentWorkflowAccessService,
            documentWorkflowWriterService,
            documentResponseMapper,
            documentDraftWorkflowService);
    var documentFileWorkflowService =
        new DocumentFileWorkflowService(
            companyProvider,
            fileService,
            mediaAssetService,
            mediaUsageService,
            objectStorage,
            outboxService,
            documentWorkflowAccessService,
            documentWorkflowWriterService,
            documentResponseMapper);
    var documentWorkflowFacadeImpl =
        new DocumentWorkflowFacadeImpl(
            documentService,
            companyProvider,
            documentWorkflowAccessService,
            documentResponseMapper,
            warehouseOperationCommandService,
            warehouseOperationPreviewService,
            documentFulfillmentWorkflowService,
            documentFileWorkflowService,
            documentDraftWorkflowService);
    facade = documentWorkflowFacadeImpl;
    preferredCurrency = Currency.builder().id(1L).code("EUR").name("Euro").build();
    company =
        Company.builder()
            .id(COMPANY_ID)
            .name("Example")
            .preferredCurrency(preferredCurrency)
            .build();
    user = User.builder().id(USER_ID).username("admin").role(UserRole.ADMIN).build();
    lenient().when(companyProvider.getCurrentCompany()).thenReturn(company);
    lenient().when(userService.findByIdOrThrow(USER_ID)).thenReturn(user);
    lenient()
        .when(relationService.findAllBySourceDocumentIdOrTargetDocumentId(anyLong(), anyLong()))
        .thenReturn(List.of());
    lenient()
        .when(activityService.findAllByDocumentIdOrderByOccurredAtAsc(anyLong()))
        .thenReturn(List.of());
    lenient()
        .when(
            mediaUsageService.findAllByEntityTypeAndEntityIdAndUsageTypeOrderBySortOrderAsc(
                any(MediaEntityType.class), anyLong(), any(MediaUsageType.class)))
        .thenReturn(List.of());
    lenient()
        .when(fileService.findAllByDocumentIdOrderByCreatedAtDesc(anyLong()))
        .thenReturn(List.of());
    lenient().when(allocationService.findAllByFulfillmentLineId(anyLong())).thenReturn(List.of());
    lenient()
        .when(allocationService.sumPostedAllocated(anyLong(), any(DocumentStatus.class)))
        .thenReturn(java.math.BigDecimal.ZERO);
    authenticateAsAdmin();
  }

  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void listRejectsInvalidPaginationAndUnknownQueue() {
    ApiException pagination =
        assertThrows(ApiException.class, () -> facade.list("ALL", null, -1, 20));
    assertEquals(DocumentApiMessageKey.DOCUMENT_INVALID_LINES, pagination.getMessageKey());

    ApiException queue =
        assertThrows(ApiException.class, () -> facade.list("UNKNOWN", null, 0, 20));
    assertEquals(DocumentApiMessageKey.DOCUMENT_INVALID_QUEUE_VIEW, queue.getMessageKey());
  }

  @Test
  void listReturnsCountsAndAnEmptyPageWhenCompanyHasNoDocuments() {
    when(documentService.findAllByCompanyIdOrderByUpdatedAtDesc(COMPANY_ID)).thenReturn(List.of());

    var page = facade.list("ALL", "  ", 0, 20);

    assertTrue(page.items().isEmpty());
    assertEquals(0, page.total());
    assertTrue(page.counts().values().stream().allMatch(count -> count == 0));
  }

  @Test
  void listAppliesQueueSearchAndPaginationRulesToCompanyDocuments() {
    final Document myDraft = document(DocumentType.PURCHASE_ORDER, DocumentStatus.DRAFT);
    myDraft.setCreatedByUser(user);
    myDraft.setPartnerNameSnapshot("Northwind");
    final Document purchase =
        documentWithLine(DocumentType.PURCHASE_ORDER, DocumentStatus.CONFIRMED);
    final Document sales = documentWithLine(DocumentType.SALES_ORDER, DocumentStatus.CONFIRMED);
    final Document partial =
        document(DocumentType.DELIVERY_NOTE, DocumentStatus.PARTIALLY_COMPLETED);
    final Document completed = document(DocumentType.SALES_ORDER, DocumentStatus.COMPLETED);
    when(documentService.findAllByCompanyIdOrderByUpdatedAtDesc(COMPANY_ID))
        .thenReturn(List.of(myDraft, purchase, sales, partial, completed));

    final var drafts = facade.list("MY_DRAFTS", "north", 0, 1);
    final var receive = facade.list("READY_TO_RECEIVE", null, 0, 10);
    final var ship = facade.list("READY_TO_SHIP", null, 0, 10);
    final var fulfilled = facade.list("PARTIALLY_FULFILLED", null, 0, 10);

    assertEquals(1, drafts.total());
    assertEquals(1, drafts.items().size());
    assertEquals(1, receive.total());
    assertEquals(1, ship.total());
    assertEquals(1, fulfilled.total());
    assertEquals(0, facade.list("ALL", "no-match", 0, 10).total());
  }

  @Test
  void capabilitiesReflectTheCurrentRole() {
    var capabilities = facade.capabilities();

    assertEquals(
        List.of(
            DocumentType.PURCHASE_ORDER,
            DocumentType.SALES_ORDER,
            DocumentType.GOODS_RECEIPT,
            DocumentType.DELIVERY_NOTE,
            DocumentType.TRANSFER_REQUEST,
            DocumentType.STOCK_COUNT,
            DocumentType.RETURN),
        capabilities.creatableTypes());
    assertFalse(capabilities.availableQueueViews().isEmpty());
  }

  @Test
  void capabilitiesLimitSalesUsersToSalesDocumentsAndDeliveryNotes() {
    user.setRole(UserRole.SALES);

    final var capabilities = facade.capabilities();

    assertEquals(
        List.of(DocumentType.SALES_ORDER, DocumentType.DELIVERY_NOTE),
        capabilities.creatableTypes());
  }

  @Test
  void managerCanCreateEverySupportedDocumentType() {
    user.setRole(UserRole.MANAGER);

    assertEquals(
        List.of(
            DocumentType.PURCHASE_ORDER,
            DocumentType.SALES_ORDER,
            DocumentType.GOODS_RECEIPT,
            DocumentType.DELIVERY_NOTE,
            DocumentType.TRANSFER_REQUEST,
            DocumentType.STOCK_COUNT,
            DocumentType.RETURN),
        facade.capabilities().creatableTypes());
  }

  @Test
  void getMapsDocumentTotalsAndMetadata() {
    final Document document = document(DocumentType.PURCHASE_ORDER, DocumentStatus.DRAFT);
    document.setDocumentNumber("PO-001");
    document.setDocumentDate(LocalDate.of(2026, 10, 31));
    document.setCreatedByUser(user);
    when(documentService.findByIdAndCompanyId(3L, COMPANY_ID))
        .thenReturn(java.util.Optional.of(document));

    var response = facade.get(3L);

    assertEquals("PO-001", response.number());
    assertEquals("PURCHASE_ORDER", response.type().name());
    assertEquals("admin", response.ownerName());
    assertEquals(
        0,
        response
            .totals()
            .gross()
            .compareTo(java.math.BigDecimal.ZERO.setScale(2, java.math.RoundingMode.UNNECESSARY)));
  }

  @Test
  void responseOffersRetryForTheLatestFailedGeneratedPdf() {
    final Document document = document(DocumentType.PURCHASE_ORDER, DocumentStatus.CONFIRMED);
    document.setCreatedByUser(user);
    final DocumentFile failedPdf = new DocumentFile();
    failedPdf.setSource(
        dev.roland.inventory_management_backend.features.document_file.DocumentFileSource
            .GENERATED_PDF);
    failedPdf.setStatus(
        dev.roland.inventory_management_backend.features.document_file.DocumentFileStatus.FAILED);
    when(documentService.findByIdAndCompanyId(3L, COMPANY_ID)).thenReturn(Optional.of(document));
    when(fileService.findAllByDocumentIdOrderByCreatedAtDesc(3L)).thenReturn(List.of(failedPdf));

    final var response = facade.get(3L);

    assertTrue(
        response
            .allowedActions()
            .contains(
                dev.roland.inventory_management_backend.features.document.enumeration.DocumentAction
                    .RETRY_PDF));
  }

  @Test
  void salesUsersCanManageTheirOwnDraftButNotAnotherUsersDraft() {
    user.setRole(UserRole.SALES);
    final Document ownDraft = document(DocumentType.SALES_ORDER, DocumentStatus.DRAFT);
    ownDraft.setCreatedByUser(user);
    final Document otherDraft = document(DocumentType.SALES_ORDER, DocumentStatus.DRAFT);
    otherDraft.setCreatedByUser(
        User.builder().id(99L).username("other").role(UserRole.SALES).build());
    when(documentService.findByIdAndCompanyId(3L, COMPANY_ID))
        .thenReturn(Optional.of(ownDraft), Optional.of(otherDraft));

    final var ownResponse = facade.get(3L);
    final var otherResponse = facade.get(3L);

    assertTrue(
        ownResponse
            .allowedActions()
            .contains(
                dev.roland.inventory_management_backend.features.document.enumeration.DocumentAction
                    .EDIT));
    assertTrue(otherResponse.allowedActions().isEmpty());
  }

  @Test
  void createDraftPersistsAndReturnsTheNewDraft() {
    when(documentService.save(any(Document.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var response =
        facade.createDraft(
            new Draft(DocumentType.PURCHASE_ORDER, null, null, null, "note", List.of()));

    assertEquals("DRAFT", response.status());
    assertEquals("note", response.notes());
  }

  @Test
  void createDraftSnapshotsWarehouseAndBuildsProductLines() {
    final Warehouse warehouse =
        Warehouse.builder().id(55L).code("WH-1").name("Main warehouse").company(company).build();
    final Unit unit = Unit.builder().id(7L).code("EA").name("Each").build();
    final Product product =
        Product.builder()
            .id(24L)
            .name("Widget")
            .sku("W-1")
            .vatRate(new BigDecimal("27"))
            .unit(unit)
            .build();
    when(warehouseService.findEntityByIdAndCompanyId(55L, COMPANY_ID)).thenReturn(warehouse);
    when(productService.findByIdAndCompanyId(24L, COMPANY_ID)).thenReturn(Optional.of(product));
    when(unitService.findById(7L)).thenReturn(Optional.of(unit));
    when(documentService.save(any(Document.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    final var response =
        facade.createDraft(
            new Draft(
                DocumentType.PURCHASE_ORDER,
                null,
                null,
                55L,
                null,
                List.of(new LineDraft(24L, 7L, new BigDecimal("3"), new BigDecimal("2.50")))));

    assertEquals("Main warehouse", response.routeName());
    assertEquals(1, response.lines().size());
    assertEquals(new BigDecimal("3"), response.lines().getFirst().orderedQuantity());
    assertEquals(new BigDecimal("2.50"), response.lines().getFirst().unitPrice());
    assertEquals(preferredCurrency.getId(), response.lines().getFirst().sourceCurrencyId());
  }

  @Test
  void draftPriceRoundTripsWithoutApplyingExchangeRateTwice() {
    final Currency sourceCurrency = Currency.builder().id(3L).code("GBP").name("Pound").build();
    final Currency targetCurrency = Currency.builder().id(2L).code("USD").name("Dollar").build();
    final Unit unit = Unit.builder().id(7L).code("EA").name("Each").build();
    final Product product =
        Product.builder()
            .id(24L)
            .name("Widget")
            .sku("W-1")
            .unit(unit)
            .currency(sourceCurrency)
            .build();
    when(currencyService.findById(2L)).thenReturn(Optional.of(targetCurrency));
    when(currencyService.findById(3L)).thenReturn(Optional.of(sourceCurrency));
    when(productService.findByIdAndCompanyId(24L, COMPANY_ID)).thenReturn(Optional.of(product));
    when(unitService.findById(7L)).thenReturn(Optional.of(unit));
    when(documentService.save(any(Document.class)))
        .thenAnswer(
            invocation -> {
              final Document document = invocation.getArgument(0);
              if (document.getId() == null) document.setId(3L);
              if (document.getVersion() == null) document.setVersion(0L);
              return document;
            });

    final var created =
        facade.createDraft(
            new Draft(
                DocumentType.PURCHASE_ORDER,
                null,
                null,
                null,
                null,
                List.of(new LineDraft(24L, 7L, new BigDecimal("2"), new BigDecimal("2.50"))),
                targetCurrency.getId(),
                List.of(new ExchangeRateDraft(sourceCurrency.getId(), new BigDecimal("1.08")))));

    assertEquals(new BigDecimal("2.7000"), created.lines().getFirst().unitPrice());
    assertEquals(new BigDecimal("2.50"), created.lines().getFirst().sourceUnitPrice());
    assertEquals(sourceCurrency.getId(), created.lines().getFirst().sourceCurrencyId());
    assertEquals("USD", created.currencyCode());
    assertEquals(new BigDecimal("1.08"), created.exchangeRates().getFirst().rate());

    final Document document = document(DocumentType.PURCHASE_ORDER, DocumentStatus.DRAFT);
    document.setCreatedByUser(user);
    document.setCurrency(targetCurrency);
    document.setExchangeRates(
        new java.util.ArrayList<>(
            List.of(exchangeRate(document, sourceCurrency, new BigDecimal("1.08")))));
    product.setCurrency(Currency.builder().id(4L).code("JPY").name("Yen").build());
    when(documentService.lockByIdAndCompanyId(3L, COMPANY_ID)).thenReturn(Optional.of(document));

    final var updated =
        facade.updateDraft(
            3L,
            new DraftUpdate(
                created.version(),
                new Draft(
                    DocumentType.PURCHASE_ORDER,
                    null,
                    null,
                    null,
                    null,
                    List.of(
                        new LineDraft(
                            24L,
                            7L,
                            new BigDecimal("2"),
                            created.lines().getFirst().unitPrice(),
                            created.lines().getFirst().sourceUnitPrice(),
                            created.lines().getFirst().sourceCurrencyId())),
                    targetCurrency.getId(),
                    List.of(
                        new ExchangeRateDraft(sourceCurrency.getId(), new BigDecimal("1.08"))))));

    assertEquals(new BigDecimal("2.7000"), updated.lines().getFirst().unitPrice());
    assertEquals(new BigDecimal("2.50"), updated.lines().getFirst().sourceUnitPrice());
    assertEquals(sourceCurrency.getId(), updated.lines().getFirst().sourceCurrencyId());
  }

  @Test
  void draftRejectsUnknownSourceCurrencySnapshot() {
    final Unit unit = Unit.builder().id(7L).code("EA").name("Each").build();
    final Product product = Product.builder().id(24L).unit(unit).build();
    when(currencyService.findById(preferredCurrency.getId()))
        .thenReturn(Optional.of(preferredCurrency));
    when(productService.findByIdAndCompanyId(24L, COMPANY_ID)).thenReturn(Optional.of(product));
    when(unitService.findById(7L)).thenReturn(Optional.of(unit));
    when(documentService.save(any(Document.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    final ApiException exception =
        assertThrows(
            ApiException.class,
            () ->
                facade.createDraft(
                    new Draft(
                        DocumentType.PURCHASE_ORDER,
                        null,
                        null,
                        null,
                        null,
                        List.of(
                            new LineDraft(
                                24L, 7L, BigDecimal.ONE, BigDecimal.TEN, BigDecimal.TEN, 999L)),
                        preferredCurrency.getId(),
                        List.of())));

    assertEquals(DocumentApiMessageKey.DOCUMENT_INVALID_LINES, exception.getMessageKey());
  }

  @Test
  void directDraftRequiresAWarehouse() {
    ApiException exception =
        assertThrows(
            ApiException.class,
            () ->
                facade.createDraft(
                    new Draft(DocumentType.DELIVERY_NOTE, null, null, null, null, List.of())));

    assertEquals(DocumentApiMessageKey.DOCUMENT_INVALID_LINES, exception.getMessageKey());
  }

  @Test
  void updateRejectsNonDraftDocuments() {
    final Document document = document(DocumentType.PURCHASE_ORDER, DocumentStatus.CONFIRMED);
    when(documentService.lockByIdAndCompanyId(3L, COMPANY_ID))
        .thenReturn(java.util.Optional.of(document));

    ApiException exception =
        assertThrows(
            ApiException.class,
            () ->
                facade.updateDraft(
                    3L,
                    new DraftUpdate(
                        "1",
                        new Draft(
                            DocumentType.PURCHASE_ORDER, null, null, null, null, List.of()))));

    assertEquals(DocumentApiMessageKey.DOCUMENT_INVALID_TRANSITION, exception.getMessageKey());
  }

  @Test
  void updateDraftSavesAnAuthorizedDraft() {
    final Document document = document(DocumentType.PURCHASE_ORDER, DocumentStatus.DRAFT);
    document.setCreatedByUser(user);
    when(documentService.lockByIdAndCompanyId(3L, COMPANY_ID))
        .thenReturn(java.util.Optional.of(document));
    when(documentService.save(document)).thenReturn(document);

    var response =
        facade.updateDraft(
            3L,
            new DraftUpdate(
                "0",
                new Draft(DocumentType.PURCHASE_ORDER, null, null, null, "updated", List.of())));

    assertEquals("updated", response.notes());
    assertEquals("DRAFT", response.status());
  }

  @Test
  void updateDraftPreservesTheManagedLinesCollection() {
    final Document document = document(DocumentType.PURCHASE_ORDER, DocumentStatus.DRAFT);
    document.setCreatedByUser(user);
    final List<dev.roland.inventory_management_backend.features.document_line.DocumentLine>
        managedLines = document.getLines();
    when(documentService.lockByIdAndCompanyId(3L, COMPANY_ID)).thenReturn(Optional.of(document));
    when(documentService.save(document)).thenReturn(document);

    facade.updateDraft(
        3L,
        new DraftUpdate(
            "0", new Draft(DocumentType.PURCHASE_ORDER, null, null, null, "updated", List.of())));

    assertTrue(document.getLines() == managedLines);
  }

  @Test
  void updateDraftUpdatesExchangeRatesInPlaceAndRecalculatesLinePrices() {
    final Currency targetCurrency = Currency.builder().id(2L).code("USD").name("Dollar").build();
    final Currency sourceCurrency = Currency.builder().id(3L).code("GBP").name("Pound").build();
    final Unit unit = Unit.builder().id(7L).code("EA").name("Each").build();
    final Product product =
        Product.builder().id(24L).name("Widget").unit(unit).currency(sourceCurrency).build();
    final Document document = document(DocumentType.PURCHASE_ORDER, DocumentStatus.DRAFT);
    document.setCreatedByUser(user);
    document.setCurrency(targetCurrency);
    final List<DocumentExchangeRate> managedRates =
        new java.util.ArrayList<>(
            List.of(exchangeRate(document, sourceCurrency, new BigDecimal("1.08"))));
    document.setExchangeRates(managedRates);
    when(currencyService.findById(2L)).thenReturn(Optional.of(targetCurrency));
    when(currencyService.findById(3L)).thenReturn(Optional.of(sourceCurrency));
    when(productService.findByIdAndCompanyId(24L, COMPANY_ID)).thenReturn(Optional.of(product));
    when(unitService.findById(7L)).thenReturn(Optional.of(unit));
    when(documentService.lockByIdAndCompanyId(3L, COMPANY_ID)).thenReturn(Optional.of(document));
    when(documentService.save(document)).thenReturn(document);

    final var response =
        facade.updateDraft(
            3L,
            new DraftUpdate(
                "0",
                new Draft(
                    DocumentType.PURCHASE_ORDER,
                    null,
                    null,
                    null,
                    null,
                    List.of(
                        new LineDraft(
                            24L,
                            7L,
                            new BigDecimal("2"),
                            new BigDecimal("5.00"),
                            new BigDecimal("5.00"),
                            3L)),
                    2L,
                    List.of(new ExchangeRateDraft(3L, new BigDecimal("1.25"))))));

    assertTrue(document.getExchangeRates() == managedRates);
    assertEquals(new BigDecimal("1.25"), managedRates.getFirst().getRate());
    assertEquals(new BigDecimal("6.2500"), response.lines().getFirst().unitPrice());
  }

  @Test
  void linkedFulfillmentDraftCannotBeEditedWithoutReallocatingItsLines() {
    final Document fulfillment = document(DocumentType.DELIVERY_NOTE, DocumentStatus.DRAFT);
    fulfillment.setCreatedByUser(user);
    final var relation =
        new dev.roland.inventory_management_backend.features.document_relation.DocumentRelation();
    relation.setSourceDocument(document(DocumentType.SALES_ORDER, DocumentStatus.CONFIRMED));
    relation.setTargetDocument(fulfillment);
    relation.setRelationType(
        dev.roland.inventory_management_backend.features.document.enumeration.DocumentRelationType
            .FULFILLS);
    when(documentService.lockByIdAndCompanyId(3L, COMPANY_ID)).thenReturn(Optional.of(fulfillment));
    when(relationService.findAllBySourceDocumentIdOrTargetDocumentId(3L, 3L))
        .thenReturn(List.of(relation));

    ApiException exception =
        assertThrows(
            ApiException.class,
            () ->
                facade.updateDraft(
                    3L,
                    new DraftUpdate(
                        "0",
                        new Draft(DocumentType.DELIVERY_NOTE, null, null, 55L, null, List.of()))));

    assertEquals(DocumentApiMessageKey.DOCUMENT_INVALID_TRANSITION, exception.getMessageKey());
  }

  @Test
  void fulfillmentDraftRejectsNonOperationTypesAndMissingWarehouse() {
    ApiException invalidType =
        assertThrows(
            ApiException.class,
            () ->
                facade.createFulfillmentDraft(
                    new FulfillmentDraft(null, DocumentType.SALES_ORDER, null, List.of())));
    assertEquals(DocumentApiMessageKey.DOCUMENT_INVALID_TRANSITION, invalidType.getMessageKey());

    ApiException missingWarehouse =
        assertThrows(
            ApiException.class,
            () ->
                facade.createFulfillmentDraft(
                    new FulfillmentDraft(null, DocumentType.GOODS_RECEIPT, null, List.of())));
    assertEquals(DocumentApiMessageKey.DOCUMENT_INVALID_LINES, missingWarehouse.getMessageKey());
  }

  @Test
  void fulfillmentDraftCanBeCreatedWithoutSourceLines() {
    final Warehouse warehouse =
        Warehouse.builder().id(55L).code("WH-1").name("Main warehouse").company(company).build();
    when(warehouseService.findEntityByIdAndCompanyId(55L, COMPANY_ID)).thenReturn(warehouse);
    when(documentService.save(any(Document.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var response =
        facade.createFulfillmentDraft(
            new FulfillmentDraft(null, DocumentType.GOODS_RECEIPT, 55L, List.of()));

    assertEquals("DRAFT", response.status());
    assertEquals("Main warehouse", response.routeName());
  }

  @Test
  void fulfillmentDraftCopiesLinesFromAConfirmedSourceOrder() {
    final Document source = documentWithLine(DocumentType.PURCHASE_ORDER, DocumentStatus.CONFIRMED);
    source.setPartnerNameSnapshot("Northwind");
    source.setTargetWarehouse(
        Warehouse.builder().id(55L).code("WH-1").name("Main warehouse").company(company).build());
    source.getLines().getFirst().setDocument(source);
    when(documentService.findByIdAndCompanyId(3L, COMPANY_ID)).thenReturn(Optional.of(source));
    when(warehouseService.findEntityByIdAndCompanyId(55L, COMPANY_ID))
        .thenReturn(source.getTargetWarehouse());
    when(documentService.save(any(Document.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    final var response =
        facade.createFulfillmentDraft(
            new FulfillmentDraft(
                3L,
                DocumentType.GOODS_RECEIPT,
                null,
                List.of(new FulfillmentLine(44L, null, new BigDecimal("2")))));

    assertEquals("DRAFT", response.status());
    assertEquals("Northwind", response.partnerName());
    assertEquals(1, response.lines().size());
    assertEquals(new BigDecimal("2"), response.lines().getFirst().orderedQuantity());
  }

  @Test
  void fulfillmentWarehouseOverrideKeepsTheSelectedWarehouseSnapshot() {
    final Document source = documentWithLine(DocumentType.SALES_ORDER, DocumentStatus.CONFIRMED);
    source.setSourceWarehouse(
        Warehouse.builder().id(55L).code("OLD").name("Old warehouse").company(company).build());
    final Warehouse selected =
        Warehouse.builder().id(56L).code("NEW").name("New warehouse").company(company).build();
    when(documentService.findByIdAndCompanyId(3L, COMPANY_ID)).thenReturn(Optional.of(source));
    when(warehouseService.findEntityByIdAndCompanyId(56L, COMPANY_ID)).thenReturn(selected);
    when(documentService.save(any(Document.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    final var response =
        facade.createFulfillmentDraft(
            new FulfillmentDraft(3L, DocumentType.DELIVERY_NOTE, 56L, List.of()));

    assertEquals("New warehouse", response.routeName());
  }

  @Test
  void previewWithoutWarehouseIsInvalidAndHasNoLines() {
    var preview =
        facade.preview(
            new FulfillmentPreview(null, null, DocumentType.DELIVERY_NOTE, null, List.of()));

    assertFalse(preview.valid());
    assertTrue(preview.lines().isEmpty());
  }

  @Test
  void previewWithWarehouseAndNoLinesIsValid() {
    final Warehouse warehouse =
        Warehouse.builder().id(55L).code("WH-1").name("Main warehouse").company(company).build();
    when(warehouseService.findEntityByIdAndCompanyId(55L, COMPANY_ID)).thenReturn(warehouse);

    var preview =
        facade.preview(
            new FulfillmentPreview(null, null, DocumentType.DELIVERY_NOTE, 55L, List.of()));

    assertTrue(preview.valid());
    assertTrue(preview.lines().isEmpty());
  }

  @Test
  void previewCalculatesStockDeltaAndReportsInsufficientStock() {
    final Warehouse warehouse =
        Warehouse.builder().id(55L).code("WH-1").name("Main warehouse").company(company).build();
    final Product product = Product.builder().id(24L).name("Widget").build();
    when(warehouseService.findEntityByIdAndCompanyId(55L, COMPANY_ID)).thenReturn(warehouse);
    when(productService.findByIdAndCompanyId(24L, COMPANY_ID)).thenReturn(Optional.of(product));
    when(balanceService.findByWarehouseAndProduct(warehouse, product)).thenReturn(Optional.empty());

    final var preview =
        facade.preview(
            new FulfillmentPreview(
                null,
                null,
                DocumentType.DELIVERY_NOTE,
                55L,
                List.of(new FulfillmentLine(null, 24L, new BigDecimal("2")))));

    assertFalse(preview.valid());
    assertEquals(new BigDecimal("-2"), preview.lines().getFirst().quantityDelta());
    assertEquals(
        DocumentApiMessageKey.DOCUMENT_INSUFFICIENT_STOCK.getKey(),
        preview.lines().getFirst().errorMessageKey());
  }

  @Test
  void directPreviewAppliesProductConversionFactor() {
    final Warehouse warehouse =
        Warehouse.builder().id(55L).code("WH-1").name("Main warehouse").company(company).build();
    final Unit main = Unit.builder().id(2L).build();
    final Unit secondary = Unit.builder().id(3L).build();
    final Product product =
        Product.builder()
            .id(24L)
            .name("Widget")
            .unit(main)
            .secondaryUnit(secondary)
            .secondaryUnitsPerMainUnit(new BigDecimal("10"))
            .build();
    when(warehouseService.findEntityByIdAndCompanyId(55L, COMPANY_ID)).thenReturn(warehouse);
    when(productService.findByIdAndCompanyId(24L, COMPANY_ID)).thenReturn(Optional.of(product));
    when(balanceService.findByWarehouseAndProduct(warehouse, product)).thenReturn(Optional.empty());

    final var preview =
        facade.preview(
            new FulfillmentPreview(
                null,
                null,
                DocumentType.DELIVERY_NOTE,
                55L,
                List.of(new FulfillmentLine(null, 24L, new BigDecimal("2")))));

    assertEquals(new BigDecimal("-20"), preview.lines().getFirst().quantityDelta());
    assertEquals(new BigDecimal("20"), preview.lines().getFirst().shortfall());
  }

  @Test
  void previewAccumulatesRepeatedProductLinesAgainstOneStockBalance() {
    final Warehouse warehouse =
        Warehouse.builder().id(55L).code("WH-1").name("Main warehouse").company(company).build();
    final Product product = Product.builder().id(24L).name("Widget").build();
    when(warehouseService.findEntityByIdAndCompanyId(55L, COMPANY_ID)).thenReturn(warehouse);
    when(productService.findByIdAndCompanyId(24L, COMPANY_ID)).thenReturn(Optional.of(product));
    when(balanceService.findByWarehouseAndProduct(warehouse, product))
        .thenReturn(
            Optional.of(
                dev.roland.inventory_management_backend.features.stock_balance.StockBalance
                    .builder()
                    .quantity(new BigDecimal("10"))
                    .build()));

    final var preview =
        facade.preview(
            new FulfillmentPreview(
                null,
                null,
                DocumentType.DELIVERY_NOTE,
                55L,
                List.of(
                    new FulfillmentLine(null, 24L, new BigDecimal("6")),
                    new FulfillmentLine(null, 24L, new BigDecimal("6")))));

    assertFalse(preview.valid());
    assertEquals(new BigDecimal("-2"), preview.lines().getLast().resultingQuantity());
  }

  @Test
  void previewUsesTheSourceOrderLineAndRejectsQuantityAboveRemaining() {
    final Document source = documentWithLine(DocumentType.PURCHASE_ORDER, DocumentStatus.CONFIRMED);
    final Product product = source.getLines().getFirst().getProduct();
    source.setTargetWarehouse(
        Warehouse.builder().id(55L).code("WH-1").name("Main warehouse").company(company).build());
    when(documentService.findByIdAndCompanyId(3L, COMPANY_ID)).thenReturn(Optional.of(source));
    when(warehouseService.findEntityByIdAndCompanyId(55L, COMPANY_ID))
        .thenReturn(source.getTargetWarehouse());
    when(productService.findByIdAndCompanyId(24L, COMPANY_ID)).thenReturn(Optional.of(product));
    when(balanceService.findByWarehouseAndProduct(source.getTargetWarehouse(), product))
        .thenReturn(Optional.empty());

    final var preview =
        facade.preview(
            new FulfillmentPreview(
                3L, null, null, 55L, List.of(new FulfillmentLine(44L, null, new BigDecimal("6")))));

    assertFalse(preview.valid());
    assertEquals(
        DocumentApiMessageKey.DOCUMENT_INVALID_LINES.getKey(),
        preview.lines().getFirst().errorMessageKey());
    assertEquals(new BigDecimal("1"), preview.lines().getFirst().shortfall());
  }

  @Test
  void uploadRejectsAnEmptyFileAndPdfGenerationRejectsUnsupportedLanguage() {
    final Document document = document(DocumentType.PURCHASE_ORDER, DocumentStatus.CONFIRMED);
    document.setCreatedByUser(user);
    when(documentService.findByIdAndCompanyId(3L, COMPANY_ID))
        .thenReturn(java.util.Optional.of(document));

    ApiException emptyFile = assertThrows(ApiException.class, () -> facade.uploadFile(3L, null));
    assertEquals(DocumentApiMessageKey.DOCUMENT_INVALID_LINES, emptyFile.getMessageKey());

    ApiException language =
        assertThrows(ApiException.class, () -> facade.generatePdf(3L, "key", "fr"));
    assertEquals(DocumentApiMessageKey.DOCUMENT_INVALID_PDF_LANGUAGE, language.getMessageKey());
  }

  @Test
  void generatePdfQueuesLocalizedJobAndSkipsDuplicateRequests() {
    final Document document = document(DocumentType.PURCHASE_ORDER, DocumentStatus.CONFIRMED);
    document.setCreatedByUser(user);
    when(documentService.lockByIdAndCompanyId(3L, COMPANY_ID)).thenReturn(Optional.of(document));
    when(fileService.findByDocumentIdAndIdempotencyKey(3L, "pdf-key")).thenReturn(Optional.empty());
    when(fileService.existsByDocumentIdAndSourceAndStatusIn(anyLong(), any(), any()))
        .thenReturn(false);
    when(fileService.findAllByDocumentIdOrderByCreatedAtDesc(3L)).thenReturn(List.of());
    when(fileService.save(any(DocumentFile.class)))
        .thenAnswer(
            invocation -> {
              final DocumentFile file = invocation.getArgument(0);
              file.setId(77L);
              return file;
            });

    final var response = facade.generatePdf(3L, "pdf-key", "hu-HU");

    assertEquals("CONFIRMED", response.status());
    org.mockito.Mockito.verify(outboxService)
        .enqueue(
            org.mockito.ArgumentMatchers.eq(
                dev.roland.inventory_management_backend.common.async.AsyncJobType.DOCUMENT_PDF),
            any(),
            org.mockito.ArgumentMatchers.eq(COMPANY_ID),
            org.mockito.ArgumentMatchers.eq(3L));
  }

  @Test
  void retryPdfReusesThePreviousLanguageAndLinksTheAttempt() {
    final Document document = document(DocumentType.PURCHASE_ORDER, DocumentStatus.CONFIRMED);
    document.setCreatedByUser(user);
    final DocumentFile previous = new DocumentFile();
    previous.setSource(
        dev.roland.inventory_management_backend.features.document_file.DocumentFileSource
            .GENERATED_PDF);
    previous.setLanguageTag("hu");
    previous.setStatus(DocumentFileStatus.FAILED);
    when(documentService.lockByIdAndCompanyId(3L, COMPANY_ID)).thenReturn(Optional.of(document));
    when(fileService.existsByDocumentIdAndSourceAndStatusIn(anyLong(), any(), any()))
        .thenReturn(false);
    when(fileService.findAllByDocumentIdOrderByCreatedAtDesc(3L)).thenReturn(List.of(previous));
    when(fileService.save(any(DocumentFile.class)))
        .thenAnswer(
            invocation -> {
              final DocumentFile file = invocation.getArgument(0);
              file.setId(78L);
              assertEquals("hu", file.getLanguageTag());
              assertEquals(previous, file.getParentFile());
              return file;
            });

    facade.retryPdf(3L, "retry-key");

    org.mockito.Mockito.verify(outboxService)
        .enqueue(
            org.mockito.ArgumentMatchers.eq(
                dev.roland.inventory_management_backend.common.async.AsyncJobType.DOCUMENT_PDF),
            any(),
            org.mockito.ArgumentMatchers.eq(COMPANY_ID),
            org.mockito.ArgumentMatchers.eq(3L));
  }

  @Test
  void retryPdfRejectsWhenTheLatestAttemptDidNotFail() {
    final Document document = document(DocumentType.PURCHASE_ORDER, DocumentStatus.CONFIRMED);
    document.setCreatedByUser(user);
    final DocumentFile completedPdf = new DocumentFile();
    completedPdf.setSource(
        dev.roland.inventory_management_backend.features.document_file.DocumentFileSource
            .GENERATED_PDF);
    completedPdf.setStatus(DocumentFileStatus.COMPLETED);
    when(documentService.lockByIdAndCompanyId(3L, COMPANY_ID)).thenReturn(Optional.of(document));
    when(fileService.existsByDocumentIdAndSourceAndStatusIn(anyLong(), any(), any()))
        .thenReturn(false);
    when(fileService.findAllByDocumentIdOrderByCreatedAtDesc(3L)).thenReturn(List.of(completedPdf));

    assertThrows(ApiException.class, () -> facade.retryPdf(3L, "retry-key"));

    org.mockito.Mockito.verify(outboxService, org.mockito.Mockito.never())
        .enqueue(any(), any(), anyLong(), anyLong());
  }

  @Test
  void retryPdfRequiresAnIdempotencyKey() {
    final ApiException error = assertThrows(ApiException.class, () -> facade.retryPdf(3L, " "));

    assertEquals(DocumentApiMessageKey.DOCUMENT_IDEMPOTENCY_KEY_REQUIRED, error.getMessageKey());
  }

  @Test
  void retryPdfIsIdempotentForARepeatedKey() {
    final Document document = document(DocumentType.PURCHASE_ORDER, DocumentStatus.CONFIRMED);
    document.setCreatedByUser(user);
    final DocumentFile previousAttempt = new DocumentFile();
    when(documentService.lockByIdAndCompanyId(3L, COMPANY_ID)).thenReturn(Optional.of(document));
    when(fileService.findByDocumentIdAndIdempotencyKey(3L, "retry-key"))
        .thenReturn(Optional.of(previousAttempt));

    facade.retryPdf(3L, "retry-key");

    org.mockito.Mockito.verify(fileService, org.mockito.Mockito.never())
        .save(any(DocumentFile.class));
    org.mockito.Mockito.verify(outboxService, org.mockito.Mockito.never())
        .enqueue(any(), any(), anyLong(), anyLong());
  }

  @Test
  void confirmMovesAValidOrderToConfirmed() {
    final Document document = documentWithLine(DocumentType.PURCHASE_ORDER, DocumentStatus.DRAFT);
    document.setDocumentNumber("PO-001");
    when(documentService.lockByIdAndCompanyId(3L, COMPANY_ID))
        .thenReturn(java.util.Optional.of(document));
    when(documentService.save(document)).thenReturn(document);

    var response = facade.confirm(3L, "0");

    assertEquals("CONFIRMED", response.status());
    assertEquals("PO-001", response.number());
  }

  @Test
  void transitionEndpointsRejectMissingVersionsAndInvalidReasons() {
    final Document draft = document(DocumentType.PURCHASE_ORDER, DocumentStatus.DRAFT);
    draft.setCreatedByUser(user);
    when(documentService.lockByIdAndCompanyId(3L, COMPANY_ID)).thenReturn(Optional.of(draft));

    final ApiException missingVersion =
        assertThrows(ApiException.class, () -> facade.confirm(3L, " "));
    assertEquals(DocumentApiMessageKey.DOCUMENT_VERSION_REQUIRED, missingVersion.getMessageKey());

    final ApiException missingReason =
        assertThrows(
            ApiException.class,
            () ->
                facade.closeRemaining(
                    3L,
                    new dev.roland.inventory_management_backend.features.document.dto
                        .CloseRemaining(" "),
                    "0"));
    assertEquals(DocumentApiMessageKey.DOCUMENT_REASON_REQUIRED, missingReason.getMessageKey());
  }

  @Test
  void closeRemainingMarksOutstandingLineAndCompletesOrder() {
    final Document order = documentWithLine(DocumentType.SALES_ORDER, DocumentStatus.CONFIRMED);
    order.setDocumentNumber("SO-001");
    when(documentService.lockByIdAndCompanyId(3L, COMPANY_ID))
        .thenReturn(java.util.Optional.of(order));
    when(documentService.save(order)).thenReturn(order);

    var response = facade.closeRemaining(3L, new CloseRemaining("no longer needed"), "0");

    assertEquals("COMPLETED", response.status());
    assertEquals("no longer needed", response.lines().getFirst().closureReason());
  }

  @Test
  void duplicatePostingIsIdempotent() {
    final Document document = document(DocumentType.DELIVERY_NOTE, DocumentStatus.DRAFT);
    when(documentService.lockByIdAndCompanyId(3L, COMPANY_ID))
        .thenReturn(java.util.Optional.of(document));
    when(documentService.postingKeyExists(3L, "key")).thenReturn(true);

    var response = facade.post(3L, "key", "0");

    assertEquals("DRAFT", response.status());
  }

  @Test
  void concurrentRetriesSerializeOnDocumentLockAndDoNotPostTwice() throws Exception {
    final Document document = document(DocumentType.DELIVERY_NOTE, DocumentStatus.DRAFT);
    final Object documentLock = new Object();
    when(documentService.lockByIdAndCompanyId(3L, COMPANY_ID))
        .thenAnswer(
            invocation -> {
              synchronized (documentLock) {
                return Optional.of(document);
              }
            });
    when(documentService.postingKeyExists(3L, "same-key")).thenReturn(true);

    final CountDownLatch ready = new CountDownLatch(2);
    final CountDownLatch start = new CountDownLatch(1);
    try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
      final Future<String> first =
          executor.submit(
              () -> {
                ready.countDown();
                start.await();
                return facade.post(3L, "same-key", "0").status();
              });
      final Future<String> second =
          executor.submit(
              () -> {
                ready.countDown();
                start.await();
                return facade.post(3L, "same-key", "0").status();
              });

      ready.await();
      start.countDown();
      assertEquals("DRAFT", first.get());
      assertEquals("DRAFT", second.get());
    }

    org.mockito.Mockito.verify(documentService, org.mockito.Mockito.times(2))
        .lockByIdAndCompanyId(3L, COMPANY_ID);
    org.mockito.Mockito.verify(documentService, org.mockito.Mockito.times(2))
        .postingKeyExists(3L, "same-key");
    org.mockito.Mockito.verify(postingService, org.mockito.Mockito.never()).complete(any());
  }

  @Test
  void postingRejectsMissingKeysTerminalDocumentsAndNonOperationTypes() {
    final ApiException missingKey =
        assertThrows(ApiException.class, () -> facade.post(3L, " ", "0"));
    assertEquals(
        DocumentApiMessageKey.DOCUMENT_IDEMPOTENCY_KEY_REQUIRED, missingKey.getMessageKey());

    final Document completed = document(DocumentType.GOODS_RECEIPT, DocumentStatus.COMPLETED);
    final Document order = document(DocumentType.PURCHASE_ORDER, DocumentStatus.DRAFT);
    when(documentService.lockByIdAndCompanyId(3L, COMPANY_ID))
        .thenReturn(Optional.of(completed), Optional.of(order));
    when(documentService.postingKeyExists(3L, "key")).thenReturn(false);

    final ApiException terminal =
        assertThrows(ApiException.class, () -> facade.post(3L, "key", "0"));
    final ApiException notOperation =
        assertThrows(ApiException.class, () -> facade.post(3L, "key", "0"));
    assertEquals(DocumentApiMessageKey.DOCUMENT_INVALID_TRANSITION, terminal.getMessageKey());
    assertEquals(DocumentApiMessageKey.DOCUMENT_INVALID_TRANSITION, notOperation.getMessageKey());
  }

  @Test
  void confirmationAndClosureRejectWrongDocumentKindsAndStates() {
    final Document receipt = document(DocumentType.GOODS_RECEIPT, DocumentStatus.DRAFT);
    final Document cancelledOrder = document(DocumentType.PURCHASE_ORDER, DocumentStatus.CANCELLED);
    when(documentService.lockByIdAndCompanyId(3L, COMPANY_ID))
        .thenReturn(Optional.of(receipt), Optional.of(cancelledOrder));

    final ApiException wrongType = assertThrows(ApiException.class, () -> facade.confirm(3L, "0"));
    final ApiException wrongState =
        assertThrows(
            ApiException.class, () -> facade.closeRemaining(3L, new CloseRemaining("reason"), "0"));
    assertEquals(DocumentApiMessageKey.DOCUMENT_INVALID_TRANSITION, wrongType.getMessageKey());
    assertEquals(DocumentApiMessageKey.DOCUMENT_INVALID_TRANSITION, wrongState.getMessageKey());
  }

  @Test
  void postGoodsReceiptAllocatesNumberAndPostsExactlyOnce() {
    final Document receipt = documentWithLine(DocumentType.GOODS_RECEIPT, DocumentStatus.DRAFT);
    final Product product = receipt.getLines().getFirst().getProduct();
    receipt.setTargetWarehouse(
        Warehouse.builder().id(55L).code("WH-1").name("Main warehouse").company(company).build());
    final var prefix =
        DocumentPrefix.builder()
            .id(6L)
            .company(company)
            .documentType(DocumentType.GOODS_RECEIPT)
            .prefix("GR")
            .build();
    final var sequence =
        DocumentSequence.builder()
            .counter(4L)
            .company(company)
            .documentPrefix(prefix)
            .year(2026)
            .build();
    when(documentService.lockByIdAndCompanyId(3L, COMPANY_ID)).thenReturn(Optional.of(receipt));
    when(documentService.findByIdAndCompanyId(3L, COMPANY_ID)).thenReturn(Optional.of(receipt));
    when(documentService.postingKeyExists(3L, "post-key")).thenReturn(false);
    when(prefixService.findFirstByCompanyIdAndDocumentTypeOrderByIdAsc(
            COMPANY_ID, DocumentType.GOODS_RECEIPT))
        .thenReturn(Optional.of(prefix));
    when(sequenceService.lockByScope(COMPANY_ID, 6L, 2026)).thenReturn(Optional.of(sequence));
    when(warehouseService.findEntityByIdAndCompanyId(55L, COMPANY_ID))
        .thenReturn(receipt.getTargetWarehouse());
    when(productService.findByIdAndCompanyId(24L, COMPANY_ID)).thenReturn(Optional.of(product));
    when(balanceService.findByWarehouseAndProduct(receipt.getTargetWarehouse(), product))
        .thenReturn(Optional.empty());
    when(postingService.complete(receipt)).thenReturn(receipt);
    when(documentService.save(receipt)).thenReturn(receipt);

    final var response = facade.post(3L, "post-key", "0");

    assertEquals("GR-2026-00005", response.number());
    assertEquals("CONFIRMED", response.status());
    org.mockito.Mockito.verify(documentService).save(any());
  }

  @Test
  void getRejectsDocumentsOutsideTheCurrentCompany() {
    when(documentService.findByIdAndCompanyId(3L, COMPANY_ID))
        .thenReturn(java.util.Optional.empty());

    assertThrows(RuntimeException.class, () -> facade.get(3L));
  }

  private void authenticateAsAdmin() {
    final CustomUserDetails principal =
        new CustomUserDetails(USER_ID, "admin", "password", UserRole.ADMIN, UserStatus.ACTIVE);
    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken(
                principal, "password", principal.getAuthorities()));
  }

  private static Document document(final DocumentType type, final DocumentStatus status) {
    return Document.builder()
        .id(3L)
        .company(Company.builder().id(COMPANY_ID).build())
        .type(type)
        .status(status)
        .documentDate(LocalDate.of(2026, 10, 31))
        .version(0L)
        .lines(new java.util.ArrayList<>())
        .build();
  }

  private static DocumentExchangeRate exchangeRate(
      final Document document, final Currency sourceCurrency, final BigDecimal rateValue) {
    final DocumentExchangeRate rate = new DocumentExchangeRate();
    rate.setDocument(document);
    rate.setSourceCurrency(sourceCurrency);
    rate.setRate(rateValue);
    return rate;
  }

  private static Document documentWithLine(final DocumentType type, final DocumentStatus status) {
    final Product product = Product.builder().id(24L).name("Widget").sku("W-1").build();
    final var line =
        dev.roland.inventory_management_backend.features.document_line.DocumentLine.builder()
            .id(44L)
            .product(product)
            .quantity(new java.math.BigDecimal("5"))
            .fulfilledQuantity(java.math.BigDecimal.ZERO)
            .closedQuantity(java.math.BigDecimal.ZERO)
            .unitPrice(new java.math.BigDecimal("2.00"))
            .productNameSnapshot("Widget")
            .productSkuSnapshot("W-1")
            .build();
    final Document document = document(type, status);
    document.setLines(List.of(line));
    return document;
  }
}
