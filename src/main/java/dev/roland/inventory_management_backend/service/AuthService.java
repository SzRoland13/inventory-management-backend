package dev.roland.inventory_management_backend.service;

import dev.roland.inventory_management_backend.dto.auth.CheckFirstLoginResponse;
import dev.roland.inventory_management_backend.dto.auth.EmailRequest;
import org.springframework.http.ResponseEntity;

public interface AuthService {

    ResponseEntity<CheckFirstLoginResponse> checkIfFirstLogin(EmailRequest request);
}
