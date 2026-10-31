package dev.roland.inventory_management_backend.common.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import dev.roland.inventory_management_backend.common.enumeration.MailType;
import dev.roland.inventory_management_backend.common.exception.MailException;
import dev.roland.inventory_management_backend.common.mail.AbstractMailable;
import dev.roland.inventory_management_backend.features.one_time_code.mail.OneTimeCodeMailFactory;
import dev.roland.inventory_management_backend.features.one_time_code.mail.OneTimeCodeMailable;

class OneTimeCodeMailFactoryTest {

  private final ResourceBundleMessageSource emailMessages = messageSource();
  private final OneTimeCodeMailFactory factory =
      new OneTimeCodeMailFactory(templateEngine(), emailMessages);

  @Test
  void composesBrandedHtmlWithEscapedLocalizedContent() {
    final var composed =
        factory.compose(new OneTimeCodeMailable("alice@example.com", "<Alice>", "123456", "hu"));

    assertEquals("alice@example.com", composed.recipient());
    assertEquals("Egyszer használatos bejelentkezési kód", composed.subject());
    assertTrue(composed.htmlBody().contains("Készletkezelő"));
    assertTrue(composed.htmlBody().contains("background-color:#f3f6fb"));
    assertTrue(composed.htmlBody().contains("&lt;Alice&gt;"));
    assertTrue(composed.htmlBody().contains("123456"));
    assertTrue(composed.attachments().isEmpty());
  }

  @Test
  void rejectsMailableOfWrongType() {
    assertThrows(MailException.class, () -> factory.compose(new OtherMailable()));
  }

  private static SpringTemplateEngine templateEngine() {
    final ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
    resolver.setPrefix("templates/");
    resolver.setSuffix(".html");
    resolver.setTemplateMode("HTML");
    resolver.setCharacterEncoding("UTF-8");

    final SpringTemplateEngine engine = new SpringTemplateEngine();
    engine.setTemplateResolver(resolver);
    engine.setMessageSource(messageSource());
    return engine;
  }

  private static ResourceBundleMessageSource messageSource() {
    final ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
    messageSource.setBasenames("email-messages");
    messageSource.setDefaultEncoding("UTF-8");
    messageSource.setFallbackToSystemLocale(false);
    return messageSource;
  }

  private static final class OtherMailable extends AbstractMailable {
    private OtherMailable() {
      super(
          MailType.ONE_TIME_CODE,
          "alice@example.com",
          "email.one-time-code.subject",
          "email.one-time-code.title",
          "email.one-time-code.preheader",
          "mail/one-time-code-mail",
          Map.of(),
          List.of(),
          "en");
    }
  }
}
