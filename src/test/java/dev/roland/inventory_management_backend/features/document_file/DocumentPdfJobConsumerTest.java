package dev.roland.inventory_management_backend.features.document_file;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.roland.inventory_management_backend.common.async.AsyncJobEnvelope;
import dev.roland.inventory_management_backend.common.async.AsyncJobType;
import dev.roland.inventory_management_backend.common.async.OutboxEncryptionService;
import dev.roland.inventory_management_backend.common.async.OutboxMessage;
import dev.roland.inventory_management_backend.common.async.OutboxMessageRepository;
import dev.roland.inventory_management_backend.common.async.OutboxStatus;
import dev.roland.inventory_management_backend.common.configuration.AppConfiguration;
import dev.roland.inventory_management_backend.features.company.Company;
import dev.roland.inventory_management_backend.features.document.Document;
import dev.roland.inventory_management_backend.features.document.repository.DocumentRepository;
import dev.roland.inventory_management_backend.features.media_asset.service.MediaAssetService;
import dev.roland.inventory_management_backend.features.media_asset.service.ThumbnailImageProcessor;
import dev.roland.inventory_management_backend.features.media_usage.service.MediaUsageService;

@ExtendWith(MockitoExtension.class)
class DocumentPdfJobConsumerTest {
  @Mock private OutboxMessageRepository outboxRepository;
  @Mock private OutboxEncryptionService encryptionService;
  @Mock private ObjectMapper objectMapper;
  @Mock private DocumentRepository documentRepository;
  @Mock private DocumentFileRepository fileRepository;
  @Mock private MediaAssetService mediaAssetService;
  @Mock private MediaUsageService mediaUsageService;
  @Mock private dev.roland.inventory_management_backend.common.service.ObjectStorageService storage;
  @Mock private DocumentPdfFactory pdfFactory;

  @Mock private ThumbnailImageProcessor thumbnailImageProcessor;

  @Mock private AppConfiguration appConfiguration;

  @InjectMocks private DocumentPdfJobConsumer consumer;

  @Test
  void usesDocumentNumberForPdfFilenameAndSafeDraftFallback() {
    final var numbered = Document.builder().id(3L).documentNumber("DN/2026 00184").build();
    assertEquals("DN-2026-00184.pdf", consumer.pdfFilename(numbered));

    final var draft = Document.builder().id(7L).build();
    assertEquals("draft-document-7.pdf", consumer.pdfFilename(draft));

    final var blankNumber = Document.builder().id(8L).documentNumber(" ").build();
    assertEquals("draft-document-8.pdf", consumer.pdfFilename(blankNumber));
  }

  @Test
  void rejectsAnEnvelopeForAMissingOutboxJob() {
    final var envelope = envelope(AsyncJobType.DOCUMENT_PDF, new byte[] {1});
    when(outboxRepository.lockById(envelope.id())).thenReturn(Optional.empty());

    assertThrows(DocumentPdfJobException.class, () -> consumer.consume(envelope));
  }

  @Test
  void ignoresTerminalJobsAndJobsWhoseRetryTimeHasNotArrived() {
    final var terminalEnvelope = envelope(AsyncJobType.DOCUMENT_PDF, new byte[] {1});
    final var terminal = message(OutboxStatus.SENT);
    when(outboxRepository.lockById(terminalEnvelope.id())).thenReturn(Optional.of(terminal));
    consumer.consume(terminalEnvelope);

    final var delayedEnvelope = envelope(AsyncJobType.DOCUMENT_PDF, new byte[] {2});
    final var delayed = message(OutboxStatus.PENDING);
    delayed.setNextAttemptAt(Instant.now().plusSeconds(60));
    when(outboxRepository.lockById(delayedEnvelope.id())).thenReturn(Optional.of(delayed));
    consumer.consume(delayedEnvelope);
  }

  @Test
  void marksAnEnvelopeWithMismatchedJobTypeAsDeadLetterPending() {
    final byte[] payload = {3};
    final var envelope = envelope(AsyncJobType.EMAIL, payload);
    final var message = message(OutboxStatus.PENDING);
    message.setEncryptedPayload(payload);
    when(outboxRepository.lockById(envelope.id())).thenReturn(Optional.of(message));

    consumer.consume(envelope);

    assertEquals(OutboxStatus.DLQ_PENDING, message.getStatus());
    assertEquals(1, message.getDeliveryAttempts());
    verify(encryptionService).decrypt(payload);
  }

  @Test
  void retriesAValidJobWhenTheRendererCannotBeReached() {
    final byte[] encrypted = {4};
    final UUID jobId = UUID.randomUUID();
    final var envelope = new AsyncJobEnvelope(jobId, AsyncJobType.DOCUMENT_PDF, encrypted, 9L, 3L);
    final var message = new OutboxMessage(jobId, AsyncJobType.DOCUMENT_PDF, encrypted, 9L, 3L);
    final var document =
        Document.builder().id(3L).company(Company.builder().id(9L).build()).build();
    final var file = new DocumentFile();
    file.setDocument(document);
    file.setSource(DocumentFileSource.GENERATED_PDF);
    file.setAttempt(1);
    when(outboxRepository.lockById(jobId)).thenReturn(Optional.of(message));
    when(encryptionService.decrypt(encrypted))
        .thenReturn("{\"documentId\":3,\"fileId\":6}".getBytes(StandardCharsets.UTF_8));
    when(documentRepository.findByIdAndCompanyId(3L, 9L)).thenReturn(Optional.of(document));
    when(fileRepository.findById(6L)).thenReturn(Optional.of(file));
    when(pdfFactory.createHtml(document, null)).thenReturn("<html></html>");
    when(appConfiguration.getDocumentPdfRendererUrl()).thenReturn("http://127.0.0.1:1");
    when(appConfiguration.getDocumentPdfRendererPath()).thenReturn("/api/v1/render");
    when(appConfiguration.getDocumentPdfRendererToken()).thenReturn("");
    when(appConfiguration.getDocumentPdfMaxAttempts()).thenReturn(3);
    when(appConfiguration.getOutboxRetryDelayMs()).thenReturn(1000);
    final var realConsumer =
        new DocumentPdfJobConsumer(
            outboxRepository,
            encryptionService,
            new ObjectMapper(),
            documentRepository,
            fileRepository,
            mediaAssetService,
            mediaUsageService,
            storage,
            pdfFactory,
            thumbnailImageProcessor,
            appConfiguration);

    realConsumer.consume(envelope);

    assertEquals(OutboxStatus.PENDING, message.getStatus());
    assertEquals(1, message.getDeliveryAttempts());
    assertEquals(DocumentFileStatus.PROCESSING, file.getStatus());
  }

  private static AsyncJobEnvelope envelope(final AsyncJobType type, final byte[] payload) {
    return new AsyncJobEnvelope(UUID.randomUUID(), type, payload, 9L, 3L);
  }

  private static OutboxMessage message(final OutboxStatus status) {
    final var message =
        new OutboxMessage(UUID.randomUUID(), AsyncJobType.DOCUMENT_PDF, new byte[] {1}, 9L, 3L);
    message.setStatus(status);
    return message;
  }
}
