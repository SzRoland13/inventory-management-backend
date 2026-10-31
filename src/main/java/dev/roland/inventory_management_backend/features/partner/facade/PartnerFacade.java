package dev.roland.inventory_management_backend.features.partner.facade;

import java.util.List;

import dev.roland.inventory_management_backend.features.contact.dto.ContactRequest;
import dev.roland.inventory_management_backend.features.contact.dto.ContactResponse;
import dev.roland.inventory_management_backend.features.partner.dto.PartnerRequest;
import dev.roland.inventory_management_backend.features.partner.dto.PartnerResponse;

/** Coordinates company scoped partner and contact workflows. */
public interface PartnerFacade {
  /**
   * Lists partners in the current company.
   *
   * @param archived optional archive filter
   * @return matching partners
   */
  List<PartnerResponse> list(Boolean archived);

  /**
   * Retrieves a company partner.
   *
   * @param id partner identifier
   * @return requested partner
   */
  PartnerResponse get(Long id);

  /**
   * Creates a company partner.
   *
   * @param request partner data
   * @return created partner
   */
  PartnerResponse create(PartnerRequest request);

  /**
   * Updates a company partner.
   *
   * @param id partner identifier
   * @param request partner data
   * @return updated partner
   */
  PartnerResponse update(Long id, PartnerRequest request);

  /**
   * Archives a company partner.
   *
   * @param id partner identifier
   * @return archived partner
   */
  PartnerResponse archive(Long id);

  /**
   * Restores a company partner.
   *
   * @param id partner identifier
   * @return restored partner
   */
  PartnerResponse restore(Long id);

  /**
   * Lists contacts for a company partner.
   *
   * @param partnerId partner identifier
   * @return partner contacts
   */
  List<ContactResponse> listContacts(Long partnerId);

  /**
   * Creates a partner contact.
   *
   * @param partnerId partner identifier
   * @param request contact data
   * @return created contact
   */
  ContactResponse createContact(Long partnerId, ContactRequest request);

  /**
   * Updates a partner contact.
   *
   * @param partnerId partner identifier
   * @param contactId contact identifier
   * @param request contact data
   * @return updated contact
   */
  ContactResponse updateContact(Long partnerId, Long contactId, ContactRequest request);

  /**
   * Removes a partner contact.
   *
   * @param partnerId partner identifier
   * @param contactId contact identifier
   * @return removed contact
   */
  ContactResponse removeContact(Long partnerId, Long contactId);
}
