package dev.roland.inventory_management_backend.controller;

import dev.roland.inventory_management_backend.dto.ApiResponse;
import dev.roland.inventory_management_backend.dto.auth.LoginResponse;
import dev.roland.inventory_management_backend.dto.user.AllUserResponse;
import dev.roland.inventory_management_backend.dto.user.RegisterUserRequest;
import dev.roland.inventory_management_backend.dto.user.Reset2FARequest;
import dev.roland.inventory_management_backend.facade.UserFacade;
import dev.roland.inventory_management_backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserFacade userFacade;


    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<LoginResponse.UserDetails>> registerUser(@Valid @RequestBody RegisterUserRequest request) {
        return userFacade.registerUser(request);
    }

    @PostMapping("/reset-2fa")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> reset2fa(@Valid @RequestBody Reset2FARequest request) {
        return userFacade.resetUser2FA(request);
    }


    @GetMapping("/check-session")
    public ResponseEntity<ApiResponse<Void>> checkSession(Authentication auth) {
        return userService.checkSession(auth);
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<AllUserResponse>> getAllUsers() {
        return userService.getAllUsers();
    }
}
