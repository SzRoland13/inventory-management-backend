package dev.roland.inventory_management_backend.features.stock_movement.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.roland.inventory_management_backend.common.exception.ApiException;
import dev.roland.inventory_management_backend.features.company.Company;
import dev.roland.inventory_management_backend.features.document.Document;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentType;
import dev.roland.inventory_management_backend.features.document_line.DocumentLine;
import dev.roland.inventory_management_backend.features.product.Product;
import dev.roland.inventory_management_backend.features.stock_balance.StockBalance;
import dev.roland.inventory_management_backend.features.stock_balance.repository.StockBalanceRepository;
import dev.roland.inventory_management_backend.features.stock_movement.StockMovement;
import dev.roland.inventory_management_backend.features.stock_movement.enumeration.StockMovementType;
import dev.roland.inventory_management_backend.features.stock_movement.repository.StockMovementRepository;
import dev.roland.inventory_management_backend.features.user.User;
import dev.roland.inventory_management_backend.features.warehouse.Warehouse;
import dev.roland.inventory_management_backend.features.warehouse.enumeration.WarehouseStatus;
import dev.roland.inventory_management_backend.features.warehouse.enumeration.WarehouseType;
import dev.roland.inventory_management_backend.features.warehouse.message.WarehouseMessageKey;
import dev.roland.inventory_management_backend.features.warehouse.repository.WarehouseRepository;

@ExtendWith(MockitoExtension.class)
class StockMovementServiceImplTest {

  @Mock private StockMovementRepository movementRepository;
  @Mock private StockBalanceRepository balanceRepository;
  @Mock private WarehouseRepository warehouseRepository;
  @Mock private Warehouse warehouse;
  @Mock private Product product;

  private StockMovementServiceImpl service;

  @BeforeEach
  void setUp() {
    service =
        new StockMovementServiceImpl(movementRepository, balanceRepository, warehouseRepository);
    when(warehouseRepository.findByIdForUpdate(warehouse.getId()))
        .thenReturn(Optional.of(warehouse));
  }

  @Test
  void createsBalanceAndMovementWhenProductHasNoExistingBalance() {
    final User user = User.builder().id(3L).build();
    final Document document = Document.builder().type(DocumentType.GOODS_RECEIPT).build();
    final DocumentLine line = DocumentLine.builder().document(document).product(product).build();
    final StockMovement movement =
        StockMovement.builder().quantityChange(new BigDecimal("5")).build();
    when(balanceRepository.findByWarehouseAndProduct(warehouse, product))
        .thenReturn(Optional.empty());
    when(balanceRepository.save(any(StockBalance.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(movementRepository.save(any(StockMovement.class))).thenReturn(movement);

    final StockMovement result =
        service.postMovement(
            line, warehouse, new BigDecimal("5"), StockMovementType.PURCHASE_INBOUND, user);

    assertSame(movement, result);
    assertEquals(new BigDecimal("5"), result.getQuantityChange());
  }

  @Test
  void updatesExistingBalanceAndAssociatesMovementWithDocumentLine() {
    final Document document = Document.builder().type(DocumentType.DELIVERY_NOTE).build();
    final DocumentLine line = DocumentLine.builder().document(document).product(product).build();
    final StockBalance balance =
        StockBalance.builder()
            .warehouse(warehouse)
            .product(product)
            .quantity(new BigDecimal("10"))
            .build();
    when(balanceRepository.findByWarehouseAndProduct(warehouse, product))
        .thenReturn(Optional.of(balance));
    when(balanceRepository.save(balance)).thenReturn(balance);
    when(movementRepository.save(any(StockMovement.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    final StockMovement movement =
        service.postMovement(
            line, warehouse, new BigDecimal("-3"), StockMovementType.SALE_OUTBOUND, null);

    assertEquals(new BigDecimal("7"), balance.getQuantity());
    assertSame(line, movement.getDocumentLine());
    assertSame(document, movement.getDocument());
    assertEquals(StockMovementType.SALE_OUTBOUND, movement.getMovementType());
  }

  @Test
  void rejectsMovementsAgainstArchivedWarehouses() {
    final Document document = Document.builder().type(DocumentType.GOODS_RECEIPT).build();
    final DocumentLine line = DocumentLine.builder().document(document).product(product).build();
    when(warehouse.getStatus()).thenReturn(WarehouseStatus.ARCHIVED);

    final ApiException exception =
        assertThrows(
            ApiException.class,
            () ->
                service.postMovement(
                    line, warehouse, BigDecimal.ONE, StockMovementType.PURCHASE_INBOUND, null));

    assertEquals(WarehouseMessageKey.WAREHOUSE_ARCHIVED_FOR_POSTING, exception.getMessageKey());
  }

  @Test
  void transitWarehousesRejectNonTransferReceipts() {
    final Document document = Document.builder().type(DocumentType.GOODS_RECEIPT).build();
    final DocumentLine line = DocumentLine.builder().document(document).product(product).build();
    when(warehouse.getType()).thenReturn(WarehouseType.TRANSIT);

    final ApiException exception =
        assertThrows(
            ApiException.class,
            () ->
                service.postMovement(
                    line, warehouse, BigDecimal.ONE, StockMovementType.PURCHASE_INBOUND, null));

    assertEquals(WarehouseMessageKey.INVALID_TRANSIT_DOCUMENT, exception.getMessageKey());
  }

  @Test
  void transitWarehousesAcceptTransferReceipts() {
    final Document document = Document.builder().type(DocumentType.TRANSFER_RECEIPT).build();
    final DocumentLine line = DocumentLine.builder().document(document).product(product).build();
    final StockMovement movement = StockMovement.builder().quantityChange(BigDecimal.ONE).build();
    when(warehouse.getType()).thenReturn(WarehouseType.TRANSIT);
    when(balanceRepository.findByWarehouseAndProduct(warehouse, product))
        .thenReturn(Optional.empty());
    when(balanceRepository.save(any(StockBalance.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(movementRepository.save(any(StockMovement.class))).thenReturn(movement);

    final StockMovement result =
        service.postMovement(line, warehouse, BigDecimal.ONE, StockMovementType.TRANSFER_IN, null);

    assertSame(movement, result);
  }

  @Test
  void rejectsStockPostingsAcrossCompanies() {
    final Company warehouseCompany = Company.builder().id(1L).build();
    final Company productCompany = Company.builder().id(2L).build();
    final Document document = Document.builder().type(DocumentType.GOODS_RECEIPT).build();
    final DocumentLine line = DocumentLine.builder().document(document).product(product).build();
    when(warehouse.getCompany()).thenReturn(warehouseCompany);
    when(product.getCompany()).thenReturn(productCompany);

    final ApiException exception =
        assertThrows(
            ApiException.class,
            () ->
                service.postMovement(
                    line, warehouse, BigDecimal.ONE, StockMovementType.PURCHASE_INBOUND, null));

    assertEquals(WarehouseMessageKey.WAREHOUSE_PRODUCT_COMPANY_MISMATCH, exception.getMessageKey());
  }
}
