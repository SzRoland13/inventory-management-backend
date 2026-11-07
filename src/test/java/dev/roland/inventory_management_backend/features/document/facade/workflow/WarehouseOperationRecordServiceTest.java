package dev.roland.inventory_management_backend.features.document.facade.workflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.roland.inventory_management_backend.common.exception.ApiException;
import dev.roland.inventory_management_backend.features.company.Company;
import dev.roland.inventory_management_backend.features.document.Document;
import dev.roland.inventory_management_backend.features.document.dto.DocumentResponse;
import dev.roland.inventory_management_backend.features.document.dto.WarehouseOperationPreview;
import dev.roland.inventory_management_backend.features.document.dto.WarehouseOperationRequest;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentStatus;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentType;
import dev.roland.inventory_management_backend.features.document.enumeration.ReturnDirection;
import dev.roland.inventory_management_backend.features.document.enumeration.ReturnDisposition;
import dev.roland.inventory_management_backend.features.document.enumeration.WarehouseOperationType;
import dev.roland.inventory_management_backend.features.document.mapper.DocumentResponseMapper;
import dev.roland.inventory_management_backend.features.document.message.DocumentApiMessageKey;
import dev.roland.inventory_management_backend.features.document.service.DocumentService;
import dev.roland.inventory_management_backend.features.document_activity.DocumentActivityEvent;
import dev.roland.inventory_management_backend.features.document_line.DocumentLine;
import dev.roland.inventory_management_backend.features.document_line.service.DocumentLineService;
import dev.roland.inventory_management_backend.features.document_line_allocation.DocumentLineAllocation;
import dev.roland.inventory_management_backend.features.document_line_allocation.service.DocumentLineAllocationService;
import dev.roland.inventory_management_backend.features.document_relation.service.DocumentRelationService;
import dev.roland.inventory_management_backend.features.product.Product;
import dev.roland.inventory_management_backend.features.product.service.ProductService;
import dev.roland.inventory_management_backend.features.stock_movement.StockMovement;
import dev.roland.inventory_management_backend.features.stock_movement.service.StockMovementService;
import dev.roland.inventory_management_backend.features.unit.Unit;
import dev.roland.inventory_management_backend.features.user.User;
import dev.roland.inventory_management_backend.features.user.enumeration.UserRole;
import dev.roland.inventory_management_backend.features.warehouse.Warehouse;
import dev.roland.inventory_management_backend.features.warehouse.service.WarehouseService;

@ExtendWith(MockitoExtension.class)
class WarehouseOperationRecordServiceTest {
  private static final Long COMPANY_ID = 3L;

  @Mock private DocumentService documentService;
  @Mock private DocumentLineService lineService;
  @Mock private ProductService productService;
  @Mock private WarehouseService warehouseService;
  @Mock private DocumentRelationService relationService;
  @Mock private DocumentLineAllocationService allocationService;
  @Mock private StockMovementService stockMovementService;
  @Mock private DocumentWorkflowAccessService accessService;
  @Mock private DocumentWorkflowWriterService writer;
  @Mock private DocumentResponseMapper responseMapper;
  @Mock private WarehouseOperationSupportService support;
  @Mock private WarehouseOperationPreviewService previewService;

  private WarehouseOperationRecordService service;
  private Company company;
  private User actor;
  private DocumentResponse response;

  @BeforeEach
  void setUp() {
    service =
        new WarehouseOperationRecordService(
            documentService,
            lineService,
            productService,
            warehouseService,
            relationService,
            allocationService,
            stockMovementService,
            accessService,
            writer,
            responseMapper,
            support,
            previewService);
    company = Company.builder().id(COMPANY_ID).build();
    actor = User.builder().role(UserRole.ADMIN).build();
    response =
        new DocumentResponse(
            null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, false, null, null, null, null, null, null, null, null, null, null,
            null, null);
  }

  @Test
  void createsCustomerReturnAndPostsInboundMovement() {
    stubRecordPersistence();
    final WarehouseOperationRequest request =
        request(
            10L,
            new BigDecimal("2"),
            null,
            ReturnDisposition.RESTOCK,
            ReturnDirection.CUSTOMER_RETURN);
    final Document sourceDocument = document(10L, DocumentType.DELIVERY_NOTE);
    final DocumentLine sourceLine = sourceLine(sourceDocument, 20L, new BigDecimal("5"));
    final Warehouse warehouse = warehouse(30L);
    sourceDocument.setSourceWarehouse(warehouse);
    final Document returned = operationDocument(DocumentType.RETURN);
    when(accessService.lock(10L, COMPANY_ID)).thenReturn(sourceDocument);
    when(documentService.postingKeyExists(10L, "key")).thenReturn(false);
    when(previewService.preview(10L, WarehouseOperationType.RETURN_CREATE, request))
        .thenReturn(validPreview());
    when(support.operationDocument(sourceDocument, DocumentType.RETURN, "reason"))
        .thenReturn(returned);
    when(support.line(20L, COMPANY_ID)).thenReturn(sourceLine);
    when(support.cloneLine(returned, sourceLine, new BigDecimal("2")))
        .thenReturn(new DocumentLine());
    when(stockMovementService.postMovement(
            any(),
            any(),
            any(),
            any(),
            any(),
            org.mockito.ArgumentMatchers.nullable(java.time.LocalDateTime.class)))
        .thenReturn(new StockMovement());

    assertSame(response, service.createReturn(request, company, actor, "key"));
    assertEquals(0, new BigDecimal("2").compareTo(sourceLine.getReturnedQuantity()));
    assertEquals(warehouse, returned.getTargetWarehouse());
    verify(writer).complete(returned);
    verify(writer).consumeKey(eq(sourceDocument), eq("key"), any(Document.class));
  }

  @Test
  void rejectsReturnWhenPreviewIsInvalid() {
    final WarehouseOperationRequest request =
        request(
            10L, BigDecimal.ONE, null, ReturnDisposition.RESTOCK, ReturnDirection.CUSTOMER_RETURN);
    when(accessService.lock(10L, COMPANY_ID)).thenReturn(document(10L, DocumentType.DELIVERY_NOTE));
    when(documentService.postingKeyExists(10L, "key")).thenReturn(false);
    when(previewService.preview(10L, WarehouseOperationType.RETURN_CREATE, request))
        .thenReturn(
            new WarehouseOperationPreview(false, null, List.of(), List.of(), List.of(), List.of()));

    final ApiException error =
        assertThrows(
            ApiException.class, () -> service.createReturn(request, company, actor, "key"));

    assertEquals(DocumentApiMessageKey.DOCUMENT_INVALID_LINES, error.getMessageKey());
  }

  @Test
  void createsCorrectionAndUsesInboundStockDeltas() {
    stubRecordPersistence();
    final Document original = document(11L, DocumentType.GOODS_RECEIPT);
    final Document correction = operationDocument(DocumentType.CORRECTION);
    final DocumentLine source = sourceLine(original, 22L, new BigDecimal("3"));
    final Warehouse warehouse = warehouse(31L);
    final Product product = Product.builder().id(40L).unit(Unit.builder().id(1L).build()).build();
    final WarehouseOperationRequest request = correctionRequest(22L, new BigDecimal("4"));
    when(previewService.preview(11L, WarehouseOperationType.CORRECTION, request))
        .thenReturn(validPreview());
    when(support.operationDocument(original, DocumentType.CORRECTION, "reason"))
        .thenReturn(correction);
    when(support.line(22L, COMPANY_ID)).thenReturn(source);
    when(support.defaultCorrectionWarehouse(original)).thenReturn(warehouse);
    when(productService.findByIdAndCompanyId(40L, COMPANY_ID)).thenReturn(Optional.of(product));
    when(support.cloneLine(correction, source, new BigDecimal("3"))).thenReturn(source);
    when(stockMovementService.postMovement(
            any(),
            any(),
            any(),
            any(),
            any(),
            org.mockito.ArgumentMatchers.nullable(java.time.LocalDateTime.class)))
        .thenReturn(new StockMovement());

    assertSame(response, service.createCorrection(original, request, company, actor, "key"));
    verify(writer).complete(correction);
    verify(writer)
        .relate(
            original,
            correction,
            dev.roland.inventory_management_backend.features.document.enumeration
                .DocumentRelationType.CORRECTS);
  }

  @Test
  void correctionReopensClosedOrderQuantityAndAuditsTheReason() {
    stubRecordPersistence();
    final Document original = document(11L, DocumentType.GOODS_RECEIPT);
    final Document order = document(12L, DocumentType.PURCHASE_ORDER);
    order.setStatus(DocumentStatus.PARTIALLY_COMPLETED);
    final Document correction = operationDocument(DocumentType.CORRECTION);
    final DocumentLine source = sourceLine(original, 22L, new BigDecimal("3"));
    final DocumentLine orderLine = sourceLine(order, 23L, new BigDecimal("10"));
    orderLine.setClosedQuantity(new BigDecimal("2"));
    orderLine.setClosureReason("initial closure");
    final DocumentLineAllocation allocation = new DocumentLineAllocation();
    allocation.setFulfillmentLine(source);
    allocation.setSourceOrderLine(orderLine);
    allocation.setQuantity(new BigDecimal("3"));
    final Warehouse warehouse = warehouse(31L);
    final Product product = Product.builder().id(40L).unit(Unit.builder().id(1L).build()).build();
    final WarehouseOperationRequest request =
        correctionRequest(
            22L,
            new BigDecimal("4"),
            new WarehouseOperationRequest.ClosureReopen(
                23L, BigDecimal.ONE, "recorded receipt quantity was understated"));
    when(accessService.lock(12L, COMPANY_ID)).thenReturn(order);
    when(previewService.preview(11L, WarehouseOperationType.CORRECTION, request))
        .thenReturn(validPreview());
    when(support.operationDocument(original, DocumentType.CORRECTION, "reason"))
        .thenReturn(correction);
    when(support.line(22L, COMPANY_ID)).thenReturn(source);
    when(support.line(23L, COMPANY_ID)).thenReturn(orderLine);
    when(allocationService.findAllByFulfillmentLineId(22L)).thenReturn(List.of(allocation));
    when(support.defaultCorrectionWarehouse(original)).thenReturn(warehouse);
    when(productService.findByIdAndCompanyId(40L, COMPANY_ID)).thenReturn(Optional.of(product));
    when(support.cloneLine(correction, source, new BigDecimal("3"))).thenReturn(source);
    when(stockMovementService.postMovement(
            any(),
            any(),
            any(),
            any(),
            any(),
            org.mockito.ArgumentMatchers.nullable(java.time.LocalDateTime.class)))
        .thenReturn(new StockMovement());

    service.createCorrection(original, request, company, actor, "correction-key");

    assertEquals(BigDecimal.ONE, orderLine.getClosedQuantity());
    final ArgumentCaptor<String> details = ArgumentCaptor.forClass(String.class);
    verify(writer)
        .addActivity(eq(order), eq(DocumentActivityEvent.REMAINDER_REOPENED), details.capture());
    assertTrue(details.getValue().contains("\"closedBefore\":2"));
    assertTrue(details.getValue().contains("\"reopened\":1"));
    assertTrue(details.getValue().contains("\"closedAfter\":1"));
    assertTrue(details.getValue().contains("initial closure"));
    assertTrue(details.getValue().contains("recorded receipt quantity was understated"));
  }

  @Test
  void postCorrectionMovementSkipsMissingWarehouseAndZeroDelta() {
    final DocumentLine line = new DocumentLine();
    service.postCorrectionMovement(line, null, BigDecimal.ONE, actor, "reason", null);
    service.postCorrectionMovement(line, warehouse(1L), BigDecimal.ZERO, actor, "reason", null);
    verify(writer, org.mockito.Mockito.never()).saveStockMovement(any(), any());
  }

  @Test
  void releasesQuarantinedReturnLineIntoStandardWarehouse() {
    stubRecordPersistence();
    final Document returned = document(50L, DocumentType.RETURN);
    final Document release = operationDocument(DocumentType.STOCK_ADJUSTMENT);
    final DocumentLine returnedLine = sourceLine(returned, 51L, new BigDecimal("4"));
    returnedLine.setWorkflowDisposition(ReturnDisposition.QUARANTINE);
    final Warehouse quarantine = warehouse(60L);
    final Warehouse target = warehouse(61L);
    final WarehouseOperationRequest request = requestWithTarget(51L, new BigDecimal("2"));
    when(support.quarantineWarehouse(COMPANY_ID)).thenReturn(quarantine);
    when(warehouseService.findEntityByIdAndCompanyId(61L, COMPANY_ID)).thenReturn(target);
    when(support.operationDocument(returned, DocumentType.STOCK_ADJUSTMENT, "reason"))
        .thenReturn(release);
    when(support.line(51L, COMPANY_ID)).thenReturn(returnedLine);
    when(support.cloneLine(release, returnedLine, new BigDecimal("2")))
        .thenReturn(new DocumentLine());
    when(stockMovementService.postMovement(
            any(),
            any(),
            any(),
            any(),
            any(),
            org.mockito.ArgumentMatchers.nullable(java.time.LocalDateTime.class)))
        .thenReturn(new StockMovement());

    assertSame(response, service.releaseQuarantine(returned, request, company, actor, "key"));

    assertEquals(0, new BigDecimal("2").compareTo(returnedLine.getReleasedQuantity()));
    assertEquals(quarantine, release.getSourceWarehouse());
    assertEquals(target, release.getTargetWarehouse());
    verify(writer).complete(release);
  }

  @Test
  void createsNonBillableReplacementDelivery() {
    stubRecordPersistence();
    final Document returned = document(70L, DocumentType.RETURN);
    final Document original = document(71L, DocumentType.DELIVERY_NOTE);
    final Document delivery = operationDocument(DocumentType.DELIVERY_NOTE);
    final Warehouse warehouse = warehouse(72L);
    final Product product = Product.builder().id(73L).unit(Unit.builder().id(1L).build()).build();
    final DocumentLine returnedLine = sourceLine(returned, 74L, BigDecimal.ONE);
    returnedLine.setProduct(product);
    final WarehouseOperationRequest request = replacementRequest(74L);
    when(accessService.lock(70L, COMPANY_ID)).thenReturn(returned);
    when(accessService.find(71L)).thenReturn(original);
    when(documentService.postingKeyExists(70L, "key")).thenReturn(false);
    when(warehouseService.findEntityByIdAndCompanyId(72L, COMPANY_ID)).thenReturn(warehouse);
    when(previewService.preview(null, WarehouseOperationType.REPLACEMENT_DELIVERY, request))
        .thenReturn(validPreview());
    when(support.operationDocument(original, DocumentType.DELIVERY_NOTE, "Replacement delivery"))
        .thenReturn(delivery);
    when(support.line(74L, COMPANY_ID)).thenReturn(returnedLine);
    when(stockMovementService.postMovement(
            any(),
            any(),
            any(),
            any(),
            any(),
            org.mockito.ArgumentMatchers.nullable(java.time.LocalDateTime.class)))
        .thenReturn(new StockMovement());

    assertSame(response, service.createReplacement(request, company, actor, "key"));

    assertEquals(true, delivery.isNonBillable());
    assertEquals(0, new BigDecimal("2").compareTo(returnedLine.getReplacedQuantity()));
    verify(writer).complete(delivery);
  }

  @Test
  void reconcilesConfirmedStockCountAndRecordsVariance() {
    stubRecordPersistence();
    final Document count = document(80L, DocumentType.STOCK_COUNT);
    final Document adjustment = operationDocument(DocumentType.STOCK_ADJUSTMENT);
    final Warehouse warehouse = warehouse(81L);
    count.setSourceWarehouse(warehouse);
    final DocumentLine countLine = sourceLine(count, 82L, new BigDecimal("3"));
    final WarehouseOperationRequest request = countRequest(82L);
    final WarehouseOperationPreview.Line result =
        WarehouseOperationPreview.Line.builder()
            .sourceLineId(82L)
            .expectedAtCount(new BigDecimal("3"))
            .countedQuantity(new BigDecimal("5"))
            .variance(new BigDecimal("2"))
            .adjustmentQuantity(new BigDecimal("2"))
            .build();
    final WarehouseOperationPreview preview =
        new WarehouseOperationPreview(true, "v1", List.of(result), List.of(), List.of(), List.of());
    when(previewService.preview(80L, WarehouseOperationType.STOCK_COUNT_RECONCILE, request))
        .thenReturn(preview);
    when(support.operationDocument(count, DocumentType.STOCK_ADJUSTMENT, "reason"))
        .thenReturn(adjustment);
    when(support.line(82L, COMPANY_ID)).thenReturn(countLine);
    when(support.cloneLine(adjustment, countLine, new BigDecimal("2")))
        .thenReturn(new DocumentLine());
    when(stockMovementService.postMovement(any(), any(), any(), any(), any()))
        .thenReturn(new StockMovement());

    assertSame(response, service.postCount(count, request, company, actor, "key"));

    assertEquals("v1", count.getCalculationVersion());
    assertEquals(new BigDecimal("3"), countLine.getExpectedQuantity());
    assertEquals(new BigDecimal("5"), countLine.getCountedQuantity());
    assertEquals(new BigDecimal("2"), countLine.getVariance());
    verify(writer).complete(count);
    verify(writer).complete(adjustment);
    final var operationOrder = inOrder(warehouseService, previewService);
    operationOrder
        .verify(warehouseService)
        .findEntityByIdAndCompanyIdForUpdate(count.getSourceWarehouse().getId(), COMPANY_ID);
    operationOrder
        .verify(previewService)
        .preview(80L, WarehouseOperationType.STOCK_COUNT_RECONCILE, request);
  }

  private WarehouseOperationRequest request(
      final Long sourceDocumentId,
      final BigDecimal quantity,
      final BigDecimal correctedQuantity,
      final ReturnDisposition disposition,
      final ReturnDirection direction) {
    final var line =
        new WarehouseOperationRequest.WarehouseOperationLine(
            20L, null, quantity, correctedQuantity, null, null, null, null, disposition, null);
    return new WarehouseOperationRequest(
        null,
        sourceDocumentId,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        direction,
        "reason",
        null,
        null,
        List.of(line));
  }

  private WarehouseOperationRequest correctionRequest(
      final Long sourceLineId, final BigDecimal correctedQuantity) {
    return correctionRequest(sourceLineId, correctedQuantity, null);
  }

  private WarehouseOperationRequest correctionRequest(
      final Long sourceLineId,
      final BigDecimal correctedQuantity,
      final WarehouseOperationRequest.ClosureReopen closureReopen) {
    final var line =
        new WarehouseOperationRequest.WarehouseOperationLine(
            sourceLineId,
            null,
            null,
            correctedQuantity,
            null,
            null,
            null,
            null,
            null,
            null,
            closureReopen == null ? List.of() : List.of(closureReopen));
    return new WarehouseOperationRequest(
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        WarehouseOperationType.CORRECTION,
        null,
        "reason",
        null,
        null,
        List.of(line));
  }

  private WarehouseOperationRequest requestWithTarget(
      final Long sourceLineId, final BigDecimal quantity) {
    final var line =
        new WarehouseOperationRequest.WarehouseOperationLine(
            sourceLineId, null, quantity, null, null, null, null, null, null, null);
    return new WarehouseOperationRequest(
        null,
        null,
        null,
        null,
        null,
        null,
        61L,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        "reason",
        null,
        null,
        List.of(line));
  }

  private WarehouseOperationRequest replacementRequest(final Long sourceLineId) {
    final var line =
        new WarehouseOperationRequest.WarehouseOperationLine(
            sourceLineId, null, new BigDecimal("2"), null, null, null, null, null, null, null);
    return new WarehouseOperationRequest(
        null,
        null,
        null,
        null,
        null,
        72L,
        null,
        70L,
        71L,
        null,
        null,
        null,
        null,
        null,
        WarehouseOperationType.REPLACEMENT_DELIVERY,
        null,
        "reason",
        null,
        null,
        List.of(line));
  }

  private WarehouseOperationRequest countRequest(final Long sourceLineId) {
    final var line =
        new WarehouseOperationRequest.WarehouseOperationLine(
            sourceLineId, null, null, null, null, null, new BigDecimal("5"), "counted", null, null);
    return new WarehouseOperationRequest(
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        "v1",
        WarehouseOperationType.STOCK_COUNT_RECONCILE,
        null,
        "reason",
        true,
        null,
        List.of(line));
  }

  private Document document(final Long id, final DocumentType type) {
    final Document document = new Document();
    document.setId(id);
    document.setCompany(company);
    document.setType(type);
    document.setLines(new ArrayList<>());
    return document;
  }

  private Document operationDocument(final DocumentType type) {
    return document(99L, type);
  }

  private DocumentLine sourceLine(final Document parent, final Long id, final BigDecimal quantity) {
    final Unit unit = Unit.builder().id(1L).code("EA").name("Each").build();
    final Product product = Product.builder().id(40L).unit(unit).build();
    final DocumentLine line = new DocumentLine();
    line.setId(id);
    line.setDocument(parent);
    line.setProduct(product);
    line.setUnitSnapshot(unit);
    line.setConversionFactorSnapshot(BigDecimal.ONE);
    line.setQuantity(quantity);
    line.setUnitPrice(BigDecimal.ONE);
    return line;
  }

  private Warehouse warehouse(final Long id) {
    return Warehouse.builder().id(id).code("W" + id).name("Warehouse " + id).build();
  }

  private WarehouseOperationPreview validPreview() {
    return new WarehouseOperationPreview(
        true, "version", List.of(), List.of(), List.of(), List.of());
  }

  private void stubRecordPersistence() {
    when(lineService.save(any(DocumentLine.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(documentService.save(any(Document.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(responseMapper.toResponse(any(Document.class))).thenReturn(response);
  }
}
