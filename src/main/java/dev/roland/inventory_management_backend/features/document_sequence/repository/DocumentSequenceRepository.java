package dev.roland.inventory_management_backend.features.document_sequence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dev.roland.inventory_management_backend.features.document_sequence.DocumentSequence;

@Repository
public interface DocumentSequenceRepository extends JpaRepository<DocumentSequence, Long> {}
