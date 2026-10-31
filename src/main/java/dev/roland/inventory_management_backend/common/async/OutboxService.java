package dev.roland.inventory_management_backend.common.async;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.roland.inventory_management_backend.common.enumeration.MailType;
import dev.roland.inventory_management_backend.common.mail.MailAttachment;
import dev.roland.inventory_management_backend.common.mail.Mailable;
import dev.roland.inventory_management_backend.common.message.AsyncJobMessageKey;
import lombok.RequiredArgsConstructor;

/** Creates durable asynchronous jobs within their caller's database transaction. */
@Service
@RequiredArgsConstructor
public class OutboxService {

  private final OutboxMessageRepository repository;
  private final OutboxEncryptionService encryptionService;
  private final ObjectMapper objectMapper;

  /**
   * Enqueues an email for delivery after the caller's transaction commits.
   *
   * @param mailable email request to persist
   * @return durable job identifier
   */
  @Transactional(propagation = Propagation.MANDATORY)
  public UUID enqueueEmail(final Mailable mailable) {
    return enqueue(AsyncJobType.EMAIL, EmailPayload.from(mailable), null, null);
  }

  /**
   * Adds a typed job payload; callers must invoke this inside the business transaction.
   *
   * @param jobType kind of job
   * @param payload serializable job data
   * @param companyId associated company, when present
   * @param documentId associated document, when present
   * @return durable job identifier
   * @throws AsyncJobException if payload serialization or encryption fails
   */
  @Transactional(propagation = Propagation.MANDATORY)
  public UUID enqueue(
      final AsyncJobType jobType,
      final Object payload,
      final Long companyId,
      final Long documentId) {
    try {
      final byte[] json = objectMapper.writeValueAsBytes(payload);
      final UUID id = UUID.randomUUID();
      repository.save(
          new OutboxMessage(id, jobType, encryptionService.encrypt(json), companyId, documentId));
      return id;
    } catch (JsonProcessingException e) {
      throw new AsyncJobException(AsyncJobMessageKey.OUTBOX_PAYLOAD_SERIALIZATION_FAILED, true, e);
    }
  }

  /**
   * Decodes serialized email job data.
   *
   * @param json serialized payload
   * @param objectMapper mapper used to decode the payload
   * @return decoded email payload
   * @throws AsyncJobException if the payload cannot be decoded
   */
  static EmailPayload decodeEmailPayload(final byte[] json, final ObjectMapper objectMapper) {
    try {
      return objectMapper.readValue(json, EmailPayload.class);
    } catch (IOException e) {
      throw new AsyncJobException(AsyncJobMessageKey.OUTBOX_PAYLOAD_INVALID, true, e);
    }
  }

  /**
   * Serializable data required to recreate an email request.
   *
   * @param type email message type
   * @param recipient email recipient
   * @param subjectKey localization key for the subject
   * @param titleKey localization key for the title
   * @param preheaderKey localization key for the preheader
   * @param template email template name
   * @param templateVariables variables used by the template
   * @param attachments email attachments
   * @param languageTag language for the message
   */
  record EmailPayload(
      MailType type,
      String recipient,
      String subjectKey,
      String titleKey,
      String preheaderKey,
      String template,
      Map<String, String> templateVariables,
      List<AttachmentPayload> attachments,
      String languageTag) {

    /**
     * Creates a payload from an email request.
     *
     * @param mailable source email request
     * @return serializable email payload
     */
    static EmailPayload from(final Mailable mailable) {
      return new EmailPayload(
          mailable.type(),
          mailable.recipient(),
          mailable.subjectKey(),
          mailable.titleKey(),
          mailable.preheaderKey(),
          mailable.template(),
          mailable.templateVariables(),
          mailable.attachments().stream().map(AttachmentPayload::from).toList(),
          mailable.languageTag());
    }

    /**
     * Recreates the typed email request.
     *
     * @return email request
     */
    Mailable toMailable() {
      return switch (type) {
        case ONE_TIME_CODE -> new dev.roland.inventory_management_backend.features.one_time_code
            .mail.OneTimeCodeMailable(
            recipient,
            templateVariables.get("username"),
            templateVariables.get("oneTimeCode"),
            languageTag);
      };
    }
  }

  /**
   * Serializable attachment data stored with an email job.
   *
   * @param fileName attachment filename
   * @param contentType attachment media type
   * @param content base64 encoded attachment bytes
   */
  record AttachmentPayload(String fileName, String contentType, String content) {
    /**
     * Creates attachment payload data from an attachment.
     *
     * @param attachment source attachment
     * @return serializable attachment payload
     */
    static AttachmentPayload from(final MailAttachment attachment) {
      return new AttachmentPayload(
          attachment.fileName(),
          attachment.contentType(),
          Base64.getEncoder().encodeToString(attachment.content()));
    }

    /**
     * Recreates the email attachment.
     *
     * @return email attachment
     */
    MailAttachment toAttachment() {
      return new MailAttachment(fileName, contentType, Base64.getDecoder().decode(content));
    }
  }
}
