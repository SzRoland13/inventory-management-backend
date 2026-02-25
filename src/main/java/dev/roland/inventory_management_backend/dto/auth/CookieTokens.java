package dev.roland.inventory_management_backend.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CookieTokens {
    private String accessToken;
    private String refreshToken;
}
