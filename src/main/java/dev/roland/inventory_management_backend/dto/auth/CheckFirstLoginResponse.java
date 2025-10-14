package dev.roland.inventory_management_backend.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckFirstLoginResponse {
    private boolean emailRegistered;
    private boolean firstLogin;
}
