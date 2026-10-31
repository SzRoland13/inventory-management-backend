package dev.roland.inventory_management_backend.common.async;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class OutboxClaimServiceTest {

  @Mock private OutboxMessageRepository repository;

  @Test
  void claimsPendingRowsWithLeasesBeforeReturningThemForPublishing() {
    final UUID id = UUID.randomUUID();
    final OutboxMessage pending =
        new OutboxMessage(id, AsyncJobType.EMAIL, new byte[] {1, 2, 3}, null, null);
    when(repository.lockPending(10)).thenReturn(List.of(pending));
    final OutboxClaimService service = new OutboxClaimService(repository);
    ReflectionTestUtils.setField(service, "leaseSeconds", 30);

    final List<ClaimedOutboxMessage> claimed = service.claimPending(10);

    assertEquals(1, claimed.size());
    assertEquals(id, claimed.getFirst().id());
    assertEquals(AsyncJobType.EMAIL, claimed.getFirst().jobType());
    assertEquals(OutboxStatus.PUBLISHING, pending.getStatus());
    assertNotNull(pending.getLeaseToken());
    assertTrue(pending.getLockedUntil().isAfter(Instant.now()));
  }

  @Test
  void publishFailurePersistsBackoffAndClearsLease() {
    final UUID id = UUID.randomUUID();
    final UUID lease = UUID.randomUUID();
    final ClaimedOutboxMessage claimed =
        new ClaimedOutboxMessage(id, lease, AsyncJobType.EMAIL, 2, new byte[] {1}, null, null);
    final Instant nextAttempt = Instant.now().plusSeconds(4);
    final Instant now = Instant.now();
    when(repository.markPublishFailed(
            id,
            lease,
            OutboxStatus.PUBLISHING,
            OutboxStatus.PENDING,
            "keyed error",
            nextAttempt,
            now))
        .thenReturn(1);
    final OutboxClaimService service = new OutboxClaimService(repository);

    assertTrue(service.markPublishFailed(claimed, "keyed error", nextAttempt, now));

    verify(repository)
        .markPublishFailed(
            id,
            lease,
            OutboxStatus.PUBLISHING,
            OutboxStatus.PENDING,
            "keyed error",
            nextAttempt,
            now);
  }

  @Test
  void clampsClaimAndLeaseBoundsAndHandlesAllPublicationOutcomes() {
    final OutboxMessage pending =
        new OutboxMessage(UUID.randomUUID(), AsyncJobType.EMAIL, new byte[] {1}, null, null);
    when(repository.lockPending(1)).thenReturn(List.of(pending));
    final OutboxClaimService service = new OutboxClaimService(repository);
    ReflectionTestUtils.setField(service, "leaseSeconds", 0);

    final ClaimedOutboxMessage claimed = service.claimPending(0).getFirst();

    assertTrue(claimed.leaseToken() != null);
    verify(repository).lockPending(1);
    when(repository.markPublished(
            claimed.id(),
            claimed.leaseToken(),
            OutboxStatus.PUBLISHING,
            OutboxStatus.QUEUED,
            Instant.EPOCH))
        .thenReturn(1);
    assertTrue(service.markPublished(claimed, Instant.EPOCH));
    assertEquals(OutboxStatus.PUBLISHING, pending.getStatus());

    final ClaimedOutboxMessage deadLetter =
        new ClaimedOutboxMessage(
            UUID.randomUUID(),
            UUID.randomUUID(),
            AsyncJobType.EMAIL,
            1,
            new byte[] {2},
            null,
            null,
            true,
            "failed");
    when(repository.lockById(deadLetter.id())).thenReturn(Optional.empty());
    assertFalse(service.markPublished(deadLetter, Instant.EPOCH));
    when(repository.markPublishFailed(any(), any(), any(), any(), anyString(), any(), any()))
        .thenReturn(1);
    assertTrue(service.markPublishFailed(deadLetter, "ignored", Instant.EPOCH, Instant.EPOCH));
    verify(repository)
        .markPublishFailed(
            deadLetter.id(),
            deadLetter.leaseToken(),
            OutboxStatus.DLQ_PUBLISHING,
            OutboxStatus.DLQ_PENDING,
            "failed",
            Instant.EPOCH,
            Instant.EPOCH);
  }
}
