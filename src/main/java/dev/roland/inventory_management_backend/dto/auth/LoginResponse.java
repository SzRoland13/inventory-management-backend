package dev.roland.inventory_management_backend.dto.auth;

import dev.roland.inventory_management_backend.enums.UserRole;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private UserDetails user;
    private TokensDetails tokens;
    private boolean firstTime2FAEnabled;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserDetails {
        private String email;
        private String username;
        private UserRole role;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TokensDetails {
        private String accessToken;
        private String refreshToken;
    }
}
