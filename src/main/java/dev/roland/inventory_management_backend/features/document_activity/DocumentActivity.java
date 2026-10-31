package dev.roland.inventory_management_backend.features.document_activity;

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
import dev.roland.inventory_management_backend.features.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Auditable event in a document's lifecycle. */
@Entity
@Table(name = "document_activity")
@Getter
@Setter
@NoArgsConstructor
public class DocumentActivity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "document_id", nullable = false)
  private Document document;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "actor_user_id")
  private User actor;

  @Enumerated(EnumType.STRING)
  @Column(name = "event_type", nullable = false, length = 80)
  private DocumentActivityEvent eventType;

  @Column(columnDefinition = "TEXT")
  private String details;

  @CreationTimestamp
  @Column(name = "occurred_at", nullable = false, updatable = false)
  private LocalDateTime occurredAt;
}
