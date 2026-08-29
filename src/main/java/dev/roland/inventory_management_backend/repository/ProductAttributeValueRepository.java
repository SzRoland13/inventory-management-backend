package dev.roland.inventory_management_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.roland.inventory_management_backend.model.ProductAttributeValue;

public interface ProductAttributeValueRepository
    extends JpaRepository<ProductAttributeValue, Long> {}
