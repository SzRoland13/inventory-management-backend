package dev.roland.inventory_management_backend.features.one_time_code.mail;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import dev.roland.inventory_management_backend.common.enumeration.MailType;
import dev.roland.inventory_management_backend.common.exception.MailException;
import dev.roland.inventory_management_backend.common.mail.ComposedMail;
import dev.roland.inventory_management_backend.common.mail.MailFactory;
import dev.roland.inventory_management_backend.common.mail.Mailable;
import dev.roland.inventory_management_backend.common.message.MailMessageKey;
import lombok.RequiredArgsConstructor;

/** Composes the first-login one-time-code email. */
@Component
@RequiredArgsConstructor
public class OneTimeCodeMailFactory implements MailFactory {

  private final TemplateEngine templateEngine;
  private final MessageSource emailMessages;

  @Override
  public MailType getType() {
    return MailType.ONE_TIME_CODE;
  }

  @Override
  public ComposedMail compose(final Mailable mailable) {
    if (!(mailable instanceof OneTimeCodeMailable)) {
      throw new MailException(MailMessageKey.INVALID_MAILABLE);
    }
    validate(mailable);

    final Locale locale = Locale.forLanguageTag(mailable.languageTag());
    final Context context = new Context(locale);
    context.setVariable("mailTitle", emailMessages.getMessage(mailable.titleKey(), null, locale));
    context.setVariable(
        "preheader", emailMessages.getMessage(mailable.preheaderKey(), null, locale));
    mailable.templateVariables().forEach(context::setVariable);

    final String subject = emailMessages.getMessage(mailable.subjectKey(), null, locale);
    final String content = templateEngine.process(mailable.template(), context);
    context.setVariable("content", content);
    final String html = templateEngine.process("mail/layout", context);

    return new ComposedMail(mailable.recipient(), subject, html, mailable.attachments());
  }

  private void validate(final Mailable mailable) {
    if (mailable.type() == null
        || mailable.recipient() == null
        || mailable.recipient().isBlank()
        || mailable.subjectKey() == null
        || mailable.titleKey() == null
        || mailable.preheaderKey() == null
        || mailable.template() == null
        || mailable.templateVariables() == null
        || mailable.templateVariables().values().stream().anyMatch(java.util.Objects::isNull)
        || mailable.attachments() == null
        || mailable.attachments().stream().anyMatch(java.util.Objects::isNull)
        || mailable.languageTag() == null
        || mailable.languageTag().isBlank()) {
      throw new MailException(MailMessageKey.INVALID_MAILABLE);
    }
  }
}
