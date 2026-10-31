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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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
import dev.roland.inventory_management_backend.features.document.Document;
import dev.roland.inventory_management_backend.features.document.dto.CloseRemaining;
import dev.roland.inventory_management_backend.features.document.dto.Draft;
import dev.roland.inventory_management_backend.features.document.dto.DraftUpdate;
import dev.roland.inventory_management_backend.features.document.dto.FulfillmentDraft;
import dev.roland.inventory_management_backend.features.document.dto.FulfillmentLine;
import dev.roland.inventory_management_backend.features.document.dto.FulfillmentPreview;
import dev.roland.inventory_management_backend.features.document.dto.LineDraft;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentStatus;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentType;
import dev.roland.inventory_management_backend.features.document.message.DocumentApiMessageKey;
import dev.roland.inventory_management_backend.features.document.repository.DocumentPostingKeyRepository;
import dev.roland.inventory_management_backend.features.document.repository.DocumentRepository;
import dev.roland.inventory_management_backend.features.document.service.DocumentPostingService;
import dev.roland.inventory_management_backend.features.document.service.DocumentService;
import dev.roland.inventory_management_backend.features.document_activity.repository.DocumentActivityRepository;
import dev.roland.inventory_management_backend.features.document_file.DocumentFile;
import dev.roland.inventory_management_backend.features.document_file.DocumentFileRepository;
import dev.roland.inventory_management_backend.features.document_line.service.DocumentLineService;
import dev.roland.inventory_management_backend.features.document_line_allocation.repository.DocumentLineAllocationRepository;
import dev.roland.inventory_management_backend.features.document_prefix.DocumentPrefix;
import dev.roland.inventory_management_backend.features.document_prefix.repository.DocumentPrefixRepository;
import dev.roland.inventory_management_backend.features.document_relation.repository.DocumentRelationRepository;
import dev.roland.inventory_management_backend.features.document_sequence.DocumentSequence;
import dev.roland.inventory_management_backend.features.document_sequence.repository.DocumentSequenceRepository;
import dev.roland.inventory_management_backend.features.media_asset.service.MediaAssetService;
import dev.roland.inventory_management_backend.features.media_usage.enumeration.MediaEntityType;
import dev.roland.inventory_management_backend.features.media_usage.enumeration.MediaUsageType;
import dev.roland.inventory_management_backend.features.media_usage.service.MediaUsageService;
import dev.roland.inventory_management_backend.features.partner.service.PartnerService;
import dev.roland.inventory_management_backend.features.product.Product;
import dev.roland.inventory_management_backend.features.product.service.ProductService;
import dev.roland.inventory_management_backend.features.stock_balance.repository.StockBalanceRepository;
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

  @Mock private DocumentRepository documentRepository;
  @Mock private DocumentService documentService;
  @Mock private DocumentLineService lineService;
  @Mock private ProductService productService;
  @Mock private UnitService unitService;
  @Mock private PartnerService partnerService;
  @Mock private ContactService contactService;
  @Mock private WarehouseService warehouseService;
  @Mock private CurrentCompanyProvider companyProvider;
  @Mock private UserService userService;
  @Mock private DocumentPostingService postingService;
  @Mock private DocumentRelationRepository relationRepository;
  @Mock private DocumentLineAllocationRepository allocationRepository;
  @Mock private DocumentActivityRepository activityRepository;
  @Mock private DocumentPostingKeyRepository postingKeyRepository;
  @Mock private DocumentFileRepository fileRepository;
  @Mock private DocumentPrefixRepository prefixRepository;
  @Mock private DocumentSequenceRepository sequenceRepository;
  @Mock private StockBalanceRepository balanceRepository;
  @Mock private MediaAssetService mediaAssetService;
  @Mock private MediaUsageService mediaUsageService;
  @Mock private ObjectStorageService objectStorage;
  @Mock private OutboxService outboxService;
  @Mock private StockMovementService stockMovementService;

  @InjectMocks private DocumentWorkflowFacadeImpl facade;

  private Company company;
  private User user;

  @BeforeEach
  void setUp() {
    company = Company.builder().id(COMPANY_ID).name("Example").build();
    user = User.builder().id(USER_ID).username("admin").role(UserRole.ADMIN).build();
    lenient().when(companyProvider.getCurrentCompany()).thenReturn(company);
    lenient().when(userService.findByIdOrThrow(USER_ID)).thenReturn(user);
    lenient()
        .when(relationRepository.findAllBySourceDocumentIdOrTargetDocumentId(anyLong(), anyLong()))
        .thenReturn(List.of());
    lenient()
        .when(activityRepository.findAllByDocumentIdOrderByOccurredAtAsc(anyLong()))
        .thenReturn(List.of());
    lenient()
        .when(
            mediaUsageService.findAllByEntityTypeAndEntityIdAndUsageTypeOrderBySortOrderAsc(
                any(MediaEntityType.class), anyLong(), any(MediaUsageType.class)))
        .thenReturn(List.of());
    lenient()
        .when(fileRepository.findAllByDocumentIdOrderByCreatedAtDesc(anyLong()))
        .thenReturn(List.of());
    lenient()
        .when(allocationRepository.findAllByFulfillmentLineId(anyLong()))
        .thenReturn(List.of());
    lenient()
        .when(allocationRepository.sumPostedAllocated(anyLong(), any(DocumentStatus.class)))
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
    when(documentRepository.findAllByCompanyIdOrderByUpdatedAtDesc(COMPANY_ID))
        .thenReturn(List.of());

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
    when(documentRepository.findAllByCompanyIdOrderByUpdatedAtDesc(COMPANY_ID))
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
            DocumentType.DELIVERY_NOTE),
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
            DocumentType.DELIVERY_NOTE),
        facade.capabilities().creatableTypes());
  }

  @Test
  void getMapsDocumentTotalsAndMetadata() {
    final Document document = document(DocumentType.PURCHASE_ORDER, DocumentStatus.DRAFT);
    document.setDocumentNumber("PO-001");
    document.setDocumentDate(LocalDate.of(2026, 10, 31));
    document.setCreatedByUser(user);
    when(documentRepository.findByIdAndCompanyId(3L, COMPANY_ID))
        .thenReturn(java.util.Optional.of(document));

    var response = facade.get(3L);

    assertEquals("PO-001", response.number());
    assertEquals("PURCHASE_ORDER", response.type().name());
    assertEquals("admin", response.ownerName());
    assertEquals(0, response.totals().gross().compareTo(java.math.BigDecimal.ZERO.setScale(2)));
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
    when(documentRepository.findByIdAndCompanyId(3L, COMPANY_ID)).thenReturn(Optional.of(document));
    when(fileRepository.findAllByDocumentIdOrderByCreatedAtDesc(3L)).thenReturn(List.of(failedPdf));

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
    when(documentRepository.findByIdAndCompanyId(3L, COMPANY_ID))
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
    when(documentRepository.lockByIdAndCompanyId(3L, COMPANY_ID))
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
    when(documentRepository.lockByIdAndCompanyId(3L, COMPANY_ID))
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
    when(documentRepository.findByIdAndCompanyId(3L, COMPANY_ID)).thenReturn(Optional.of(source));
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
    when(balanceRepository.findByWarehouseAndProduct(warehouse, product))
        .thenReturn(Optional.empty());

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
  void previewUsesTheSourceOrderLineAndRejectsQuantityAboveRemaining() {
    final Document source = documentWithLine(DocumentType.PURCHASE_ORDER, DocumentStatus.CONFIRMED);
    final Product product = source.getLines().getFirst().getProduct();
    source.setTargetWarehouse(
        Warehouse.builder().id(55L).code("WH-1").name("Main warehouse").company(company).build());
    when(documentRepository.findByIdAndCompanyId(3L, COMPANY_ID)).thenReturn(Optional.of(source));
    when(warehouseService.findEntityByIdAndCompanyId(55L, COMPANY_ID))
        .thenReturn(source.getTargetWarehouse());
    when(productService.findByIdAndCompanyId(24L, COMPANY_ID)).thenReturn(Optional.of(product));
    when(balanceRepository.findByWarehouseAndProduct(source.getTargetWarehouse(), product))
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
    when(documentRepository.findByIdAndCompanyId(3L, COMPANY_ID))
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
    when(documentRepository.findByIdAndCompanyId(3L, COMPANY_ID)).thenReturn(Optional.of(document));
    when(fileRepository.findByDocumentIdAndIdempotencyKey(3L, "pdf-key"))
        .thenReturn(Optional.empty());
    when(fileRepository.existsByDocumentIdAndSourceAndStatusIn(anyLong(), any(), any()))
        .thenReturn(false);
    when(fileRepository.findAllByDocumentIdOrderByCreatedAtDesc(3L)).thenReturn(List.of());
    when(fileRepository.save(any(DocumentFile.class)))
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
    previous.setStatus(
        dev.roland.inventory_management_backend.features.document_file.DocumentFileStatus.FAILED);
    when(documentRepository.findByIdAndCompanyId(3L, COMPANY_ID)).thenReturn(Optional.of(document));
    when(fileRepository.existsByDocumentIdAndSourceAndStatusIn(anyLong(), any(), any()))
        .thenReturn(false);
    when(fileRepository.findAllByDocumentIdOrderByCreatedAtDesc(3L)).thenReturn(List.of(previous));
    when(fileRepository.save(any(DocumentFile.class)))
        .thenAnswer(
            invocation -> {
              final DocumentFile file = invocation.getArgument(0);
              file.setId(78L);
              assertEquals("hu", file.getLanguageTag());
              assertEquals(previous, file.getParentFile());
              return file;
            });

    facade.retryPdf(3L, null);

    org.mockito.Mockito.verify(outboxService)
        .enqueue(
            org.mockito.ArgumentMatchers.eq(
                dev.roland.inventory_management_backend.common.async.AsyncJobType.DOCUMENT_PDF),
            any(),
            org.mockito.ArgumentMatchers.eq(COMPANY_ID),
            org.mockito.ArgumentMatchers.eq(3L));
  }

  @Test
  void confirmMovesAValidOrderToConfirmed() {
    final Document document = documentWithLine(DocumentType.PURCHASE_ORDER, DocumentStatus.DRAFT);
    document.setDocumentNumber("PO-001");
    when(documentRepository.lockByIdAndCompanyId(3L, COMPANY_ID))
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
    when(documentRepository.lockByIdAndCompanyId(3L, COMPANY_ID)).thenReturn(Optional.of(draft));

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
    when(documentRepository.lockByIdAndCompanyId(3L, COMPANY_ID))
        .thenReturn(java.util.Optional.of(order));
    when(documentService.save(order)).thenReturn(order);

    var response = facade.closeRemaining(3L, new CloseRemaining("no longer needed"), "0");

    assertEquals("COMPLETED", response.status());
    assertEquals("no longer needed", response.lines().getFirst().closureReason());
  }

  @Test
  void duplicatePostingIsIdempotent() {
    final Document document = document(DocumentType.DELIVERY_NOTE, DocumentStatus.DRAFT);
    when(documentRepository.lockByIdAndCompanyId(3L, COMPANY_ID))
        .thenReturn(java.util.Optional.of(document));
    when(postingKeyRepository.existsByDocumentIdAndIdempotencyKey(3L, "key")).thenReturn(true);

    var response = facade.post(3L, "key", "0");

    assertEquals("DRAFT", response.status());
  }

  @Test
  void postingRejectsMissingKeysTerminalDocumentsAndNonOperationTypes() {
    final ApiException missingKey =
        assertThrows(ApiException.class, () -> facade.post(3L, " ", "0"));
    assertEquals(
        DocumentApiMessageKey.DOCUMENT_IDEMPOTENCY_KEY_REQUIRED, missingKey.getMessageKey());

    final Document completed = document(DocumentType.GOODS_RECEIPT, DocumentStatus.COMPLETED);
    final Document order = document(DocumentType.PURCHASE_ORDER, DocumentStatus.DRAFT);
    when(documentRepository.lockByIdAndCompanyId(3L, COMPANY_ID))
        .thenReturn(Optional.of(completed), Optional.of(order));
    when(postingKeyRepository.existsByDocumentIdAndIdempotencyKey(3L, "key")).thenReturn(false);

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
    when(documentRepository.lockByIdAndCompanyId(3L, COMPANY_ID))
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
    when(documentRepository.lockByIdAndCompanyId(3L, COMPANY_ID)).thenReturn(Optional.of(receipt));
    when(documentRepository.findByIdAndCompanyId(3L, COMPANY_ID)).thenReturn(Optional.of(receipt));
    when(postingKeyRepository.existsByDocumentIdAndIdempotencyKey(3L, "post-key"))
        .thenReturn(false);
    when(prefixRepository.findFirstByCompanyIdAndDocumentTypeOrderByIdAsc(
            COMPANY_ID, DocumentType.GOODS_RECEIPT))
        .thenReturn(Optional.of(prefix));
    when(sequenceRepository.lockByScope(COMPANY_ID, 6L, 2026)).thenReturn(Optional.of(sequence));
    when(warehouseService.findEntityByIdAndCompanyId(55L, COMPANY_ID))
        .thenReturn(receipt.getTargetWarehouse());
    when(productService.findByIdAndCompanyId(24L, COMPANY_ID)).thenReturn(Optional.of(product));
    when(balanceRepository.findByWarehouseAndProduct(receipt.getTargetWarehouse(), product))
        .thenReturn(Optional.empty());
    when(postingService.complete(receipt)).thenReturn(receipt);
    when(documentService.save(receipt)).thenReturn(receipt);

    final var response = facade.post(3L, "post-key", "0");

    assertEquals("GR-2026-00005", response.number());
    assertEquals("CONFIRMED", response.status());
    org.mockito.Mockito.verify(postingKeyRepository).save(any());
  }

  @Test
  void getRejectsDocumentsOutsideTheCurrentCompany() {
    when(documentRepository.findByIdAndCompanyId(3L, COMPANY_ID))
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
        .lines(List.of())
        .build();
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
