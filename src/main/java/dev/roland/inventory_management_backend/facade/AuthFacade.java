package dev.roland.inventory_management_backend.facade;

import dev.roland.inventory_management_backend.dto.auth.EmailRequest;
import org.springframework.http.ResponseEntity;

public interface AuthFacade {

    ResponseEntity<Void> handleFirstLogin(EmailRequest request);
}
