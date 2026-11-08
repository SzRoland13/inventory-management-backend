package dev.roland.inventory_management_backend.features.document.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import dev.roland.inventory_management_backend.common.validation.ValidationKeys;
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
 * @param effectiveAt when the stock movement takes effect, or null to use posting time
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
    @Positive(message = ValidationKeys.INVALID_VALUE) Long documentId,
    @Positive(message = ValidationKeys.INVALID_VALUE) Long sourceDocumentId,
    @Positive(message = ValidationKeys.INVALID_VALUE) Long sourceWarehouseId,
    @Positive(message = ValidationKeys.INVALID_VALUE) Long destinationWarehouseId,
    @Positive(message = ValidationKeys.INVALID_VALUE) Long transitWarehouseId,
    @Positive(message = ValidationKeys.INVALID_VALUE) Long warehouseId,
    @Positive(message = ValidationKeys.INVALID_VALUE) Long targetWarehouseId,
    @Positive(message = ValidationKeys.INVALID_VALUE) Long returnDocumentId,
    @Positive(message = ValidationKeys.INVALID_VALUE) Long originalDeliveryId,
    LocalDate date,
    LocalDateTime countedAt,
    @Size(max = 100, message = ValidationKeys.TOO_LONG) String version,
    @Size(max = 255, message = ValidationKeys.TOO_LONG) String idempotencyKey,
    @Size(max = 100, message = ValidationKeys.TOO_LONG) String calculationVersion,
    LocalDateTime effectiveAt,
    WarehouseOperationType operation,
    ReturnDirection direction,
    @Size(max = 500, message = ValidationKeys.TOO_LONG) String reason,
    Boolean confirmed,
    Boolean nonBillable,
    @Valid List<WarehouseOperationLine> lines) {
  /** Keeps the existing request constructor source-compatible. */
  public WarehouseOperationRequest(
      final Long documentId,
      final Long sourceDocumentId,
      final Long sourceWarehouseId,
      final Long destinationWarehouseId,
      final Long transitWarehouseId,
      final Long warehouseId,
      final Long targetWarehouseId,
      final Long returnDocumentId,
      final Long originalDeliveryId,
      final LocalDate date,
      final LocalDateTime countedAt,
      final String version,
      final String idempotencyKey,
      final String calculationVersion,
      final WarehouseOperationType operation,
      final ReturnDirection direction,
      final String reason,
      final Boolean confirmed,
      final Boolean nonBillable,
      final List<WarehouseOperationLine> lines) {
    this(
        documentId,
        sourceDocumentId,
        sourceWarehouseId,
        destinationWarehouseId,
        transitWarehouseId,
        warehouseId,
        targetWarehouseId,
        returnDocumentId,
        originalDeliveryId,
        date,
        countedAt,
        version,
        idempotencyKey,
        calculationVersion,
        null,
        operation,
        direction,
        reason,
        confirmed,
        nonBillable,
        lines);
  }

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
   * @param closureReopens explicit reasoned reopenings of source order lines
   * @param sourceLineId the sourceLineId value
   * @param varianceReason the varianceReason value
   * @param unitId unit selected for the entered quantity
   */
  public record WarehouseOperationLine(
      @Positive(message = ValidationKeys.INVALID_VALUE) Long sourceLineId,
      @Positive(message = ValidationKeys.INVALID_VALUE) Long productId,
      @Positive(message = ValidationKeys.INVALID_VALUE)
          @Digits(integer = 16, fraction = 8, message = ValidationKeys.INVALID_VALUE)
          BigDecimal quantity,
      @Positive(message = ValidationKeys.INVALID_VALUE)
          @Digits(integer = 16, fraction = 8, message = ValidationKeys.INVALID_VALUE)
          BigDecimal correctedQuantity,
      @Positive(message = ValidationKeys.INVALID_VALUE) Long correctedProductId,
      @Positive(message = ValidationKeys.INVALID_VALUE) Long correctedWarehouseId,
      @DecimalMin(value = "0", message = ValidationKeys.INVALID_VALUE)
          @Digits(integer = 16, fraction = 8, message = ValidationKeys.INVALID_VALUE)
          BigDecimal countedQuantity,
      @Size(max = 500, message = ValidationKeys.TOO_LONG) String varianceReason,
      ReturnDisposition disposition,
      @Size(max = 500, message = ValidationKeys.TOO_LONG) String reason,
      @Valid List<ClosureReopen> closureReopens,
      @Positive(message = ValidationKeys.INVALID_VALUE) Long unitId) {
    /** Keeps the pre-reopen request shape source-compatible for other warehouse operations. */
    public WarehouseOperationLine(
        final Long sourceLineId,
        final Long productId,
        final BigDecimal quantity,
        final BigDecimal correctedQuantity,
        final Long correctedProductId,
        final Long correctedWarehouseId,
        final BigDecimal countedQuantity,
        final String varianceReason,
        final ReturnDisposition disposition,
        final String reason) {
      this(
          sourceLineId,
          productId,
          quantity,
          correctedQuantity,
          correctedProductId,
          correctedWarehouseId,
          countedQuantity,
          varianceReason,
          disposition,
          reason,
          List.of(),
          null);
    }

    /** Keeps the closure-reopen request constructor source-compatible. */
    public WarehouseOperationLine(
        final Long sourceLineId,
        final Long productId,
        final BigDecimal quantity,
        final BigDecimal correctedQuantity,
        final Long correctedProductId,
        final Long correctedWarehouseId,
        final BigDecimal countedQuantity,
        final String varianceReason,
        final ReturnDisposition disposition,
        final String reason,
        final List<ClosureReopen> closureReopens) {
      this(
          sourceLineId,
          productId,
          quantity,
          correctedQuantity,
          correctedProductId,
          correctedWarehouseId,
          countedQuantity,
          varianceReason,
          disposition,
          reason,
          closureReopens,
          null);
    }

    /** Creates a defensive copy of closure adjustments. */
    public WarehouseOperationLine {
      closureReopens = immutableCopy(closureReopens);
    }

    @Override
    public List<ClosureReopen> closureReopens() {
      return immutableCopy(closureReopens);
    }
  }

  /**
   * An explicit reduction of a closed quantity on a source order line.
   *
   * @param sourceOrderLineId the source order line to reopen
   * @param quantity the quantity to reopen
   * @param reason the reason for reopening the quantity
   */
  public record ClosureReopen(
      @Positive(message = ValidationKeys.INVALID_VALUE) Long sourceOrderLineId,
      @Positive(message = ValidationKeys.INVALID_VALUE) BigDecimal quantity,
      @Size(max = 500, message = ValidationKeys.TOO_LONG) String reason) {}
}
