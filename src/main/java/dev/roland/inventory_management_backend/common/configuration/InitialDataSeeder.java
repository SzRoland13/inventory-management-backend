package dev.roland.inventory_management_backend.common.configuration;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import dev.roland.inventory_management_backend.features.company.Company;
import dev.roland.inventory_management_backend.features.company.service.CompanyService;
import dev.roland.inventory_management_backend.features.document.enumeration.DocumentType;
import dev.roland.inventory_management_backend.features.document_prefix.DocumentPrefix;
import dev.roland.inventory_management_backend.features.document_prefix.service.DocumentPrefixService;
import dev.roland.inventory_management_backend.features.user.User;
import dev.roland.inventory_management_backend.features.user.enumeration.UserRole;
import dev.roland.inventory_management_backend.features.user.enumeration.UserStatus;
import dev.roland.inventory_management_backend.features.user.service.UserService;
import lombok.RequiredArgsConstructor;

/** Seeds the first company, administrator, and document prefixes after database migrations. */
@Component
@RequiredArgsConstructor
public class InitialDataSeeder implements CommandLineRunner {

  private static final long SEED_LOCK_ID = 741028619L;

  private final CompanyService companyService;
  private final UserService userService;
  private final DocumentPrefixService documentPrefixService;
  private final JdbcTemplate jdbcTemplate;
  private final AppConfiguration appConfiguration;

  /**
   * Creates initial data once, using an advisory lock to serialize concurrent application starts.
   */
  @Override
  @Transactional
  public void run(final String... args) {
    if ("org.postgresql.Driver".equals(appConfiguration.getDatabaseDriverClassName())) {
      jdbcTemplate.execute("SELECT pg_advisory_xact_lock(" + SEED_LOCK_ID + ")");
    }

    List<Company> companies = companyService.findAll();
    if (companies.isEmpty()) {
      requireConfiguration("INITIAL_ADMIN_USERNAME", appConfiguration.getInitialAdminUsername());
      requireConfiguration("INITIAL_ADMIN_EMAIL", appConfiguration.getInitialAdminEmail());
      requireConfiguration("INITIAL_COMPANY_NAME", appConfiguration.getInitialCompanyName());

      final Company company =
          companyService.save(
              Company.builder().name(appConfiguration.getInitialCompanyName()).build());
      userService.save(
          User.builder()
              .username(appConfiguration.getInitialAdminUsername())
              .email(appConfiguration.getInitialAdminEmail())
              .role(UserRole.ADMIN)
              .status(UserStatus.SETUP_REQUIRED)
              .company(company)
              .build());
      companies = List.of(company);
    }

    for (final Company company : companies) {
      for (final DocumentType type : DocumentType.values()) {
        if (documentPrefixService
            .findFirstByCompanyIdAndDocumentTypeOrderByIdAsc(company.getId(), type)
            .isEmpty()) {
          documentPrefixService.save(
              DocumentPrefix.builder()
                  .company(company)
                  .documentType(type)
                  .prefix(type.getDefaultPrefix())
                  .build());
        }
      }
    }
  }

  private void requireConfiguration(final String key, final String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalStateException(
          "Missing required environment variable " + key + " for initial database setup");
    }
  }
}
