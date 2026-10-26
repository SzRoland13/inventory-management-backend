package dev.roland.inventory_management_backend.features.contact.service;

import java.util.List;
import java.util.Optional;

import dev.roland.inventory_management_backend.features.contact.Contact;

/** Persistence operations for partner contacts. */
public interface ContactService {
  List<Contact> list(Long partnerId, Long companyId);

  Optional<Contact> find(Long id, Long partnerId, Long companyId);

  Contact save(Contact contact);
}
