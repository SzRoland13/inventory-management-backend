package dev.roland.inventory_management_backend.features.document.facade.workflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.roland.inventory_management_backend.common.service.CurrentCompanyProvider;
import dev.roland.inventory_management_backend.features.company.Company;
import dev.roland.inventory_management_backend.features.document.Document;
import dev.roland.inventory_management_backend.features.document.dto.WarehouseOperationPreview;
import dev.roland.inventory_management_backend.features.document.dto.WarehouseOperationRequest;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentRelationType;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentStatus;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentType;
import dev.roland.inventory_management_backend.features.document.enumeration.ReturnDirection;
import dev.roland.inventory_management_backend.features.document.enumeration.ReturnDisposition;
import dev.roland.inventory_management_backend.features.document.enumeration.WarehouseOperationType;
import dev.roland.inventory_management_backend.features.document.mapper.DocumentResponseMapper;
import dev.roland.inventory_management_backend.features.document_line.DocumentLine;
import dev.roland.inventory_management_backend.features.document_line_allocation.service.DocumentLineAllocationService;
import dev.roland.inventory_management_backend.features.document_relation.DocumentRelation;
import dev.roland.inventory_management_backend.features.document_relation.service.DocumentRelationService;
import dev.roland.inventory_management_backend.features.product.Product;
import dev.roland.inventory_management_backend.features.product.service.ProductService;
import dev.roland.inventory_management_backend.features.stock_balance.StockBalance;
import dev.roland.inventory_management_backend.features.stock_balance.service.StockBalanceService;
import dev.roland.inventory_management_backend.features.stock_movement.service.StockMovementService;
import dev.roland.inventory_management_backend.features.user.User;
import dev.roland.inventory_management_backend.features.user.enumeration.UserRole;
import dev.roland.inventory_management_backend.features.warehouse.Warehouse;
import dev.roland.inventory_management_backend.features.warehouse.enumeration.WarehouseStatus;
import dev.roland.inventory_management_backend.features.warehouse.enumeration.WarehouseType;
import dev.roland.inventory_management_backend.features.warehouse.service.WarehouseService;

@ExtendWith(MockitoExtension.class)
class WarehouseOperationPreviewServiceTest {
  private static final Long COMPANY_ID = 9L;
  private static final Long LINE_ID = 22L;
  private static final Long DOCUMENT_ID = 31L;

  @Mock private ProductService productService;
  @Mock private WarehouseService warehouseService;
  @Mock private CurrentCompanyProvider companyProvider;
  @Mock private DocumentRelationService relationService;
  @Mock private DocumentLineAllocationService allocationService;
  @Mock private StockBalanceService balanceService;
  @Mock private StockMovementService stockMovementService;
  @Mock private DocumentWorkflowAccessService accessService;
  @Mock private DocumentResponseMapper responseMapper;
  @Mock private WarehouseOperationSupportService supportService;
  @Mock private WarehouseTransferPreviewService transferPreviewService;

  private WarehouseOperationPreviewService service;
  private Company company;
  private Product product;
  private Warehouse warehouse;

  @BeforeEach
  void setUp() {
    service =
        new WarehouseOperationPreviewService(
            productService,
            warehouseService,
            companyProvider,
            relationService,
            allocationService,
            balanceService,
            stockMovementService,
            accessService,
            responseMapper,
            supportService,
            transferPreviewService);
    company = Company.builder().id(COMPANY_ID).build();
    product = Product.builder().id(7L).name("Widget").build();
    warehouse =
        Warehouse.builder()
            .id(40L)
            .company(company)
            .type(WarehouseType.STANDARD)
            .status(WarehouseStatus.ACTIVE)
            .build();
    lenient().when(companyProvider.getCurrentCompany()).thenReturn(company);
    lenient()
        .when(productService.findByIdAndCompanyId(anyLong(), anyLong()))
        .thenReturn(Optional.of(product));
    lenient()
        .when(relationService.findAllBySourceDocumentIdOrTargetDocumentId(anyLong(), anyLong()))
        .thenReturn(List.of());
    lenient()
        .when(accessService.currentActor())
        .thenReturn(User.builder().role(UserRole.ADMIN).build());
    lenient()
        .when(balanceService.findByWarehouseAndProduct(any(), any()))
        .thenReturn(Optional.empty());
    lenient()
        .when(stockMovementService.findEffectiveThrough(anyLong(), anyLong(), any()))
        .thenReturn(List.of());
    lenient()
        .when(stockMovementService.findEffectiveAfter(anyLong(), anyLong(), any()))
        .thenReturn(List.of());
  }

  @Test
  void rejectsEmptyRequestsAndPreservesUnmappedOperationLines() {
    final WarehouseOperationPreview empty =
        service.preview(
            null,
            WarehouseOperationType.RETURN_CREATE,
            request(null, null, null, null, null, null, null, null, List.of(), null));
    final WarehouseOperationPreview unmapped =
        service.preview(
            null,
            WarehouseOperationType.REVERSAL,
            request(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                List.of(line(null, 7L, "2", null, null)),
                null));

    assertFalse(empty.valid());
    assertTrue(empty.lines().isEmpty());
    assertEquals(1, unmapped.lines().size());
    assertEquals(7L, unmapped.lines().getFirst().productId());
  }

  @Test
  void previewsCustomerAndSupplierReturnsAndReportsInvalidQuantities() {
    final Document delivery = document(DocumentType.DELIVERY_NOTE, DocumentStatus.COMPLETED);
    delivery.setSourceWarehouse(warehouse);
    final DocumentLine delivered = documentLine(delivery, new BigDecimal("4"));
    when(accessService.find(DOCUMENT_ID)).thenReturn(delivery);
    when(supportService.line(LINE_ID, COMPANY_ID)).thenReturn(delivered);
    when(warehouseService.findEntityByIdAndCompanyId(40L, COMPANY_ID)).thenReturn(warehouse);

    final WarehouseOperationPreview customer =
        service.preview(
            null,
            WarehouseOperationType.RETURN_CREATE,
            request(
                DOCUMENT_ID,
                null,
                40L,
                null,
                null,
                null,
                ReturnDirection.CUSTOMER_RETURN,
                "damaged",
                List.of(line(LINE_ID, null, "2", null, ReturnDisposition.DAMAGED)),
                null));

    assertTrue(customer.valid());
    assertEquals(new BigDecimal("4"), customer.lines().getFirst().returnableQuantity());
    final WarehouseOperationPreview rejected =
        service.preview(
            null,
            WarehouseOperationType.RETURN_CREATE,
            request(
                DOCUMENT_ID,
                null,
                40L,
                null,
                null,
                null,
                ReturnDirection.CUSTOMER_RETURN,
                "reason",
                List.of(line(LINE_ID, null, "5", null, ReturnDisposition.RESTOCK)),
                null));
    assertFalse(rejected.valid());

    final Document receipt = document(DocumentType.GOODS_RECEIPT, DocumentStatus.COMPLETED);
    receipt.setId(32L);
    receipt.setTargetWarehouse(warehouse);
    final DocumentLine received = documentLine(receipt, new BigDecimal("4"));
    when(accessService.find(32L)).thenReturn(receipt);
    when(supportService.line(LINE_ID, COMPANY_ID)).thenReturn(received);
    final WarehouseOperationPreview supplier =
        service.preview(
            null,
            WarehouseOperationType.RETURN_CREATE,
            request(
                32L,
                null,
                null,
                null,
                null,
                null,
                ReturnDirection.SUPPLIER_RETURN,
                "supplier return",
                List.of(line(LINE_ID, null, "1", null, ReturnDisposition.SUPPLIER_RETURN)),
                null));
    assertTrue(supplier.valid());
    assertEquals(new BigDecimal("-1"), supplier.lines().getFirst().quantityDelta());
  }

  @Test
  void previewsCorrectionsAndIncludesRelatedReturns() {
    final Document document = document(DocumentType.DELIVERY_NOTE, DocumentStatus.COMPLETED);
    document.setSourceWarehouse(warehouse);
    document.setCompletedAt(LocalDateTime.now());
    final DocumentLine source = documentLine(document, new BigDecimal("3"));
    source.setReturnedQuantity(BigDecimal.ONE);
    document.setLines(List.of(source));
    when(accessService.find(DOCUMENT_ID)).thenReturn(document);
    when(supportService.line(LINE_ID, COMPANY_ID)).thenReturn(source);
    when(supportService.defaultCorrectionWarehouse(document)).thenReturn(warehouse);
    when(supportService.correctionStockDeltas(
            document, source, line(LINE_ID, null, "0", "4", null), COMPANY_ID))
        .thenReturn(Map.of(new WarehouseOperationSupportService.StockKey(40L, 7L), BigDecimal.ONE));
    when(warehouseService.findEntityByIdAndCompanyId(40L, COMPANY_ID)).thenReturn(warehouse);
    when(productService.findByIdAndCompanyId(7L, COMPANY_ID)).thenReturn(Optional.of(product));
    when(allocationService.findAllByFulfillmentLineId(LINE_ID)).thenReturn(List.of());
    final Document returnDoc = document(DocumentType.RETURN, DocumentStatus.COMPLETED);
    returnDoc.setReferenceNumber("RET-4");
    final DocumentRelation relation = new DocumentRelation();
    relation.setRelationType(DocumentRelationType.RETURNS);
    relation.setSourceDocument(document);
    relation.setTargetDocument(returnDoc);
    when(relationService.findAllBySourceDocumentIdOrTargetDocumentId(DOCUMENT_ID, DOCUMENT_ID))
        .thenReturn(List.of(relation));

    final WarehouseOperationPreview preview =
        service.preview(
            DOCUMENT_ID,
            WarehouseOperationType.CORRECTION,
            request(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "corrected quantity",
                List.of(line(LINE_ID, null, "0", "4", null)),
                WarehouseOperationType.CORRECT_RECORDING));

    assertTrue(preview.valid());
    assertEquals(new BigDecimal("1"), preview.lines().getFirst().quantityDelta());
    assertEquals("RET-4", preview.relatedReturns().getFirst().number());
  }

  @Test
  void previewsQuarantineReleaseAndReplacementAvailability() {
    final Document returned = document(DocumentType.RETURN, DocumentStatus.COMPLETED);
    final DocumentLine quarantineLine = documentLine(returned, new BigDecimal("3"));
    quarantineLine.setWorkflowDisposition(ReturnDisposition.QUARANTINE);
    quarantineLine.setReleasedQuantity(BigDecimal.ZERO);
    returned.setLines(List.of(quarantineLine));
    final Warehouse quarantine =
        Warehouse.builder()
            .id(99L)
            .company(company)
            .type(WarehouseType.QUARANTINE)
            .status(WarehouseStatus.ACTIVE)
            .build();
    when(accessService.find(DOCUMENT_ID)).thenReturn(returned);
    when(accessService.find(32L)).thenReturn(returned);
    when(supportService.line(LINE_ID, COMPANY_ID)).thenReturn(quarantineLine);
    when(warehouseService.ensureQuarantineWarehouse(company)).thenReturn(quarantine);
    when(warehouseService.findEntityByIdAndCompanyId(40L, COMPANY_ID)).thenReturn(warehouse);
    when(balanceService.findByWarehouseAndProduct(quarantine, product))
        .thenReturn(Optional.of(StockBalance.builder().quantity(new BigDecimal("4")).build()));

    final WarehouseOperationPreview release =
        service.preview(
            DOCUMENT_ID,
            WarehouseOperationType.QUARANTINE_RELEASE,
            request(
                null,
                null,
                null,
                null,
                40L,
                null,
                null,
                "release",
                List.of(line(LINE_ID, null, "2", null, null)),
                null));
    assertTrue(release.valid());

    final Document original = document(DocumentType.DELIVERY_NOTE, DocumentStatus.COMPLETED);
    final DocumentLine originalLine = documentLine(original, new BigDecimal("3"));
    final DocumentLine returnLine = documentLine(returned, new BigDecimal("2"));
    returnLine.setSourceLine(originalLine);
    returnLine.setReplacedQuantity(BigDecimal.ZERO);
    returned.setWorkflowDirection(ReturnDirection.CUSTOMER_RETURN);
    when(accessService.find(41L)).thenReturn(original);
    when(supportService.line(LINE_ID, COMPANY_ID)).thenReturn(returnLine);
    when(balanceService.findByWarehouseAndProduct(warehouse, product))
        .thenReturn(Optional.of(StockBalance.builder().quantity(new BigDecimal("5")).build()));

    final WarehouseOperationPreview replacement =
        service.preview(
            null,
            WarehouseOperationType.REPLACEMENT_DELIVERY,
            request(
                null,
                41L,
                40L,
                null,
                null,
                32L,
                null,
                "replace",
                List.of(line(LINE_ID, 7L, "1", null, null)),
                null));
    assertTrue(replacement.valid());
    assertNotNull(replacement.lines().getFirst().resultingQuantity());
  }

  @Test
  void rebasesStockCountAgainstMovementsAfterCountTime() {
    final Document count = document(DocumentType.STOCK_COUNT, DocumentStatus.DRAFT);
    count.setSourceWarehouse(warehouse);
    final DocumentLine countLine = documentLine(count, BigDecimal.ZERO);
    count.setLines(List.of(countLine));
    when(accessService.find(DOCUMENT_ID)).thenReturn(count);
    when(supportService.line(LINE_ID, COMPANY_ID)).thenReturn(countLine);
    when(stockMovementService.findEffectiveThrough(7L, 40L, LocalDateTime.of(2026, 10, 1, 10, 0)))
        .thenReturn(List.of());
    when(stockMovementService.findEffectiveAfter(7L, 40L, LocalDateTime.of(2026, 10, 1, 10, 0)))
        .thenReturn(List.of());
    when(balanceService.findByWarehouseAndProduct(warehouse, product)).thenReturn(Optional.empty());
    when(supportService.hashCalculationVersion(any())).thenReturn("count-version");

    final WarehouseOperationRequest countRequest =
        request(
            null,
            null,
            null,
            LocalDateTime.of(2026, 10, 1, 10, 0),
            null,
            null,
            null,
            null,
            List.of(line(LINE_ID, null, null, null, null)),
            null);
    final WarehouseOperationPreview preview =
        service.preview(DOCUMENT_ID, WarehouseOperationType.STOCK_COUNT_RECONCILE, countRequest);

    assertTrue(preview.valid());
    assertNotNull(preview.calculationVersion(), preview.toString());
    assertEquals(BigDecimal.ONE, preview.lines().getFirst().resultingQuantity());
  }

  @Test
  void reportsMissingReturnSourceDirectionReasonAndDisposition() {
    final WarehouseOperationPreview preview =
        service.preview(
            null,
            WarehouseOperationType.RETURN_CREATE,
            request(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                " ",
                List.of(line(null, 7L, "0", null, null)),
                null));

    assertFalse(preview.valid());
    assertEquals(1, preview.lines().size());
    assertEquals(BigDecimal.ZERO, preview.lines().getFirst().returnableQuantity());
  }

  @Test
  void reportsCorrectionTransitionWorkflowQuantityAndMissingSourceConflicts() {
    final Document document = document(DocumentType.STOCK_ADJUSTMENT, DocumentStatus.DRAFT);
    document.setCreatedAt(null);
    when(accessService.find(DOCUMENT_ID)).thenReturn(document);

    final WarehouseOperationPreview preview =
        service.preview(
            DOCUMENT_ID,
            WarehouseOperationType.CORRECTION,
            request(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                " ",
                List.of(line(null, 7L, "0", "-1", null)),
                WarehouseOperationType.REVERSAL));

    assertFalse(preview.valid());
    assertEquals(1, preview.lines().size());
    assertNotNull(preview.lines().getFirst().errorMessageKey());
  }

  @Test
  void rejectsQuarantineReleaseForNonManagersAndInvalidWarehouseTargets() {
    final Document returned = document(DocumentType.RETURN, DocumentStatus.COMPLETED);
    final Warehouse quarantine =
        Warehouse.builder()
            .id(99L)
            .company(company)
            .type(WarehouseType.QUARANTINE)
            .status(WarehouseStatus.ACTIVE)
            .build();
    when(accessService.find(DOCUMENT_ID)).thenReturn(returned);
    when(accessService.currentActor()).thenReturn(User.builder().role(UserRole.SALES).build());
    when(warehouseService.ensureQuarantineWarehouse(company)).thenReturn(quarantine);
    when(warehouseService.findEntityByIdAndCompanyId(40L, COMPANY_ID)).thenReturn(quarantine);

    final WarehouseOperationPreview preview =
        service.preview(
            DOCUMENT_ID,
            WarehouseOperationType.QUARANTINE_RELEASE,
            request(
                null,
                null,
                null,
                null,
                40L,
                null,
                null,
                null,
                List.of(line(null, 7L, "1", null, null)),
                null));

    assertFalse(preview.valid());
    assertNotNull(preview.lines().getFirst().errorMessageKey());
  }

  @Test
  void invalidStockCountRequestsReportWrongDocumentAndMissingCountTime() {
    final WarehouseOperationPreview wrongType =
        service.preview(
            DOCUMENT_ID,
            WarehouseOperationType.STOCK_COUNT_RECONCILE,
            request(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                List.of(line(LINE_ID, null, null, null, null)),
                null));
    final Document count = document(DocumentType.STOCK_COUNT, DocumentStatus.DRAFT);
    count.setCountedAt(null);
    when(accessService.find(DOCUMENT_ID)).thenReturn(count);
    final WarehouseOperationPreview missingTime =
        service.preview(
            DOCUMENT_ID,
            WarehouseOperationType.STOCK_COUNT_RECONCILE,
            request(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                List.of(line(LINE_ID, null, null, null, null)),
                null));

    assertFalse(wrongType.valid());
    assertFalse(missingTime.valid());
    assertEquals(0, missingTime.lines().size());
  }

  private Document document(final DocumentType type, final DocumentStatus status) {
    final Document document = new Document();
    document.setId(DOCUMENT_ID);
    document.setCompany(company);
    document.setType(type);
    document.setStatus(status);
    document.setCreatedAt(LocalDateTime.of(2026, 9, 1, 10, 0));
    return document;
  }

  private DocumentLine documentLine(final Document document, final BigDecimal quantity) {
    final DocumentLine line = new DocumentLine();
    line.setId(LINE_ID);
    line.setDocument(document);
    line.setProduct(product);
    line.setQuantity(quantity);
    line.setFulfilledQuantity(BigDecimal.ZERO);
    line.setClosedQuantity(BigDecimal.ZERO);
    line.setReturnedQuantity(BigDecimal.ZERO);
    return line;
  }

  private WarehouseOperationRequest.WarehouseOperationLine line(
      final Long sourceLineId,
      final Long productId,
      final String quantity,
      final String correctedQuantity,
      final ReturnDisposition disposition) {
    return new WarehouseOperationRequest.WarehouseOperationLine(
        sourceLineId,
        productId,
        quantity == null ? null : new BigDecimal(quantity),
        correctedQuantity == null ? null : new BigDecimal(correctedQuantity),
        null,
        null,
        BigDecimal.ONE,
        null,
        disposition,
        null);
  }

  private WarehouseOperationRequest request(
      final Long sourceDocumentId,
      final Long originalDeliveryId,
      final Long warehouseId,
      final LocalDateTime countedAt,
      final Long targetWarehouseId,
      final Long returnDocumentId,
      final ReturnDirection direction,
      final String reason,
      final List<WarehouseOperationRequest.WarehouseOperationLine> lines,
      final WarehouseOperationType requestOperation) {
    return new WarehouseOperationRequest(
        null,
        sourceDocumentId,
        null,
        null,
        null,
        warehouseId,
        targetWarehouseId,
        returnDocumentId,
        originalDeliveryId,
        LocalDate.of(2026, 10, 1),
        countedAt,
        "0",
        null,
        null,
        requestOperation,
        direction,
        reason,
        true,
        false,
        lines);
  }
}
