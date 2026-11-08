package dev.roland.inventory_management_backend.features.partner.dto;

import java.time.LocalDateTime;
import java.util.List;

import dev.roland.inventory_management_backend.features.contact.dto.ContactResponse;
import dev.roland.inventory_management_backend.features.partner.enumeration.PartnerType;

/**
 * Partner response including its contact history.
 *
 * @param id partner identifier
 * @param name partner display name
 * @param type partner classification
 * @param taxNumber optional tax registration number
 * @param vatNumber optional EU VAT number
 * @param registrationNumber optional company registration number
 * @param bankAccount optional bank account number
 * @param iban optional international bank account number
 * @param archived whether the partner is archived
 * @param createdAt creation timestamp
 * @param updatedAt last update timestamp
 * @param contacts partner contacts
 */
public record PartnerResponse(
    Long id,
    String name,
    PartnerType type,
    String taxNumber,
    String vatNumber,
    String registrationNumber,
    String bankAccount,
    String iban,
    boolean archived,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    List<ContactResponse> contacts) {
  /**
   * Copies the contacts list to prevent caller mutation.
   *
   * @param id partner identifier
   * @param name partner display name
   * @param type partner classification
   * @param taxNumber optional tax registration number
   * @param vatNumber optional EU VAT number
   * @param registrationNumber optional company registration number
   * @param bankAccount optional bank account number
   * @param iban optional international bank account number
   * @param archived whether the partner is archived
   * @param createdAt creation timestamp
   * @param updatedAt last update timestamp
   * @param contacts partner contacts
   */
  public PartnerResponse {
    contacts = List.copyOf(contacts);
  }

  /** Keeps the original constructor available for existing integrations. */
  public PartnerResponse(
      final Long id,
      final String name,
      final PartnerType type,
      final String taxNumber,
      final boolean archived,
      final LocalDateTime createdAt,
      final LocalDateTime updatedAt,
      final List<ContactResponse> contacts) {
    this(
        id, name, type, taxNumber, null, null, null, null, archived, createdAt, updatedAt,
        contacts);
  }
}
