package dev.roland.inventory_management_backend.common.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import dev.roland.inventory_management_backend.features.company.Company;
import dev.roland.inventory_management_backend.features.company.service.CompanyService;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentType;
import dev.roland.inventory_management_backend.features.document_prefix.DocumentPrefix;
import dev.roland.inventory_management_backend.features.document_prefix.service.DocumentPrefixService;
import dev.roland.inventory_management_backend.features.user.User;
import dev.roland.inventory_management_backend.features.user.enumeration.UserRole;
import dev.roland.inventory_management_backend.features.user.enumeration.UserStatus;
import dev.roland.inventory_management_backend.features.user.service.UserService;

@ExtendWith(MockitoExtension.class)
class InitialDataSeederTest {

  @Mock private CompanyService companyService;
  @Mock private UserService userService;
  @Mock private DocumentPrefixService documentPrefixService;
  @Mock private JdbcTemplate jdbcTemplate;
  @Mock private AppConfiguration appConfiguration;

  private InitialDataSeeder seeder;

  @BeforeEach
  void setUp() {
    seeder =
        new InitialDataSeeder(
            companyService, userService, documentPrefixService, jdbcTemplate, appConfiguration);
    lenient().when(appConfiguration.getDatabaseDriverClassName()).thenReturn("org.h2.Driver");
    lenient().when(appConfiguration.getInitialAdminUsername()).thenReturn("initial-admin");
    lenient().when(appConfiguration.getInitialAdminEmail()).thenReturn("admin@example.com");
    lenient().when(appConfiguration.getInitialCompanyName()).thenReturn("Example Company");
  }

  @Test
  void createsCompanySetupAdminAndEveryDocumentPrefixForEmptyDatabase() {
    final Company company = Company.builder().id(10L).name("Example Company").build();
    when(companyService.findAll()).thenReturn(List.of());
    when(companyService.save(any(Company.class))).thenReturn(company);

    seeder.run();

    verify(companyService).save(any(Company.class));
    final ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    verify(userService).save(userCaptor.capture());
    assertEquals("initial-admin", userCaptor.getValue().getUsername());
    assertEquals("admin@example.com", userCaptor.getValue().getEmail());
    assertEquals(UserRole.ADMIN, userCaptor.getValue().getRole());
    assertEquals(UserStatus.SETUP_REQUIRED, userCaptor.getValue().getStatus());
    assertEquals(company, userCaptor.getValue().getCompany());

    final ArgumentCaptor<DocumentPrefix> prefixCaptor =
        ArgumentCaptor.forClass(DocumentPrefix.class);
    verify(documentPrefixService, org.mockito.Mockito.times(DocumentType.values().length))
        .save(prefixCaptor.capture());
    assertEquals(DocumentType.values().length, prefixCaptor.getAllValues().size());
    for (final DocumentPrefix prefix : prefixCaptor.getAllValues()) {
      assertEquals(company, prefix.getCompany());
      assertEquals(prefix.getDocumentType().getDefaultPrefix(), prefix.getPrefix());
    }
  }

  @Test
  void doesNotSeedWhenACompanyAlreadyExists() {
    when(companyService.findAll())
        .thenReturn(List.of(Company.builder().id(10L).name("Existing Company").build()));

    seeder.run();

    verify(companyService, never()).save(any(Company.class));
    verify(userService, never()).save(any(User.class));
    verify(documentPrefixService, never()).save(any(DocumentPrefix.class));
  }

  @Test
  void rejectsMissingConfigurationWhenDatabaseHasNoCompany() {
    when(companyService.findAll()).thenReturn(List.of());
    when(appConfiguration.getInitialAdminEmail()).thenReturn(" ");

    final IllegalStateException exception =
        assertThrows(IllegalStateException.class, () -> seeder.run());

    assertEquals(
        "Missing required environment variable INITIAL_ADMIN_EMAIL for initial database setup",
        exception.getMessage());
    verify(companyService, never()).save(any(Company.class));
  }
}
