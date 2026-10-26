package dev.roland.inventory_management_backend.features.partner.message;

import dev.roland.inventory_management_backend.common.message.MessageKey;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Message keys for partner and contact operations. */
@Getter
@RequiredArgsConstructor
public enum PartnerMessageKey implements MessageKey {
  PARTNERS_RETRIEVED("partner.list.retrieved"),
  PARTNER_RETRIEVED("partner.retrieved"),
  PARTNER_CREATED("partner.created"),
  PARTNER_UPDATED("partner.updated"),
  PARTNER_ARCHIVED("partner.archived"),
  PARTNER_RESTORED("partner.restored"),
  PARTNER_TAX_NUMBER_EXISTS("partner.error.tax_number_exists"),
  PARTNER_ALREADY_ARCHIVED("partner.error.already_archived"),
  PARTNER_NOT_ARCHIVED("partner.error.not_archived"),
  PARTNER_ARCHIVED_FOR_TRANSACTION("partner.error.archived_for_transaction"),
  CONTACTS_RETRIEVED("contact.list.retrieved"),
  CONTACT_CREATED("contact.created"),
  CONTACT_UPDATED("contact.updated"),
  CONTACT_REMOVED("contact.removed"),
  CONTACT_ALREADY_REMOVED("contact.error.already_removed"),
  CONTACT_REMOVED_READ_ONLY("contact.error.removed_read_only");

  private final String key;
}
