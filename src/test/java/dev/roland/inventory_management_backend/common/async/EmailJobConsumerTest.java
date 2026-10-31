package dev.roland.inventory_management_backend.common.async;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.roland.inventory_management_backend.common.configuration.AsyncJobConfiguration;
import dev.roland.inventory_management_backend.common.message.AsyncJobMessageKey;
import dev.roland.inventory_management_backend.common.service.EmailService;
import dev.roland.inventory_management_backend.features.one_time_code.mail.OneTimeCodeMailable;

@ExtendWith(MockitoExtension.class)
class EmailJobConsumerTest {

  private static final String SECRET = "test-outbox-encryption-secret-at-least-32-bytes";

  @Mock private OutboxMessageRepository repository;
  @Mock private EmailService emailService;
  @Mock private RabbitTemplate rabbitTemplate;

  @Test
  void skipsAlreadyCompletedMessageOnRedelivery() {
    final OutboxMessage message =
        new OutboxMessage(UUID.randomUUID(), AsyncJobType.EMAIL, new byte[0], null, null);
    message.setStatus(OutboxStatus.SENT);
    when(repository.lockById(message.getId())).thenReturn(Optional.of(message));
    final EmailJobConsumer consumer = consumer();

    consumer.consume(
        new AsyncJobEnvelope(message.getId(), AsyncJobType.EMAIL, new byte[0], null, null));

    verify(emailService, never()).send(any(OneTimeCodeMailable.class));
  }

  @Test
  void persistsRetryWithoutBrokerForwardingAndIgnoresEarlyRedelivery() throws Exception {
    final OutboxEncryptionService encryption = new OutboxEncryptionService(SECRET);
    final ObjectMapper mapper = new ObjectMapper();
    final byte[] payload =
        mapper.writeValueAsBytes(
            OutboxService.EmailPayload.from(
                new OneTimeCodeMailable("alice@example.com", "Alice", "123456", "en")));
    final UUID id = UUID.randomUUID();
    final OutboxMessage message =
        new OutboxMessage(id, AsyncJobType.EMAIL, encryption.encrypt(payload), null, null);
    message.setStatus(OutboxStatus.QUEUED);
    when(repository.lockById(id)).thenReturn(Optional.of(message));
    doThrow(new AsyncJobException(AsyncJobMessageKey.EMAIL_DELIVERY_FAILED, false))
        .when(emailService)
        .send(any(OneTimeCodeMailable.class));
    final EmailJobConsumer consumer = consumer();

    consumer.consume(
        new AsyncJobEnvelope(id, AsyncJobType.EMAIL, message.getEncryptedPayload(), null, null));

    assertEquals(1, message.getDeliveryAttempts());
    assertEquals(OutboxStatus.PENDING, message.getStatus());
    Assertions.assertTrue(message.getNextAttemptAt().isAfter(Instant.now()));
    consumer.consume(
        new AsyncJobEnvelope(id, AsyncJobType.EMAIL, message.getEncryptedPayload(), null, null));
    verify(emailService).send(any(OneTimeCodeMailable.class));
    verify(rabbitTemplate, never())
        .convertAndSend(
            eq(AsyncJobConfiguration.EXCHANGE),
            any(String.class),
            any(AsyncJobEnvelope.class),
            any(MessagePostProcessor.class),
            any(CorrelationData.class));
  }

  @Test
  void persistsExhaustionWithoutBrokerAndNeverResendsOnRedelivery() throws Exception {
    final OutboxEncryptionService encryption = new OutboxEncryptionService(SECRET);
    final ObjectMapper mapper = new ObjectMapper();
    final byte[] payload =
        mapper.writeValueAsBytes(
            OutboxService.EmailPayload.from(
                new OneTimeCodeMailable("alice@example.com", "Alice", "123456", "en")));
    final UUID id = UUID.randomUUID();
    final OutboxMessage message =
        new OutboxMessage(id, AsyncJobType.EMAIL, encryption.encrypt(payload), null, null);
    message.setStatus(OutboxStatus.QUEUED);
    message.setDeliveryAttempts(4);
    when(repository.lockById(id)).thenReturn(Optional.of(message));
    doThrow(new AsyncJobException(AsyncJobMessageKey.EMAIL_DELIVERY_FAILED, false))
        .when(emailService)
        .send(any(OneTimeCodeMailable.class));
    final EmailJobConsumer consumer = consumer();

    consumer.consume(
        new AsyncJobEnvelope(id, AsyncJobType.EMAIL, message.getEncryptedPayload(), null, null));

    assertEquals(5, message.getDeliveryAttempts());
    assertEquals(OutboxStatus.DLQ_PENDING, message.getStatus());
    consumer.consume(
        new AsyncJobEnvelope(id, AsyncJobType.EMAIL, message.getEncryptedPayload(), null, null));
    verify(emailService).send(any(OneTimeCodeMailable.class));
    verify(rabbitTemplate, never())
        .convertAndSend(
            eq(AsyncJobConfiguration.EXCHANGE),
            any(String.class),
            any(AsyncJobEnvelope.class),
            any(MessagePostProcessor.class),
            any(CorrelationData.class));
  }

  private EmailJobConsumer consumer() {
    final EmailJobConsumer consumer =
        new EmailJobConsumer(
            repository, new OutboxEncryptionService(SECRET), new ObjectMapper(), emailService);
    ReflectionTestUtils.setField(consumer, "maxAttempts", 5);
    ReflectionTestUtils.setField(consumer, "retryDelayMs", 3000);
    return consumer;
  }
}
