package dev.roland.inventory_management_backend.common.async;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.common.configuration.AsyncJobConfiguration;
import dev.roland.inventory_management_backend.common.message.AsyncJobMessageKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Publishes claimed outbox rows and waits for broker confirms outside database transactions. */
@Service
@ConditionalOnProperty(
    name = "app.outbox.publisher-enabled",
    havingValue = "true",
    matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {

  private final OutboxClaimService claimService;
  private final RabbitTemplate rabbitTemplate;

  @Value("${app.outbox.publish-batch-size:50}")
  private int batchSize;

  @Value("${app.outbox.publish-confirm-timeout-ms:10000}")
  private int confirmTimeoutMs;

  /** Publishes eligible jobs and persists broker outcomes. */
  @Scheduled(fixedDelayString = "${app.outbox.publish-interval-ms:1000}")
  public void publishPending() {
    final List<ClaimedOutboxMessage> claimed = claimService.claimPending(batchSize);
    final List<PendingPublish> pending = new ArrayList<>(claimed.size());
    for (final ClaimedOutboxMessage job : claimed) {
      try {
        final CorrelationData correlation = new CorrelationData(job.id().toString());
        rabbitTemplate.convertAndSend(
            AsyncJobConfiguration.EXCHANGE,
            job.deadLetter()
                ? job.jobType().getDeadLetterRoutingKey()
                : job.jobType().getRoutingKey(),
            job.toEnvelope(),
            brokerMessage -> {
              if (job.deadLetter()) {
                brokerMessage.getMessageProperties().setHeader("failure", job.failure());
                brokerMessage.getMessageProperties().setHeader("jobType", job.jobType().name());
                brokerMessage.getMessageProperties().setHeader("companyId", job.companyId());
                brokerMessage.getMessageProperties().setHeader("documentId", job.documentId());
              }
              brokerMessage.getMessageProperties().setMessageId(job.id().toString());
              brokerMessage.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
              return brokerMessage;
            },
            correlation);
        pending.add(new PendingPublish(job, correlation));
      } catch (RuntimeException e) {
        recordFailure(job, e);
      }
    }

    final long deadline =
        System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(Math.max(1, confirmTimeoutMs));
    for (final PendingPublish publish : pending) {
      try {
        final long remainingNanos = deadline - System.nanoTime();
        if (remainingNanos <= 0) {
          throw new AsyncJobException(AsyncJobMessageKey.RABBITMQ_PUBLISH_REJECTED, false);
        }
        final CorrelationData.Confirm confirm =
            publish.correlation().getFuture().get(remainingNanos, TimeUnit.NANOSECONDS);
        if (!confirm.isAck()) {
          throw new AsyncJobException(AsyncJobMessageKey.RABBITMQ_PUBLISH_REJECTED, false);
        }
        if (publish.correlation().getReturned() != null) {
          throw new AsyncJobException(AsyncJobMessageKey.RABBITMQ_MESSAGE_UNROUTABLE, false);
        }
        if (!claimService.markPublished(publish.job(), Instant.now())) {
          log.info(
              "Outbox publication completed after its lease expired for job {}",
              publish.job().id());
        }
      } catch (Exception e) {
        if (e instanceof InterruptedException) {
          Thread.currentThread().interrupt();
        }
        recordFailure(publish.job(), e);
      }
    }
  }

  private void recordFailure(final ClaimedOutboxMessage job, final Throwable error) {
    final AsyncJobException keyedError =
        error instanceof AsyncJobException asyncJobException
            ? asyncJobException
            : new AsyncJobException(AsyncJobMessageKey.RABBITMQ_PUBLISH_REJECTED, false, error);
    final Instant now = Instant.now();
    final int failures = job.publishAttempts() + 1;
    final long delaySeconds = Math.min(300, 1L << Math.min(failures, 8));
    if (!claimService.markPublishFailed(
        job, shortError(keyedError), now.plusSeconds(delaySeconds), now)) {
      log.info("Outbox failure arrived after its lease expired for job {}", job.id());
      return;
    }
    log.warn("Outbox publish failed for job {}", job.id(), keyedError);
  }

  /**
   * Formats a bounded diagnostic string for persistence.
   *
   * @param exception failure to describe
   * @return diagnostic text truncated to the database limit
   */
  static String shortError(final Throwable exception) {
    final String errorKey =
        exception instanceof AsyncJobException asyncJobException
            ? asyncJobException.getMessageKey().getKey()
            : exception.getClass().getSimpleName();
    final Throwable diagnostic =
        exception instanceof AsyncJobException && exception.getCause() != null
            ? exception.getCause()
            : exception;
    final String message = diagnostic.getMessage();
    final String details =
        errorKey
            + ": "
            + diagnostic.getClass().getSimpleName()
            + (message == null ? "" : ": " + message);
    return details.substring(0, Math.min(2000, details.length()));
  }

  private record PendingPublish(ClaimedOutboxMessage job, CorrelationData correlation) {}
}
