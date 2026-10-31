package dev.roland.inventory_management_backend.common.service;

import java.nio.charset.StandardCharsets;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import dev.roland.inventory_management_backend.common.async.AsyncJobException;
import dev.roland.inventory_management_backend.common.async.OutboxService;
import dev.roland.inventory_management_backend.common.mail.ComposedMail;
import dev.roland.inventory_management_backend.common.mail.MailComposer;
import dev.roland.inventory_management_backend.common.mail.Mailable;
import dev.roland.inventory_management_backend.common.message.AsyncJobMessageKey;
import lombok.RequiredArgsConstructor;

/** Sends composed email messages through Spring Mail. */
@Service
@RequiredArgsConstructor
public class EmailService {

  private final JavaMailSender javaMailSender;
  private final MailComposer mailComposer;
  private final OutboxService outboxService;

  @Value("${app.mail.from:noreply@example.com}")
  private String sender;

  /**
   * Composes a typed request and sends the resulting message.
   *
   * @param mailable typed email request
   */
  public void send(final Mailable mailable) {
    send(mailComposer.compose(mailable));
  }

  /**
   * Sends a composed message; composition itself can be tested without SMTP.
   *
   * @param mail composed email message
   * @throws AsyncJobException when email delivery fails
   */
  public void send(final ComposedMail mail) {
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
    } catch (MessagingException | RuntimeException e) {
      throw new AsyncJobException(AsyncJobMessageKey.EMAIL_DELIVERY_FAILED, false, e);
    }
  }

  /**
   * Records an email job in the caller's transaction for asynchronous delivery.
   *
   * @param mailable typed email request
   */
  @Transactional(propagation = Propagation.MANDATORY)
  public void enqueue(final Mailable mailable) {
    outboxService.enqueueEmail(mailable);
  }
}
