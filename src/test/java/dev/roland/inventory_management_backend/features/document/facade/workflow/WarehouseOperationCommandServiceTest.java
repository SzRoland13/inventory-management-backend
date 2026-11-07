package dev.roland.inventory_management_backend.features.document.facade.workflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.roland.inventory_management_backend.common.exception.ApiException;
import dev.roland.inventory_management_backend.common.service.CurrentCompanyProvider;
import dev.roland.inventory_management_backend.features.company.Company;
import dev.roland.inventory_management_backend.features.document.Document;
import dev.roland.inventory_management_backend.features.document.dto.DocumentResponse;
import dev.roland.inventory_management_backend.features.document.dto.WarehouseOperationPreview;
import dev.roland.inventory_management_backend.features.document.dto.WarehouseOperationRequest;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentStatus;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentType;
import dev.roland.inventory_management_backend.features.document.enumeration.WarehouseOperationType;
import dev.roland.inventory_management_backend.features.document.mapper.DocumentResponseMapper;
import dev.roland.inventory_management_backend.features.document.message.DocumentApiMessageKey;
import dev.roland.inventory_management_backend.features.document.service.DocumentService;
import dev.roland.inventory_management_backend.features.document_line.DocumentLine;
import dev.roland.inventory_management_backend.features.document_line.service.DocumentLineService;
import dev.roland.inventory_management_backend.features.product.Product;
import dev.roland.inventory_management_backend.features.product.service.ProductService;
import dev.roland.inventory_management_backend.features.stock_movement.StockMovement;
import dev.roland.inventory_management_backend.features.stock_movement.service.StockMovementService;
import dev.roland.inventory_management_backend.features.unit.Unit;
import dev.roland.inventory_management_backend.features.user.User;
import dev.roland.inventory_management_backend.features.user.enumeration.UserRole;
import dev.roland.inventory_management_backend.features.warehouse.Warehouse;
import dev.roland.inventory_management_backend.features.warehouse.enumeration.WarehouseStatus;
import dev.roland.inventory_management_backend.features.warehouse.enumeration.WarehouseType;
import dev.roland.inventory_management_backend.features.warehouse.service.WarehouseService;

@ExtendWith(MockitoExtension.class)
class WarehouseOperationCommandServiceTest {
  private static final Long COMPANY_ID = 15L;

  @Mock private DocumentService documentService;
  @Mock private DocumentLineService lineService;
  @Mock private ProductService productService;
  @Mock private WarehouseService warehouseService;
  @Mock private CurrentCompanyProvider companyProvider;
  @Mock private StockMovementService stockMovementService;
  @Mock private DocumentWorkflowAccessService accessService;
  @Mock private DocumentWorkflowWriterService writer;
  @Mock private DocumentResponseMapper responseMapper;
  @Mock private WarehouseOperationSupportService supportService;
  @Mock private WarehouseOperationPreviewService previewService;
  @Mock private WarehouseOperationRecordService recordService;

  private WarehouseOperationCommandService service;
  private Company company;
  private User actor;
  private Warehouse source;
  private Warehouse destination;
  private Product product;
  private DocumentResponse response;

  @BeforeEach
  void setUp() {
    service =
        new WarehouseOperationCommandService(
            documentService,
            lineService,
            productService,
            warehouseService,
            companyProvider,
            stockMovementService,
            accessService,
            writer,
            responseMapper,
            supportService,
            previewService,
            recordService);
    company = Company.builder().id(COMPANY_ID).build();
    actor = User.builder().id(6L).role(UserRole.ADMIN).build();
    source = warehouse(20L, WarehouseType.STANDARD);
    destination = warehouse(21L, WarehouseType.STANDARD);
    final Unit unit = Unit.builder().id(2L).code("EA").name("Each").build();
    product = Product.builder().id(30L).name("Widget").unit(unit).build();
    response =
        new DocumentResponse(
            null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, false, null, null, null, null, null, null, null, null, null, null,
            null, null);
    when(companyProvider.getCurrentCompany()).thenReturn(company);
    when(accessService.currentActor()).thenReturn(actor);
  }

  @Test
  void createsTransferDraftAndSnapshotsItsProductLines() {
    stubDocumentCreation();
    when(warehouseService.findEntityByIdAndCompanyId(20L, COMPANY_ID)).thenReturn(source);
    when(warehouseService.findEntityByIdAndCompanyId(21L, COMPANY_ID)).thenReturn(destination);
    final WarehouseOperationRequest request =
        request(20L, 21L, null, null, List.of(operationLine(new BigDecimal("3"), null)));

    final DocumentResponse result =
        service.apply(null, WarehouseOperationType.TRANSFER_DRAFT, request, null, null);

    assertSame(response, result);
    verify(writer).addActivity(any(Document.class), any(), any());
    verify(lineService).save(any(DocumentLine.class));
  }

  @Test
  void createsStockCountDraftWithExpectedQuantityAtItsCountTime() {
    stubDocumentCreation();
    final LocalDateTime countedAt = LocalDateTime.of(2026, 10, 20, 12, 0);
    final WarehouseOperationRequest request =
        request(
            null,
            null,
            20L,
            countedAt,
            List.of(operationLine(BigDecimal.ZERO, new BigDecimal("5"))));
    when(warehouseService.findEntityByIdAndCompanyId(20L, COMPANY_ID)).thenReturn(source);
    when(stockMovementService.findEffectiveThrough(30L, 20L, countedAt)).thenReturn(List.of());

    final DocumentResponse result =
        service.apply(null, WarehouseOperationType.STOCK_COUNT_CREATE, request, null, null);

    assertSame(response, result);
    verify(lineService).save(any(DocumentLine.class));
    verify(writer).addActivity(any(Document.class), any(), any());
  }

  @Test
  void rejectsInvalidTransferWarehouseCombinationsAndMissingRequests() {
    final ApiException missing =
        assertThrows(
            ApiException.class,
            () -> service.apply(null, WarehouseOperationType.TRANSFER_DRAFT, null, null, null));
    assertEquals(DocumentApiMessageKey.DOCUMENT_INVALID_LINES, missing.getMessageKey());

    final WarehouseOperationRequest sameWarehouse =
        request(20L, 20L, null, null, List.of(operationLine(BigDecimal.ONE, null)));
    when(warehouseService.findEntityByIdAndCompanyId(20L, COMPANY_ID)).thenReturn(source);
    final ApiException invalid =
        assertThrows(
            ApiException.class,
            () ->
                service.apply(
                    null, WarehouseOperationType.TRANSFER_DRAFT, sameWarehouse, null, null));
    assertEquals(DocumentApiMessageKey.DOCUMENT_INVALID_LINES, invalid.getMessageKey());
  }

  @Test
  void transferPostingChecksPreviewBeforeWritingChildDocuments() {
    final Document root = new Document();
    root.setId(50L);
    root.setCompany(company);
    root.setType(DocumentType.TRANSFER_REQUEST);
    when(accessService.lock(50L, COMPANY_ID)).thenReturn(root);
    when(documentService.postingKeyExists(50L, "key")).thenReturn(false);
    when(previewService.preview(anyLong(), any(), any()))
        .thenReturn(
            new WarehouseOperationPreview(
                false, null, List.of(), List.of("invalid"), List.of(), List.of()));

    final ApiException invalid =
        assertThrows(
            ApiException.class,
            () ->
                service.apply(
                    50L,
                    WarehouseOperationType.TRANSFER_DISPATCH,
                    request(null, null, null, null, List.of(operationLine(BigDecimal.ONE, null))),
                    "key",
                    "0"));

    assertEquals(DocumentApiMessageKey.DOCUMENT_INVALID_LINES, invalid.getMessageKey());
  }

  @Test
  void dispatchesTransferWithoutTransitWarehouseAndCompletesFullyFulfilledRequest() {
    stubResponseAndPersistence();
    final Document root = transferRoot(50L, null);
    final DocumentLine sourceLine = root.getLines().get(0);
    final Document child = operationDocument(DocumentType.TRANSFER_DELIVERY);
    final WarehouseOperationRequest request = transferOperationRequest(null);
    stubTransferPosting(root, request, child, sourceLine, WarehouseOperationType.TRANSFER_PREVIEW);

    assertSame(
        response,
        service.apply(50L, WarehouseOperationType.TRANSFER_DISPATCH, request, "key", "0"));

    assertEquals(0, new BigDecimal("2").compareTo(sourceLine.getFulfilledQuantity()));
    verify(writer).complete(child);
    verify(writer).complete(root);
  }

  @Test
  void resolvesTransitDamageIntoQuarantineAndKeepsPartialRequestOpen() {
    stubResponseAndPersistence();
    final Warehouse transit = warehouse(22L, WarehouseType.TRANSIT);
    final Document root = transferRoot(50L, transit);
    final DocumentLine sourceLine = root.getLines().get(0);
    sourceLine.setFulfilledQuantity(BigDecimal.ONE);
    final Document child = operationDocument(DocumentType.TRANSFER_DELIVERY);
    final WarehouseOperationRequest request =
        transferOperationRequest(WarehouseOperationType.DAMAGE);
    stubTransferPosting(
        root, request, child, sourceLine, WarehouseOperationType.TRANSFER_RESOLUTION);
    when(supportService.quarantineWarehouse(COMPANY_ID))
        .thenReturn(warehouse(23L, WarehouseType.QUARANTINE));

    assertSame(
        response, service.apply(50L, WarehouseOperationType.TRANSFER_RESOLVE, request, "key", "0"));

    assertEquals(DocumentStatus.PARTIALLY_COMPLETED, root.getStatus());
    assertEquals(0, BigDecimal.ZERO.compareTo(sourceLine.getInTransitQuantity()));
    verify(writer).consumeKey(root, "key");
  }

  @Test
  void receivesInTransitGoodsAndCompletesTransferRequest() {
    stubResponseAndPersistence();
    final Warehouse transit = warehouse(22L, WarehouseType.TRANSIT);
    final Document root = transferRoot(50L, transit);
    final DocumentLine sourceLine = root.getLines().get(0);
    sourceLine.setFulfilledQuantity(new BigDecimal("2"));
    final Document child = operationDocument(DocumentType.TRANSFER_RECEIPT);
    final WarehouseOperationRequest request = transferOperationRequest(null);
    stubTransferPosting(root, request, child, sourceLine, WarehouseOperationType.TRANSFER_PREVIEW);

    assertSame(
        response, service.apply(50L, WarehouseOperationType.TRANSFER_RECEIPT, request, "key", "0"));

    assertEquals(0, BigDecimal.ZERO.compareTo(sourceLine.getInTransitQuantity()));
    verify(writer).complete(child);
    verify(writer).complete(root);
  }

  @Test
  void rejectsOperationThatDoesNotMatchAnApplicableWorkflow() {
    final ApiException error =
        assertThrows(
            ApiException.class,
            () ->
                service.apply(
                    null,
                    WarehouseOperationType.TRANSFER_PREVIEW,
                    request(null, null, null, null, List.of()),
                    null,
                    null));

    assertEquals(DocumentApiMessageKey.DOCUMENT_INVALID_TRANSITION, error.getMessageKey());
  }

  private Warehouse warehouse(final Long id, final WarehouseType type) {
    return Warehouse.builder()
        .id(id)
        .company(company)
        .type(type)
        .status(WarehouseStatus.ACTIVE)
        .build();
  }

  private void stubDocumentCreation() {
    stubResponseAndPersistence();
    when(productService.findByIdAndCompanyId(30L, COMPANY_ID)).thenReturn(Optional.of(product));
  }

  private void stubResponseAndPersistence() {
    when(responseMapper.toResponse(any(Document.class))).thenReturn(response);
    when(documentService.save(any(Document.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(lineService.save(any(DocumentLine.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
  }

  private void stubTransferPosting(
      final Document root,
      final WarehouseOperationRequest request,
      final Document child,
      final DocumentLine sourceLine,
      final WarehouseOperationType previewType) {
    when(accessService.lock(50L, COMPANY_ID)).thenReturn(root);
    when(documentService.postingKeyExists(50L, "key")).thenReturn(false);
    when(supportService.withOperation(org.mockito.ArgumentMatchers.eq(request), any()))
        .thenReturn(request);
    when(previewService.preview(50L, previewType, request))
        .thenReturn(
            new WarehouseOperationPreview(
                true, "version", List.of(), List.of(), List.of(), List.of()));
    when(supportService.operationDocument(org.mockito.ArgumentMatchers.eq(root), any(), any()))
        .thenReturn(child);
    when(supportService.line(90L, COMPANY_ID)).thenReturn(sourceLine);
    when(supportService.cloneLine(child, sourceLine, new BigDecimal("2")))
        .thenReturn(new DocumentLine());
    when(stockMovementService.postMovement(
            any(),
            any(),
            any(),
            any(),
            any(),
            org.mockito.ArgumentMatchers.nullable(java.time.LocalDateTime.class)))
        .thenReturn(new StockMovement());
  }

  private Document transferRoot(final Long id, final Warehouse transit) {
    final Document root = new Document();
    root.setId(id);
    root.setCompany(company);
    root.setType(DocumentType.TRANSFER_REQUEST);
    root.setStatus(DocumentStatus.DRAFT);
    root.setSourceWarehouse(source);
    root.setTargetWarehouse(destination);
    root.setTransitWarehouse(transit);
    final DocumentLine line = new DocumentLine();
    line.setId(90L);
    line.setDocument(root);
    line.setProduct(product);
    line.setQuantity(new BigDecimal("2"));
    line.setFulfilledQuantity(BigDecimal.ZERO);
    line.setInTransitQuantity(transit == null ? null : new BigDecimal("2"));
    root.setLines(new java.util.ArrayList<>(List.of(line)));
    return root;
  }

  private Document operationDocument(final DocumentType type) {
    final Document document = new Document();
    document.setType(type);
    document.setLines(new java.util.ArrayList<>());
    return document;
  }

  private WarehouseOperationRequest transferOperationRequest(
      final WarehouseOperationType resolution) {
    final var line =
        new WarehouseOperationRequest.WarehouseOperationLine(
            90L, null, new BigDecimal("2"), null, null, null, null, null, null, null);
    return new WarehouseOperationRequest(
        50L,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        LocalDate.now(),
        null,
        "0",
        null,
        null,
        resolution,
        null,
        "reason",
        true,
        false,
        List.of(line));
  }

  private WarehouseOperationRequest request(
      final Long sourceWarehouseId,
      final Long destinationWarehouseId,
      final Long warehouseId,
      final LocalDateTime countedAt,
      final List<WarehouseOperationRequest.WarehouseOperationLine> lines) {
    return new WarehouseOperationRequest(
        null,
        null,
        sourceWarehouseId,
        destinationWarehouseId,
        null,
        warehouseId,
        null,
        null,
        null,
        LocalDate.now(),
        countedAt,
        "0",
        null,
        null,
        null,
        null,
        "reason",
        true,
        false,
        lines);
  }

  private WarehouseOperationRequest.WarehouseOperationLine operationLine(
      final BigDecimal quantity, final BigDecimal countedQuantity) {
    return new WarehouseOperationRequest.WarehouseOperationLine(
        null, 30L, quantity, null, null, null, countedQuantity, null, null, null);
  }
}
