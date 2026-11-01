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
  @lombok.Builder(toBuilder = true)
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
    @JsonProperty("expectedQuantity")
    public BigDecimal expectedQuantity() {
      return expectedAtCount;
    }

    public Line withProductName(final String value) {
      return toBuilder().productName(value).build();
    }
  }

  public record Movement(String description, LocalDateTime effectiveAt, BigDecimal quantity) {}

  public record DocumentRef(Long id, String number, BigDecimal quantity) {}
}
