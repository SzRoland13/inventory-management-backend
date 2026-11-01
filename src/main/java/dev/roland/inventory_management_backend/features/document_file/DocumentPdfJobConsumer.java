package dev.roland.inventory_management_backend.features.document_file;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

import jakarta.transaction.Transactional;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.roland.inventory_management_backend.common.async.AsyncJobEnvelope;
import dev.roland.inventory_management_backend.common.async.AsyncJobType;
import dev.roland.inventory_management_backend.common.async.OutboxEncryptionService;
import dev.roland.inventory_management_backend.common.async.OutboxMessage;
import dev.roland.inventory_management_backend.common.async.OutboxMessageRepository;
import dev.roland.inventory_management_backend.common.async.OutboxStatus;
import dev.roland.inventory_management_backend.common.configuration.AppConfiguration;
import dev.roland.inventory_management_backend.common.service.ObjectStorageService;
import dev.roland.inventory_management_backend.features.document.Document;
import dev.roland.inventory_management_backend.features.document.message.DocumentApiMessageKey;
import dev.roland.inventory_management_backend.features.document.repository.DocumentRepository;
import dev.roland.inventory_management_backend.features.media_asset.MediaAsset;
import dev.roland.inventory_management_backend.features.media_asset.service.MediaAssetService;
import dev.roland.inventory_management_backend.features.media_usage.MediaUsage;
import dev.roland.inventory_management_backend.features.media_usage.enumeration.MediaEntityType;
import dev.roland.inventory_management_backend.features.media_usage.enumeration.MediaUsageType;
import dev.roland.inventory_management_backend.features.media_usage.service.MediaUsageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Renders document snapshots asynchronously and records durable attempt outcomes. */
@Component
@RequiredArgsConstructor
@Slf4j
public class DocumentPdfJobConsumer {
  private final OutboxMessageRepository outboxRepository;
  private final OutboxEncryptionService encryptionService;
  private final ObjectMapper objectMapper;
  private final DocumentRepository documentRepository;
  private final DocumentFileRepository fileRepository;
  private final MediaAssetService mediaAssetService;
  private final MediaUsageService mediaUsageService;
  private final ObjectStorageService objectStorage;
  private final DocumentPdfFactory documentPdfFactory;
  private final AppConfiguration appConfiguration;

  /**
   * Processes a queued document PDF job.
   *
   * @param envelope envelope value
   */
  @RabbitListener(queues = "inventory.document-pdf")
  @Transactional
  public void consume(final AsyncJobEnvelope envelope) {
    final OutboxMessage message =
        outboxRepository
            .lockById(envelope.id())
            .orElseThrow(
                () ->
                    new DocumentPdfJobException(
                        DocumentApiMessageKey.DOCUMENT_PDF_JOB_NOT_FOUND, true));
    if (terminal(message)
        || (message.getNextAttemptAt() != null
            && message.getNextAttemptAt().isAfter(Instant.now()))) {
      return;
    }
    try {
      if (!matches(message, envelope) || message.getJobType() != AsyncJobType.DOCUMENT_PDF) {
        throw new DocumentPdfJobException(DocumentApiMessageKey.DOCUMENT_PDF_JOB_INVALID, true);
      }
      final JsonNode payload = readPayload(message);
      final long documentId = payload.path("documentId").asLong();
      final long fileId = payload.path("fileId").asLong();
      if (!Objects.equals(message.getDocumentId(), documentId)
          || !Objects.equals(message.getCompanyId(), envelope.companyId())) {
        throw new DocumentPdfJobException(DocumentApiMessageKey.DOCUMENT_PDF_JOB_INVALID, true);
      }
      final Document document =
          documentRepository
              .findByIdAndCompanyId(documentId, message.getCompanyId())
              .orElseThrow(
                  () ->
                      new DocumentPdfJobException(
                          DocumentApiMessageKey.DOCUMENT_PDF_JOB_INVALID, true));
      final DocumentFile file =
          fileRepository
              .findById(fileId)
              .filter(
                  f ->
                      f.getDocument().getId().equals(documentId)
                          && f.getSource() == DocumentFileSource.GENERATED_PDF)
              .orElseThrow(
                  () ->
                      new DocumentPdfJobException(
                          DocumentApiMessageKey.DOCUMENT_PDF_JOB_INVALID, true));
      file.setStatus(DocumentFileStatus.PROCESSING);
      final byte[] pdf = render(document, file.getLanguageTag());
      final String path =
          objectStorage.generateSolidObjectPath(
              "document-" + documentId + "-attempt-" + file.getAttempt() + ".pdf",
              MediaEntityType.DOCUMENT,
              documentId,
              MediaUsageType.DOCUMENT_PDF);
      objectStorage.upload(path, pdf.length, "application/pdf", new ByteArrayInputStream(pdf));
      final MediaAsset asset =
          mediaAssetService.save(
              MediaAsset.builder()
                  .objectPath(path)
                  .filename("document-" + documentId + ".pdf")
                  .mimeType("application/pdf")
                  .fileSize((long) pdf.length)
                  .build());
      mediaUsageService.save(
          MediaUsage.builder()
              .mediaAsset(asset)
              .entityType(MediaEntityType.DOCUMENT)
              .entityId(documentId)
              .usageType(MediaUsageType.DOCUMENT_PDF)
              .build());
      file.setMediaAsset(asset);
      file.setStatus(DocumentFileStatus.COMPLETED);
      file.setErrorMessage(null);
      message.setStatus(OutboxStatus.SENT);
      message.setEncryptedPayload(new byte[0]);
      message.setLastError(null);
      message.setUpdatedAt(Instant.now());
    } catch (RuntimeException error) {
      final int attempts = message.getDeliveryAttempts() + 1;
      message.setDeliveryAttempts(attempts);
      final boolean permanent =
          error instanceof DocumentPdfJobException jobException && jobException.isPermanent();
      final String details = diagnostic(error);
      message.setLastError(details);
      message.setUpdatedAt(Instant.now());
      if (permanent || attempts >= Math.max(1, appConfiguration.getDocumentPdfMaxAttempts())) {
        message.setStatus(OutboxStatus.DLQ_PENDING);
        final Long fileId = extractFileId(message);
        if (fileId != null) {
          fileRepository
              .findById(fileId)
              .ifPresent(
                  file -> {
                    file.setStatus(DocumentFileStatus.FAILED);
                    file.setErrorMessage(details);
                  });
        }
        log.error(
            "Document PDF job {} failed permanently after {} attempts",
            message.getId(),
            attempts,
            error);
      } else {
        message.setStatus(OutboxStatus.PENDING);
        message.setNextAttemptAt(
            Instant.now().plusMillis(Math.max(1, appConfiguration.getOutboxRetryDelayMs())));
        log.warn(
            "Document PDF job {} scheduled for retry {}", message.getId(), attempts + 1, error);
      }
      message.setLeaseToken(null);
      message.setLockedUntil(null);
    }
  }

  private byte[] render(final Document document, final String languageTag) {
    final String html = documentPdfFactory.createHtml(document, languageTag);
    final RenderRequest request =
        new RenderRequest(
            html,
            new RenderOptions(
                "A4",
                false,
                Map.of("top", "10mm", "right", "10mm", "bottom", "10mm", "left", "10mm")));
    final SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setConnectTimeout(Duration.ofSeconds(5));
    requestFactory.setReadTimeout(Duration.ofSeconds(25));
    final RestClient.RequestBodySpec call =
        RestClient.builder()
            .baseUrl(appConfiguration.getDocumentPdfRendererUrl())
            .requestFactory(requestFactory)
            .build()
            .post()
            .uri(appConfiguration.getDocumentPdfRendererPath())
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_PDF);
    if (appConfiguration.getDocumentPdfRendererToken() != null
        && !appConfiguration.getDocumentPdfRendererToken().isBlank()) {
      call.header("Authorization", "Bearer " + appConfiguration.getDocumentPdfRendererToken());
    }
    final byte[] result;
    try {
      result = call.body(request).retrieve().body(byte[].class);
    } catch (RestClientResponseException error) {
      throw new DocumentPdfJobException(
          DocumentApiMessageKey.DOCUMENT_PDF_RENDER_FAILED,
          error.getStatusCode().is4xxClientError(),
          error);
    } catch (RestClientException error) {
      throw new DocumentPdfJobException(
          DocumentApiMessageKey.DOCUMENT_PDF_RENDER_FAILED, false, error);
    }
    if (result == null || result.length == 0) {
      throw new DocumentPdfJobException(DocumentApiMessageKey.DOCUMENT_PDF_EMPTY_RESULT, false);
    }
    return result;
  }

  private String value(final String value) {
    return value == null ? "" : value;
  }

  private String diagnostic(final RuntimeException error) {
    String diagnostic;
    if (error instanceof DocumentPdfJobException jobException) {
      final Throwable cause = jobException.getCause();
      diagnostic =
          jobException.getMessageKey().getKey()
              + (cause instanceof RuntimeException runtimeCause
                  ? ": " + diagnostic(runtimeCause)
                  : "");
    } else if (error instanceof RestClientResponseException response) {
      try {
        final JsonNode body = objectMapper.readTree(response.getResponseBodyAsByteArray());
        final JsonNode detail = body.path("error");
        if (!detail.isMissingNode()) {
          diagnostic =
              detail.path("code").asText("HTTP_" + response.getStatusCode().value())
                  + ": "
                  + detail.path("message").asText("Renderer request failed");
        } else {
          diagnostic = error.getClass().getSimpleName() + ": " + value(error.getMessage());
        }
      } catch (IOException ignored) {
        // Fall back to the bounded exception description for non-JSON responses.
        diagnostic = error.getClass().getSimpleName() + ": " + value(error.getMessage());
      }
    } else {
      diagnostic = error.getClass().getSimpleName() + ": " + value(error.getMessage());
    }
    return diagnostic.length() <= 1000 ? diagnostic : diagnostic.substring(0, 1000);
  }

  private Long extractFileId(final OutboxMessage message) {
    Long result;
    try {
      result =
          objectMapper
              .readTree(encryptionService.decrypt(message.getEncryptedPayload()))
              .path("fileId")
              .asLong();
    } catch (RuntimeException | IOException ignored) {
      // Return null when the queued payload cannot be decoded.
      result = null;
    }
    return result;
  }

  private JsonNode readPayload(final OutboxMessage message) {
    try {
      return objectMapper.readTree(encryptionService.decrypt(message.getEncryptedPayload()));
    } catch (IOException error) {
      throw new DocumentPdfJobException(
          DocumentApiMessageKey.DOCUMENT_PDF_PAYLOAD_INVALID, true, error);
    }
  }

  private boolean matches(final OutboxMessage message, final AsyncJobEnvelope envelope) {
    return message.getJobType() == envelope.jobType()
        && Objects.equals(message.getCompanyId(), envelope.companyId())
        && Objects.equals(message.getDocumentId(), envelope.documentId())
        && MessageDigest.isEqual(message.getEncryptedPayload(), envelope.encryptedPayload());
  }

  private boolean terminal(final OutboxMessage message) {
    return message.getStatus() == OutboxStatus.SENT
        || message.getStatus() == OutboxStatus.FAILED
        || message.getStatus() == OutboxStatus.DLQ_PENDING
        || message.getStatus() == OutboxStatus.DLQ_PUBLISHING;
  }

  private record RenderRequest(String html, RenderOptions options) {}

  private record RenderOptions(String format, boolean landscape, Map<String, String> margin) {}
}
