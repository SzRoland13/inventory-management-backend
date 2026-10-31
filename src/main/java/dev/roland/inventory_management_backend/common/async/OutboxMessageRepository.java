package dev.roland.inventory_management_backend.common.async;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Persistence operations for durable asynchronous jobs. */
public interface OutboxMessageRepository extends JpaRepository<OutboxMessage, UUID> {

  /**
   * Locks a batch of eligible jobs for publication.
   *
   * @param batchSize maximum number of jobs to lock
   * @return locked outbox jobs
   */
  @Query(
      value =
          """
          select * from outbox_messages
          where (status in ('PENDING', 'DLQ_PENDING')
              and (next_attempt_at is null or next_attempt_at <= CURRENT_TIMESTAMP))
            or (status in ('PUBLISHING', 'DLQ_PUBLISHING')
              and locked_until <= CURRENT_TIMESTAMP)
          order by created_at limit :batchSize for update skip locked
          """,
      nativeQuery = true)
  List<OutboxMessage> lockPending(@Param("batchSize") int batchSize);

  /**
   * Marks a lease as successfully published.
   *
   * @param id durable job identifier
   * @param leaseToken publication lease token
   * @param publishingStatus expected current status
   * @param targetStatus status to assign after publication
   * @param updatedAt publication timestamp
   * @return number of updated rows
   */
  @Modifying
  @Query(
      """
          update OutboxMessage m
          set m.status = :targetStatus, m.leaseToken = null, m.lockedUntil = null,
              m.nextAttemptAt = null, m.lastError = null, m.updatedAt = :updatedAt
          where m.id = :id and m.status = :publishingStatus and m.leaseToken = :leaseToken
      """)
  int markPublished(
      @Param("id") UUID id,
      @Param("leaseToken") UUID leaseToken,
      @Param("publishingStatus") OutboxStatus publishingStatus,
      @Param("targetStatus") OutboxStatus targetStatus,
      @Param("updatedAt") Instant updatedAt);

  /**
   * Marks a lease as failed and schedules a retry.
   *
   * @param id durable job identifier
   * @param leaseToken publication lease token
   * @param publishingStatus expected current status
   * @param pendingStatus status to assign for retry
   * @param lastError recorded failure details
   * @param nextAttemptAt scheduled retry time
   * @param updatedAt failure timestamp
   * @return number of updated rows
   */
  @Modifying
  @Query(
      """
          update OutboxMessage m
          set m.status = :pendingStatus, m.publishAttempts = m.publishAttempts + 1,
              m.leaseToken = null, m.lockedUntil = null, m.nextAttemptAt = :nextAttemptAt,
              m.lastError = :lastError, m.updatedAt = :updatedAt
          where m.id = :id and m.status = :publishingStatus and m.leaseToken = :leaseToken
      """)
  int markPublishFailed(
      @Param("id") UUID id,
      @Param("leaseToken") UUID leaseToken,
      @Param("publishingStatus") OutboxStatus publishingStatus,
      @Param("pendingStatus") OutboxStatus pendingStatus,
      @Param("lastError") String lastError,
      @Param("nextAttemptAt") Instant nextAttemptAt,
      @Param("updatedAt") Instant updatedAt);

  /**
   * Locks one job for consumer processing.
   *
   * @param id durable job identifier
   * @return locked job, if it exists
   */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select m from OutboxMessage m where m.id = :id")
  Optional<OutboxMessage> lockById(@Param("id") UUID id);

  /**
   * Counts jobs in the supplied state.
   *
   * @param status state to count
   * @return number of jobs in that state
   */
  long countByStatus(OutboxStatus status);
}
