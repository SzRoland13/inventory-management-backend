package dev.roland.inventory_management_backend.features.product_attribute_definition.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.roland.inventory_management_backend.features.product_attribute_definition.ProductAttributeDefinition;

/** Provides database queries for product attribute definition records. */
public interface ProductAttributeDefinitionRepository
    extends JpaRepository<ProductAttributeDefinition, Long> {}
