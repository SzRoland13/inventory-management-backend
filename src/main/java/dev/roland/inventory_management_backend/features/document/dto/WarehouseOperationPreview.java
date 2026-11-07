package dev.roland.inventory_management_backend.features.document.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;

/**
 * Preview of a transfer, return, count, or correction operation.
 *
 * @param blockers the blockers value
 * @param calculationVersion the calculationVersion value
 * @param lines the lines value
 * @param relatedMovements the relatedMovements value
 * @param relatedReturns the relatedReturns value
 * @param valid the valid value
 */
public record WarehouseOperationPreview(
    boolean valid,
    String calculationVersion,
    List<Line> lines,
    List<String> blockers,
    List<Movement> relatedMovements,
    List<DocumentRef> relatedReturns) {
  /** Creates a preview with immutable copies of its list components. */
  public WarehouseOperationPreview {
    lines = immutableCopy(lines);
    blockers = immutableCopy(blockers);
    relatedMovements = immutableCopy(relatedMovements);
    relatedReturns = immutableCopy(relatedReturns);
  }

  @Override
  public List<Line> lines() {
    return immutableCopy(lines);
  }

  @Override
  public List<String> blockers() {
    return immutableCopy(blockers);
  }

  @Override
  public List<Movement> relatedMovements() {
    return immutableCopy(relatedMovements);
  }

  @Override
  public List<DocumentRef> relatedReturns() {
    return immutableCopy(relatedReturns);
  }

  private static <T> List<T> immutableCopy(final List<T> values) {
    return values == null ? null : List.copyOf(values);
  }

  /**
   * Preview details for one document line.
   *
   * @param adjustmentQuantity the adjustmentQuantity value
   * @param closedQuantity the closedQuantity value
   * @param correctedFulfilledQuantity the correctedFulfilledQuantity value
   * @param correctedQuantity the correctedQuantity value
   * @param countedQuantity the countedQuantity value
   * @param destinationQuantity the destinationQuantity value
   * @param errorMessageKey the errorMessageKey value
   * @param expectedAtCount the expectedAtCount value
   * @param originalQuantity the originalQuantity value
   * @param productId the productId value
   * @param productName the productName value
   * @param quantityDelta the quantityDelta value
   * @param rebasedTargetQuantity the rebasedTargetQuantity value
   * @param remainingQuantity the remainingQuantity value
   * @param resultingQuantity the resultingQuantity value
   * @param returnableQuantity the returnableQuantity value
   * @param returnedQuantity the returnedQuantity value
   * @param sourceLineId the sourceLineId value
   * @param sourceQuantity the sourceQuantity value
   * @param targetWarehouseId the targetWarehouseId value
   * @param transitQuantity the transitQuantity value
   * @param variance the variance value
   */
  @Builder(toBuilder = true)
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
    /** Returns the quantity expected at the time of the count. */
    @JsonProperty("expectedQuantity")
    public BigDecimal expectedQuantity() {
      return expectedAtCount;
    }

    /** Returns a copy containing the resolved product name. */
    public Line withProductName(final String value) {
      return toBuilder().productName(value).build();
    }
  }

  /**
   * Describes a stock movement included in the operation preview.
   *
   * @param description the description value
   * @param effectiveAt the effectiveAt value
   * @param quantity the quantity value
   */
  public record Movement(String description, LocalDateTime effectiveAt, BigDecimal quantity) {}

  /**
   * Identifies a related document and its quantity.
   *
   * @param id the id value
   * @param number the number value
   * @param quantity the quantity value
   */
  public record DocumentRef(Long id, String number, BigDecimal quantity) {}
}
