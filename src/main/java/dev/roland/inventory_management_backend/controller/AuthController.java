package dev.roland.inventory_management_backend.controller;

import dev.roland.inventory_management_backend.dto.auth.CheckFirstLoginResponse;
import dev.roland.inventory_management_backend.dto.auth.EmailRequest;
import dev.roland.inventory_management_backend.facade.AuthFacade;
import dev.roland.inventory_management_backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("api/V1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuthFacade authFacade;

    @PostMapping("/check-first-login")
    ResponseEntity<CheckFirstLoginResponse> checkIfFirstLogin(@RequestBody EmailRequest request) {
        return authService.checkIfFirstLogin(request);
    }

    @PostMapping("/first-login")
    ResponseEntity<Void> handleFirstLogin(@RequestBody EmailRequest request) {
        return authFacade.handleFirstLogin(request);
    }
}
