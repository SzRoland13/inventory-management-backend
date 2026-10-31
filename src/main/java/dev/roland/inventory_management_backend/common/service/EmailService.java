package dev.roland.inventory_management_backend.common.service;

import java.nio.charset.StandardCharsets;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.common.mail.ComposedMail;
import dev.roland.inventory_management_backend.common.mail.MailComposer;
import dev.roland.inventory_management_backend.common.mail.Mailable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Sends composed email messages through Spring Mail. */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

  private final JavaMailSender javaMailSender;
  private final MailComposer mailComposer;

  @Value("${app.mail.from:noreply@example.com}")
  private String sender;

  /**
   * Composes a typed request and sends the resulting message.
   *
   * @param mailable typed email request
   * @return whether the transport accepted the message
   */
  public boolean send(final Mailable mailable) {
    return send(mailComposer.compose(mailable));
  }

  /**
   * Sends a composed message; composition itself can be tested without SMTP.
   *
   * @param mail composed email message
   * @return whether the transport accepted the message
   */
  public boolean send(final ComposedMail mail) {
    boolean sent = false;
    try {
      final MimeMessage mimeMessage = javaMailSender.createMimeMessage();
      final MimeMessageHelper helper =
          new MimeMessageHelper(
              mimeMessage,
              MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
              StandardCharsets.UTF_8.name());

      helper.setFrom(sender);
      helper.setTo(mail.recipient());
      helper.setSubject(mail.subject());
      helper.setText(mail.htmlBody(), true);

      for (var attachment : mail.attachments()) {
        helper.addAttachment(
            attachment.fileName(),
            new ByteArrayResource(attachment.content()),
            attachment.contentType());
      }

      javaMailSender.send(mimeMessage);
      sent = true;
    } catch (MessagingException | RuntimeException e) {
      log.warn("Failed to send email", e);
    }
    return sent;
  }
}
