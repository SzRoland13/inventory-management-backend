package dev.roland.inventory_management_backend.features.contact.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.roland.inventory_management_backend.features.contact.Contact;

/** Queries contacts through their company-owned partner. */
public interface ContactRepository extends JpaRepository<Contact, Long> {
  List<Contact> findAllByPartnerIdAndPartnerCompanyIdOrderByIdAsc(Long partnerId, Long companyId);

  Optional<Contact> findByIdAndPartnerIdAndPartnerCompanyId(
      Long id, Long partnerId, Long companyId);
}
