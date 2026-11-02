package dev.roland.inventory_management_backend.features.document.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import dev.roland.inventory_management_backend.features.document.enumeration.ReturnDirection;
import dev.roland.inventory_management_backend.features.document.enumeration.ReturnDisposition;
import dev.roland.inventory_management_backend.features.document.enumeration.WarehouseOperationType;

/**
 * Request body shared by warehouse document operations.
 *
 * @param calculationVersion the calculationVersion value
 * @param confirmed the confirmed value
 * @param countedAt the countedAt value
 * @param date the date value
 * @param destinationWarehouseId the destinationWarehouseId value
 * @param direction the direction value
 * @param documentId the documentId value
 * @param idempotencyKey the idempotencyKey value
 * @param lines the lines value
 * @param nonBillable the nonBillable value
 * @param operation the operation value
 * @param originalDeliveryId the originalDeliveryId value
 * @param reason the reason value
 * @param returnDocumentId the returnDocumentId value
 * @param sourceDocumentId the sourceDocumentId value
 * @param sourceWarehouseId the sourceWarehouseId value
 * @param targetWarehouseId the targetWarehouseId value
 * @param transitWarehouseId the transitWarehouseId value
 * @param version the version value
 * @param warehouseId the warehouseId value
 */
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
  /** Creates a request with an immutable copy of its line list. */
  public WarehouseOperationRequest {
    lines = immutableCopy(lines);
  }

  @Override
  public List<WarehouseOperationLine> lines() {
    return immutableCopy(lines);
  }

  private static <T> List<T> immutableCopy(final List<T> values) {
    return values == null ? null : List.copyOf(values);
  }

  /**
   * Request values for one line in a warehouse operation.
   *
   * @param correctedProductId the correctedProductId value
   * @param correctedQuantity the correctedQuantity value
   * @param correctedWarehouseId the correctedWarehouseId value
   * @param countedQuantity the countedQuantity value
   * @param disposition the disposition value
   * @param productId the productId value
   * @param quantity the quantity value
   * @param reason the reason value
   * @param sourceLineId the sourceLineId value
   * @param varianceReason the varianceReason value
   */
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
