package dev.roland.inventory_management_backend.common.mail;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import dev.roland.inventory_management_backend.common.enumeration.MailType;

/** Immutable description of an email use case and its rendering data. */
public interface Mailable extends Serializable {
  /**
   * Returns the stable discriminator for this email use case.
   *
   * @return stable email type
   */
  MailType type();

  /**
   * Returns the recipient email address.
   *
   * @return recipient address
   */
  String recipient();

  /**
   * Returns the resource-bundle key for the subject.
   *
   * @return subject message key
   */
  String subjectKey();

  /**
   * Returns the resource-bundle key for the shared layout title.
   *
   * @return title message key
   */
  String titleKey();

  /**
   * Returns the resource-bundle key for the preview text.
   *
   * @return preheader message key
   */
  String preheaderKey();

  /**
   * Returns the Thymeleaf template path without its file extension.
   *
   * @return template path
   */
  String template();

  /**
   * Returns immutable data supplied to the template.
   *
   * @return template variables
   */
  Map<String, String> templateVariables();

  /**
   * Returns the attachments included in the outbound message.
   *
   * @return message attachments
   */
  List<MailAttachment> attachments();

  /**
   * Returns the language tag captured when the email request was created.
   *
   * @return language tag
   */
  String languageTag();
}
