package dev.roland.inventory_management_backend.features.product_attribute_option.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.roland.inventory_management_backend.features.product_attribute_option.ProductAttributeOption;

/** Provides database queries for product attribute option records. */
public interface ProductAttributeOptionRepository
    extends JpaRepository<ProductAttributeOption, Long> {}
