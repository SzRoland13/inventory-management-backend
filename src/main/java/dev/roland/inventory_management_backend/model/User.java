package dev.roland.inventory_management_backend.model;

import dev.roland.inventory_management_backend.model.enums.UserRole;
import dev.roland.inventory_management_backend.model.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "email", nullable=false, unique=true)
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

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Warehouse> warehouses = new ArrayList<>();
}

