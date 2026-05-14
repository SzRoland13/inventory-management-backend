package dev.roland.inventory_management_backend.facade.implementation;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.dto.user.AddEditUserRequest;
import dev.roland.inventory_management_backend.dto.user.AllUserResponse;
import dev.roland.inventory_management_backend.dto.user.UserDto;
import dev.roland.inventory_management_backend.dto.user.UserDtoWithAvatar;
import dev.roland.inventory_management_backend.enums.MediaEntityType;
import dev.roland.inventory_management_backend.enums.MediaUsageType;
import dev.roland.inventory_management_backend.enums.UserRole;
import dev.roland.inventory_management_backend.enums.UserStatus;
import dev.roland.inventory_management_backend.exception.ApiException;
import dev.roland.inventory_management_backend.facade.MediaAssetFacade;
import dev.roland.inventory_management_backend.facade.UserFacade;
import dev.roland.inventory_management_backend.message_key.UserMessageKey;
import dev.roland.inventory_management_backend.model.MediaAsset;
import dev.roland.inventory_management_backend.model.MediaUsage;
import dev.roland.inventory_management_backend.model.User;
import dev.roland.inventory_management_backend.service.MediaAssetService;
import dev.roland.inventory_management_backend.service.MediaUsageService;
import dev.roland.inventory_management_backend.service.UserService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserFacadeImpl implements UserFacade {

  private final UserService userService;
  private final MediaAssetService mediaAssetService;
  private final MediaUsageService mediaUsageService;
  private final MediaAssetFacade mediaAssetFacade;

  /**
   * Handles new user registration.
   *
   * @param request user details for registration.
   * @return created user entity.
   */
  @Override
  public UserDto registerUser(AddEditUserRequest request) {
    User user =
        User.builder()
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

    return new UserDto(newUser);
  }

  /**
   * Handles 2FA reset for a single user.
   *
   * @param id user id to reset the 2fa for.
   */
  @Override
  public void resetUser2FA(Long id) {
    User user = userService.findByIdOrThrow(id);

    if (user.getTotpSecret() == null && !user.is2faEnabled()) {
      throw new ApiException(UserMessageKey.TWO_FA_NOT_ENABLED);
    }

    user.setTotpSecret(null);
    user.set2faEnabled(false);
    userService.save(user);
  }

  /**
   * Suspends a user and resets their password and 2FA. Can suspend users in any status
   * (SETUP_REQUIRED, ACTIVE).
   *
   * @param id user id to suspend.
   */
  @Override
  public void suspendUser(Long id) {
    User user = userService.findByIdOrThrow(id);

    if (user.getStatus() == UserStatus.SUSPENDED) {
      throw new ApiException(UserMessageKey.USER_ALREADY_SUSPENDED);
    }

    user.setStatus(UserStatus.SUSPENDED);

    resetUserPasswordAndAuth(user);
    userService.save(user);
  }

  /**
   * Activates a suspended user. Always returns user to SETUP_REQUIRED status so they must set up
   * their account again.
   *
   * @param id user id to activate.
   */
  @Override
  public void activateUser(Long id) {
    User user = userService.findByIdOrThrow(id);

    if (user.getStatus() != UserStatus.SUSPENDED) {
      throw new ApiException(UserMessageKey.USER_NOT_SUSPENDED);
    }

    // Always return to SETUP_REQUIRED after activation
    // User will need to set up password and optionally 2FA again
    user.setStatus(UserStatus.SETUP_REQUIRED);
    userService.save(user);
  }

  /**
   * Resets a user's password and 2FA. Only works if user has completed initial setup.
   *
   * @param id user id to reset password for.
   */
  @Override
  public void resetPassword(Long id) {
    User user = userService.findByIdOrThrow(id);

    if (!user.isOtcSetupComplete()) {
      throw new ApiException(UserMessageKey.PASSWORD_NOT_SET);
    }

    resetUserPasswordAndAuth(user);
    user.setStatus(UserStatus.SETUP_REQUIRED);

    userService.save(user);
  }

  /**
   * Helper method to reset user password and authentication. Sets password to null so user must
   * request new one-time password. Resets 2FA setup completely (only if it exists).
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

  @Override
  public void updateAvatar(Long id, Long mediaAssetId) {
    User user = userService.findByIdOrThrow(id);

    Optional<MediaUsage> existingUsage =
        mediaUsageService.findByEntityTypeAndEntityIdAndUsageType(
            MediaEntityType.USER, user.getId(), MediaUsageType.AVATAR);

    // if same avatar -> do nothing
    if (existingUsage.isPresent()
        && existingUsage.get().getMediaAsset().getId().equals(mediaAssetId)) {
      return;
    }

    // delete old avatar usage and possibly the asset
    existingUsage.ifPresent(
        usage -> {
          MediaAsset oldAsset = usage.getMediaAsset();
          mediaUsageService.delete(usage);
          if (mediaUsageService.usageCountByMediaAssetId(oldAsset.getId()) == 0) {
            mediaAssetFacade.deleteAsset(oldAsset.getId());
          }
        });

    MediaAsset newAsset = mediaAssetService.findByIdOrThrow(mediaAssetId);

    MediaUsage usage =
        MediaUsage.builder()
            .mediaAsset(newAsset)
            .entityType(MediaEntityType.USER)
            .entityId(user.getId())
            .usageType(MediaUsageType.AVATAR)
            .build();

    mediaUsageService.save(usage);
  }

  /**
   * Returns all the saved users.
   *
   * @return a {@link java.util.List} of {@link UserDto}
   */
  @Override
  public AllUserResponse getAllUsers() {
    List<User> users = userService.findAll();

    return new AllUserResponse(mapUsersToUserDtos(users));
  }

  private List<UserDtoWithAvatar> mapUsersToUserDtos(List<User> users) {
    return users.stream()
        .map(
            user -> {
              Optional<MediaUsage> avatarUsage =
                  mediaUsageService.findByEntityTypeAndEntityIdAndUsageType(
                      MediaEntityType.USER, user.getId(), MediaUsageType.AVATAR);

              String avatarUrl =
                  avatarUsage
                      .map(
                          usage ->
                              mediaAssetFacade
                                  .getPreview(usage.getMediaAsset().getId())
                                  .getGetUrl())
                      .orElse(null);

              return new UserDtoWithAvatar(user, avatarUrl);
            })
        .toList();
  }
}
