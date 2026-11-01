package dev.roland.inventory_management_backend.common.async;

import java.security.MessageDigest;
import java.util.Objects;

import lombok.experimental.UtilityClass;

/** Shared checks for duplicate and stale outbox deliveries. */
@UtilityClass
public final class OutboxMessageSupport {

  /**
   * Checks whether a broker delivery matches its persisted outbox message.
   *
   * @param message persisted outbox message
   * @param envelope broker delivery envelope
   * @return whether the envelope matches the persisted message
   */
  public static boolean matchesEnvelope(
      final OutboxMessage message, final AsyncJobEnvelope envelope) {
    return message.getJobType() == envelope.jobType()
        && Objects.equals(message.getCompanyId(), envelope.companyId())
        && Objects.equals(message.getDocumentId(), envelope.documentId())
        && envelope.encryptedPayload() != null
        && MessageDigest.isEqual(message.getEncryptedPayload(), envelope.encryptedPayload());
  }

  /**
   * Checks whether an outbox message has reached a state that must not be processed again.
   *
   * @param message persisted outbox message
   * @return whether processing is complete or dead-lettered
   */
  public static boolean isTerminal(final OutboxMessage message) {
    return message.getStatus() == OutboxStatus.SENT
        || message.getStatus() == OutboxStatus.FAILED
        || message.getStatus() == OutboxStatus.DLQ_PENDING
        || message.getStatus() == OutboxStatus.DLQ_PUBLISHING;
  }
}
