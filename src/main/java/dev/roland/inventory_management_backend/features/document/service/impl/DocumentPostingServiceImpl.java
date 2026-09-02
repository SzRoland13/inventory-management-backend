package dev.roland.inventory_management_backend.features.document.service.impl;

import java.time.LocalDateTime;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.features.document.Document;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentStatus;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentType;
import dev.roland.inventory_management_backend.features.document.service.DocumentPostingService;
import dev.roland.inventory_management_backend.features.document.service.DocumentService;
import dev.roland.inventory_management_backend.features.document_line.DocumentLine;
import dev.roland.inventory_management_backend.features.stock_movement.enumeration.StockMovementType;
import dev.roland.inventory_management_backend.features.stock_movement.service.StockMovementService;
import dev.roland.inventory_management_backend.features.warehouse.Warehouse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DocumentPostingServiceImpl implements DocumentPostingService {
  private final DocumentService documentService;
  private final StockMovementService stockMovementService;

  /** {@inheritDoc} */
  @Transactional
  @Override
  public Document complete(Document document) {
    if (document.getStatus() == DocumentStatus.COMPLETED) {
      return document;
    }

    if (document.getLines() != null) {
      for (DocumentLine line : document.getLines()) {
        postLine(document, line);
      }
    }

    document.setStatus(DocumentStatus.COMPLETED);
    document.setCompletedAt(LocalDateTime.now());
    return documentService.save(document);
  }

  private void postLine(Document document, DocumentLine line) {
    DocumentType type = document.getType();
    if (type == DocumentType.GOODS_RECEIPT) {
      post(
          line,
          document.getTargetWarehouse(),
          line.getQuantity(),
          StockMovementType.PURCHASE_INBOUND);
    } else if (type == DocumentType.DELIVERY_NOTE) {
      post(
          line,
          document.getSourceWarehouse(),
          line.getQuantity().negate(),
          StockMovementType.SALE_OUTBOUND);
    } else if (type == DocumentType.TRANSFER_DELIVERY) {
      post(
          line,
          document.getSourceWarehouse(),
          line.getQuantity().negate(),
          StockMovementType.TRANSFER_OUT);
    } else if (type == DocumentType.TRANSFER_RECEIPT) {
      post(line, document.getTargetWarehouse(), line.getQuantity(), StockMovementType.TRANSFER_IN);
    } else if (type == DocumentType.STOCK_ADJUSTMENT) {
      StockMovementType movementType =
          line.getQuantity().signum() >= 0
              ? StockMovementType.ADJUSTMENT_INCREASE
              : StockMovementType.ADJUSTMENT_DECREASE;
      Warehouse warehouse =
          document.getTargetWarehouse() != null
              ? document.getTargetWarehouse()
              : document.getSourceWarehouse();
      post(line, warehouse, line.getQuantity(), movementType);
    }
  }

  private void post(
      DocumentLine line,
      Warehouse warehouse,
      java.math.BigDecimal quantityChange,
      StockMovementType movementType) {
    if (warehouse == null) {
      throw new IllegalStateException("Stock-affecting documents require a warehouse");
    }

    stockMovementService.postMovement(
        line, warehouse, quantityChange, movementType, line.getDocument().getCreatedByUser());
  }
}
