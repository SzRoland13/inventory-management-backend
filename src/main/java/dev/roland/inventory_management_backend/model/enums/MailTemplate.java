package dev.roland.inventory_management_backend.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MailTemplate {
    ONE_TIME_CODE_MAIL("one-time-code-mail");

    private final String fileName;

    public static MailTemplate fromFileName(String fileName) {
        for (MailTemplate template : values()) {
            if (template.fileName.equalsIgnoreCase(fileName)) {
                return template;
            }
        }
        throw new IllegalArgumentException("Unknown template name: " + fileName);
    }
}
