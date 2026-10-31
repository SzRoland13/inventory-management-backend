package dev.roland.inventory_management_backend.features.one_time_code.mail;

import java.util.List;
import java.util.Map;

import dev.roland.inventory_management_backend.common.enumeration.MailType;
import dev.roland.inventory_management_backend.common.mail.AbstractMailable;

/** Email request for sending a first-login verification code. */
public final class OneTimeCodeMailable extends AbstractMailable {

  private static final String SUBJECT_KEY = "email.one-time-code.subject";
  private static final String TITLE_KEY = "email.one-time-code.title";
  private static final String PREHEADER_KEY = "email.one-time-code.preheader";
  private static final String TEMPLATE = "mail/one-time-code-mail";

  /**
   * Creates a one-time-code email request.
   *
   * @param recipient recipient email address
   * @param username recipient name shown in the email
   * @param oneTimeCode verification code
   * @param languageTag locale used to render the email
   */
  public OneTimeCodeMailable(
      final String recipient,
      final String username,
      final String oneTimeCode,
      final String languageTag) {
    super(
        MailType.ONE_TIME_CODE,
        recipient,
        SUBJECT_KEY,
        TITLE_KEY,
        PREHEADER_KEY,
        TEMPLATE,
        Map.of("username", username, "oneTimeCode", oneTimeCode),
        List.of(),
        languageTag);
  }
}
