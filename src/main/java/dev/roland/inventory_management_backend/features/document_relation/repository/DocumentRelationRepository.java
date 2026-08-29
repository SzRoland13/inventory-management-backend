package dev.roland.inventory_management_backend.features.document_relation.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.roland.inventory_management_backend.features.document_relation.DocumentRelation;

public interface DocumentRelationRepository extends JpaRepository<DocumentRelation, Long> {}
