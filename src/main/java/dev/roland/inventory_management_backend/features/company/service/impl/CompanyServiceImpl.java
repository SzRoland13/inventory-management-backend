package dev.roland.inventory_management_backend.features.company.service.impl;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.common.message.MessageKey;
import dev.roland.inventory_management_backend.common.message.NotFoundMessageKey;
import dev.roland.inventory_management_backend.features.company.Company;
import dev.roland.inventory_management_backend.features.company.repository.CompanyRepository;
import dev.roland.inventory_management_backend.features.company.service.CompanyService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {
  private final CompanyRepository companyRepository;

  /** {@inheritDoc} */
  @Override
  public JpaRepository<Company, Long> getRepository() {
    return companyRepository;
  }

  /** {@inheritDoc} */
  @Override
  public MessageKey getNotFoundMessageKey() {
    return NotFoundMessageKey.COMPANY;
  }

  /** {@inheritDoc} */
  @Override
  public Optional<Company> findFirstByOrderByIdAsc() {
    return companyRepository.findFirstByOrderByIdAsc();
  }

  /** {@inheritDoc} */
  @Override
  public Company getCompanyOrCreateNew() {
    return findFirstByOrderByIdAsc().orElseGet(() -> save(Company.builder().name("").build()));
  }
}
