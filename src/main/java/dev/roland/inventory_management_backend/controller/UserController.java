package dev.roland.inventory_management_backend.controller;

import dev.roland.inventory_management_backend.dto.ApiResponse;
import dev.roland.inventory_management_backend.dto.user.*;
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
    public ResponseEntity<ApiResponse<UserDto>> registerUser(@Valid @RequestBody AddEditUserRequest request) {
        return userFacade.registerUser(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDto>> updateUser(@PathVariable Long id, @RequestBody AddEditUserRequest updateRequest) {
        return userService.updateUser(id, updateRequest);
    }

    @GetMapping("/check-session")
    public ResponseEntity<ApiResponse<Void>> checkSession(Authentication auth) {
        return userService.checkSession(auth);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AllUserResponse>> getAllUsers() {
        return userService.getAllUsers();
    }

    @PostMapping("/reset-2fa/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> reset2fa(@PathVariable Long id) {
        return userFacade.resetUser2FA(id);
    }

    @PostMapping("/suspend/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> suspendUser(@PathVariable Long id) {
        return userFacade.suspendUser(id);
    }

    @PostMapping("/activate/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> activateUser(@PathVariable Long id) {
        return userFacade.activateUser(id);
    }

    @PostMapping("/reset-password/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@PathVariable Long id) {
        return userFacade.resetPassword(id);
    }
}
