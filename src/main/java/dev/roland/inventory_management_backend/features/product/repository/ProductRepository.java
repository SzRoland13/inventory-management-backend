package dev.roland.inventory_management_backend.features.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.roland.inventory_management_backend.features.product.Product;

/** Provides database queries for product catalog records. */
public interface ProductRepository extends JpaRepository<Product, Long> {}
