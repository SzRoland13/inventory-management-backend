package dev.roland.inventory_management_backend.features.document.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.roland.inventory_management_backend.features.document.Document;

/** Provides database queries for inventory document records. */
public interface DocumentRepository extends JpaRepository<Document, Long> {}
