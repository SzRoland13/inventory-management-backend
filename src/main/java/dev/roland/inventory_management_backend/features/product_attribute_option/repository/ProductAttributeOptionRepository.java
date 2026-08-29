package dev.roland.inventory_management_backend.features.product_attribute_option.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.roland.inventory_management_backend.features.product_attribute_option.ProductAttributeOption;

public interface ProductAttributeOptionRepository
    extends JpaRepository<ProductAttributeOption, Long> {}
