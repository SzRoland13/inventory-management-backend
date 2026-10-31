package dev.roland.inventory_management_backend.common.mail;

import java.util.List;
import java.util.Objects;

/**
 * Fully composed outbound email.
 *
 * @param recipient recipient email address
 * @param subject localized email subject
 * @param htmlBody rendered HTML message
 * @param attachments message attachments
 */
public record ComposedMail(
    String recipient, String subject, String htmlBody, List<MailAttachment> attachments) {

  /**
   * Creates a composed HTML email and copies the attachment list.
   *
   * @param recipient recipient email address
   * @param subject localized email subject
   * @param htmlBody rendered HTML message
   * @param attachments message attachments
   */
  public ComposedMail {
    Objects.requireNonNull(recipient);
    Objects.requireNonNull(subject);
    Objects.requireNonNull(htmlBody);
    attachments = List.copyOf(attachments);
  }
}
