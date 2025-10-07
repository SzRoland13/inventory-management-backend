package dev.roland.inventory_management_backend.dto.auth;

import lombok.Data;

@Data
public class ShortLivedTokenResponse {

    private String shortLivedToken;
}
