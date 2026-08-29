package dev.roland.inventory_management_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.roland.inventory_management_backend.model.Document;

public interface DocumentRepository extends JpaRepository<Document, Long> {}
