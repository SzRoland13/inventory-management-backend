package dev.roland.inventory_management_backend.common.mail;

import java.util.List;
import java.util.Map;

import dev.roland.inventory_management_backend.common.enumeration.MailType;
import lombok.Getter;
import lombok.experimental.Accessors;

/** Base implementation of the common email request data. */
@Getter
@Accessors(fluent = true)
public abstract class AbstractMailable implements Mailable {

  private final MailType type;
  private final String recipient;
  private final String subjectKey;
  private final String titleKey;
  private final String preheaderKey;
  private final String template;
  private final Map<String, String> templateVariables;
  private final List<MailAttachment> attachments;
  private final String languageTag;

  /**
   * Creates a mailable with its template and localization data.
   *
   * @param type stable email use-case type
   * @param recipient recipient email address
   * @param subjectKey resource-bundle key for the subject
   * @param titleKey resource-bundle key for the shared layout title
   * @param preheaderKey resource-bundle key for preview text
   * @param template Thymeleaf template path without its extension
   * @param templateVariables immutable values exposed to the template
   * @param attachments attachments for the message
   * @param languageTag locale used to render the email
   */
  protected AbstractMailable(
      final MailType type,
      final String recipient,
      final String subjectKey,
      final String titleKey,
      final String preheaderKey,
      final String template,
      final Map<String, String> templateVariables,
      final List<MailAttachment> attachments,
      final String languageTag) {
    this.type = type;
    this.recipient = recipient;
    this.subjectKey = subjectKey;
    this.titleKey = titleKey;
    this.preheaderKey = preheaderKey;
    this.template = template;
    this.templateVariables = templateVariables;
    this.attachments = attachments;
    this.languageTag = languageTag;
  }
}
