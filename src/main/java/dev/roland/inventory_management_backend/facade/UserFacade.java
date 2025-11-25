package dev.roland.inventory_management_backend.facade;

import dev.roland.inventory_management_backend.dto.ApiResponse;
import dev.roland.inventory_management_backend.dto.auth.LoginResponse;
import dev.roland.inventory_management_backend.dto.user.RegisterUserRequest;
import org.springframework.http.ResponseEntity;

public interface UserFacade {


    /**
     * Handles new user registration.
     *
     * @param request user details for registration.
     * @return created user entity.
     */
    ResponseEntity<ApiResponse<LoginResponse.UserDetails>> registerUser(RegisterUserRequest request);

    /**
     * Handles 2FA reset for a user.
     *
     * @param id user's id.
     * @return void.
     */
    ResponseEntity<ApiResponse<Void>> resetUser2FA(Long id);
}
