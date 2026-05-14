package dev.roland.inventory_management_backend.service.implementation;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.message_key.MessageKey;
import dev.roland.inventory_management_backend.message_key.NotFoundMessageKey;
import dev.roland.inventory_management_backend.model.Company;
import dev.roland.inventory_management_backend.repository.CompanyRepository;
import dev.roland.inventory_management_backend.service.CompanyService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {
  private final CompanyRepository companyRepository;

  @Override
  public JpaRepository<Company, Long> getRepository() {
    return companyRepository;
  }

  @Override
  public MessageKey getNotFoundMessageKey() {
    return NotFoundMessageKey.COMPANY;
  }

  @Override
  public Optional<Company> findFirstByOrderByIdAsc() {
    return companyRepository.findFirstByOrderByIdAsc();
  }

  @Override
  public Company getCompanyOrCreateNew() {
    return findFirstByOrderByIdAsc().orElseGet(() -> save(Company.builder().name("").build()));
  }
}
