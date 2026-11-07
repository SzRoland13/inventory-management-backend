package dev.roland.inventory_management_backend.features.document.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.common.exception.ApiException;
import dev.roland.inventory_management_backend.features.document.Document;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentStatus;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentType;
import dev.roland.inventory_management_backend.features.document.message.DocumentApiMessageKey;
import dev.roland.inventory_management_backend.features.document.service.DocumentPostingService;
import dev.roland.inventory_management_backend.features.document.service.DocumentService;
import dev.roland.inventory_management_backend.features.document_line.DocumentLine;
import dev.roland.inventory_management_backend.features.stock_movement.enumeration.StockMovementType;
import dev.roland.inventory_management_backend.features.stock_movement.service.StockMovementService;
import dev.roland.inventory_management_backend.features.warehouse.Warehouse;
import lombok.RequiredArgsConstructor;

/** Implements the inventory document service operations. */
@Service
@RequiredArgsConstructor
public class DocumentPostingServiceImpl implements DocumentPostingService {
  private final DocumentService documentService;
  private final StockMovementService stockMovementService;

  /**
   * {@inheritDoc}
   *
   * @param document document supplied to this method
   * @return complete result
   */
  @Transactional
  @Override
  public Document complete(final Document document) {
    return complete(document, null);
  }

  /** {@inheritDoc} */
  @Transactional
  @Override
  public Document complete(final Document document, final LocalDateTime effectiveAt) {
    Document result = document;
    if (document.getStatus() != DocumentStatus.COMPLETED) {
      if (document.getLines() != null) {
        for (DocumentLine line : document.getLines()) {
          postLine(document, line, effectiveAt);
        }
      }

      document.setStatus(DocumentStatus.COMPLETED);
      document.setCompletedAt(LocalDateTime.now());
      result = documentService.save(document);
    }
    return result;
  }

  private void postLine(
      final Document document, final DocumentLine line, final LocalDateTime effectiveAt) {
    final DocumentType type = document.getType();
    final BigDecimal stockQuantity =
        line.getQuantity().multiply(line.getConversionFactorSnapshot());
    if (type == DocumentType.GOODS_RECEIPT) {
      post(
          line,
          document.getTargetWarehouse(),
          stockQuantity,
          StockMovementType.PURCHASE_INBOUND,
          effectiveAt);
    } else if (type == DocumentType.DELIVERY_NOTE) {
      post(
          line,
          document.getSourceWarehouse(),
          stockQuantity.negate(),
          StockMovementType.SALE_OUTBOUND,
          effectiveAt);
    } else if (type == DocumentType.TRANSFER_DELIVERY) {
      post(
          line,
          document.getSourceWarehouse(),
          stockQuantity.negate(),
          StockMovementType.TRANSFER_OUT,
          effectiveAt);
    } else if (type == DocumentType.TRANSFER_RECEIPT) {
      post(
          line,
          document.getTargetWarehouse(),
          stockQuantity,
          StockMovementType.TRANSFER_IN,
          effectiveAt);
    } else if (type == DocumentType.STOCK_ADJUSTMENT) {
      final StockMovementType movementType =
          stockQuantity.signum() >= 0
              ? StockMovementType.ADJUSTMENT_INCREASE
              : StockMovementType.ADJUSTMENT_DECREASE;
      final Warehouse warehouse =
          document.getTargetWarehouse() != null
              ? document.getTargetWarehouse()
              : document.getSourceWarehouse();
      post(line, warehouse, stockQuantity, movementType, effectiveAt);
    }
  }

  private void post(
      final DocumentLine line,
      final Warehouse warehouse,
      final BigDecimal quantityChange,
      final StockMovementType movementType,
      final LocalDateTime effectiveAt) {
    if (warehouse == null) {
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_WAREHOUSE_REQUIRED);
    }

    if (effectiveAt == null) {
      stockMovementService.postMovement(
          line, warehouse, quantityChange, movementType, line.getDocument().getCreatedByUser());
    } else {
      stockMovementService.postMovement(
          line,
          warehouse,
          quantityChange,
          movementType,
          line.getDocument().getCreatedByUser(),
          effectiveAt);
    }
  }
}
