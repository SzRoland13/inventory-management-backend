package dev.roland.inventory_management_backend.service;

import dev.roland.inventory_management_backend.dto.ApiResponse;
import dev.roland.inventory_management_backend.dto.auth.CheckFirstLoginResponse;
import dev.roland.inventory_management_backend.dto.auth.EmailRequest;
import dev.roland.inventory_management_backend.dto.auth.PasswordSetupRequest;
import dev.roland.inventory_management_backend.messageKey.ApiException;
import org.springframework.http.ResponseEntity;

public interface AuthService {

    /**
     * This method checks if provided credentials are valid and if it is first login (missing password)
     *
     * @param request email address of user
     * @throws ApiException if user is not registered
     * @return success or failure ApiResponse based on if the user is trying to log in first time or not
     */
    ResponseEntity<ApiResponse<CheckFirstLoginResponse>> checkIfFirstLogin(EmailRequest request);

    /**
     * This method checks if provided credentials are valid and if it is then saves the new password of user
     *
     * @param request email of user and the password two times
     * @return Returns void if everything worked
     * @throws ApiException if user credentials are invalid or the two passwords do not match
     */
    ResponseEntity<ApiResponse<Void>> handleSetupOfNewPassword(PasswordSetupRequest request);
}
