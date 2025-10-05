package dev.roland.inventory_management_backend.dto.auth;

import dev.roland.inventory_management_backend.model.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
public class LoginResponse {

    private UserDetails user;
    private TokensDetails tokens;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @RequiredArgsConstructor
    public static class UserDetails {
        private String email;
        private String username;
        private UserRole role;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @RequiredArgsConstructor
    public static class TokensDetails {
        private String accessToken;
        private String refreshToken;
    }
}
