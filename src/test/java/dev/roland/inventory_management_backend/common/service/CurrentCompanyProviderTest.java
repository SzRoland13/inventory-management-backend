package dev.roland.inventory_management_backend.common.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import dev.roland.inventory_management_backend.common.exception.NotFoundException;
import dev.roland.inventory_management_backend.features.auth.CustomUserDetails;
import dev.roland.inventory_management_backend.features.company.Company;
import dev.roland.inventory_management_backend.features.company.service.CompanyService;
import dev.roland.inventory_management_backend.features.user.User;
import dev.roland.inventory_management_backend.features.user.enumeration.UserRole;
import dev.roland.inventory_management_backend.features.user.enumeration.UserStatus;
import dev.roland.inventory_management_backend.features.user.service.UserService;

@ExtendWith(MockitoExtension.class)
class CurrentCompanyProviderTest {
  @Mock private UserService userService;
  @Mock private CompanyService companyService;
  private CurrentCompanyProvider provider;

  @BeforeEach
  void setUp() {
    provider = new CurrentCompanyProvider(userService, companyService);
    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken(
                new CustomUserDetails(12L, "member", "", UserRole.SALES, UserStatus.ACTIVE), null));
  }

  @AfterEach
  void clearContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void resolvesCompanyThroughAuthenticatedUser() {
    Company company = Company.builder().id(31L).build();
    when(userService.findByIdOrThrow(12L)).thenReturn(User.builder().company(company).build());
    when(companyService.findByIdOrThrow(31L)).thenReturn(company);

    assertEquals(company, provider.getCurrentCompany());
  }

  @Test
  void rejectsRequestsWithoutAuthenticatedUserPrincipal() {
    SecurityContextHolder.clearContext();
    assertThrows(NotFoundException.class, provider::getCurrentCompany);
  }
}
