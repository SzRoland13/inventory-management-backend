package dev.roland.inventory_management_backend.features.stock_movement.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.common.exception.ApiException;
import dev.roland.inventory_management_backend.common.exception.NotFoundException;
import dev.roland.inventory_management_backend.common.message.MessageKey;
import dev.roland.inventory_management_backend.common.message.NotFoundMessageKey;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentType;
import dev.roland.inventory_management_backend.features.document_line.DocumentLine;
import dev.roland.inventory_management_backend.features.stock_balance.StockBalance;
import dev.roland.inventory_management_backend.features.stock_balance.repository.StockBalanceRepository;
import dev.roland.inventory_management_backend.features.stock_movement.StockMovement;
import dev.roland.inventory_management_backend.features.stock_movement.dto.StockMovementQueryRow;
import dev.roland.inventory_management_backend.features.stock_movement.enumeration.StockMovementType;
import dev.roland.inventory_management_backend.features.stock_movement.repository.StockMovementRepository;
import dev.roland.inventory_management_backend.features.stock_movement.service.StockMovementService;
import dev.roland.inventory_management_backend.features.user.User;
import dev.roland.inventory_management_backend.features.warehouse.Warehouse;
import dev.roland.inventory_management_backend.features.warehouse.enumeration.WarehouseStatus;
import dev.roland.inventory_management_backend.features.warehouse.enumeration.WarehouseType;
import dev.roland.inventory_management_backend.features.warehouse.message.WarehouseMessageKey;
import dev.roland.inventory_management_backend.features.warehouse.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;

/** Implements the stock movement service operations. */
@Service
@RequiredArgsConstructor
public class StockMovementServiceImpl implements StockMovementService {
  private final StockMovementRepository stockMovementRepository;
  private final StockBalanceRepository stockBalanceRepository;
  private final WarehouseRepository warehouseRepository;

  /** {@inheritDoc} */
  @Override
  public JpaRepository<StockMovement, Long> getRepository() {
    return stockMovementRepository;
  }

  /** {@inheritDoc} */
  @Override
  public MessageKey getNotFoundMessageKey() {
    return NotFoundMessageKey.STOCK_MOVEMENT;
  }

  @Override
  public Page<StockMovementQueryRow> findCompanyMovements(
      final Long companyId,
      final Long productId,
      final Long warehouseId,
      final LocalDateTime startDate,
      final LocalDateTime endDate,
      final Pageable pageable) {
    return stockMovementRepository.findCompanyMovements(
        companyId, productId, warehouseId, startDate, endDate, pageable);
  }

  /**
   * {@inheritDoc}
   *
   * @param documentLine document line supplied to this method
   * @param warehouse warehouse supplied to this method
   * @param quantityChange quantity change supplied to this method
   * @param movementType movement type supplied to this method
   * @param createdByUser created by user supplied to this method
   * @return post movement result
   */
  @Transactional
  @Override
  public StockMovement postMovement(
      final DocumentLine documentLine,
      final Warehouse warehouse,
      final BigDecimal quantityChange,
      final StockMovementType movementType,
      final User createdByUser) {
    final Warehouse lockedWarehouse =
        warehouseRepository
            .findByIdForUpdate(warehouse.getId())
            .orElseThrow(() -> new NotFoundException(NotFoundMessageKey.WAREHOUSE));
    validateWarehousePosting(documentLine, lockedWarehouse, quantityChange, movementType);
    final StockBalance balance =
        stockBalanceRepository
            .findByWarehouseAndProduct(lockedWarehouse, documentLine.getProduct())
            .orElseGet(
                () ->
                    StockBalance.builder()
                        .warehouse(lockedWarehouse)
                        .product(documentLine.getProduct())
                        .quantity(BigDecimal.ZERO)
                        .build());

    balance.setQuantity(balance.getQuantity().add(quantityChange));
    stockBalanceRepository.save(balance);

    final StockMovement movement =
        StockMovement.builder()
            .document(documentLine.getDocument())
            .documentLine(documentLine)
            .warehouse(lockedWarehouse)
            .product(documentLine.getProduct())
            .quantityChange(quantityChange)
            .movementType(movementType)
            .createdByUser(createdByUser)
            .build();

    return stockMovementRepository.save(movement);
  }

  private void validateWarehousePosting(
      final DocumentLine line,
      final Warehouse warehouse,
      final BigDecimal quantityChange,
      final StockMovementType movementType) {
    if (warehouse.getStatus() == WarehouseStatus.ARCHIVED) {
      throw new ApiException(WarehouseMessageKey.WAREHOUSE_ARCHIVED_FOR_POSTING);
    }

    if (warehouse.getCompany() != null
        && line.getProduct().getCompany() != null
        && !warehouse.getCompany().getId().equals(line.getProduct().getCompany().getId())) {
      throw new ApiException(WarehouseMessageKey.WAREHOUSE_PRODUCT_COMPANY_MISMATCH);
    }

    if (warehouse.getType() != WarehouseType.TRANSIT) {
      return;
    }

    final DocumentType documentType = line.getDocument().getType();
    final boolean validTransitReceipt =
        documentType == DocumentType.TRANSFER_RECEIPT
            && movementType == StockMovementType.TRANSFER_IN
            && quantityChange.signum() > 0;
    final boolean validTransitDelivery =
        documentType == DocumentType.TRANSFER_DELIVERY
            && movementType == StockMovementType.TRANSFER_OUT
            && quantityChange.signum() < 0;
    if (!validTransitReceipt && !validTransitDelivery) {
      throw new ApiException(WarehouseMessageKey.INVALID_TRANSIT_DOCUMENT);
    }
  }

  @Override
  public boolean existsByProductId(final Long id) {
    return stockMovementRepository.existsByProductId(id);
  }
}
