package dev.roland.inventory_management_backend.dto.mail;

import java.util.Map;

import dev.roland.inventory_management_backend.enums.MailTemplate;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmailDetails {

  private String recipient;
  private String msgBody;
  private String subject;
  private MailTemplate templateName;
  private Map<String, Object> templateModel;
}
