package dev.roland.inventory_management_backend.service;

import dev.roland.inventory_management_backend.dto.auth.*;
import org.springframework.http.ResponseEntity;

public interface AuthService {

    ResponseEntity<CheckFirstLoginResponse> checkIfFirstLogin(EmailRequest request);

    ResponseEntity<Void> handleSetupOfNewPassword(PasswordSetupRequest request);
}
