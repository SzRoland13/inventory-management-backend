package dev.roland.inventory_management_backend.features.document.facade.workflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.roland.inventory_management_backend.features.company.Company;
import dev.roland.inventory_management_backend.features.document.Document;
import dev.roland.inventory_management_backend.features.document.dto.WarehouseOperationPreview;
import dev.roland.inventory_management_backend.features.document.dto.WarehouseOperationRequest;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentType;
import dev.roland.inventory_management_backend.features.document.enumeration.WarehouseOperationType;
import dev.roland.inventory_management_backend.features.document.message.DocumentApiMessageKey;
import dev.roland.inventory_management_backend.features.document_line.DocumentLine;
import dev.roland.inventory_management_backend.features.product.Product;
import dev.roland.inventory_management_backend.features.stock_balance.StockBalance;
import dev.roland.inventory_management_backend.features.stock_balance.service.StockBalanceService;
import dev.roland.inventory_management_backend.features.warehouse.Warehouse;

@ExtendWith(MockitoExtension.class)
class WarehouseTransferPreviewServiceTest {
  private static final Long COMPANY_ID = 9L;
  private static final Long DOCUMENT_ID = 12L;

  @Mock private StockBalanceService balanceService;
  @Mock private WarehouseOperationSupportService supportService;

  private WarehouseTransferPreviewService service;
  private Company company;
  private Warehouse sourceWarehouse;
  private Product product;
  private Document document;
  private DocumentLine sourceLine;

  @BeforeEach
  void setUp() {
    service = new WarehouseTransferPreviewService(balanceService, supportService);
    company = Company.builder().id(COMPANY_ID).build();
    sourceWarehouse = Warehouse.builder().id(4L).company(company).build();
    product = Product.builder().id(5L).build();
    document = new Document();
    document.setId(DOCUMENT_ID);
    document.setType(DocumentType.TRANSFER_REQUEST);
    document.setSourceWarehouse(sourceWarehouse);
    sourceLine = new DocumentLine();
    sourceLine.setId(6L);
    sourceLine.setDocument(document);
    sourceLine.setProduct(product);
    sourceLine.setQuantity(new BigDecimal("5"));
    sourceLine.setFulfilledQuantity(new BigDecimal("1"));
  }

  @Test
  void validDispatchUsesAvailableStockAndRemainingQuantity() {
    final WarehouseOperationRequest request = request(WarehouseOperationType.DISPATCH, 2L);
    when(supportService.line(6L, COMPANY_ID)).thenReturn(sourceLine);
    when(balanceService.findByWarehouseAndProduct(sourceWarehouse, product))
        .thenReturn(Optional.of(StockBalance.builder().quantity(new BigDecimal("4")).build()));
    final List<WarehouseOperationPreview.Line> lines = new ArrayList<>();
    final List<String> blockers = new ArrayList<>();

    service.appendLines(
        document, company, WarehouseOperationType.TRANSFER_PREVIEW, request, lines, blockers);

    assertTrue(blockers.isEmpty());
    assertEquals(new BigDecimal("4"), lines.getFirst().sourceQuantity());
    assertEquals(new BigDecimal("-2"), lines.getFirst().quantityDelta());
  }

  @Test
  void validReceiptUsesTransitQuantity() {
    sourceLine.setInTransitQuantity(new BigDecimal("3"));
    final WarehouseOperationRequest request = request(WarehouseOperationType.RECEIPT, 2L);
    when(supportService.line(6L, COMPANY_ID)).thenReturn(sourceLine);
    when(balanceService.findByWarehouseAndProduct(sourceWarehouse, product))
        .thenReturn(Optional.empty());
    final List<WarehouseOperationPreview.Line> lines = new ArrayList<>();
    final List<String> blockers = new ArrayList<>();

    service.appendLines(
        document, company, WarehouseOperationType.TRANSFER_PREVIEW, request, lines, blockers);

    assertTrue(blockers.isEmpty());
    assertEquals(new BigDecimal("3"), lines.getFirst().transitQuantity());
    assertEquals(new BigDecimal("1"), lines.getFirst().resultingQuantity());
  }

  @Test
  void missingOrCrossDocumentLinesAddBlockers() {
    final WarehouseOperationRequest request = request(WarehouseOperationType.LOSS, 8L);
    when(supportService.line(6L, COMPANY_ID)).thenReturn(sourceLine);
    final List<WarehouseOperationPreview.Line> lines = new ArrayList<>();
    final List<String> blockers = new ArrayList<>();

    service.appendLines(
        null, company, WarehouseOperationType.TRANSFER_RESOLUTION, request, lines, blockers);

    assertEquals(List.of(DocumentApiMessageKey.DOCUMENT_INVALID_LINES.getKey()), blockers);
    assertEquals(6L, lines.getFirst().sourceLineId());
  }

  private WarehouseOperationRequest request(
      final WarehouseOperationType operation, final long quantity) {
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
        LocalDate.now(),
        null,
        null,
        null,
        null,
        operation,
        null,
        "reason",
        true,
        false,
        List.of(
            new WarehouseOperationRequest.WarehouseOperationLine(
                6L, null, BigDecimal.valueOf(quantity), null, null, null, null, null, null, null)));
  }
}
