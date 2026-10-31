package dev.roland.inventory_management_backend.common.mail;

import java.util.Objects;

/**
 * Attachment content and metadata for a composed email.
 *
 * @param fileName attachment filename
 * @param contentType attachment MIME type
 * @param content attachment bytes
 */
public record MailAttachment(String fileName, String contentType, byte[] content) {

  /**
   * Creates an attachment and copies its content defensively.
   *
   * @param fileName attachment filename
   * @param contentType attachment MIME type
   * @param content attachment bytes
   */
  public MailAttachment {
    Objects.requireNonNull(fileName);
    Objects.requireNonNull(contentType);
    content = content.clone();
  }

  /**
   * Returns a defensive copy of the attachment bytes.
   *
   * @return attachment bytes
   */
  @Override
  public byte[] content() {
    return content.clone();
  }
}
