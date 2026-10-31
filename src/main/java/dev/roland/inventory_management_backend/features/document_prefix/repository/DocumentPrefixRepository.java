package dev.roland.inventory_management_backend.features.document_prefix.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dev.roland.inventory_management_backend.features.document_prefix.DocumentPrefix;

/** Provides database queries for document numbering prefix records. */
@Repository
public interface DocumentPrefixRepository extends JpaRepository<DocumentPrefix, Long> {
  /**
   * Finds document prefixes owned by a company.
   *
   * @param companyId owning company identifier
   * @return matching prefixes
   */
  List<DocumentPrefix> findAllByCompanyId(Long companyId);

  /**
   * Finds a document prefix within its owning company.
   *
   * @param id prefix identifier
   * @param companyId owning company identifier
   * @return matching prefix, if it exists
   */
  Optional<DocumentPrefix> findByIdAndCompanyId(Long id, Long companyId);
}
