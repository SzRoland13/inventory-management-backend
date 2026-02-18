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

    @PostMapping("/reset-2fa")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> reset2fa(@RequestBody Reset2FaRequest request) {
        return userFacade.resetUser2FA(request);
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

}
