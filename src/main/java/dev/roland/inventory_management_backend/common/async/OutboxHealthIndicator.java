package dev.roland.inventory_management_backend.common.async;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** Exposes pending and failed outbox counts through the Actuator health endpoint. */
@Component
@RequiredArgsConstructor
public class OutboxHealthIndicator implements HealthIndicator {

  private final OutboxMessageRepository repository;

  @Override
  public Health health() {
    final long pending = repository.countByStatus(OutboxStatus.PENDING);
    final long publishing = repository.countByStatus(OutboxStatus.PUBLISHING);
    final long failed = repository.countByStatus(OutboxStatus.FAILED);
    final long deadLetterPending = repository.countByStatus(OutboxStatus.DLQ_PENDING);
    final long deadLetterPublishing = repository.countByStatus(OutboxStatus.DLQ_PUBLISHING);
    return Health.up()
        .withDetail("deadLetterPending", deadLetterPending)
        .withDetail("deadLetterPublishing", deadLetterPublishing)
        .withDetail("pending", pending)
        .withDetail("publishing", publishing)
        .withDetail("failed", failed)
        .build();
  }
}
