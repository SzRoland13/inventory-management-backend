package dev.roland.inventory_management_backend.features.document_activity.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.features.document_activity.DocumentActivity;
import dev.roland.inventory_management_backend.features.document_activity.repository.DocumentActivityRepository;
import dev.roland.inventory_management_backend.features.document_activity.service.DocumentActivityService;
import lombok.RequiredArgsConstructor;

/** Implements document activity persistence operations. */
@Service
@RequiredArgsConstructor
public class DocumentActivityServiceImpl implements DocumentActivityService {
  private final DocumentActivityRepository repository;

  @Override
  public DocumentActivity save(final DocumentActivity activity) {
    return repository.save(activity);
  }

  @Override
  public List<DocumentActivity> findAllByDocumentIdOrderByOccurredAtAsc(final Long documentId) {
    return repository.findAllByDocumentIdOrderByOccurredAtAsc(documentId);
  }
}
