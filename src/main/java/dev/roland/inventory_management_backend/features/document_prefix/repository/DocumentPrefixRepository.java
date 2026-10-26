package dev.roland.inventory_management_backend.features.document_prefix.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dev.roland.inventory_management_backend.features.document_prefix.DocumentPrefix;

/** Provides database queries for document numbering prefix records. */
@Repository
public interface DocumentPrefixRepository extends JpaRepository<DocumentPrefix, Long> {
  List<DocumentPrefix> findAllByCompanyId(Long companyId);

  Optional<DocumentPrefix> findByIdAndCompanyId(Long id, Long companyId);
}
