package dev.roland.inventory_management_backend.common.service;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import dev.roland.inventory_management_backend.common.dto.mail.EmailDetails;
import lombok.RequiredArgsConstructor;

/** Sends plain-text and Mustache-rendered HTML email through Spring Mail. */
@Service
@RequiredArgsConstructor
public class EmailService {

  private final JavaMailSender javaMailSender;

  @Value("${spring.mail.username}")
  private String sender;

  /**
   * Sends a plain text email.
   *
   * @param details recipient, subject, and body of the email
   * @return true when the mail sender accepts the message, otherwise false
   */
  public boolean sendSimpleMail(final EmailDetails details) {
    boolean sent = false;
    try {
      final SimpleMailMessage mailMessage = new SimpleMailMessage();

      mailMessage.setFrom(sender);
      mailMessage.setTo(details.getRecipient());
      mailMessage.setText(details.getMsgBody());
      mailMessage.setSubject(details.getSubject());

      javaMailSender.send(mailMessage);
      sent = true;
    } catch (Exception e) {
      // Report failure to the caller.
    }
    return sent;
  }

  /**
   * Sends an HTML email using a Mustache template.
   *
   * @param details contains recipient, subject, template name, and model
   * @return true if email sent successfully, false otherwise
   */
  public boolean sendMailWithTemplate(final EmailDetails details) {
    boolean sent = false;
    try {
      final String htmlBody =
          renderTemplate(details.getTemplateName().getFileName(), details.getTemplateModel());
      sendHtmlEmail(details.getRecipient(), details.getSubject(), htmlBody);
      sent = true;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return sent;
  }

  private String renderTemplate(final String templateName, final Map<String, Object> model)
      throws IOException {
    final MustacheFactory mf = new DefaultMustacheFactory();
    final Mustache mustache = mf.compile("templates/" + templateName + ".mustache");

    try (StringWriter writer = new StringWriter()) {
      mustache.execute(writer, model).flush();
      return writer.toString();
    }
  }

  private void sendHtmlEmail(final String to, final String subject, final String htmlBody)
      throws MessagingException {
    final MimeMessage mimeMessage = javaMailSender.createMimeMessage();
    final MimeMessageHelper helper =
        new MimeMessageHelper(
            mimeMessage,
            MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
            StandardCharsets.UTF_8.name());

    helper.setFrom(sender);
    helper.setTo(to);
    helper.setSubject(subject);
    helper.setText(htmlBody, true);

    javaMailSender.send(mimeMessage);
  }
}
