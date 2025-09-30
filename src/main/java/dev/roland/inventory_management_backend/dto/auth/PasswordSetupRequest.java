package dev.roland.inventory_management_backend.dto.auth;


import lombok.Data;

@Data
public class PasswordSetupRequest {

    private String email;
    private String password;
    private String repeatPassword;
}
