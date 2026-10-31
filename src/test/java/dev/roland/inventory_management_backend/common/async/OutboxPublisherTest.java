package dev.roland.inventory_management_backend.common.async;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import dev.roland.inventory_management_backend.common.configuration.AsyncJobConfiguration;
import dev.roland.inventory_management_backend.common.message.AsyncJobMessageKey;

@ExtendWith(MockitoExtension.class)
class OutboxPublisherTest {

  @Mock private OutboxClaimService claimService;
  @Mock private RabbitTemplate rabbitTemplate;

  @Test
  void doesNothingWhenNoJobsAreEligible() {
    when(claimService.claimPending(50)).thenReturn(List.of());

    publisher().publishPending();

    verify(claimService).claimPending(50);
  }

  @Test
  void acknowledgesRoutedPublicationAndAppliesPersistentMessageMetadata() throws Exception {
    final ClaimedOutboxMessage job = job(false);
    when(claimService.claimPending(50)).thenReturn(List.of(job));
    doAnswer(
            invocation -> {
              final MessagePostProcessor processor = invocation.getArgument(3);
              final CorrelationData correlation = invocation.getArgument(4);
              final Message message = processor.postProcessMessage(new Message(new byte[0]));
              Assertions.assertEquals(
                  job.id().toString(), message.getMessageProperties().getMessageId());
              Assertions.assertEquals(
                  MessageDeliveryMode.PERSISTENT, message.getMessageProperties().getDeliveryMode());
              correlation.getFuture().complete(new CorrelationData.Confirm(true, null));
              return null;
            })
        .when(rabbitTemplate)
        .convertAndSend(
            eq(AsyncJobConfiguration.EXCHANGE),
            anyString(),
            any(AsyncJobEnvelope.class),
            any(MessagePostProcessor.class),
            any(CorrelationData.class));

    publisher().publishPending();

    verify(claimService).markPublished(eq(job), any());
  }

  @Test
  void persistsRejectedAndUnroutableConfirmsForRetry() {
    final ClaimedOutboxMessage rejected = job(false);
    final ClaimedOutboxMessage returned = job(false);
    final ClaimedOutboxMessage confirmed = job(false);
    final AtomicInteger sends = new AtomicInteger();
    when(claimService.claimPending(50)).thenReturn(List.of(rejected, returned, confirmed));
    when(claimService.markPublishFailed(any(), anyString(), any(), any())).thenReturn(true);
    when(claimService.markPublished(any(), any())).thenReturn(true);
    doAnswer(
            invocation -> {
              final CorrelationData correlation = invocation.getArgument(4);
              final int send = sends.getAndIncrement();
              correlation.getFuture().complete(new CorrelationData.Confirm(send != 0, null));
              if (send == 1) {
                correlation.setReturned(
                    new ReturnedMessage(new Message(new byte[0]), 312, "NO_ROUTE", "x", "r"));
              }
              return null;
            })
        .when(rabbitTemplate)
        .convertAndSend(
            eq(AsyncJobConfiguration.EXCHANGE),
            anyString(),
            any(AsyncJobEnvelope.class),
            any(MessagePostProcessor.class),
            any(CorrelationData.class));

    publisher().publishPending();

    verify(claimService).markPublishFailed(eq(rejected), anyString(), any(), any());
    verify(claimService).markPublishFailed(eq(returned), anyString(), any(), any());
    verify(claimService).markPublished(eq(confirmed), any());
  }

  @Test
  void handlesImmediateBrokerFailureAndTimedOutConfirm() {
    final ClaimedOutboxMessage failedSend = job(false);
    final ClaimedOutboxMessage timeout = job(false);
    when(claimService.claimPending(50)).thenReturn(List.of(failedSend, timeout));
    doThrow(new IllegalStateException("connection unavailable"))
        .doAnswer(
            invocation -> {
              final CorrelationData correlation = invocation.getArgument(4);
              correlation.setReturned(null);
              return null;
            })
        .when(rabbitTemplate)
        .convertAndSend(
            eq(AsyncJobConfiguration.EXCHANGE),
            anyString(),
            any(AsyncJobEnvelope.class),
            any(MessagePostProcessor.class),
            any(CorrelationData.class));
    ReflectionTestUtils.setField(publisher(), "confirmTimeoutMs", 1);

    publisher().publishPending();

    verify(claimService, Mockito.times(2))
        .markPublishFailed(any(ClaimedOutboxMessage.class), anyString(), any(), any());
  }

  @Test
  void decoratesDeadLetterMessagesWithFailureContext() throws Exception {
    final ClaimedOutboxMessage job =
        new ClaimedOutboxMessage(
            UUID.randomUUID(),
            UUID.randomUUID(),
            AsyncJobType.EMAIL,
            5,
            new byte[] {1},
            12L,
            34L,
            true,
            "delivery failed");
    when(claimService.claimPending(50)).thenReturn(List.of(job));
    doAnswer(
            invocation -> {
              final MessagePostProcessor processor = invocation.getArgument(3);
              final CorrelationData correlation = invocation.getArgument(4);
              final Message message = processor.postProcessMessage(new Message(new byte[0]));
              final MessageProperties properties = message.getMessageProperties();
              Assertions.assertEquals("delivery failed", properties.getHeader("failure"));
              Assertions.assertEquals("EMAIL", properties.getHeader("jobType"));
              Assertions.assertEquals(
                  12L, ((Number) properties.getHeader("companyId")).longValue());
              Assertions.assertEquals(
                  34L, ((Number) properties.getHeader("documentId")).longValue());
              correlation.getFuture().complete(new CorrelationData.Confirm(true, null));
              return null;
            })
        .when(rabbitTemplate)
        .convertAndSend(
            eq(AsyncJobConfiguration.EXCHANGE),
            anyString(),
            any(AsyncJobEnvelope.class),
            any(MessagePostProcessor.class),
            any(CorrelationData.class));

    publisher().publishPending();

    verify(claimService).markPublished(eq(job), any());
  }

  @Test
  void formatsShortDiagnosticsForPlainKeyedAndLongFailures() {
    assertEquals(
        "IllegalStateException: IllegalStateException: unavailable",
        OutboxPublisher.shortError(new IllegalStateException("unavailable")));
    assertEquals(
        "IllegalArgumentException: IllegalArgumentException",
        OutboxPublisher.shortError(new IllegalArgumentException()));
    final AsyncJobException keyed =
        new AsyncJobException(
            AsyncJobMessageKey.RABBITMQ_PUBLISH_REJECTED,
            false,
            new IllegalStateException("broker down"));
    Assertions.assertTrue(
        OutboxPublisher.shortError(keyed)
            .startsWith("async.rabbitmq.publish-rejected: IllegalStateException: broker down"));
    Assertions.assertEquals(
        2000, OutboxPublisher.shortError(new IllegalStateException("x".repeat(3000))).length());
  }

  private OutboxPublisher publisher() {
    final OutboxPublisher publisher = new OutboxPublisher(claimService, rabbitTemplate);
    ReflectionTestUtils.setField(publisher, "batchSize", 50);
    ReflectionTestUtils.setField(publisher, "confirmTimeoutMs", 100);
    return publisher;
  }

  private ClaimedOutboxMessage job(final boolean deadLetter) {
    return new ClaimedOutboxMessage(
        UUID.randomUUID(),
        UUID.randomUUID(),
        AsyncJobType.EMAIL,
        0,
        new byte[] {1},
        null,
        null,
        deadLetter,
        deadLetter ? "failed" : null);
  }
}
