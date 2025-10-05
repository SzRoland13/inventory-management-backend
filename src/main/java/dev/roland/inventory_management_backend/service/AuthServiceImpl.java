package dev.roland.inventory_management_backend.service;

import dev.roland.inventory_management_backend.dto.ApiResponse;
import dev.roland.inventory_management_backend.dto.auth.CheckFirstLoginResponse;
import dev.roland.inventory_management_backend.dto.auth.EmailRequest;
import dev.roland.inventory_management_backend.dto.auth.PasswordSetupRequest;
import dev.roland.inventory_management_backend.messageKey.ApiException;
import dev.roland.inventory_management_backend.messageKey.AuthMessageKey;
import dev.roland.inventory_management_backend.model.User;
import dev.roland.inventory_management_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    /**
     * This method checks if provided credentials are valid and if it is first login (missing password)
     *
     * @param request email address of user
     * @return success or failure ApiResponse based on if the user is trying to log in first time or not
     * @throws ApiException if user is not registered
     */
    @Override
    public ResponseEntity<ApiResponse<CheckFirstLoginResponse>> checkIfFirstLogin(EmailRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ApiException(AuthMessageKey.INVALID_CREDENTIALS));

        if (user.getPassword() == null) {
            return ResponseEntity.ok(ApiResponse.success(AuthMessageKey.LOGIN_SUCCESS, new CheckFirstLoginResponse(true, true)));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.failure(AuthMessageKey.NOT_FIRST_LOGIN, new CheckFirstLoginResponse(true, false)));
        }
    }

    /**
     * This method checks if provided credentials are valid and if it is then saves the new password of user
     *
     * @param request email of user and the password two times
     * @return Returns void if everything worked
     * @throws ApiException if user credentials are invalid or the two passwords do not match
     */
    @Override
    public ResponseEntity<ApiResponse<Void>> handleSetupOfNewPassword(PasswordSetupRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(
                () -> new ApiException(AuthMessageKey.INVALID_CREDENTIALS)
        );

        if (user.getPassword() != null) {
           throw new ApiException(AuthMessageKey.NOT_FIRST_LOGIN);
        }

        if (!request.getPassword().equals(request.getRepeatPassword())) {
            throw new ApiException(AuthMessageKey.PASSWORDS_NOT_MATCH);
        }

        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success(AuthMessageKey.PASSWORD_SETUP_SUCCESS, null));
    }


}
