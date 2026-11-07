package dev.roland.inventory_management_backend.features.document.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.common.message.MessageKey;
import dev.roland.inventory_management_backend.common.message.NotFoundMessageKey;
import dev.roland.inventory_management_backend.features.document.Document;
import dev.roland.inventory_management_backend.features.document.DocumentPostingKey;
import dev.roland.inventory_management_backend.features.document.repository.DocumentPostingKeyRepository;
import dev.roland.inventory_management_backend.features.document.repository.DocumentRepository;
import dev.roland.inventory_management_backend.features.document.service.DocumentService;
import lombok.RequiredArgsConstructor;

/** Implements the inventory document service operations. */
@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {
  private final DocumentRepository documentRepository;
  private final DocumentPostingKeyRepository postingKeyRepository;

  /** {@inheritDoc} */
  @Override
  public JpaRepository<Document, Long> getRepository() {
    return documentRepository;
  }

  /**
   * {@inheritDoc}
   *
   * @return get not found message key result
   */
  @Override
  public MessageKey getNotFoundMessageKey() {
    return NotFoundMessageKey.DOCUMENT;
  }

  @Override
  public Optional<Document> findByIdAndCompanyId(final Long id, final Long companyId) {
    return documentRepository.findByIdAndCompanyId(id, companyId);
  }

  @Override
  public Optional<Document> lockByIdAndCompanyId(final Long id, final Long companyId) {
    return documentRepository.lockByIdAndCompanyId(id, companyId);
  }

  @Override
  public List<Document> findAllByCompanyIdOrderByUpdatedAtDesc(final Long companyId) {
    return documentRepository.findAllByCompanyIdOrderByUpdatedAtDesc(companyId);
  }

  @Override
  public void flush() {
    documentRepository.flush();
  }

  @Override
  public boolean postingKeyExists(final Long documentId, final String key) {
    return postingKeyRepository.existsByDocumentIdAndIdempotencyKey(documentId, key);
  }

  @Override
  public Optional<DocumentPostingKey> findPostingKey(final Long documentId, final String key) {
    return postingKeyRepository.findByDocumentIdAndIdempotencyKey(documentId, key);
  }

  @Override
  public DocumentPostingKey savePostingKey(final DocumentPostingKey key) {
    return postingKeyRepository.save(key);
  }
}
