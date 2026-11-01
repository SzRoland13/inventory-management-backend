package dev.roland.inventory_management_backend.features.document_file.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.features.document_file.DocumentFile;
import dev.roland.inventory_management_backend.features.document_file.DocumentFileRepository;
import dev.roland.inventory_management_backend.features.document_file.DocumentFileSource;
import dev.roland.inventory_management_backend.features.document_file.DocumentFileStatus;
import dev.roland.inventory_management_backend.features.document_file.service.DocumentFileService;
import lombok.RequiredArgsConstructor;

/** Implements document file persistence operations. */
@Service
@RequiredArgsConstructor
public class DocumentFileServiceImpl implements DocumentFileService {
  private final DocumentFileRepository repository;

  @Override
  public DocumentFile save(final DocumentFile file) {
    return repository.save(file);
  }

  @Override
  public List<DocumentFile> findAllByDocumentIdOrderByCreatedAtDesc(final Long documentId) {
    return repository.findAllByDocumentIdOrderByCreatedAtDesc(documentId);
  }

  @Override
  public Optional<DocumentFile> findByDocumentIdAndIdempotencyKey(
      final Long documentId, final String key) {
    return repository.findByDocumentIdAndIdempotencyKey(documentId, key);
  }

  @Override
  public boolean existsByDocumentIdAndSourceAndStatusIn(
      final Long documentId,
      final DocumentFileSource source,
      final List<DocumentFileStatus> statuses) {
    return repository.existsByDocumentIdAndSourceAndStatusIn(documentId, source, statuses);
  }
}
