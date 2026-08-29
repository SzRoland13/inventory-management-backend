package dev.roland.inventory_management_backend.features.user;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import dev.roland.inventory_management_backend.common.persistance.IdInterface;
import dev.roland.inventory_management_backend.enums.UserRole;
import dev.roland.inventory_management_backend.enums.UserStatus;
import dev.roland.inventory_management_backend.features.warehouse.Warehouse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Represents an application user and the user's authentication state.
 *
 * <p>The entity stores identity, role, onboarding status, password, and TOTP configuration. Other
 * models reference users as creators or owners of warehouses, documents, media assets, stock
 * movements, one-time codes, and refresh tokens. The warehouse collection is the only explicit
 * inverse association currently mapped on this entity.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class User implements IdInterface<Long> {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "username", nullable = false, unique = true, length = 100)
  private String username;

  @Column(name = "email", nullable = false, unique = true)
  private String email;

  @Column(name = "password")
  private String password;

  @Column(name = "role", nullable = false, length = 50)
  @Enumerated(EnumType.STRING)
  private UserRole role;

  @Column(name = "totp_secret")
  private String totpSecret;

  @Column(name = "is_2fa_enabled", nullable = false)
  private boolean is2faEnabled = false;

  @Column(name = "is_otc_setup_complete", nullable = false)
  private boolean isOtcSetupComplete = false;

  @Column(name = "status", nullable = false, length = 50)
  @Enumerated(EnumType.STRING)
  private UserStatus status;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Warehouse> warehouses = new ArrayList<>();
}
