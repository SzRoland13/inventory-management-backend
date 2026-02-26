package dev.roland.inventory_management_backend.service.implementation;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.dto.auth.CheckFirstLoginResponse;
import dev.roland.inventory_management_backend.dto.auth.EmailRequest;
import dev.roland.inventory_management_backend.dto.auth.PasswordSetupRequest;
import dev.roland.inventory_management_backend.dto.user.UserDto;
import dev.roland.inventory_management_backend.exception.ApiException;
import dev.roland.inventory_management_backend.exception.UnauthorizedException;
import dev.roland.inventory_management_backend.messageKey.AuthMessageKey;
import dev.roland.inventory_management_backend.model.User;
import dev.roland.inventory_management_backend.service.AuthService;
import dev.roland.inventory_management_backend.service.UserService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

  private final UserService userService;
  private final PasswordEncoder passwordEncoder;

  /**
   * This method checks if provided credentials are valid and if it is first login (missing
   * password)
   *
   * @param request email address of user
   * @return ApiResponse based on if the user is trying to log in first time or not
   * @throws ApiException if user is not registered
   */
  @Override
  public CheckFirstLoginResponse checkIfFirstLogin(EmailRequest request) {
    User user = userService.findUserByEmailOrThrow(request.getEmail());

    return new CheckFirstLoginResponse(true, user.getPassword() == null);
  }

  /**
   * This method checks if provided credentials are valid and if it is then saves the new password
   * of user
   *
   * @param request email of user and the password two times
   * @throws ApiException if user credentials are invalid or the two passwords do not match
   */
  @Override
  public void handleSetupOfNewPassword(PasswordSetupRequest request) {
    User user = userService.findUserByEmailOrThrow(request.getEmail());

    if (user.getPassword() != null || user.isOtcSetupComplete()) {
      throw new ApiException(AuthMessageKey.NOT_FIRST_LOGIN);
    }

    if (!request.getPassword().equals(request.getRepeatPassword())) {
      throw new ApiException(AuthMessageKey.PASSWORDS_NOT_MATCH);
    }

    user.setPassword(passwordEncoder.encode(request.getPassword()));
    user.setOtcSetupComplete(true);

    userService.save(user);
  }

  /**
   * Validates the current authenticated user session.
   *
   * @param authentication the {@link Authentication} object automatically injected by Spring
   *     Security, representing the currently authenticated user
   * @throws UnauthorizedException if the authentication is missing, invalid, or the principal
   *     cannot be resolved
   */
  @Override
  public UserDto checkSession(Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      throw new UnauthorizedException(AuthMessageKey.INVALID_TOKEN);
    }

    Object principal = authentication.getPrincipal();

    if (!(principal instanceof UserDetails userDetails)) {
      throw new UnauthorizedException(AuthMessageKey.INVALID_TOKEN);
    }

    User user = userService.findByUsernameOrThrow(userDetails.getUsername());

    return new UserDto(user);
  }
}
