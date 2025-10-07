package dev.roland.inventory_management_backend.facade;

import dev.roland.inventory_management_backend.dto.ApiResponse;
import dev.roland.inventory_management_backend.dto.auth.LoginResponse;
import dev.roland.inventory_management_backend.dto.user.RegisterUserRequest;
import dev.roland.inventory_management_backend.dto.user.Reset2FARequest;
import dev.roland.inventory_management_backend.messageKey.ApiException;
import dev.roland.inventory_management_backend.messageKey.AuthMessageKey;
import dev.roland.inventory_management_backend.messageKey.UserMessageKey;
import dev.roland.inventory_management_backend.model.User;
import dev.roland.inventory_management_backend.model.enums.UserRole;
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
    public ResponseEntity<ApiResponse<LoginResponse.UserDetails>> registerUser(RegisterUserRequest request) {
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .build();

        try {
            user.setRole(UserRole.valueOf(request.getRole()));
        } catch (IllegalArgumentException e) {
            throw new ApiException(UserMessageKey.INVALID_ROLE);
        }

        User newUser = userService.save(user);

        LoginResponse.UserDetails userDetails = new LoginResponse.UserDetails(
                newUser.getEmail(),
                newUser.getUsername(),
                newUser.getRole()
        );

        return ResponseEntity.ok(ApiResponse.success(UserMessageKey.REGISTRATION_SUCCESSFUL, userDetails));
    }

    /**
     * Handles 2FA reset for a user.
     *
     * @param request user's id.
     * @return void.
     */
    @Override
    public ResponseEntity<ApiResponse<Void>> resetUser2FA(Reset2FARequest request) {
        User user = userService.findUserById(request.getUserId()).orElseThrow(
                () -> new ApiException(AuthMessageKey.INVALID_CREDENTIALS)
        );

        user.setTotpSecret(null);
        user.set2faEnabled(false);
        userService.save(user);

        return ResponseEntity.ok(ApiResponse.success(UserMessageKey.TWO_FA_SETUP_RESET_COMPLETE, null));
    }
}
