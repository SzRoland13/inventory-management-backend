package dev.roland.inventory_management_backend.controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.roland.inventory_management_backend.dto.ApiResponse;
import dev.roland.inventory_management_backend.dto.user.AddEditUserRequest;
import dev.roland.inventory_management_backend.dto.user.AllUserResponse;
import dev.roland.inventory_management_backend.dto.user.UserDto;
import dev.roland.inventory_management_backend.facade.UserFacade;
import dev.roland.inventory_management_backend.messageKey.GenericMessageKey;
import dev.roland.inventory_management_backend.messageKey.UserMessageKey;
import dev.roland.inventory_management_backend.service.UserService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1/user")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;
  private final UserFacade userFacade;

  @PostMapping("/register")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<UserDto>> registerUser(
      @Valid @RequestBody AddEditUserRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(
            UserMessageKey.REGISTRATION_SUCCESSFUL, userFacade.registerUser(request)));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<UserDto>> updateUser(
      @PathVariable Long id, @RequestBody AddEditUserRequest updateRequest) {
    return ResponseEntity.ok(
        ApiResponse.success(
            UserMessageKey.UPDATE_SUCCESS, userService.updateUser(id, updateRequest)));
  }

  @GetMapping("/all")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<AllUserResponse>> getAllUsers() {
    return ResponseEntity.ok(
        ApiResponse.success(GenericMessageKey.REQUEST_SUCCESS, userService.getAllUsers()));
  }

  @PostMapping("/reset-2fa/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<Void>> reset2fa(@PathVariable Long id) {
    userFacade.resetUser2FA(id);

    return ResponseEntity.ok(ApiResponse.success(UserMessageKey.TWO_FA_SETUP_RESET_COMPLETE, null));
  }

  @PostMapping("/suspend/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<Void>> suspendUser(@PathVariable Long id) {
    userFacade.suspendUser(id);

    return ResponseEntity.ok(ApiResponse.success(UserMessageKey.USER_SUSPENDED, null));
  }

  @PostMapping("/activate/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<Void>> activateUser(@PathVariable Long id) {
    userFacade.activateUser(id);

    return ResponseEntity.ok(ApiResponse.success(UserMessageKey.USER_ACTIVATED, null));
  }

  @PostMapping("/reset-password/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<Void>> resetPassword(@PathVariable Long id) {
    userFacade.resetPassword(id);

    return ResponseEntity.ok(ApiResponse.success(UserMessageKey.PASSWORD_RESET_COMPLETE, null));
  }
}
