package dev.roland.inventory_management_backend.features.document.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import dev.roland.inventory_management_backend.features.document.enumeration.ReturnDirection;
import dev.roland.inventory_management_backend.features.document.enumeration.ReturnDisposition;
import dev.roland.inventory_management_backend.features.document.enumeration.WarehouseOperationType;

/** Request body shared by warehouse document operations. */
public record WarehouseOperationRequest(
    Long documentId,
    Long sourceDocumentId,
    Long sourceWarehouseId,
    Long destinationWarehouseId,
    Long transitWarehouseId,
    Long warehouseId,
    Long targetWarehouseId,
    Long returnDocumentId,
    Long originalDeliveryId,
    LocalDate date,
    LocalDateTime countedAt,
    String version,
    String idempotencyKey,
    String calculationVersion,
    WarehouseOperationType operation,
    ReturnDirection direction,
    String reason,
    Boolean confirmed,
    Boolean nonBillable,
    List<WarehouseOperationLine> lines) {
  public record WarehouseOperationLine(
      Long sourceLineId,
      Long productId,
      BigDecimal quantity,
      BigDecimal correctedQuantity,
      Long correctedProductId,
      Long correctedWarehouseId,
      BigDecimal countedQuantity,
      String varianceReason,
      ReturnDisposition disposition,
      String reason) {}
}
