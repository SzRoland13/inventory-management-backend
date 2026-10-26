package dev.roland.inventory_management_backend.features.contact.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.features.contact.Contact;
import dev.roland.inventory_management_backend.features.contact.repository.ContactRepository;
import dev.roland.inventory_management_backend.features.contact.service.ContactService;
import lombok.RequiredArgsConstructor;

/** Implements contact persistence. */
@Service
@RequiredArgsConstructor
public class ContactServiceImpl implements ContactService {
  private final ContactRepository repository;

  @Override
  public List<Contact> list(Long partnerId, Long companyId) {
    return repository.findAllByPartnerIdAndPartnerCompanyIdOrderByIdAsc(partnerId, companyId);
  }

  @Override
  public Optional<Contact> find(Long id, Long partnerId, Long companyId) {
    return repository.findByIdAndPartnerIdAndPartnerCompanyId(id, partnerId, companyId);
  }

  @Override
  public Contact save(Contact contact) {
    return repository.save(contact);
  }
}
