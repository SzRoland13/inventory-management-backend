package dev.roland.inventory_management_backend.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import org.hibernate.annotations.UpdateTimestamp;

import dev.roland.inventory_management_backend.enums.PartnerType;
import lombok.*;

@Entity
@Table(name = "partners")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Partner {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "type", nullable = false, length = 50)
  @Enumerated(EnumType.STRING)
  private PartnerType type;

  @Column(name = "tax_number", length = 50)
  private String taxNumber;

  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;
}
