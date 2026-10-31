package dev.roland.inventory_management_backend.features.document.repository;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dev.roland.inventory_management_backend.features.document.Document;

/** Provides database queries for inventory document records. */
public interface DocumentRepository extends JpaRepository<Document, Long> {
  Optional<Document> findByIdAndCompanyId(Long id, Long companyId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select d from Document d where d.id = :id and d.company.id = :companyId")
  Optional<Document> lockByIdAndCompanyId(@Param("id") Long id, @Param("companyId") Long companyId);

  List<Document> findAllByCompanyIdOrderByUpdatedAtDesc(Long companyId);
}
