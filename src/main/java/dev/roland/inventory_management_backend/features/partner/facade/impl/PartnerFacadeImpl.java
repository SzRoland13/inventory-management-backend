package dev.roland.inventory_management_backend.features.partner.facade.impl;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.common.exception.ApiException;
import dev.roland.inventory_management_backend.common.exception.NotFoundException;
import dev.roland.inventory_management_backend.common.message.NotFoundMessageKey;
import dev.roland.inventory_management_backend.common.service.CurrentCompanyProvider;
import dev.roland.inventory_management_backend.features.company.Company;
import dev.roland.inventory_management_backend.features.contact.Contact;
import dev.roland.inventory_management_backend.features.contact.dto.ContactRequest;
import dev.roland.inventory_management_backend.features.contact.dto.ContactResponse;
import dev.roland.inventory_management_backend.features.contact.service.ContactService;
import dev.roland.inventory_management_backend.features.partner.Partner;
import dev.roland.inventory_management_backend.features.partner.dto.PartnerRequest;
import dev.roland.inventory_management_backend.features.partner.dto.PartnerResponse;
import dev.roland.inventory_management_backend.features.partner.facade.PartnerFacade;
import dev.roland.inventory_management_backend.features.partner.message.PartnerMessageKey;
import dev.roland.inventory_management_backend.features.partner.service.PartnerService;
import lombok.RequiredArgsConstructor;

/** Implements partner and contact application workflows. */
@Service
@RequiredArgsConstructor
public class PartnerFacadeImpl implements PartnerFacade {
  private final PartnerService partnerService;
  private final ContactService contactService;
  private final CurrentCompanyProvider currentCompanyProvider;

  @Override
  public List<PartnerResponse> list(final Boolean archived) {
    final Long companyId = currentCompany().getId();
    return partnerService.list(companyId, archived).stream().map(this::toResponse).toList();
  }

  @Override
  public PartnerResponse get(final Long id) {
    return toResponse(findPartner(id));
  }

  @Transactional
  @Override
  public PartnerResponse create(final PartnerRequest request) {
    final Company company = currentCompany();
    final String tax = normalizeTax(request.taxNumber());
    checkTax(company.getId(), tax, null);
    final Partner partner = new Partner();
    partner.setCompany(company);
    apply(partner, request, tax);
    return toResponse(partnerService.save(partner));
  }

  @Transactional
  @Override
  public PartnerResponse update(final Long id, final PartnerRequest request) {
    final Partner partner = findPartner(id);
    ensureActive(partner);
    final String tax = normalizeTax(request.taxNumber());
    checkTax(partner.getCompany().getId(), tax, id);
    apply(partner, request, tax);
    return toResponse(partnerService.save(partner));
  }

  @Transactional
  @Override
  public PartnerResponse archive(final Long id) {
    final Partner partner = findPartner(id);
    if (partner.getDeletedAt() != null) {
      throw new ApiException(PartnerMessageKey.PARTNER_ALREADY_ARCHIVED);
    }
    partner.setDeletedAt(LocalDateTime.now());
    return toResponse(partnerService.save(partner));
  }

  @Transactional
  @Override
  public PartnerResponse restore(final Long id) {
    final Partner partner = findPartner(id);
    if (partner.getDeletedAt() == null) {
      throw new ApiException(PartnerMessageKey.PARTNER_NOT_ARCHIVED);
    }
    partner.setDeletedAt(null);
    return toResponse(partnerService.save(partner));
  }

  @Override
  public List<ContactResponse> listContacts(final Long partnerId) {
    final Partner partner = findPartner(partnerId);
    return contactService.list(partnerId, partner.getCompany().getId()).stream()
        .map(this::toContactResponse)
        .toList();
  }

  @Transactional
  @Override
  public ContactResponse createContact(final Long partnerId, final ContactRequest request) {
    final Partner partner = findPartner(partnerId);
    ensureActive(partner);
    final Contact contact = new Contact();
    contact.setPartner(partner);
    apply(contact, request);
    return toContactResponse(contactService.save(contact));
  }

  @Transactional
  @Override
  public ContactResponse updateContact(
      final Long partnerId, final Long contactId, final ContactRequest request) {
    final Partner partner = findPartner(partnerId);
    ensureActive(partner);
    final Contact contact = findContact(contactId, partnerId, partner.getCompany().getId());
    if (contact.getDeletedAt() != null) {
      throw new ApiException(PartnerMessageKey.CONTACT_REMOVED_READ_ONLY);
    }
    apply(contact, request);
    return toContactResponse(contactService.save(contact));
  }

  @Transactional
  @Override
  public ContactResponse removeContact(final Long partnerId, final Long contactId) {
    final Partner partner = findPartner(partnerId);
    ensureActive(partner);
    final Contact contact = findContact(contactId, partnerId, partner.getCompany().getId());
    if (contact.getDeletedAt() != null) {
      throw new ApiException(PartnerMessageKey.CONTACT_ALREADY_REMOVED);
    }
    contact.setDeletedAt(LocalDateTime.now());
    return toContactResponse(contactService.save(contact));
  }

  private Company currentCompany() {
    return currentCompanyProvider.getCurrentCompany();
  }

  private Partner findPartner(final Long id) {
    return partnerService
        .find(id, currentCompany().getId())
        .orElseThrow(() -> new NotFoundException(NotFoundMessageKey.PARTNER));
  }

  private Contact findContact(final Long id, final Long partnerId, final Long companyId) {
    return contactService
        .find(id, partnerId, companyId)
        .orElseThrow(() -> new NotFoundException(NotFoundMessageKey.CONTACT));
  }

  private void checkTax(final Long companyId, final String tax, final Long exceptId) {
    if (tax != null && partnerService.taxNumberExists(companyId, tax, exceptId)) {
      throw new ApiException(PartnerMessageKey.PARTNER_TAX_NUMBER_EXISTS);
    }
  }

  private static String normalizeTax(final String value) {
    return value == null || value.isBlank() ? null : value.trim();
  }

  private static void ensureActive(final Partner partner) {
    if (partner.getDeletedAt() != null) {
      throw new ApiException(PartnerMessageKey.PARTNER_ARCHIVED_FOR_TRANSACTION);
    }
  }

  private static void apply(final Partner partner, final PartnerRequest request, final String tax) {
    partner.setName(request.name().trim());
    partner.setType(request.type());
    partner.setTaxNumber(tax);
  }

  private static void apply(final Contact contact, final ContactRequest request) {
    contact.setType(request.type());
    contact.setAddressLine1(request.addressLine1().trim());
    contact.setAddressLine2(request.addressLine2() == null ? null : request.addressLine2().trim());
    contact.setCity(request.city().trim());
    contact.setCountry(request.country().trim().toUpperCase(java.util.Locale.ROOT));
    contact.setPhone(
        request.phone() == null || request.phone().isBlank() ? null : request.phone().trim());
    contact.setEmail(
        request.email() == null || request.email().isBlank() ? null : request.email().trim());
  }

  private PartnerResponse toResponse(final Partner partner) {
    final List<ContactResponse> contacts =
        contactService.list(partner.getId(), partner.getCompany().getId()).stream()
            .map(this::toContactResponse)
            .toList();
    return new PartnerResponse(
        partner.getId(),
        partner.getName(),
        partner.getType(),
        partner.getTaxNumber(),
        partner.getDeletedAt() != null,
        partner.getCreatedAt(),
        partner.getUpdatedAt(),
        contacts);
  }

  private ContactResponse toContactResponse(final Contact contact) {
    return new ContactResponse(
        contact.getId(),
        contact.getType(),
        contact.getAddressLine1(),
        contact.getAddressLine2(),
        contact.getCity(),
        contact.getCountry(),
        contact.getPhone(),
        contact.getEmail(),
        contact.getDeletedAt() != null,
        contact.getCreatedAt(),
        contact.getUpdatedAt());
  }
}
