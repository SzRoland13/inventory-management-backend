package dev.roland.inventory_management_backend.features.partner;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.roland.inventory_management_backend.common.annotation.AdminOnly;
import dev.roland.inventory_management_backend.common.dto.ApiResponse;
import dev.roland.inventory_management_backend.features.contact.dto.ContactRequest;
import dev.roland.inventory_management_backend.features.contact.dto.ContactResponse;
import dev.roland.inventory_management_backend.features.partner.dto.PartnerRequest;
import dev.roland.inventory_management_backend.features.partner.dto.PartnerResponse;
import dev.roland.inventory_management_backend.features.partner.facade.PartnerFacade;
import dev.roland.inventory_management_backend.features.partner.message.PartnerMessageKey;
import lombok.RequiredArgsConstructor;

/** Exposes company-scoped partner and contact operations. */
@RestController
@RequestMapping(PartnerController.BASE_PATH)
@RequiredArgsConstructor
public class PartnerController {
  public static final String BASE_PATH = "api/v1/partners";
  public static final String ID_PATH = "/{id}";
  public static final String ARCHIVE_PATH = ID_PATH + "/archive";
  public static final String RESTORE_PATH = ID_PATH + "/restore";
  public static final String CONTACTS_PATH = ID_PATH + "/contacts";
  public static final String CONTACT_ID_PATH = CONTACTS_PATH + "/{contactId}";

  private final PartnerFacade facade;

  /**
   * Lists partners in the current company.
   *
   * @param archived optional archive filter
   * @return matching partners
   */
  @GetMapping
  public ResponseEntity<ApiResponse<List<PartnerResponse>>> list(
      @RequestParam(required = false) final Boolean archived) {
    return ResponseEntity.ok(
        ApiResponse.success(PartnerMessageKey.PARTNERS_RETRIEVED, facade.list(archived)));
  }

  /**
   * Retrieves a partner.
   *
   * @param id partner identifier
   * @return requested partner
   */
  @GetMapping(ID_PATH)
  public ResponseEntity<ApiResponse<PartnerResponse>> get(@PathVariable final Long id) {
    return ResponseEntity.ok(
        ApiResponse.success(PartnerMessageKey.PARTNER_RETRIEVED, facade.get(id)));
  }

  /**
   * Creates a partner.
   *
   * @param request partner data
   * @return created partner
   */
  @PostMapping
  @AdminOnly
  public ResponseEntity<ApiResponse<PartnerResponse>> create(
      @Valid @RequestBody final PartnerRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(PartnerMessageKey.PARTNER_CREATED, facade.create(request)));
  }

  /**
   * Updates a partner.
   *
   * @param id partner identifier
   * @param request partner data
   * @return updated partner
   */
  @PutMapping(ID_PATH)
  @AdminOnly
  public ResponseEntity<ApiResponse<PartnerResponse>> update(
      @PathVariable final Long id, @Valid @RequestBody final PartnerRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(PartnerMessageKey.PARTNER_UPDATED, facade.update(id, request)));
  }

  /**
   * Archives a partner.
   *
   * @param id partner identifier
   * @return archived partner
   */
  @PostMapping(ARCHIVE_PATH)
  @AdminOnly
  public ResponseEntity<ApiResponse<PartnerResponse>> archive(@PathVariable final Long id) {
    return ResponseEntity.ok(
        ApiResponse.success(PartnerMessageKey.PARTNER_ARCHIVED, facade.archive(id)));
  }

  /**
   * Restores a partner.
   *
   * @param id partner identifier
   * @return restored partner
   */
  @PostMapping(RESTORE_PATH)
  @AdminOnly
  public ResponseEntity<ApiResponse<PartnerResponse>> restore(@PathVariable final Long id) {
    return ResponseEntity.ok(
        ApiResponse.success(PartnerMessageKey.PARTNER_RESTORED, facade.restore(id)));
  }

  /**
   * Lists contacts for a partner.
   *
   * @param id partner identifier
   * @return partner contacts
   */
  @GetMapping(CONTACTS_PATH)
  public ResponseEntity<ApiResponse<List<ContactResponse>>> listContacts(
      @PathVariable final Long id) {
    return ResponseEntity.ok(
        ApiResponse.success(PartnerMessageKey.CONTACTS_RETRIEVED, facade.listContacts(id)));
  }

  /**
   * Creates a contact for a partner.
   *
   * @param id partner identifier
   * @param request contact data
   * @return created contact
   */
  @PostMapping(CONTACTS_PATH)
  @AdminOnly
  public ResponseEntity<ApiResponse<ContactResponse>> createContact(
      @PathVariable final Long id, @Valid @RequestBody final ContactRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            ApiResponse.success(
                PartnerMessageKey.CONTACT_CREATED, facade.createContact(id, request)));
  }

  /**
   * Updates a partner contact.
   *
   * @param id partner identifier
   * @param contactId contact identifier
   * @param request contact data
   * @return updated contact
   */
  @PutMapping(CONTACT_ID_PATH)
  @AdminOnly
  public ResponseEntity<ApiResponse<ContactResponse>> updateContact(
      @PathVariable final Long id,
      @PathVariable final Long contactId,
      @Valid @RequestBody final ContactRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(
            PartnerMessageKey.CONTACT_UPDATED, facade.updateContact(id, contactId, request)));
  }

  /**
   * Removes a partner contact.
   *
   * @param id partner identifier
   * @param contactId contact identifier
   * @return removed contact
   */
  @DeleteMapping(CONTACT_ID_PATH)
  @AdminOnly
  public ResponseEntity<ApiResponse<ContactResponse>> removeContact(
      @PathVariable final Long id, @PathVariable final Long contactId) {
    return ResponseEntity.ok(
        ApiResponse.success(
            PartnerMessageKey.CONTACT_REMOVED, facade.removeContact(id, contactId)));
  }
}
