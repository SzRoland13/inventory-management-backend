package dev.roland.inventory_management_backend.common.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Properties;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import dev.roland.inventory_management_backend.common.dto.mail.EmailDetails;
import dev.roland.inventory_management_backend.common.enumeration.MailTemplate;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

  @Mock private JavaMailSender mailSender;

  private EmailService service;

  @BeforeEach
  void setUp() {
    service = new EmailService(mailSender);
    ReflectionTestUtils.setField(service, "sender", "noreply@example.com");
  }

  @Test
  void sendsSimpleAndTemplatedEmail() throws Exception {
    final EmailDetails simple =
        EmailDetails.builder()
            .recipient("alice@example.com")
            .subject("Welcome")
            .msgBody("Hello")
            .build();
    when(mailSender.createMimeMessage())
        .thenReturn(new MimeMessage(Session.getInstance(new Properties())));

    assertEquals(true, service.sendSimpleMail(simple));
    assertEquals(
        true,
        service.sendMailWithTemplate(
            EmailDetails.builder()
                .recipient("alice@example.com")
                .subject("Code")
                .templateName(MailTemplate.ONE_TIME_CODE_MAIL)
                .templateModel(Map.of("username", "Alice", "oneTimeCode", "123456"))
                .build()));

    verify(mailSender).send(any(SimpleMailMessage.class));
    verify(mailSender).send(any(MimeMessage.class));
  }

  @Test
  void returnsFalseWhenSimpleEmailCannotBeSent() {
    doThrow(new MailSendException("mail unavailable"))
        .when(mailSender)
        .send(any(SimpleMailMessage.class));

    assertEquals(
        false,
        service.sendSimpleMail(
            EmailDetails.builder()
                .recipient("alice@example.com")
                .subject("Welcome")
                .msgBody("Hello")
                .build()));
  }
}
