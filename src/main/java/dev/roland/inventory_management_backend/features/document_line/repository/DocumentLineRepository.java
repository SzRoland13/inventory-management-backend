package dev.roland.inventory_management_backend.features.document_line.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.roland.inventory_management_backend.features.document_line.DocumentLine;

public interface DocumentLineRepository extends JpaRepository<DocumentLine, Long> {}
