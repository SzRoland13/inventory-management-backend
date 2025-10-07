package dev.roland.inventory_management_backend.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Reset2FARequest {

    @NotNull
    private Long userId;
}
