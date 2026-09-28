package dev.roland.inventory_management_backend.features.product_attribute_option.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.roland.inventory_management_backend.features.product_attribute_option.ProductAttributeOption;

/** Provides database queries for product attribute option records. */
public interface ProductAttributeOptionRepository
    extends JpaRepository<ProductAttributeOption, Long> {
  /**
   * Finds matching records using the supplied criteria.
   *
   * @param definitionId the attribute definition identifier
   * @return the matching resources
   */
  List<ProductAttributeOption> findAllByDefinitionId(Long definitionId);

  /**
   * Checks whether a matching record exists.
   *
   * @param definitionId the attribute definition identifier
   * @return true if a matching record exists
   */
  boolean existsByDefinitionId(Long definitionId);

  /**
   * Checks whether a matching record exists.
   *
   * @param definitionId the attribute definition identifier
   * @param value the option value
   * @return true if a matching record exists
   */
  boolean existsByDefinitionIdAndValue(Long definitionId, String value);

  /**
   * Checks whether a matching record exists.
   *
   * @param definitionId the attribute definition identifier
   * @param value the option value
   * @param id the resource identifier
   * @return true if a matching record exists
   */
  boolean existsByDefinitionIdAndValueAndIdNot(Long definitionId, String value, Long id);
}
