package dev.roland.inventory_management_backend.features.unit.facade.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.roland.inventory_management_backend.common.exception.ApiException;
import dev.roland.inventory_management_backend.features.company.Company;
import dev.roland.inventory_management_backend.features.company.service.CompanyService;
import dev.roland.inventory_management_backend.features.document_line.service.DocumentLineService;
import dev.roland.inventory_management_backend.features.product.dto.UnitRequest;
import dev.roland.inventory_management_backend.features.product.mapper.ProductSettingsMapperImpl;
import dev.roland.inventory_management_backend.features.product.message.ProductMessageKey;
import dev.roland.inventory_management_backend.features.product.service.ProductService;
import dev.roland.inventory_management_backend.features.unit.Unit;
import dev.roland.inventory_management_backend.features.unit.service.UnitService;

@ExtendWith(MockitoExtension.class)
class UnitFacadeImplTest {

  @Mock private UnitService unitService;
  @Mock private ProductService productService;
  @Mock private DocumentLineService documentLineService;
  @Mock private CompanyService companyService;

  private UnitFacadeImpl unitFacade;
  private Company company;

  @BeforeEach
  void setUp() {
    unitFacade =
        new UnitFacadeImpl(
            unitService,
            productService,
            documentLineService,
            companyService,
            new ProductSettingsMapperImpl());
    company = Company.builder().id(1L).build();
    when(companyService.getCompanyOrCreateNew()).thenReturn(company);
  }

  @Test
  void listsCompanyUnits() {
    when(unitService.findSelectableUnits(1L))
        .thenReturn(List.of(unit(10L, "KG", "Kilogram", "kg", false)));
    assertEquals("KG", unitFacade.list().getFirst().code());
  }

  @Test
  void createsAndUpdatesCompanyUnitWithTrimmedValues() {
    when(unitService.save(any(Unit.class))).thenAnswer(call -> call.getArgument(0));
    final var created = unitFacade.create(new UnitRequest(" KG ", " Kilogram ", " kg "));

    assertEquals("KG", created.code());
    assertEquals("Kilogram", created.name());
    assertEquals("kg", created.symbol());

    final Unit existing = unit(10L, "G", "Gram", "g", false);
    existing.setCompany(company);
    when(unitService.findById(10L)).thenReturn(Optional.of(existing));
    final var updated = unitFacade.update(10L, new UnitRequest(" GR ", " Grams ", " gr "));

    assertEquals("GR", updated.code());
    assertEquals("Grams", updated.name());
    assertEquals("gr", updated.symbol());
  }

  @Test
  void allowsUpdatingAUnitWithoutChangingItsExistingCode() {
    final Unit existing = unit(10L, "KG", "Kilogram", "kg", false);
    existing.setCompany(company);
    when(unitService.findById(10L)).thenReturn(Optional.of(existing));
    when(unitService.save(existing)).thenReturn(existing);
    lenient().when(unitService.existsByCompanyIdAndCode(1L, "KG")).thenReturn(true);

    final var updated = unitFacade.update(10L, new UnitRequest(" KG ", "Kilograms ", "kgs"));

    assertEquals("KG", updated.code());
    assertEquals("Kilograms", updated.name());
    assertEquals("kgs", updated.symbol());
  }

  @Test
  void rejectsDuplicateUnitCodeAndUnitDeletionWhileInUse() {
    when(unitService.existsByCompanyIdAndCode(1L, "KG")).thenReturn(true);
    final ApiException duplicate =
        assertThrows(
            ApiException.class, () -> unitFacade.create(new UnitRequest(" KG ", "Kilogram", "kg")));
    assertEquals(ProductMessageKey.INVALID_PRODUCT_DATA, duplicate.getMessageKey());

    final Unit existing = unit(10L, "KG", "Kilogram", "kg", false);
    existing.setCompany(company);
    when(unitService.findById(10L)).thenReturn(Optional.of(existing));
    when(productService.existsUsingUnit(10L)).thenReturn(true);
    final ApiException inUse = assertThrows(ApiException.class, () -> unitFacade.delete(10L));
    assertEquals(ProductMessageKey.PRODUCT_UNIT_IN_USE, inUse.getMessageKey());
    verify(unitService, never()).delete(existing);
  }

  @Test
  void deletesUnusedCompanyUnit() {
    final Unit existing = unit(10L, "KG", "Kilogram", "kg", false);
    existing.setCompany(company);
    when(unitService.findById(10L)).thenReturn(Optional.of(existing));

    unitFacade.delete(10L);

    verify(unitService).delete(existing);
  }

  private Unit unit(
      final Long id,
      final String code,
      final String name,
      final String symbol,
      final boolean system) {
    return Unit.builder().id(id).code(code).name(name).symbol(symbol).system(system).build();
  }
}
