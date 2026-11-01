package dev.roland.inventory_management_backend.features.document_sequence.service;

import java.util.Optional;

import dev.roland.inventory_management_backend.common.service.BaseService;
import dev.roland.inventory_management_backend.features.document_sequence.DocumentSequence;

/** Defines operations supported by the document numbering sequence feature. */
public interface DocumentSequenceService extends BaseService<DocumentSequence, Long> {
  /**
   * Creates the sequence row if it does not already exist.
   *
   * @param companyId owning company identifier
   * @param prefixId prefix identifier
   * @param year document year
   * @return number of rows inserted
   */
  int ensureSequenceExists(Long companyId, Long prefixId, Integer year);

  /**
   * Locks the sequence row for a company, prefix, and year.
   *
   * @param companyId owning company identifier
   * @param prefixId prefix identifier
   * @param year document year
   * @return locked sequence, if found
   */
  Optional<DocumentSequence> lockByScope(Long companyId, Long prefixId, Integer year);
}
