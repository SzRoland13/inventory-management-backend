package dev.roland.inventory_management_backend.service.implementation;

import java.math.BigDecimal;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.enums.StockMovementType;
import dev.roland.inventory_management_backend.message_key.MessageKey;
import dev.roland.inventory_management_backend.message_key.NotFoundMessageKey;
import dev.roland.inventory_management_backend.model.DocumentLine;
import dev.roland.inventory_management_backend.model.StockBalance;
import dev.roland.inventory_management_backend.model.StockMovement;
import dev.roland.inventory_management_backend.model.User;
import dev.roland.inventory_management_backend.model.Warehouse;
import dev.roland.inventory_management_backend.repository.StockBalanceRepository;
import dev.roland.inventory_management_backend.repository.StockMovementRepository;
import dev.roland.inventory_management_backend.service.StockMovementService;
import lombok.RequiredArgsConstructor;

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

  /** {@inheritDoc} */
  @Transactional
  @Override
  public StockMovement postMovement(
      DocumentLine documentLine,
      Warehouse warehouse,
      BigDecimal quantityChange,
      StockMovementType movementType,
      User createdByUser) {
    StockBalance balance =
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

    StockMovement movement =
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
