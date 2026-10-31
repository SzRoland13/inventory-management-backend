package dev.roland.inventory_management_backend.features.document_file;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentFileRepository extends JpaRepository<DocumentFile, Long> {
  List<DocumentFile> findAllByDocumentIdOrderByCreatedAtDesc(Long documentId);

  Optional<DocumentFile> findByDocumentIdAndIdempotencyKey(Long documentId, String idempotencyKey);

  boolean existsByDocumentIdAndSourceAndStatusIn(
      Long documentId, DocumentFileSource source, List<DocumentFileStatus> statuses);
}
