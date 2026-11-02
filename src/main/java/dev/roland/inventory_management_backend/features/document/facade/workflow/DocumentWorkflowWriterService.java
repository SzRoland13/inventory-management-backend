package dev.roland.inventory_management_backend.features.document.facade.workflow;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.features.document.Document;
import dev.roland.inventory_management_backend.features.document.DocumentPostingKey;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentStatus;
import dev.roland.inventory_management_backend.features.document.service.DocumentService;
import dev.roland.inventory_management_backend.features.document_activity.DocumentActivity;
import dev.roland.inventory_management_backend.features.document_activity.DocumentActivityEvent;
import dev.roland.inventory_management_backend.features.document_activity.service.DocumentActivityService;
import dev.roland.inventory_management_backend.features.document_prefix.DocumentPrefix;
import dev.roland.inventory_management_backend.features.document_prefix.service.DocumentPrefixService;
import dev.roland.inventory_management_backend.features.document_relation.DocumentRelation;
import dev.roland.inventory_management_backend.features.document_relation.service.DocumentRelationService;
import dev.roland.inventory_management_backend.features.document_sequence.DocumentSequence;
import dev.roland.inventory_management_backend.features.document_sequence.service.DocumentSequenceService;
import dev.roland.inventory_management_backend.features.stock_movement.StockMovement;
import dev.roland.inventory_management_backend.features.stock_movement.service.StockMovementService;
import lombok.RequiredArgsConstructor;

/** Persists common document events, links, stock movements, and sequence numbers. */
@Service
@RequiredArgsConstructor
public class DocumentWorkflowWriterService {
  private final DocumentActivityService activityService;
  private final DocumentWorkflowAccessService accessService;
  private final DocumentService documentService;
  private final DocumentPrefixService prefixService;
  private final DocumentSequenceService sequenceService;
  private final DocumentRelationService relationService;
  private final StockMovementService stockMovementService;

  /**
   * Records an activity event for a document.
   *
   * @param details the details value
   * @param document the document value
   * @param type the type value
   */
  public void addActivity(
      final Document document, final DocumentActivityEvent type, final String details) {
    final DocumentActivity event = new DocumentActivity();
    event.setDocument(document);
    event.setActor(accessService.currentActor());
    event.setEventType(type);
    event.setDetails(details);
    activityService.save(event);
  }

  /**
   * Allocates a document number when one has not already been assigned.
   *
   * @param document the document value
   */
  public void allocateNumber(final Document document) {
    if (document.getDocumentNumber() != null) {
      return;
    }
    final Long companyId = document.getCompany().getId();
    final int year = document.getDocumentDate().getYear();
    final DocumentPrefix prefix =
        prefixService
            .findFirstByCompanyIdAndDocumentTypeOrderByIdAsc(companyId, document.getType())
            .orElseGet(
                () ->
                    prefixService.save(
                        DocumentPrefix.builder()
                            .company(document.getCompany())
                            .documentType(document.getType())
                            .prefix(document.getType().getDefaultPrefix())
                            .build()));
    final String prefixText = prefix.getPrefix();
    sequenceService.ensureSequenceExists(companyId, prefix.getId(), year);
    final DocumentSequence sequence =
        sequenceService
            .lockByScope(companyId, prefix.getId(), year)
            .orElseThrow(
                () ->
                    new dev.roland.inventory_management_backend.common.exception.ApiException(
                        dev.roland.inventory_management_backend.features.document.message
                            .DocumentApiMessageKey.DOCUMENT_SEQUENCE_UNAVAILABLE));
    sequence.setCounter(sequence.getCounter() + 1);
    document.setDocumentSequence(sequence);
    document.setDocumentNumber(
        prefixText + "-" + year + "-" + String.format("%05d", sequence.getCounter()));
  }

  /**
   * Marks a document complete and records its completion time.
   *
   * @param document the document value
   */
  public void complete(final Document document) {
    document.setStatus(DocumentStatus.COMPLETED);
    document.setCompletedAt(LocalDateTime.now());
  }

  /**
   * Updates the document's modification time.
   *
   * @param document the document value
   */
  public void touch(final Document document) {
    document.setUpdatedAt(LocalDateTime.now());
  }

  /**
   * Creates a posting key for a document and idempotency key.
   *
   * @param document the document value
   * @param key the key value
   * @return the resulting value
   */
  public DocumentPostingKey postingKey(final Document document, final String key) {
    final DocumentPostingKey postingKey = new DocumentPostingKey();
    postingKey.setDocument(document);
    postingKey.setIdempotencyKey(key);
    return postingKey;
  }

  /**
   * Persists the idempotency key for a document operation.
   *
   * @param document the document value
   * @param key the key value
   */
  public void consumeKey(final Document document, final String key) {
    documentService.savePostingKey(postingKey(document, key));
  }

  /**
   * * * Creates a relation between source and target documents.
   *
   * @param type the type value
   * @param target the target value
   * @param source the source value
   */
  public void relate(
      final Document source,
      final Document target,
      final dev.roland.inventory_management_backend.features.document.enumeration
              .DocumentRelationType
          type) {
    final DocumentRelation relation = new DocumentRelation();
    relation.setSourceDocument(source);
    relation.setTargetDocument(target);
    relation.setRelationType(type);
    relationService.save(relation);
  }

  /**
   * Persists a stock movement and its reason.
   *
   * @param movement the movement value
   * @param reason the reason value
   */
  public void saveStockMovement(final StockMovement movement, final String reason) {
    movement.setReason(reason);
    stockMovementService.save(movement);
  }
}
