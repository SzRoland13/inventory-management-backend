package dev.roland.inventory_management_backend.features.document_line_allocation.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dev.roland.inventory_management_backend.features.document.enumeration.DocumentStatus;
import dev.roland.inventory_management_backend.features.document_line_allocation.DocumentLineAllocation;

/** Queries source allocations for fulfillment calculations. */
public interface DocumentLineAllocationRepository
    extends JpaRepository<DocumentLineAllocation, Long> {
  /**
   * Finds records matching the supplied criteria.
   *
   * @param lineId line identifier value
   * @return the requested result
   */
  List<DocumentLineAllocation> findAllByFulfillmentLineId(Long lineId);

  /**
   * Sums allocations matching the supplied criteria.
   *
   * @param lineId line identifier value
   * @param status status value
   * @return the requested result
   */
  @Query(
      "select coalesce(sum(a.quantity), 0) from DocumentLineAllocation a "
          + "where a.sourceOrderLine.id = :lineId and a.fulfillmentLine.document.status = "
          + ":status")
  BigDecimal sumPostedAllocated(
      @Param("lineId") Long lineId, @Param("status") DocumentStatus status);
}
