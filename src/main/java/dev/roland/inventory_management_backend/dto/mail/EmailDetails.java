package dev.roland.inventory_management_backend.dto.mail;

import dev.roland.inventory_management_backend.model.enums.MailTemplate;
import lombok.*;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
public class EmailDetails {

    private String recipient;
    private String msgBody;
    private String subject;
    private MailTemplate templateName;
    private Map<String, Object> templateModel;
}
