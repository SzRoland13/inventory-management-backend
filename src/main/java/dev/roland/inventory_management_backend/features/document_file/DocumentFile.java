package dev.roland.inventory_management_backend.features.document_file;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;

import dev.roland.inventory_management_backend.features.document.Document;
import dev.roland.inventory_management_backend.features.media_asset.MediaAsset;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Stored upload or generated PDF attempt for a business document. */
@Entity
@Table(name = "document_files")
@Getter
@Setter
@NoArgsConstructor
public class DocumentFile {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "document_id", nullable = false)
  private Document document;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "media_asset_id")
  private MediaAsset mediaAsset;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private DocumentFileSource source;

  @Enumerated(EnumType.STRING)
  @Column(length = 30)
  private DocumentFileStatus status;

  @Column(name = "idempotency_key", length = 200)
  private String idempotencyKey;

  @Column(name = "language_tag", length = 10)
  private String languageTag;

  @Column(nullable = false)
  private Integer attempt = 1;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_file_id")
  private DocumentFile parentFile;

  @Column(name = "error_message", columnDefinition = "TEXT")
  private String errorMessage;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;
}
