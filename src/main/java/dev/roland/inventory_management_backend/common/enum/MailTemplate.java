package dev.roland.inventory_management_backend.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Email templates available to the mail service. */
@Getter
@RequiredArgsConstructor
public enum MailTemplate {
  /** Represents the one-time code mail value. */
  ONE_TIME_CODE_MAIL("one-time-code-mail");

  private final String fileName;

  /** Resolves a template by its file name, ignoring case. */
  public static MailTemplate fromFileName(String fileName) {
    for (MailTemplate template : values()) {
      if (template.fileName.equalsIgnoreCase(fileName)) {
        return template;
      }
    }
    throw new IllegalArgumentException("Unknown template name: " + fileName);
  }
}
