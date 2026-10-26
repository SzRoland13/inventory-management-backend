package dev.roland.inventory_management_backend.features.partner.facade.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.roland.inventory_management_backend.common.exception.NotFoundException;
import dev.roland.inventory_management_backend.common.service.CurrentCompanyProvider;
import dev.roland.inventory_management_backend.features.company.Company;
import dev.roland.inventory_management_backend.features.contact.Contact;
import dev.roland.inventory_management_backend.features.contact.enumeration.ContactType;
import dev.roland.inventory_management_backend.features.contact.service.ContactService;
import dev.roland.inventory_management_backend.features.partner.Partner;
import dev.roland.inventory_management_backend.features.partner.dto.PartnerRequest;
import dev.roland.inventory_management_backend.features.partner.enumeration.PartnerType;
import dev.roland.inventory_management_backend.features.partner.message.PartnerMessageKey;
import dev.roland.inventory_management_backend.features.partner.service.PartnerService;

@ExtendWith(MockitoExtension.class)
class PartnerFacadeImplTest {
  @Mock private PartnerService partnerService;
  @Mock private ContactService contactService;
  @Mock private CurrentCompanyProvider currentCompanyProvider;
  private PartnerFacadeImpl facade;
  private Company company;

  @BeforeEach
  void setUp() {
    facade = new PartnerFacadeImpl(partnerService, contactService, currentCompanyProvider);
    company = Company.builder().id(4L).build();
    when(currentCompanyProvider.getCurrentCompany()).thenReturn(company);
  }

  @Test
  void readsPartnerOnlyWithinAuthenticatedCompany() {
    when(partnerService.find(19L, 4L)).thenReturn(Optional.empty());
    assertThrows(NotFoundException.class, () -> facade.get(19L));
    verify(partnerService).find(19L, 4L);
  }

  @Test
  void createNormalizesBlankTaxNumberAndAllowsDuplicateNames() {
    Partner saved =
        Partner.builder().id(3L).company(company).name("Acme").type(PartnerType.CUSTOMER).build();
    when(partnerService.save(any(Partner.class))).thenReturn(saved);
    when(contactService.list(3L, 4L)).thenReturn(List.of());
    var response = facade.create(new PartnerRequest(" Acme ", PartnerType.CUSTOMER, "  "));
    assertEquals("Acme", response.name());
    assertEquals(null, response.taxNumber());
    assertEquals(4L, saved.getCompany().getId());
  }

  @Test
  void contactRemovalRetainsHistoryAndMarksItRemoved() {
    Partner partner =
        Partner.builder().id(3L).company(company).name("Acme").type(PartnerType.BOTH).build();
    Contact contact =
        Contact.builder()
            .id(6L)
            .partner(partner)
            .type(ContactType.BILLING)
            .addressLine1("Main St")
            .city("Budapest")
            .country("HU")
            .build();
    when(partnerService.find(3L, 4L)).thenReturn(Optional.of(partner));
    when(contactService.find(6L, 3L, 4L)).thenReturn(Optional.of(contact));
    when(contactService.save(contact)).thenAnswer(invocation -> invocation.getArgument(0));
    var response = facade.removeContact(3L, 6L);
    assertEquals(true, response.removed());
    verify(contactService).save(contact);
  }

  @Test
  void rejectsDuplicateTaxNumberForCurrentCompany() {
    when(partnerService.taxNumberExists(4L, "TAX-1", null)).thenReturn(true);
    var exception =
        assertThrows(
            dev.roland.inventory_management_backend.common.exception.ApiException.class,
            () -> facade.create(new PartnerRequest("Acme", PartnerType.CUSTOMER, " TAX-1 ")));
    assertEquals(PartnerMessageKey.PARTNER_TAX_NUMBER_EXISTS, exception.getMessageKey());
  }
}
