package dev.roland.inventory_management_backend.common.async;

import java.util.UUID;

/**
 * Immutable snapshot of an outbox row claimed for broker publication.
 *
 * @param id durable job identifier
 * @param leaseToken token proving ownership of the publication lease
 * @param jobType kind of asynchronous job
 * @param publishAttempts number of previous publication failures
 * @param encryptedPayload protected job payload
 * @param companyId associated company, when present
 * @param documentId associated document, when present
 * @param deadLetter whether this snapshot publishes a terminal failure
 * @param failure recorded failure for a dead-letter publication
 */
public record ClaimedOutboxMessage(
    UUID id,
    UUID leaseToken,
    AsyncJobType jobType,
    int publishAttempts,
    byte[] encryptedPayload,
    Long companyId,
    Long documentId,
    boolean deadLetter,
    String failure) {

  /** Creates a non-dead-letter publication snapshot. */
  public ClaimedOutboxMessage(
      final UUID id,
      final UUID leaseToken,
      final AsyncJobType jobType,
      final int publishAttempts,
      final byte[] encryptedPayload,
      final Long companyId,
      final Long documentId) {
    this(
        id,
        leaseToken,
        jobType,
        publishAttempts,
        encryptedPayload,
        companyId,
        documentId,
        false,
        null);
  }

  /** Creates an immutable publication snapshot. */
  public ClaimedOutboxMessage {
    encryptedPayload = encryptedPayload.clone();
  }

  @Override
  public byte[] encryptedPayload() {
    return encryptedPayload.clone();
  }

  /**
   * Creates the broker envelope for this snapshot.
   *
   * @return broker envelope
   */
  AsyncJobEnvelope toEnvelope() {
    return new AsyncJobEnvelope(id, jobType, encryptedPayload, companyId, documentId);
  }
}
