package dev.roland.inventory_management_backend.features.document_prefix.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dev.roland.inventory_management_backend.features.document_prefix.DocumentPrefix;

/** Provides database queries for document numbering prefix records. */
@Repository
public interface DocumentPrefixRepository extends JpaRepository<DocumentPrefix, Long> {}
