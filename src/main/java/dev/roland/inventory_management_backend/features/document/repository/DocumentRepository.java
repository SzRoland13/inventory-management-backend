package dev.roland.inventory_management_backend.features.document.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.roland.inventory_management_backend.features.document.Document;

public interface DocumentRepository extends JpaRepository<Document, Long> {}
