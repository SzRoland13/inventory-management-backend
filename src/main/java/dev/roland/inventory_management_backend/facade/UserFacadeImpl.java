package dev.roland.inventory_management_backend.facade;

import dev.roland.inventory_management_backend.dto.ApiResponse;
import dev.roland.inventory_management_backend.dto.user.AddEditUserRequest;
import dev.roland.inventory_management_backend.dto.user.UserDto;
import dev.roland.inventory_management_backend.messageKey.ApiException;
import dev.roland.inventory_management_backend.messageKey.AuthMessageKey;
import dev.roland.inventory_management_backend.messageKey.UserMessageKey;
import dev.roland.inventory_management_backend.model.User;
import dev.roland.inventory_management_backend.model.enums.UserRole;
import dev.roland.inventory_management_backend.model.enums.UserStatus;
import dev.roland.inventory_management_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserFacadeImpl implements UserFacade {

    private final UserService userService;

    /**
     * Handles new user registration.
     *
     * @param request user details for registration.
     * @return created user entity.
     */
    @Override
    public ResponseEntity<ApiResponse<UserDto>> registerUser(AddEditUserRequest request) {
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .status(UserStatus.SETUP_REQUIRED)
                .build();

        try {
            user.setRole(UserRole.valueOf(request.getRole()));
        } catch (IllegalArgumentException e) {
            throw new ApiException(UserMessageKey.INVALID_ROLE);
        }

        User newUser = userService.save(user);

        return ResponseEntity.ok(ApiResponse.success(UserMessageKey.REGISTRATION_SUCCESSFUL, new UserDto(newUser)));
    }

    /**
     * Handles 2FA reset for a single user.
     *
     * @param id user id to reset the 2fa for.
     * @return void.
     */
    @Override
    public ResponseEntity<ApiResponse<Void>> resetUser2FA(Long id) {
        User user = userService.findUserById(id).orElseThrow(
                () -> new ApiException(AuthMessageKey.INVALID_CREDENTIALS)
        );

        if (user.getTotpSecret() == null && !user.is2faEnabled()) {
            throw new ApiException(UserMessageKey.TWO_FA_NOT_ENABLED);
        }

        user.setTotpSecret(null);
        user.set2faEnabled(false);
        userService.save(user);

        return ResponseEntity.ok(ApiResponse.success(UserMessageKey.TWO_FA_SETUP_RESET_COMPLETE, null));
    }

    /**
     * Suspends a user and resets their password and 2FA.
     * Can suspend users in any status (SETUP_REQUIRED, ACTIVE).
     *
     * @param id user id to suspend.
     * @return void.
     */
    @Override
    public ResponseEntity<ApiResponse<Void>> suspendUser(Long id) {
        User user = userService.findUserById(id).orElseThrow(
                () -> new ApiException(AuthMessageKey.INVALID_CREDENTIALS)
        );

        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new ApiException(UserMessageKey.USER_ALREADY_SUSPENDED);
        }

        user.setStatus(UserStatus.SUSPENDED);

        resetUserPasswordAndAuth(user);
        userService.save(user);

        return ResponseEntity.ok(ApiResponse.success(UserMessageKey.USER_SUSPENDED, null));
    }

    /**
     * Activates a suspended user.
     * Always returns user to SETUP_REQUIRED status so they must set up their account again.
     *
     * @param id user id to activate.
     * @return void.
     */
    @Override
    public ResponseEntity<ApiResponse<Void>> activateUser(Long id) {
        User user = userService.findUserById(id).orElseThrow(
                () -> new ApiException(AuthMessageKey.INVALID_CREDENTIALS)
        );

        if (user.getStatus() != UserStatus.SUSPENDED) {
            throw new ApiException(UserMessageKey.USER_NOT_SUSPENDED);
        }

        // Always return to SETUP_REQUIRED after activation
        // User will need to set up password and optionally 2FA again
        user.setStatus(UserStatus.SETUP_REQUIRED);
        userService.save(user);

        return ResponseEntity.ok(ApiResponse.success(UserMessageKey.USER_ACTIVATED, null));
    }

    /**
     * Resets a user's password and 2FA.
     * Only works if user has completed initial setup.
     *
     * @param id user id to reset password for.
     * @return void.
     */
    @Override
    public ResponseEntity<ApiResponse<Void>> resetPassword(Long id) {
        User user = userService.findUserById(id).orElseThrow(
                () -> new ApiException(AuthMessageKey.INVALID_CREDENTIALS)
        );

        if (!user.isOtcSetupComplete()) {
            throw new ApiException(UserMessageKey.PASSWORD_NOT_SET);
        }

        resetUserPasswordAndAuth(user);
        user.setStatus(UserStatus.SETUP_REQUIRED);

        userService.save(user);

        return ResponseEntity.ok(ApiResponse.success(UserMessageKey.PASSWORD_RESET_COMPLETE, null));
    }

    /**
     * Helper method to reset user password and authentication.
     * Sets password to null so user must request new one-time password.
     * Resets 2FA setup completely (only if it exists).
     *
     * @param user the user to reset.
     */
    private void resetUserPasswordAndAuth(User user) {
        // Reset password - set to null so user must request new one-time password
        user.setPassword(null);
        user.setOtcSetupComplete(false);

        // Reset 2FA only if it's actually enabled
        if (user.is2faEnabled() || user.getTotpSecret() != null) {
            user.setTotpSecret(null);
            user.set2faEnabled(false);
        }
    }
}