package dev.roland.inventory_management_backend.facade;

import dev.roland.inventory_management_backend.dto.ApiResponse;
import dev.roland.inventory_management_backend.dto.user.RegisterUserRequest;
import dev.roland.inventory_management_backend.dto.user.Reset2FaRequest;
import dev.roland.inventory_management_backend.dto.user.UserDto;
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
    public ResponseEntity<ApiResponse<UserDto>> registerUser(RegisterUserRequest request) {
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

        return ResponseEntity.ok(ApiResponse.success(UserMessageKey.REGISTRATION_SUCCESSFUL, new UserDto(newUser)));
    }

    /**
     * Handles 2FA reset for a user.
     *
     * @param request user ids to reset the 2fa for.
     * @return void.
     */
    @Override
    public ResponseEntity<ApiResponse<Void>> resetUser2FA(Reset2FaRequest request) {
        for (Long id : request.getIds()) {
            reset2FA(id);
        }

        return ResponseEntity.ok(ApiResponse.success(UserMessageKey.TWO_FA_SETUP_RESET_COMPLETE, null));
    }

    private void reset2FA(Long id) {
        User user = userService.findUserById(id).orElseThrow(
                () -> new ApiException(AuthMessageKey.INVALID_CREDENTIALS)
        );

        user.setTotpSecret(null);
        user.set2faEnabled(false);
        userService.save(user);
    }
}
