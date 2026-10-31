package dev.roland.inventory_management_backend.common.async;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OutboxMessageRepositoryTest {

  @Autowired private OutboxMessageRepository repository;

  @PersistenceContext private EntityManager entityManager;

  @Test
  void exhaustedJobRetriesDlqPublicationAndRetainsDeliveryDiagnostics() {
    final OutboxMessage message =
        new OutboxMessage(UUID.randomUUID(), AsyncJobType.EMAIL, new byte[] {1}, null, null);
    message.setStatus(OutboxStatus.DLQ_PENDING);
    message.setDeliveryAttempts(5);
    message.setLastError("SMTP exhausted");
    repository.saveAndFlush(message);
    final OutboxClaimService claims = new OutboxClaimService(repository);
    final ClaimedOutboxMessage first = claims.claimPending(5).getFirst();
    Assertions.assertTrue(first.deadLetter());
    claims.markPublishFailed(
        first, "broker unavailable", Instant.now().minusSeconds(1), Instant.now());
    repository.flush();
    entityManager.clear();
    final OutboxMessage waiting = repository.findById(message.getId()).orElseThrow();
    assertEquals(OutboxStatus.DLQ_PENDING, waiting.getStatus());
    assertEquals(5, waiting.getDeliveryAttempts());
    assertEquals("SMTP exhausted", waiting.getLastError());
    final ClaimedOutboxMessage second = claims.claimPending(5).getFirst();
    Assertions.assertTrue(second.deadLetter());
    Assertions.assertFalse(claims.markPublished(first, Instant.now()));
    Assertions.assertTrue(claims.markPublished(second, Instant.now()));
    repository.flush();
    entityManager.clear();
    final OutboxMessage completed = repository.findById(message.getId()).orElseThrow();
    assertEquals(OutboxStatus.FAILED, completed.getStatus());
    assertEquals("SMTP exhausted", completed.getLastError());
    assertEquals(0, completed.getEncryptedPayload().length);
  }

  @Test
  void deliveryRetryIsClaimableOnlyAfterPersistedDelay() {
    final OutboxMessage message =
        new OutboxMessage(UUID.randomUUID(), AsyncJobType.EMAIL, new byte[] {1}, null, null);
    message.setDeliveryAttempts(1);
    message.setNextAttemptAt(Instant.now().plusSeconds(60));
    repository.saveAndFlush(message);
    entityManager.clear();
    assertEquals(0, repository.lockPending(5).size());

    final OutboxMessage retry = repository.findById(message.getId()).orElseThrow();
    retry.setNextAttemptAt(Instant.now().minusSeconds(60));
    repository.flush();
    entityManager.clear();
    final OutboxMessage due = repository.lockPending(5).getFirst();
    assertEquals(message.getId(), due.getId());
    assertEquals(1, due.getDeliveryAttempts());
  }

  @Test
  void claimsDueRowsAndPersistsRetryState() {
    final OutboxMessage message =
        repository.save(
            new OutboxMessage(
                UUID.randomUUID(), AsyncJobType.EMAIL, new byte[] {1, 2, 3}, null, null));
    repository.flush();

    final OutboxMessage claimed = repository.lockPending(5).getFirst();
    final UUID leaseToken = UUID.randomUUID();
    claimed.setStatus(OutboxStatus.PUBLISHING);
    claimed.setLeaseToken(leaseToken);
    claimed.setLockedUntil(Instant.now().plusSeconds(30));
    repository.flush();

    final Instant nextAttemptAt = Instant.now().plusSeconds(8).truncatedTo(ChronoUnit.MICROS);
    final int updated =
        repository.markPublishFailed(
            message.getId(),
            leaseToken,
            OutboxStatus.PUBLISHING,
            OutboxStatus.PENDING,
            "async.rabbitmq.publish-rejected",
            nextAttemptAt,
            Instant.now());

    assertEquals(1, updated);
    repository.flush();
    entityManager.clear();
    final OutboxMessage failedPublish = repository.findById(message.getId()).orElseThrow();
    assertEquals(OutboxStatus.PENDING, failedPublish.getStatus());
    assertEquals(1, failedPublish.getPublishAttempts());
    assertEquals(nextAttemptAt, failedPublish.getNextAttemptAt());
    assertEquals("async.rabbitmq.publish-rejected", failedPublish.getLastError());
  }
}
