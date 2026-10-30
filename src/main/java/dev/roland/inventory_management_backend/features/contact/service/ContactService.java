package dev.roland.inventory_management_backend.features.contact.service;

import java.util.List;
import java.util.Optional;

import dev.roland.inventory_management_backend.features.contact.Contact;

/** Persistence operations for partner contacts. */
public interface ContactService {
  /**
   * Lists contacts belonging to a company-owned partner.
   *
   * @param partnerId partner identifier
   * @param companyId owning company identifier
   * @return matching contacts
   */
  List<Contact> list(Long partnerId, Long companyId);

  /**
   * Finds a contact belonging to a company-owned partner.
   *
   * @param id contact identifier
   * @param partnerId partner identifier
   * @param companyId owning company identifier
   * @return matching contact, if it exists
   */
  Optional<Contact> find(Long id, Long partnerId, Long companyId);

  /**
   * Saves a contact.
   *
   * @param contact contact to persist
   * @return saved contact
   */
  Contact save(Contact contact);
}
