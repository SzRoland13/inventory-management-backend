package dev.roland.inventory_management_backend.features.partner.facade;

import java.util.List;

import dev.roland.inventory_management_backend.features.contact.dto.ContactRequest;
import dev.roland.inventory_management_backend.features.contact.dto.ContactResponse;
import dev.roland.inventory_management_backend.features.partner.dto.PartnerRequest;
import dev.roland.inventory_management_backend.features.partner.dto.PartnerResponse;

/** Coordinates company scoped partner and contact workflows. */
public interface PartnerFacade {
  List<PartnerResponse> list(Boolean archived);

  PartnerResponse get(Long id);

  PartnerResponse create(PartnerRequest request);

  PartnerResponse update(Long id, PartnerRequest request);

  PartnerResponse archive(Long id);

  PartnerResponse restore(Long id);

  List<ContactResponse> listContacts(Long partnerId);

  ContactResponse createContact(Long partnerId, ContactRequest request);

  ContactResponse updateContact(Long partnerId, Long contactId, ContactRequest request);

  ContactResponse removeContact(Long partnerId, Long contactId);
}
