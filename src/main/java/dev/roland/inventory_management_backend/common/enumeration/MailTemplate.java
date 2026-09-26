package dev.roland.inventory_management_backend.common.enumeration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Email templates available to the mail service. */
@Getter
@RequiredArgsConstructor
public enum MailTemplate {
  /** Represents the one-time code mail value. */
  ONE_TIME_CODE_MAIL("one-time-code-mail");

  private final String fileName;

  /**
   * Resolves a template by its file name, ignoring case.
   *
   * @param fileName template file name to resolve
   * @return matching template
   * @throws IllegalArgumentException when no template has that file name
   */
  public static MailTemplate fromFileName(final String fileName) {
    for (MailTemplate template : values()) {
      if (template.fileName.equalsIgnoreCase(fileName)) {
        return template;
      }
    }
    throw new IllegalArgumentException("Unknown template name: " + fileName);
  }
}
