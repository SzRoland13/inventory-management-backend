package dev.roland.inventory_management_backend.features.stock_movement.service.impl;

import java.math.BigDecimal;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.common.message.MessageKey;
import dev.roland.inventory_management_backend.common.message.NotFoundMessageKey;
import dev.roland.inventory_management_backend.features.document_line.DocumentLine;
import dev.roland.inventory_management_backend.features.stock_balance.StockBalance;
import dev.roland.inventory_management_backend.features.stock_balance.repository.StockBalanceRepository;
import dev.roland.inventory_management_backend.features.stock_movement.StockMovement;
import dev.roland.inventory_management_backend.features.stock_movement.enumeration.StockMovementType;
import dev.roland.inventory_management_backend.features.stock_movement.repository.StockMovementRepository;
import dev.roland.inventory_management_backend.features.stock_movement.service.StockMovementService;
import dev.roland.inventory_management_backend.features.user.User;
import dev.roland.inventory_management_backend.features.warehouse.Warehouse;
import lombok.RequiredArgsConstructor;

/** Implements the stock movement service operations. */
@Service
@RequiredArgsConstructor
public class StockMovementServiceImpl implements StockMovementService {
  private final StockMovementRepository stockMovementRepository;
  private final StockBalanceRepository stockBalanceRepository;

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
    final StockBalance balance =
        stockBalanceRepository
            .findByWarehouseAndProduct(warehouse, documentLine.getProduct())
            .orElseGet(
                () ->
                    StockBalance.builder()
                        .warehouse(warehouse)
                        .product(documentLine.getProduct())
                        .quantity(BigDecimal.ZERO)
                        .build());

    balance.setQuantity(balance.getQuantity().add(quantityChange));
    stockBalanceRepository.save(balance);

    final StockMovement movement =
        StockMovement.builder()
            .document(documentLine.getDocument())
            .documentLine(documentLine)
            .warehouse(warehouse)
            .product(documentLine.getProduct())
            .quantityChange(quantityChange)
            .movementType(movementType)
            .createdByUser(createdByUser)
            .build();

    return stockMovementRepository.save(movement);
  }
}
