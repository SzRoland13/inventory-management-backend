package dev.roland.inventory_management_backend.common.service;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.common.exception.NotFoundException;
import dev.roland.inventory_management_backend.common.message.NotFoundMessageKey;
import dev.roland.inventory_management_backend.features.auth.CustomUserDetails;
import dev.roland.inventory_management_backend.features.company.Company;
import dev.roland.inventory_management_backend.features.company.service.CompanyService;
import dev.roland.inventory_management_backend.features.user.service.UserService;
import lombok.RequiredArgsConstructor;

/** Resolves the company owned by the authenticated user for tenant-scoped workflows. */
@Service
@RequiredArgsConstructor
public class CurrentCompanyProvider {
  private final UserService userService;
  private final CompanyService companyService;

  /** Returns the company of the authenticated principal. */
  public Company getCurrentCompany() {
    var authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null) {
      throw new NotFoundException(NotFoundMessageKey.COMPANY);
    }

    Object principal = authentication.getPrincipal();

    if (!(principal instanceof CustomUserDetails details)) {
      throw new NotFoundException(NotFoundMessageKey.COMPANY);
    }
    var user = userService.findByIdOrThrow(details.getUserId());

    if (user.getCompany() == null || user.getCompany().getId() == null) {
      throw new NotFoundException(NotFoundMessageKey.COMPANY);
    }

    return companyService.findByIdOrThrow(user.getCompany().getId());
  }
}
