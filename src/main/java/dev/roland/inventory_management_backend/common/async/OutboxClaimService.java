package dev.roland.inventory_management_backend.common.async;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/** Claims batches briefly and persists publication outcomes without holding locks over I/O. */
@Service
@RequiredArgsConstructor
public class OutboxClaimService {

  private final OutboxMessageRepository repository;

  @Value("${app.outbox.publish-lease-seconds:120}")
  private int leaseSeconds;

  /**
   * Claims eligible jobs for a bounded period.
   *
   * @param batchSize maximum number of jobs to claim
   * @return claimed job snapshots
   */
  @Transactional
  public List<ClaimedOutboxMessage> claimPending(final int batchSize) {
    final Instant now = Instant.now();
    final Instant lockedUntil = now.plus(Math.max(1, leaseSeconds), ChronoUnit.SECONDS);
    return repository.lockPending(Math.max(1, batchSize)).stream()
        .map(
            message -> {
              final UUID leaseToken = UUID.randomUUID();
              final boolean deadLetter =
                  message.getStatus() == OutboxStatus.DLQ_PENDING
                      || message.getStatus() == OutboxStatus.DLQ_PUBLISHING;
              message.setStatus(deadLetter ? OutboxStatus.DLQ_PUBLISHING : OutboxStatus.PUBLISHING);
              message.setLeaseToken(leaseToken);
              message.setLockedUntil(lockedUntil);
              message.setUpdatedAt(now);
              return new ClaimedOutboxMessage(
                  message.getId(),
                  leaseToken,
                  message.getJobType(),
                  message.getPublishAttempts(),
                  message.getEncryptedPayload(),
                  message.getCompanyId(),
                  message.getDocumentId(),
                  deadLetter,
                  message.getLastError());
            })
        .toList();
  }

  /**
   * Records a successful broker publication if the lease is still valid.
   *
   * @param claimed published job snapshot
   * @param updatedAt publication timestamp
   * @return whether the lease was still valid
   */
  @Transactional
  public boolean markPublished(final ClaimedOutboxMessage claimed, final Instant updatedAt) {
    final boolean published;
    if (claimed.deadLetter()) {
      published =
          repository
              .lockById(claimed.id())
              .filter(
                  message ->
                      message.getStatus() == OutboxStatus.DLQ_PUBLISHING
                          && claimed.leaseToken().equals(message.getLeaseToken()))
              .map(
                  message -> {
                    message.setStatus(OutboxStatus.FAILED);
                    message.setEncryptedPayload(new byte[0]);
                    message.setLeaseToken(null);
                    message.setLockedUntil(null);
                    message.setNextAttemptAt(null);
                    message.setUpdatedAt(updatedAt);
                    return true;
                  })
              .orElse(false);
    } else {
      published =
          repository.markPublished(
                  claimed.id(),
                  claimed.leaseToken(),
                  OutboxStatus.PUBLISHING,
                  OutboxStatus.QUEUED,
                  updatedAt)
              == 1;
    }
    return published;
  }

  /**
   * Records a failed broker publication and schedules another attempt.
   *
   * @param claimed failed job snapshot
   * @param error failure details
   * @param nextAttemptAt scheduled retry time
   * @param updatedAt failure timestamp
   * @return whether the lease was still valid
   */
  @Transactional
  public boolean markPublishFailed(
      final ClaimedOutboxMessage claimed,
      final String error,
      final Instant nextAttemptAt,
      final Instant updatedAt) {
    return repository.markPublishFailed(
            claimed.id(),
            claimed.leaseToken(),
            claimed.deadLetter() ? OutboxStatus.DLQ_PUBLISHING : OutboxStatus.PUBLISHING,
            claimed.deadLetter() ? OutboxStatus.DLQ_PENDING : OutboxStatus.PENDING,
            claimed.deadLetter() ? claimed.failure() : error,
            nextAttemptAt,
            updatedAt)
        == 1;
  }
}
