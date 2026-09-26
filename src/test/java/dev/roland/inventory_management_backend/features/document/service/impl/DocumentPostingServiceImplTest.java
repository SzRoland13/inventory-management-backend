package dev.roland.inventory_management_backend.features.document.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.roland.inventory_management_backend.features.document.Document;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentStatus;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentType;
import dev.roland.inventory_management_backend.features.document.service.DocumentService;
import dev.roland.inventory_management_backend.features.document_line.DocumentLine;
import dev.roland.inventory_management_backend.features.stock_movement.enumeration.StockMovementType;
import dev.roland.inventory_management_backend.features.stock_movement.service.StockMovementService;
import dev.roland.inventory_management_backend.features.warehouse.Warehouse;

@ExtendWith(MockitoExtension.class)
class DocumentPostingServiceImplTest {

  @Mock private DocumentService documentService;
  @Mock private StockMovementService stockMovementService;
  @Mock private Warehouse warehouse;

  private DocumentPostingServiceImpl service;

  @BeforeEach
  void setUp() {
    service = new DocumentPostingServiceImpl(documentService, stockMovementService);
  }

  @Test
  void postsReceiptAndSavesCompletedDocument() {
    final Document document = document(DocumentType.GOODS_RECEIPT, warehouse, null, "5.00");
    when(documentService.save(document)).thenReturn(document);

    final Document result = service.complete(document);

    assertSame(document, result);
    assertEquals(DocumentStatus.COMPLETED, document.getStatus());
    assertNotNull(document.getCompletedAt());
    verify(stockMovementService)
        .postMovement(
            document.getLines().get(0),
            warehouse,
            new BigDecimal("5.00"),
            StockMovementType.PURCHASE_INBOUND,
            null);
    verify(documentService).save(document);
  }

  @Test
  void postsOutboundAndTransferMovementsWithNegativeQuantities() {
    final Document delivery = document(DocumentType.DELIVERY_NOTE, null, warehouse, "4");
    service.complete(delivery);
    verify(stockMovementService)
        .postMovement(
            delivery.getLines().get(0),
            warehouse,
            new BigDecimal("-4"),
            StockMovementType.SALE_OUTBOUND,
            null);

    final Document transfer = document(DocumentType.TRANSFER_DELIVERY, null, warehouse, "3");
    service.complete(transfer);
    verify(stockMovementService)
        .postMovement(
            transfer.getLines().get(0),
            warehouse,
            new BigDecimal("-3"),
            StockMovementType.TRANSFER_OUT,
            null);
  }

  @Test
  void postsTransferReceiptAndStockAdjustmentBasedOnSignAndAvailableWarehouse() {
    final Document receipt = document(DocumentType.TRANSFER_RECEIPT, warehouse, null, "2");
    service.complete(receipt);
    verify(stockMovementService)
        .postMovement(
            receipt.getLines().get(0),
            warehouse,
            new BigDecimal("2"),
            StockMovementType.TRANSFER_IN,
            null);

    final Document positiveAdjustment =
        document(DocumentType.STOCK_ADJUSTMENT, null, warehouse, "0");
    service.complete(positiveAdjustment);
    verify(stockMovementService)
        .postMovement(
            positiveAdjustment.getLines().get(0),
            warehouse,
            BigDecimal.ZERO,
            StockMovementType.ADJUSTMENT_INCREASE,
            null);

    final Document negativeAdjustment =
        document(DocumentType.STOCK_ADJUSTMENT, null, warehouse, "-2");
    service.complete(negativeAdjustment);
    verify(stockMovementService)
        .postMovement(
            negativeAdjustment.getLines().get(0),
            warehouse,
            new BigDecimal("-2"),
            StockMovementType.ADJUSTMENT_DECREASE,
            null);
  }

  @Test
  void completedDocumentsAreNotPostedOrSavedAgain() {
    final Document document =
        Document.builder()
            .status(DocumentStatus.COMPLETED)
            .type(DocumentType.GOODS_RECEIPT)
            .build();

    assertSame(document, service.complete(document));

    verify(stockMovementService, never())
        .postMovement(
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any());
    verify(documentService, never()).save(document);
  }

  @Test
  void missingWarehouseForStockMovementIsRejected() {
    final Document document = document(DocumentType.GOODS_RECEIPT, null, null, "1");

    assertThrows(IllegalStateException.class, () -> service.complete(document));
    verify(documentService, never()).save(document);
  }

  @Test
  void nonStockDocumentCompletesWithoutMovementAndNullLinesAreAllowed() {
    final Document document =
        Document.builder().type(DocumentType.PURCHASE_ORDER).status(DocumentStatus.DRAFT).build();
    when(documentService.save(document)).thenReturn(document);

    service.complete(document);

    assertEquals(DocumentStatus.COMPLETED, document.getStatus());
    verify(stockMovementService, never())
        .postMovement(
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any());
    verify(documentService).save(document);
  }

  private Document document(
      final DocumentType type,
      final Warehouse target,
      final Warehouse source,
      final String quantity) {
    final Document document =
        Document.builder()
            .type(type)
            .status(DocumentStatus.DRAFT)
            .targetWarehouse(target)
            .sourceWarehouse(source)
            .build();
    final DocumentLine line =
        DocumentLine.builder().document(document).quantity(new BigDecimal(quantity)).build();
    document.setLines(List.of(line));
    return document;
  }
}
