package dev.roland.inventory_management_backend.features.product_category.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.roland.inventory_management_backend.features.product_category.ProductCategory;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {}
