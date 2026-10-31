package dev.roland.inventory_management_backend.common.async;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Durable record for publishable asynchronous work and its final delivery state. */
@Entity
@Table(name = "outbox_messages")
@Getter
@Setter
@NoArgsConstructor
public class OutboxMessage {

  @Id private UUID id;

  @Column(name = "job_type", nullable = false, length = 80)
  @Enumerated(EnumType.STRING)
  private AsyncJobType jobType;

  @JdbcTypeCode(SqlTypes.VARBINARY)
  @Column(name = "encrypted_payload", nullable = false, columnDefinition = "bytea")
  private byte[] encryptedPayload;

  @Column(name = "company_id")
  private Long companyId;

  @Column(name = "document_id")
  private Long documentId;

  @Column(name = "lease_token")
  private UUID leaseToken;

  @Column(name = "locked_until")
  private Instant lockedUntil;

  @Column(name = "next_attempt_at")
  private Instant nextAttemptAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private OutboxStatus status = OutboxStatus.PENDING;

  @Column(name = "publish_attempts", nullable = false)
  private int publishAttempts;

  @Column(name = "delivery_attempts", nullable = false)
  private int deliveryAttempts;

  @Column(name = "last_error", columnDefinition = "text")
  private String lastError;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt = Instant.now();

  /**
   * Creates a pending outbox record.
   *
   * @param id durable job identifier
   * @param jobType kind of asynchronous job
   * @param encryptedPayload protected job payload
   * @param companyId associated company, when present
   * @param documentId associated document, when present
   */
  public OutboxMessage(
      final UUID id,
      final AsyncJobType jobType,
      final byte[] encryptedPayload,
      final Long companyId,
      final Long documentId) {
    this.id = id;
    this.jobType = jobType;
    this.encryptedPayload = encryptedPayload.clone();
    this.companyId = companyId;
    this.documentId = documentId;
  }
}
