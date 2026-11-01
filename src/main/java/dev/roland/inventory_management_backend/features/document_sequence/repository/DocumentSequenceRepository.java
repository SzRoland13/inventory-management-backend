package dev.roland.inventory_management_backend.features.document_sequence.repository;

import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import dev.roland.inventory_management_backend.features.document_sequence.DocumentSequence;

/** Provides database queries for document numbering sequence records. */
@Repository
public interface DocumentSequenceRepository extends JpaRepository<DocumentSequence, Long> {
  /**
   * Ensures the required sequence exists.
   *
   * @param companyId company identifier value
   * @param prefixId prefix identifier value
   * @param year year value
   * @return the requested result
   */
  @Modifying
  @Query(
      value =
          "insert into document_sequences(company_id, document_prefix_id, year, counter, "
              + "updated_at) "
              + "values (:companyId, :prefixId, :year, 0, current_timestamp) "
              + "on conflict (company_id, document_prefix_id, year) do nothing",
      nativeQuery = true)
  int ensureSequenceExists(
      @Param("companyId") Long companyId,
      @Param("prefixId") Long prefixId,
      @Param("year") Integer year);

  /**
   * Locks and returns the matching record.
   *
   * @param companyId company identifier value
   * @param prefixId prefix identifier value
   * @param year year value
   * @return the requested result
   */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(
      "select s from DocumentSequence s where s.company.id = :companyId "
          + "and s.documentPrefix.id = :prefixId and s.year = :year")
  Optional<DocumentSequence> lockByScope(
      @Param("companyId") Long companyId,
      @Param("prefixId") Long prefixId,
      @Param("year") Integer year);
}
