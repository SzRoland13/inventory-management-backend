package dev.roland.inventory_management_backend.dto.auth;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ShortLifeTokenResponse {

    private boolean is2faEnabled;
    private String shortLifeToken;
    private Instant expiresAt;
}
