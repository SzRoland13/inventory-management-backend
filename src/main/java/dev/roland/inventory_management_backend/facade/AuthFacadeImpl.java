package dev.roland.inventory_management_backend.facade;

import dev.roland.inventory_management_backend.dto.auth.EmailRequest;
import dev.roland.inventory_management_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthFacadeImpl  implements AuthFacade {

    private final UserService userService;

    @Override
    public ResponseEntity<Void> handleFirstLogin(EmailRequest request) {
        /*
        * what i want to do here is to first check if user is valid, but does not have password,
        * then call the emailService (first make the email service :D ) to send out an email with a
        * one time login code
        */

        return null;
    }
}
