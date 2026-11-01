package dev.roland.inventory_management_backend.features.document_line_allocation.service;

import java.math.BigDecimal;
import java.util.List;

import dev.roland.inventory_management_backend.features.document.enumeration.DocumentStatus;
import dev.roland.inventory_management_backend.features.document_line_allocation.DocumentLineAllocation;

/** Owns persistence operations for document line allocations. */
public interface DocumentLineAllocationService {
  /**
   * Persists an allocation between a fulfillment line and its source order line.
   *
   * @param allocation allocation to persist
   * @return persisted allocation
   */
  DocumentLineAllocation save(DocumentLineAllocation allocation);

  /**
   * Lists allocations created by a fulfillment line.
   *
   * @param lineId fulfillment line identifier
   * @return allocations for the line
   */
  List<DocumentLineAllocation> findAllByFulfillmentLineId(Long lineId);

  /**
   * Sums allocations posted with the supplied document status.
   *
   * @param lineId source order line identifier
   * @param status fulfillment document status
   * @return total allocated quantity
   */
  BigDecimal sumPostedAllocated(Long lineId, DocumentStatus status);
}
