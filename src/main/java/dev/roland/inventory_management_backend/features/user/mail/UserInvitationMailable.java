package dev.roland.inventory_management_backend.features.user.mail;

import java.util.List;
import java.util.Map;

import dev.roland.inventory_management_backend.common.enumeration.MailType;
import dev.roland.inventory_management_backend.common.mail.AbstractMailable;

/** Email notifying a newly added user about their account. */
public final class UserInvitationMailable extends AbstractMailable {

  /**
   * Creates a new-user notification email.
   *
   * @param recipient recipient email address
   * @param username recipient name shown in the email
   * @param languageTag locale used to render the email
   * @param setupLink link used to set the initial password
   */
  public UserInvitationMailable(
      final String recipient,
      final String username,
      final String languageTag,
      final String setupLink) {
    super(
        MailType.USER_INVITATION,
        recipient,
        "email.user-invitation.subject",
        "email.user-invitation.title",
        "email.user-invitation.preheader",
        "mail/user-invitation-mail",
        Map.of("username", username, "setupLink", setupLink),
        List.of(),
        languageTag);
  }
}
