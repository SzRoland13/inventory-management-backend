package dev.roland.inventory_management_backend.features.document.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Preview of a transfer, return, count, or correction operation. */
public record WarehouseOperationPreview(
    boolean valid,
    String calculationVersion,
    List<Line> lines,
    List<String> blockers,
    List<Movement> relatedMovements,
    List<DocumentRef> relatedReturns) {
  public record Line(
      Long sourceLineId,
      Long productId,
      BigDecimal sourceQuantity,
      BigDecimal transitQuantity,
      BigDecimal destinationQuantity,
      BigDecimal expectedAtCount,
      BigDecimal countedQuantity,
      BigDecimal variance,
      BigDecimal rebasedTargetQuantity,
      BigDecimal adjustmentQuantity,
      BigDecimal originalQuantity,
      BigDecimal correctedQuantity,
      BigDecimal correctedFulfilledQuantity,
      BigDecimal closedQuantity,
      BigDecimal returnedQuantity,
      BigDecimal returnableQuantity,
      BigDecimal remainingQuantity,
      BigDecimal quantityDelta,
      BigDecimal resultingQuantity,
      Long targetWarehouseId,
      String errorMessageKey,
      String productName) {
    public Line(
        final Long sourceLineId,
        final Long productId,
        final BigDecimal sourceQuantity,
        final BigDecimal transitQuantity,
        final BigDecimal destinationQuantity,
        final BigDecimal expectedAtCount,
        final BigDecimal countedQuantity,
        final BigDecimal variance,
        final BigDecimal rebasedTargetQuantity,
        final BigDecimal adjustmentQuantity,
        final BigDecimal originalQuantity,
        final BigDecimal correctedQuantity,
        final BigDecimal correctedFulfilledQuantity,
        final BigDecimal closedQuantity,
        final BigDecimal returnedQuantity,
        final BigDecimal returnableQuantity,
        final BigDecimal remainingQuantity,
        final BigDecimal quantityDelta,
        final BigDecimal resultingQuantity,
        final Long targetWarehouseId,
        final String errorMessageKey) {
      this(
          sourceLineId,
          productId,
          sourceQuantity,
          transitQuantity,
          destinationQuantity,
          expectedAtCount,
          countedQuantity,
          variance,
          rebasedTargetQuantity,
          adjustmentQuantity,
          originalQuantity,
          correctedQuantity,
          correctedFulfilledQuantity,
          closedQuantity,
          returnedQuantity,
          returnableQuantity,
          remainingQuantity,
          quantityDelta,
          resultingQuantity,
          targetWarehouseId,
          errorMessageKey,
          null);
    }

    @JsonProperty("expectedQuantity")
    public BigDecimal expectedQuantity() {
      return expectedAtCount;
    }

    public Line withProductName(final String value) {
      return new Line(
          sourceLineId,
          productId,
          sourceQuantity,
          transitQuantity,
          destinationQuantity,
          expectedAtCount,
          countedQuantity,
          variance,
          rebasedTargetQuantity,
          adjustmentQuantity,
          originalQuantity,
          correctedQuantity,
          correctedFulfilledQuantity,
          closedQuantity,
          returnedQuantity,
          returnableQuantity,
          remainingQuantity,
          quantityDelta,
          resultingQuantity,
          targetWarehouseId,
          errorMessageKey,
          value);
    }
  }

  public record Movement(String description, LocalDateTime effectiveAt, BigDecimal quantity) {}

  public record DocumentRef(Long id, String number, BigDecimal quantity) {}
}
