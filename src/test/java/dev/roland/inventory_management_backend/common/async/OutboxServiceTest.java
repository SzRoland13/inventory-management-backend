package dev.roland.inventory_management_backend.common.async;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.roland.inventory_management_backend.features.one_time_code.mail.OneTimeCodeMailable;

@ExtendWith(MockitoExtension.class)
class OutboxServiceTest {

  private static final String SECRET = "test-outbox-encryption-secret-at-least-32-bytes";

  @Mock private OutboxMessageRepository repository;

  @Test
  void encryptsEmailPayloadAndPreservesTypedMailableData() throws Exception {
    final ObjectMapper mapper = new ObjectMapper();
    final OutboxEncryptionService encryption = new OutboxEncryptionService(SECRET);
    final OutboxService service = new OutboxService(repository, encryption, mapper);
    when(repository.save(any(OutboxMessage.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    final OneTimeCodeMailable mailable =
        new OneTimeCodeMailable("alice@example.com", "Alice", "private-code", "hu-HU");

    final UUID jobId = service.enqueueEmail(mailable);

    final ArgumentCaptor<OutboxMessage> captor = ArgumentCaptor.forClass(OutboxMessage.class);
    verify(repository).save(captor.capture());
    final OutboxMessage persisted = captor.getValue();
    final String storedValue = new String(persisted.getEncryptedPayload(), StandardCharsets.UTF_8);
    assertEquals(jobId, persisted.getId());
    assertEquals(AsyncJobType.EMAIL, persisted.getJobType());
    assertNotEquals("private-code", storedValue);
    assertFalse(storedValue.contains("private-code"));

    final OutboxService.EmailPayload payload =
        OutboxService.decodeEmailPayload(
            encryption.decrypt(persisted.getEncryptedPayload()), mapper);
    assertEquals("alice@example.com", payload.recipient());
    assertEquals("hu-HU", payload.languageTag());
    assertEquals("private-code", payload.templateVariables().get("oneTimeCode"));
    assertTrue(payload.toMailable() instanceof OneTimeCodeMailable);
  }

  @Test
  void rejectsTamperedPayload() {
    final OutboxEncryptionService encryption = new OutboxEncryptionService(SECRET);
    final byte[] encrypted = encryption.encrypt("payload".getBytes(StandardCharsets.UTF_8));
    encrypted[encrypted.length - 1] ^= 1;

    Assertions.assertThrows(AsyncJobException.class, () -> encryption.decrypt(encrypted));
  }

  @Test
  void rejectsMissingEncryptionConfiguration() {
    final OutboxEncryptionService encryption = new OutboxEncryptionService("too-short");

    Assertions.assertThrows(
        AsyncJobException.class,
        () -> encryption.encrypt("payload".getBytes(StandardCharsets.UTF_8)));
    Assertions.assertThrows(AsyncJobException.class, () -> encryption.decrypt(new byte[13]));
  }

  @Test
  void rejectsInvalidEncryptedPayloads() {
    final OutboxEncryptionService encryption = new OutboxEncryptionService(SECRET);
    final OutboxEncryptionService wrongKey =
        new OutboxEncryptionService("a-different-secret-with-at-least-32-bytes");

    Assertions.assertThrows(AsyncJobException.class, () -> encryption.decrypt(new byte[12]));
    final byte[] encrypted = encryption.encrypt("payload".getBytes(StandardCharsets.UTF_8));
    Assertions.assertThrows(AsyncJobException.class, () -> wrongKey.decrypt(encrypted));
  }
}
