package dev.roland.inventory_management_backend.common.dto.mail;

import java.util.Map;

import dev.roland.inventory_management_backend.common.enumeration.MailTemplate;
import lombok.Builder;
import lombok.Data;

/** Carries the recipient, subject, body, and optional template data for an email. */
@Data
@Builder
public class EmailDetails {

  private String recipient;
  private String msgBody;
  private String subject;
  private MailTemplate templateName;
  private Map<String, Object> templateModel;
}
