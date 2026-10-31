package dev.roland.inventory_management_backend.common.mail;

import dev.roland.inventory_management_backend.common.enumeration.MailType;

/** Composes a mailable into a complete message without using an SMTP transport. */
public interface MailFactory {
  /**
   * Returns the mail type handled by this factory.
   *
   * @return supported mail type
   */
  MailType getType();

  /**
   * Renders a mailable into a complete outbound message.
   *
   * @param mailable email request to compose
   * @return composed email message
   */
  ComposedMail compose(Mailable mailable);
}
