package dev.roland.inventory_management_backend.features.product_attribute_value.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.roland.inventory_management_backend.features.product_attribute_value.ProductAttributeValue;

public interface ProductAttributeValueRepository
    extends JpaRepository<ProductAttributeValue, Long> {}
