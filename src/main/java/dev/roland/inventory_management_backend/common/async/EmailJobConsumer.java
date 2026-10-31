package dev.roland.inventory_management_backend.common.async;

import java.security.MessageDigest;
import java.time.Instant;
import java.util.Objects;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.roland.inventory_management_backend.common.exception.MailException;
import dev.roland.inventory_management_backend.common.mail.Mailable;
import dev.roland.inventory_management_backend.common.message.AsyncJobMessageKey;
import dev.roland.inventory_management_backend.common.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Sends queued email jobs and records retry/failure state. */
@Component
@RequiredArgsConstructor
@Slf4j
public class EmailJobConsumer {

  private final OutboxMessageRepository repository;
  private final OutboxEncryptionService encryptionService;
  private final ObjectMapper objectMapper;
  private final EmailService emailService;

  @Value("${app.outbox.max-attempts:5}")
  private int maxAttempts;

  @Value("${app.outbox.retry-delay-ms:3000}")
  private int retryDelayMs;

  /**
   * Processes an email job and persists its delivery outcome.
   *
   * @param envelope broker message containing the job
   */
  @RabbitListener(
      queues =
          "#{T(dev.roland.inventory_management_backend.common.async.AsyncJobType).EMAIL"
              + ".getQueueName()}")
  @Transactional
  public void consume(final AsyncJobEnvelope envelope) {
    final OutboxMessage message =
        repository
            .lockById(envelope.id())
            .orElseThrow(
                () -> new AsyncJobException(AsyncJobMessageKey.OUTBOX_JOB_NOT_FOUND, true));
    if (message.getStatus() == OutboxStatus.SENT
        || message.getStatus() == OutboxStatus.FAILED
        || message.getStatus() == OutboxStatus.DLQ_PENDING
        || message.getStatus() == OutboxStatus.DLQ_PUBLISHING
        // A duplicate broker delivery must not bypass a persisted retry delay.
        || (message.getNextAttemptAt() != null
            && message.getNextAttemptAt().isAfter(Instant.now()))) {
      return;
    }
    try {
      if (!envelopeMatches(message, envelope)) {
        throw new AsyncJobException(AsyncJobMessageKey.OUTBOX_PAYLOAD_INVALID, true);
      }
      if (message.getJobType() != AsyncJobType.EMAIL) {
        throw new AsyncJobException(AsyncJobMessageKey.OUTBOX_JOB_TYPE_UNSUPPORTED, true);
      }
      final Mailable mailable;
      try {
        final byte[] payloadJson = encryptionService.decrypt(message.getEncryptedPayload());
        final OutboxService.EmailPayload payload =
            OutboxService.decodeEmailPayload(payloadJson, objectMapper);
        mailable = payload.toMailable();
      } catch (AsyncJobException e) {
        throw e;
      } catch (RuntimeException e) {
        throw new AsyncJobException(AsyncJobMessageKey.OUTBOX_PAYLOAD_INVALID, true, e);
      }
      emailService.send(mailable);
      message.setStatus(OutboxStatus.SENT);
      message.setEncryptedPayload(new byte[0]);
      message.setLastError(null);
      message.setUpdatedAt(Instant.now());
    } catch (RuntimeException e) {
      final AsyncJobException failure =
          e instanceof AsyncJobException asyncJobException
              ? asyncJobException
              : e instanceof MailException
                  ? new AsyncJobException(AsyncJobMessageKey.EMAIL_DELIVERY_FAILED, true, e)
                  : new AsyncJobException(AsyncJobMessageKey.EMAIL_DELIVERY_FAILED, false, e);
      final int attempts = message.getDeliveryAttempts() + 1;
      message.setDeliveryAttempts(attempts);
      message.setLastError(OutboxPublisher.shortError(failure));
      message.setUpdatedAt(Instant.now());
      if (failure.isPermanent() || attempts >= maxAttempts) {
        message.setStatus(OutboxStatus.DLQ_PENDING);
        message.setNextAttemptAt(null);
        message.setLeaseToken(null);
        message.setLockedUntil(null);
        log.error("Email job {} exhausted {} delivery attempts", message.getId(), attempts, e);
      } else {
        // Commit retry scheduling with the failed attempt. The outbox publisher will
        // recover publication failures; no TTL dead-letter forwarding is required.
        message.setStatus(OutboxStatus.PENDING);
        message.setNextAttemptAt(Instant.now().plusMillis(Math.max(1, retryDelayMs)));
        message.setLeaseToken(null);
        message.setLockedUntil(null);
        log.warn("Email job {} scheduled for attempt {}", message.getId(), attempts + 1, e);
      }
    }
  }

  private boolean envelopeMatches(final OutboxMessage message, final AsyncJobEnvelope envelope) {
    return message.getJobType() == envelope.jobType()
        && Objects.equals(message.getCompanyId(), envelope.companyId())
        && Objects.equals(message.getDocumentId(), envelope.documentId())
        && envelope.encryptedPayload() != null
        && MessageDigest.isEqual(message.getEncryptedPayload(), envelope.encryptedPayload());
  }
}
