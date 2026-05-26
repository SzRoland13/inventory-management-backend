package dev.roland.inventory_management_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.roland.inventory_management_backend.model.ProductCategory;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {}
