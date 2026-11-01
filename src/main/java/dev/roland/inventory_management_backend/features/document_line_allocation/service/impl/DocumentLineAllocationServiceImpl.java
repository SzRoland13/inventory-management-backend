package dev.roland.inventory_management_backend.features.document_line_allocation.service.impl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.features.document.enumeration.DocumentStatus;
import dev.roland.inventory_management_backend.features.document_line_allocation.DocumentLineAllocation;
import dev.roland.inventory_management_backend.features.document_line_allocation.repository.DocumentLineAllocationRepository;
import dev.roland.inventory_management_backend.features.document_line_allocation.service.DocumentLineAllocationService;
import lombok.RequiredArgsConstructor;

/** Implements document line allocation persistence operations. */
@Service
@RequiredArgsConstructor
public class DocumentLineAllocationServiceImpl implements DocumentLineAllocationService {
  private final DocumentLineAllocationRepository repository;

  @Override
  public DocumentLineAllocation save(final DocumentLineAllocation allocation) {
    return repository.save(allocation);
  }

  @Override
  public List<DocumentLineAllocation> findAllByFulfillmentLineId(final Long lineId) {
    return repository.findAllByFulfillmentLineId(lineId);
  }

  @Override
  public BigDecimal sumPostedAllocated(final Long lineId, final DocumentStatus status) {
    return repository.sumPostedAllocated(lineId, status);
  }
}
