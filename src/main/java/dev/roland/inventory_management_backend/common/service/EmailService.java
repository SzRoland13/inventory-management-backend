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

import dev.roland.inventory_management_backend.dto.mail.EmailDetails;
import lombok.RequiredArgsConstructor;

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
  public boolean sendSimpleMail(EmailDetails details) {
    try {
      SimpleMailMessage mailMessage = new SimpleMailMessage();

      mailMessage.setFrom(sender);
      mailMessage.setTo(details.getRecipient());
      mailMessage.setText(details.getMsgBody());
      mailMessage.setSubject(details.getSubject());

      javaMailSender.send(mailMessage);

      return true;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Sends an HTML email using a Mustache template.
   *
   * @param details contains recipient, subject, template name, and model
   * @return true if email sent successfully, false otherwise
   */
  public boolean sendMailWithTemplate(EmailDetails details) {
    try {
      String htmlBody =
          renderTemplate(details.getTemplateName().getFileName(), details.getTemplateModel());
      sendHtmlEmail(details.getRecipient(), details.getSubject(), htmlBody);
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  private String renderTemplate(String templateName, Map<String, Object> model) throws IOException {
    MustacheFactory mf = new DefaultMustacheFactory();
    Mustache mustache = mf.compile("templates/" + templateName + ".mustache");

    try (StringWriter writer = new StringWriter()) {
      mustache.execute(writer, model).flush();
      return writer.toString();
    }
  }

  private void sendHtmlEmail(String to, String subject, String htmlBody) throws MessagingException {
    MimeMessage mimeMessage = javaMailSender.createMimeMessage();
    MimeMessageHelper helper =
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
