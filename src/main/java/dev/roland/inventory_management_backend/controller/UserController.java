package dev.roland.inventory_management_backend.controller;

import static dev.roland.inventory_management_backend.controller.UserController.USER_BASE_ENDPOINT;

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
import dev.roland.inventory_management_backend.dto.user.AvatarUploadRequest;
import dev.roland.inventory_management_backend.dto.user.UserDto;
import dev.roland.inventory_management_backend.facade.UserFacade;
import dev.roland.inventory_management_backend.message_key.GenericMessageKey;
import dev.roland.inventory_management_backend.message_key.UserMessageKey;
import dev.roland.inventory_management_backend.service.UserService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(USER_BASE_ENDPOINT)
@RequiredArgsConstructor
public class UserController {
  public static final String USER_BASE_ENDPOINT = "api/v1/user";
  public static final String REGISTER_ENDPOINT = "/register";
  public static final String ID_PARAM = "/{id}";
  public static final String ALL_USERS_ENDPOINT = "/all";
  public static final String RESET_TWO_FA_ENDPOINT = "/reset-2fa" + ID_PARAM;
  public static final String SUSPEND_ENDPOINT = "/suspend" + ID_PARAM;
  public static final String ACTIVATE_ENDPOINT = "/activate" + ID_PARAM;
  public static final String RESET_PASSWORD_ENDPOINT = "/reset-password" + ID_PARAM;
  public static final String AVATAR_ENDPOINT = ID_PARAM + "/avatar";

  private final UserService userService;
  private final UserFacade userFacade;

  @PostMapping(REGISTER_ENDPOINT)
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<UserDto>> registerUser(
      @Valid @RequestBody AddEditUserRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(
            UserMessageKey.REGISTRATION_SUCCESSFUL, userFacade.registerUser(request)));
  }

  @PutMapping(ID_PARAM)
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<UserDto>> updateUser(
      @PathVariable Long id, @RequestBody AddEditUserRequest updateRequest) {
    return ResponseEntity.ok(
        ApiResponse.success(
            UserMessageKey.UPDATE_SUCCESS, userService.updateUser(id, updateRequest)));
  }

  @GetMapping(ALL_USERS_ENDPOINT)
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<AllUserResponse>> getAllUsers() {
    return ResponseEntity.ok(
        ApiResponse.success(GenericMessageKey.REQUEST_SUCCESS, userFacade.getAllUsers()));
  }

  @PostMapping(RESET_TWO_FA_ENDPOINT)
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<Void>> reset2fa(@PathVariable Long id) {
    userFacade.resetUser2FA(id);

    return ResponseEntity.ok(ApiResponse.success(UserMessageKey.TWO_FA_SETUP_RESET_COMPLETE, null));
  }

  @PostMapping(SUSPEND_ENDPOINT)
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<Void>> suspendUser(@PathVariable Long id) {
    userFacade.suspendUser(id);

    return ResponseEntity.ok(ApiResponse.success(UserMessageKey.USER_SUSPENDED, null));
  }

  @PostMapping(ACTIVATE_ENDPOINT)
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<Void>> activateUser(@PathVariable Long id) {
    userFacade.activateUser(id);

    return ResponseEntity.ok(ApiResponse.success(UserMessageKey.USER_ACTIVATED, null));
  }

  @PostMapping(RESET_PASSWORD_ENDPOINT)
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<Void>> resetPassword(@PathVariable Long id) {
    userFacade.resetPassword(id);

    return ResponseEntity.ok(ApiResponse.success(UserMessageKey.PASSWORD_RESET_COMPLETE, null));
  }

  @PostMapping(AVATAR_ENDPOINT)
  public ResponseEntity<ApiResponse<Void>> updateAvatar(
      @PathVariable Long id, @RequestBody AvatarUploadRequest request) {
    userFacade.updateAvatar(id, request.getMediaAssetId());

    return ResponseEntity.ok(ApiResponse.success(UserMessageKey.AVATAR_UPDATED, null));
  }
}
