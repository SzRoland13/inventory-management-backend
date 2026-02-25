package dev.roland.inventory_management_backend.model;

import dev.roland.inventory_management_backend.model.interfaces.IdInterface;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken implements IdInterface<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String token;

    @Column(name = "expiry_date", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime expiryDate;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
