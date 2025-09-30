package dev.roland.inventory_management_backend.facade;

import dev.roland.inventory_management_backend.dto.auth.EmailRequest;
import dev.roland.inventory_management_backend.dto.auth.LoginRequest;
import dev.roland.inventory_management_backend.dto.auth.LoginResponse;
import org.springframework.http.ResponseEntity;

public interface AuthFacade {

    ResponseEntity<Void> handleFirstLogin(EmailRequest request);

    ResponseEntity<LoginResponse> handleLogin(LoginRequest request);
}
