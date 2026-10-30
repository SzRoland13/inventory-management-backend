package dev.roland.inventory_management_backend.features.contact.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.roland.inventory_management_backend.features.contact.Contact;

/** Queries contacts through their company-owned partner. */
public interface ContactRepository extends JpaRepository<Contact, Long> {
  /**
   * Lists contacts through their company-owned partner.
   *
   * @param partnerId partner identifier
   * @param companyId owning company identifier
   * @return matching contacts in identifier order
   */
  List<Contact> findAllByPartnerIdAndPartnerCompanyIdOrderByIdAsc(Long partnerId, Long companyId);

  /**
   * Finds a contact through its company-owned partner.
   *
   * @param id contact identifier
   * @param partnerId partner identifier
   * @param companyId owning company identifier
   * @return matching contact, if it exists
   */
  Optional<Contact> findByIdAndPartnerIdAndPartnerCompanyId(
      Long id, Long partnerId, Long companyId);
}
