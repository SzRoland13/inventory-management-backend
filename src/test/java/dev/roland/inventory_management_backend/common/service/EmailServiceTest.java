package dev.roland.inventory_management_backend.common.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Properties;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import dev.roland.inventory_management_backend.common.mail.ComposedMail;
import dev.roland.inventory_management_backend.common.mail.MailComposer;
import dev.roland.inventory_management_backend.features.one_time_code.mail.OneTimeCodeMailable;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

  @Mock private JavaMailSender mailSender;
  @Mock private MailComposer mailComposer;

  private EmailService service;

  @BeforeEach
  void setUp() {
    service = new EmailService(mailSender, mailComposer);
    ReflectionTestUtils.setField(service, "sender", "noreply@example.com");
  }

  @Test
  void composesAndSendsTypedMailable() throws Exception {
    final OneTimeCodeMailable mailable =
        new OneTimeCodeMailable("alice@example.com", "Alice", "123456", "en");
    final ComposedMail composed =
        new ComposedMail("alice@example.com", "Code", "<p>Code</p>", List.of());
    when(mailComposer.compose(mailable)).thenReturn(composed);
    when(mailSender.createMimeMessage())
        .thenReturn(new MimeMessage(Session.getInstance(new Properties())));

    assertTrue(service.send(mailable));

    verify(mailComposer).compose(mailable);
    verify(mailSender).send(any(MimeMessage.class));
  }

  @Test
  void returnsFalseWhenTransportFails() throws Exception {
    when(mailSender.createMimeMessage())
        .thenReturn(new MimeMessage(Session.getInstance(new Properties())));
    doThrow(new MailSendException("mail unavailable"))
        .when(mailSender)
        .send(any(MimeMessage.class));

    assertFalse(
        service.send(new ComposedMail("alice@example.com", "Code", "<p>Code</p>", List.of())));
  }
}
