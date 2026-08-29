package dev.roland.inventory_management_backend.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;

import dev.roland.inventory_management_backend.enums.MediaEntityType;
import dev.roland.inventory_management_backend.enums.MediaUsageType;
import dev.roland.inventory_management_backend.model.interfaces.IdInterface;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Describes how a {@link MediaAsset} is used by a domain entity.
 *
 * <p>Because media can belong to users, companies, products, or future entity types, this table
 * stores the target entity type and identifier rather than a foreign key to one specific table. The
 * usage type identifies roles such as avatar, logo, or product image.
 */
@Entity
@Table(
    name = "media_usages",
    indexes = {@Index(name = "idx_media_usage_entity", columnList = "entity_type, entity_id")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class MediaUsage implements IdInterface<Long> {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "media_asset_id", nullable = false)
  private MediaAsset mediaAsset;

  @Column(name = "entity_type", nullable = false, length = 80)
  @Enumerated(EnumType.STRING)
  private MediaEntityType entityType;

  @Column(name = "entity_id", nullable = false)
  private Long entityId;

  @Column(name = "usage_type", nullable = false, length = 80)
  @Enumerated(EnumType.STRING)
  private MediaUsageType usageType;

  @Column(name = "sort_order")
  private Integer sortOrder = 1;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;
}
