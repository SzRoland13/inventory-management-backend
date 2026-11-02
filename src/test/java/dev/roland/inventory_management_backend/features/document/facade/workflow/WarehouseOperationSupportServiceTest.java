package dev.roland.inventory_management_backend.features.document.facade.workflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.roland.inventory_management_backend.common.exception.ApiException;
import dev.roland.inventory_management_backend.common.exception.NotFoundException;
import dev.roland.inventory_management_backend.common.message.NotFoundMessageKey;
import dev.roland.inventory_management_backend.common.service.CurrentCompanyProvider;
import dev.roland.inventory_management_backend.features.company.Company;
import dev.roland.inventory_management_backend.features.document.Document;
import dev.roland.inventory_management_backend.features.document.dto.WarehouseOperationRequest;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentStatus;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentType;
import dev.roland.inventory_management_backend.features.document.facade.workflow.WarehouseOperationSupportService.StockKey;
import dev.roland.inventory_management_backend.features.document.mapper.DocumentResponseMapper;
import dev.roland.inventory_management_backend.features.document.message.DocumentApiMessageKey;
import dev.roland.inventory_management_backend.features.document_line.DocumentLine;
import dev.roland.inventory_management_backend.features.document_line.service.DocumentLineService;
import dev.roland.inventory_management_backend.features.product.Product;
import dev.roland.inventory_management_backend.features.user.User;
import dev.roland.inventory_management_backend.features.user.enumeration.UserRole;
import dev.roland.inventory_management_backend.features.warehouse.Warehouse;
import dev.roland.inventory_management_backend.features.warehouse.service.WarehouseService;

@ExtendWith(MockitoExtension.class)
class WarehouseOperationSupportServiceTest {
  private static final Long COMPANY_ID = 8L;

  @Mock private DocumentWorkflowAccessService accessService;
  @Mock private DocumentWorkflowWriterService writer;
  @Mock private DocumentResponseMapper responseMapper;
  @Mock private DocumentDraftWorkflowService draftWorkflowService;
  @Mock private DocumentLineService lineService;
  @Mock private WarehouseService warehouseService;
  @Mock private CurrentCompanyProvider companyProvider;

  private WarehouseOperationSupportService service;
  private Company company;

  @BeforeEach
  void setUp() {
    service =
        new WarehouseOperationSupportService(
            accessService,
            writer,
            responseMapper,
            draftWorkflowService,
            lineService,
            warehouseService,
            companyProvider);
    company = Company.builder().id(COMPANY_ID).build();
  }

  @Test
  void operationDocumentCopiesWorkflowReferencesAndStartsAsDraft() {
    final Warehouse source = Warehouse.builder().id(1L).build();
    final Warehouse target = Warehouse.builder().id(2L).build();
    final Document parent = new Document();
    parent.setCompany(company);
    parent.setSourceWarehouse(source);
    parent.setTargetWarehouse(target);
    parent.setType(DocumentType.GOODS_RECEIPT);
    when(accessService.currentActor()).thenReturn(new User());

    final Document operation = service.operationDocument(parent, DocumentType.CORRECTION, "reason");

    assertEquals(COMPANY_ID, operation.getCompany().getId());
    assertEquals(source, operation.getSourceWarehouse());
    assertEquals(target, operation.getTargetWarehouse());
    assertEquals(DocumentType.CORRECTION, operation.getType());
    assertEquals(DocumentStatus.DRAFT, operation.getStatus());
    assertEquals("reason", operation.getWorkflowReason());
    assertTrue(operation.getLines().isEmpty());
  }

  @Test
  void completionAndLineCopyRecordTheirChanges() {
    final Document document = new Document();
    final DocumentLine source = new DocumentLine();
    final DocumentLine copy = new DocumentLine();
    when(draftWorkflowService.copyOperationLine(document, source, BigDecimal.TEN)).thenReturn(copy);

    service.complete(document);
    final DocumentLine result = service.cloneLine(document, source, BigDecimal.TEN);

    assertEquals(DocumentStatus.COMPLETED, document.getStatus());
    assertNotNull(document.getCompletedAt());
    assertEquals(source, result.getSourceLine());
  }

  @Test
  void lineLookupRequiresTheRequestedCompany() {
    final Document document = new Document();
    document.setCompany(company);
    final DocumentLine line = new DocumentLine();
    line.setDocument(document);
    when(lineService.findByIdOrThrow(3L)).thenReturn(line);
    when(accessService.notFound()).thenReturn(new NotFoundException(NotFoundMessageKey.DOCUMENT));

    assertEquals(line, service.line(3L, COMPANY_ID));
    assertThrows(NotFoundException.class, () -> service.line(3L, COMPANY_ID + 1));
  }

  @Test
  void delegatesRelationsStockMovementsKeysAndVersionChecks() {
    final Document document = new Document();
    final DocumentLine line = new DocumentLine();
    final var movement =
        new dev.roland.inventory_management_backend.features.stock_movement.StockMovement();

    service.relate(
        document,
        document,
        dev.roland.inventory_management_backend.features.document.enumeration.DocumentRelationType
            .RETURNS);
    service.stock(movement, "reason");
    service.consumeKey(document, "key");
    service.requireVersion(document, "version");

    verify(writer)
        .relate(
            document,
            document,
            dev.roland.inventory_management_backend.features.document.enumeration
                .DocumentRelationType.RETURNS);
    verify(writer).saveStockMovement(movement, "reason");
    verify(writer).consumeKey(document, "key");
    verify(accessService)
        .checkVersion(
            org.mockito.ArgumentMatchers.eq(document),
            org.mockito.ArgumentMatchers.eq("version"),
            org.mockito.ArgumentMatchers.any());
    assertNotNull(line);
  }

  @Test
  void requiresAnIdempotencyKeyAndHashesComponentsDeterministically() {
    final ApiException missing = assertThrows(ApiException.class, () -> service.requireKey(" "));

    assertEquals(DocumentApiMessageKey.DOCUMENT_IDEMPOTENCY_KEY_REQUIRED, missing.getMessageKey());
    service.requireKey("key");
    final String first = service.hashCalculationVersion(List.of("one", "two"));
    assertEquals(first, service.hashCalculationVersion(List.of("one", "two")));
    assertNotEquals(first, service.hashCalculationVersion(List.of("two", "one")));
  }

  @Test
  void detectsDuplicateNonNullSourceLines() {
    final var first = lineRequest(1L);
    final var duplicate = lineRequest(1L);

    assertTrue(service.hasDuplicateSourceLines(List.of(first, duplicate)));
    assertFalse(service.hasDuplicateSourceLines(List.of(first, lineRequest(2L))));
    assertFalse(service.hasDuplicateSourceLines(null));
  }

  @Test
  void computesCorrectionDeltasAndSelectsPrimaryWarehouseByDocumentType() {
    final Warehouse target = Warehouse.builder().id(4L).build();
    final Document receipt = new Document();
    receipt.setType(DocumentType.GOODS_RECEIPT);
    receipt.setTargetWarehouse(target);
    final Product product = Product.builder().id(5L).build();
    final DocumentLine source = new DocumentLine();
    source.setProduct(product);
    source.setQuantity(new BigDecimal("3"));
    final var requestLine =
        new WarehouseOperationRequest.WarehouseOperationLine(
            9L, null, null, new BigDecimal("5"), null, null, null, null, null, null);

    final Map<StockKey, BigDecimal> deltas =
        service.correctionStockDeltas(receipt, source, requestLine, COMPANY_ID);

    assertEquals(target, service.defaultCorrectionWarehouse(receipt));
    assertEquals(new BigDecimal("2"), deltas.get(new StockKey(4L, 5L)));
    receipt.setType(DocumentType.DELIVERY_NOTE);
    receipt.setSourceWarehouse(target);
    assertEquals(target, service.defaultCorrectionWarehouse(receipt));

    final Warehouse transit = Warehouse.builder().id(6L).build();
    final Warehouse sourceWarehouse = Warehouse.builder().id(7L).build();
    final Document transferDelivery = new Document();
    transferDelivery.setType(DocumentType.TRANSFER_DELIVERY);
    transferDelivery.setSourceWarehouse(sourceWarehouse);
    transferDelivery.setTransitWarehouse(transit);
    final Map<StockKey, BigDecimal> dispatchDeltas =
        service.correctionStockDeltas(transferDelivery, source, requestLine, COMPANY_ID);
    assertEquals(new BigDecimal("-2"), dispatchDeltas.get(new StockKey(7L, 5L)));
    assertEquals(new BigDecimal("2"), dispatchDeltas.get(new StockKey(6L, 5L)));

    final Document transferReceipt = new Document();
    transferReceipt.setType(DocumentType.TRANSFER_RECEIPT);
    transferReceipt.setTargetWarehouse(target);
    transferReceipt.setTransitWarehouse(transit);
    final Map<StockKey, BigDecimal> receiptDeltas =
        service.correctionStockDeltas(transferReceipt, source, requestLine, COMPANY_ID);
    assertEquals(new BigDecimal("-2"), receiptDeltas.get(new StockKey(6L, 5L)));
    assertEquals(new BigDecimal("2"), receiptDeltas.get(new StockKey(4L, 5L)));
  }

  @Test
  void validatesRolesReasonsAndCorrectionOverrides() {
    service.requireRole(
        User.builder().role(UserRole.MANAGER).build(), UserRole.ADMIN, UserRole.MANAGER);
    assertThrows(ApiException.class, () -> service.requireRole(null, UserRole.ADMIN));
    assertThrows(ApiException.class, () -> service.requireRole(new User(), UserRole.ADMIN));
    service.requireReason("valid");
    assertThrows(ApiException.class, () -> service.requireReason(null));

    final Warehouse original = Warehouse.builder().id(4L).build();
    final Warehouse override = Warehouse.builder().id(8L).build();
    final Document document = new Document();
    document.setType(DocumentType.GOODS_RECEIPT);
    document.setTargetWarehouse(original);
    final Product oldProduct = Product.builder().id(5L).build();
    final DocumentLine source = new DocumentLine();
    source.setProduct(oldProduct);
    source.setQuantity(new BigDecimal("3"));
    final var overrideLine =
        new WarehouseOperationRequest.WarehouseOperationLine(
            9L, null, null, new BigDecimal("5"), 11L, 8L, null, null, null, null);
    when(warehouseService.findEntityByIdAndCompanyId(8L, COMPANY_ID)).thenReturn(override);

    final Map<StockKey, BigDecimal> deltas =
        service.correctionStockDeltas(document, source, overrideLine, COMPANY_ID);

    assertEquals(new BigDecimal("-3"), deltas.get(new StockKey(4L, 5L)));
    assertEquals(new BigDecimal("5"), deltas.get(new StockKey(8L, 11L)));
  }

  @Test
  void addsOnlyNonzeroEffectsAndCreatesQuarantineWarehouseForCurrentCompany() {
    final Map<StockKey, BigDecimal> effects = new java.util.HashMap<>();
    final Warehouse warehouse = Warehouse.builder().id(3L).build();
    service.addEffect(effects, warehouse, 7L, BigDecimal.ONE);
    service.addEffect(effects, warehouse, 7L, BigDecimal.ZERO);
    service.addEffect(effects, null, 7L, BigDecimal.ONE);
    when(companyProvider.getCurrentCompany()).thenReturn(company);
    when(warehouseService.ensureQuarantineWarehouse(company)).thenReturn(warehouse);

    assertEquals(Map.of(new StockKey(3L, 7L), BigDecimal.ONE), effects);
    assertEquals(warehouse, service.quarantineWarehouse(COMPANY_ID));
    assertThrows(ApiException.class, () -> service.quarantineWarehouse(COMPANY_ID + 1));
  }

  private WarehouseOperationRequest.WarehouseOperationLine lineRequest(final Long sourceLineId) {
    return new WarehouseOperationRequest.WarehouseOperationLine(
        sourceLineId, null, null, null, null, null, null, null, null, null);
  }
}
