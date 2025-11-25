package dev.roland.inventory_management_backend.facade;

import dev.roland.inventory_management_backend.dto.ApiResponse;
import dev.roland.inventory_management_backend.dto.user.RegisterUserRequest;
import dev.roland.inventory_management_backend.dto.user.Reset2FaRequest;
import dev.roland.inventory_management_backend.dto.user.UserDto;
import org.springframework.http.ResponseEntity;

public interface UserFacade {


    /**
     * Handles new user registration.
     *
     * @param request user details for registration.
     * @return created user entity.
     */
    ResponseEntity<ApiResponse<UserDto>> registerUser(RegisterUserRequest request);

    /**
     * Handles 2FA reset for a user.
     *
     * @param request user ids to reset the 2fa for.
     * @return void.
     */
    ResponseEntity<ApiResponse<Void>> resetUser2FA(Reset2FaRequest request);
}
