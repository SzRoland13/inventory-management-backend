package dev.roland.inventory_management_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dev.roland.inventory_management_backend.model.DocumentSequence;

@Repository
public interface DocumentSequenceRepository extends JpaRepository<DocumentSequence, Long> {}
