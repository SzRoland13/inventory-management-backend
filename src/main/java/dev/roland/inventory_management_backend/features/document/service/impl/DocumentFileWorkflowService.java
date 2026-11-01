package dev.roland.inventory_management_backend.features.document.service.impl;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import dev.roland.inventory_management_backend.common.async.AsyncJobType;
import dev.roland.inventory_management_backend.common.async.OutboxService;
import dev.roland.inventory_management_backend.common.exception.ApiException;
import dev.roland.inventory_management_backend.common.service.CurrentCompanyProvider;
import dev.roland.inventory_management_backend.common.service.ObjectStorageService;
import dev.roland.inventory_management_backend.features.company.Company;
import dev.roland.inventory_management_backend.features.document.Document;
import dev.roland.inventory_management_backend.features.document.dto.DocumentResponse;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentStatus;
import dev.roland.inventory_management_backend.features.document.mapper.DocumentResponseMapper;
import dev.roland.inventory_management_backend.features.document.message.DocumentApiMessageKey;
import dev.roland.inventory_management_backend.features.document_activity.DocumentActivityEvent;
import dev.roland.inventory_management_backend.features.document_file.DocumentFile;
import dev.roland.inventory_management_backend.features.document_file.DocumentFileSource;
import dev.roland.inventory_management_backend.features.document_file.DocumentFileStatus;
import dev.roland.inventory_management_backend.features.document_file.service.DocumentFileService;
import dev.roland.inventory_management_backend.features.media_asset.MediaAsset;
import dev.roland.inventory_management_backend.features.media_asset.service.MediaAssetService;
import dev.roland.inventory_management_backend.features.media_usage.MediaUsage;
import dev.roland.inventory_management_backend.features.media_usage.enumeration.MediaEntityType;
import dev.roland.inventory_management_backend.features.media_usage.enumeration.MediaUsageType;
import dev.roland.inventory_management_backend.features.media_usage.service.MediaUsageService;
import lombok.RequiredArgsConstructor;

/** Handles document attachments and generated PDFs. */
@Service
@RequiredArgsConstructor
public class DocumentFileWorkflowService {
  private final CurrentCompanyProvider companyProvider;
  private final DocumentFileService fileService;
  private final MediaAssetService mediaAssetService;
  private final MediaUsageService mediaUsageService;
  private final ObjectStorageService objectStorage;
  private final OutboxService outboxService;
  private final DocumentWorkflowAccessService accessService;
  private final DocumentWorkflowWriterService writer;
  private final DocumentResponseMapper responseMapper;

  private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

  @Transactional
  public DocumentResponse uploadFile(final Long id, final MultipartFile file) {
    final Document document = accessService.find(id);
    if (!accessService.canManageDocument(document, accessService.currentActor())) {
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_TRANSITION);
    }
    if (document.getStatus() == DocumentStatus.CANCELLED) {
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_TRANSITION);
    }
    if (file == null || file.isEmpty()) {
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_LINES);
    }
    try {
      final String filename = file.getOriginalFilename();
      final String original =
          filename == null ? "upload" : filename.replaceAll("[^a-zA-Z0-9._-]", "_");
      final String objectName = UUID.randomUUID() + "-" + original;
      final String path =
          objectStorage.generateSolidObjectPath(
              objectName, MediaEntityType.DOCUMENT, document.getId(), MediaUsageType.DOCUMENT_FILE);
      objectStorage.upload(
          path,
          file.getSize(),
          file.getContentType() == null ? DEFAULT_CONTENT_TYPE : file.getContentType(),
          file.getInputStream());
      final MediaAsset asset =
          mediaAssetService.save(
              MediaAsset.builder()
                  .objectPath(path)
                  .filename(original)
                  .mimeType(
                      file.getContentType() == null ? DEFAULT_CONTENT_TYPE : file.getContentType())
                  .fileSize(file.getSize())
                  .uploadedBy(accessService.currentActor())
                  .build());
      final MediaUsage usage =
          MediaUsage.builder()
              .mediaAsset(asset)
              .entityType(MediaEntityType.DOCUMENT)
              .entityId(id)
              .usageType(MediaUsageType.DOCUMENT_FILE)
              .build();
      mediaUsageService.save(usage);
      final DocumentFile entry = new DocumentFile();
      entry.setDocument(document);
      entry.setMediaAsset(asset);
      entry.setSource(DocumentFileSource.UPLOAD);
      fileService.save(entry);
      writer.addActivity(document, DocumentActivityEvent.FILE_UPLOADED, original);
      return responseMapper.toResponse(document);
    } catch (IOException ex) {
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_LINES);
    }
  }

  @Transactional
  public DocumentResponse generatePdf(
      final Long id, final String idempotencyKey, final String language) {
    return queuePdf(id, idempotencyKey, normalizePdfLanguage(language));
  }

  @Transactional
  public DocumentResponse retryPdf(final Long id, final String idempotencyKey) {
    return queuePdf(id, idempotencyKey, null);
  }

  DocumentResponse queuePdf(
      final Long id, final String idempotencyKey, final String requestedLanguage) {
    final Company company = companyProvider.getCurrentCompany();
    final Document document = accessService.lock(id, company.getId());
    if (!accessService.canManageDocument(document, accessService.currentActor())) {
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_TRANSITION);
    }
    if (document.getStatus() == DocumentStatus.DRAFT
        || document.getStatus() == DocumentStatus.CANCELLED) {
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_TRANSITION);
    }
    final String key =
        idempotencyKey == null || idempotencyKey.isBlank()
            ? UUID.randomUUID().toString()
            : idempotencyKey;
    final boolean duplicateRequest =
        idempotencyKey != null
            && fileService.findByDocumentIdAndIdempotencyKey(id, idempotencyKey).isPresent();
    final boolean busy =
        fileService.existsByDocumentIdAndSourceAndStatusIn(
            id,
            DocumentFileSource.GENERATED_PDF,
            List.of(DocumentFileStatus.QUEUED, DocumentFileStatus.PROCESSING));
    if (!duplicateRequest && !busy) {
      final List<DocumentFile> prior =
          fileService.findAllByDocumentIdOrderByCreatedAtDesc(id).stream()
              .filter(f -> f.getSource() == DocumentFileSource.GENERATED_PDF)
              .toList();
      final String languageTag =
          requestedLanguage == null
              ? prior.stream()
                  .map(DocumentFile::getLanguageTag)
                  .filter(Objects::nonNull)
                  .findFirst()
                  .orElse("en")
              : requestedLanguage;
      final DocumentFile attempt = new DocumentFile();
      attempt.setDocument(document);
      attempt.setSource(DocumentFileSource.GENERATED_PDF);
      attempt.setStatus(DocumentFileStatus.QUEUED);
      attempt.setLanguageTag(languageTag);
      attempt.setIdempotencyKey(key);
      attempt.setAttempt(prior.size() + 1);
      if (!prior.isEmpty()) {
        attempt.setParentFile(prior.getFirst());
      }
      final DocumentFile saved = fileService.save(attempt);
      outboxService.enqueue(
          AsyncJobType.DOCUMENT_PDF,
          Map.of("documentId", id, "fileId", saved.getId(), "idempotencyKey", key),
          company.getId(),
          id);
      writer.addActivity(document, DocumentActivityEvent.PDF_QUEUED, String.valueOf(saved.getId()));
    }
    return responseMapper.toResponse(document);
  }

  String normalizePdfLanguage(final String language) {
    final String languageTag =
        language == null || language.isBlank()
            ? "en"
            : Locale.forLanguageTag(language.trim()).getLanguage();
    if (!"en".equals(languageTag) && !"hu".equals(languageTag)) {
      throw new ApiException(DocumentApiMessageKey.DOCUMENT_INVALID_PDF_LANGUAGE);
    }
    return languageTag;
  }
}
