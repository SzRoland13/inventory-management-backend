package dev.roland.inventory_management_backend.features.user.facade.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.common.exception.ApiException;
import dev.roland.inventory_management_backend.features.media_asset.MediaAsset;
import dev.roland.inventory_management_backend.features.media_asset.facade.MediaAssetFacade;
import dev.roland.inventory_management_backend.features.media_asset.service.MediaAssetService;
import dev.roland.inventory_management_backend.features.media_usage.MediaUsage;
import dev.roland.inventory_management_backend.features.media_usage.enumeration.MediaEntityType;
import dev.roland.inventory_management_backend.features.media_usage.enumeration.MediaUsageType;
import dev.roland.inventory_management_backend.features.media_usage.service.MediaUsageService;
import dev.roland.inventory_management_backend.features.user.User;
import dev.roland.inventory_management_backend.features.user.dto.AddEditUserRequest;
import dev.roland.inventory_management_backend.features.user.dto.AllUserResponse;
import dev.roland.inventory_management_backend.features.user.dto.UserDto;
import dev.roland.inventory_management_backend.features.user.dto.UserDtoWithAvatar;
import dev.roland.inventory_management_backend.features.user.enumeration.UserRole;
import dev.roland.inventory_management_backend.features.user.enumeration.UserStatus;
import dev.roland.inventory_management_backend.features.user.facade.UserFacade;
import dev.roland.inventory_management_backend.features.user.message.UserMessageKey;
import dev.roland.inventory_management_backend.features.user.service.UserService;
import lombok.RequiredArgsConstructor;

/** Implements the user account service operations. */
@Service
@RequiredArgsConstructor
public class UserFacadeImpl implements UserFacade {

  private final UserService userService;
  private final MediaAssetService mediaAssetService;
  private final MediaUsageService mediaUsageService;
  private final MediaAssetFacade mediaAssetFacade;

  /** {@inheritDoc} */
  @Override
  public UserDto registerUser(final AddEditUserRequest request) {
    final User user =
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

    final User newUser = userService.save(user);

    return new UserDto(newUser);
  }

  /** {@inheritDoc} */
  @Override
  public void resetUserTwoFactorAuth(final Long id) {
    final User user = userService.findByIdOrThrow(id);

    if (user.getTotpSecret() == null && !user.is2faEnabled()) {
      throw new ApiException(UserMessageKey.TWO_FA_NOT_ENABLED);
    }

    user.setTotpSecret(null);
    user.set2faEnabled(false);
    userService.save(user);
  }

  /** {@inheritDoc} */
  @Override
  public void suspendUser(final Long id) {
    final User user = userService.findByIdOrThrow(id);

    if (user.getStatus() == UserStatus.SUSPENDED) {
      throw new ApiException(UserMessageKey.USER_ALREADY_SUSPENDED);
    }

    user.setStatus(UserStatus.SUSPENDED);

    resetUserPasswordAndAuth(user);
    userService.save(user);
  }

  /** {@inheritDoc} */
  @Override
  public void activateUser(final Long id) {
    final User user = userService.findByIdOrThrow(id);

    if (user.getStatus() != UserStatus.SUSPENDED) {
      throw new ApiException(UserMessageKey.USER_NOT_SUSPENDED);
    }

    // Always return to SETUP_REQUIRED after activation
    // User will need to set up password and optionally 2FA again
    user.setStatus(UserStatus.SETUP_REQUIRED);
    userService.save(user);
  }

  /** {@inheritDoc} */
  @Override
  public void resetPassword(final Long id) {
    final User user = userService.findByIdOrThrow(id);

    if (!user.isOtcSetupComplete()) {
      throw new ApiException(UserMessageKey.PASSWORD_NOT_SET);
    }

    resetUserPasswordAndAuth(user);
    user.setStatus(UserStatus.SETUP_REQUIRED);

    userService.save(user);
  }

  /**
   * Clears the user's password and authentication setup state.
   *
   * @param user account whose credentials and authentication state are reset
   */
  private void resetUserPasswordAndAuth(final User user) {
    // Reset password - set to null so user must request new one-time password
    user.setPassword(null);
    user.setOtcSetupComplete(false);

    // Reset 2FA only if it's actually enabled
    if (user.is2faEnabled() || user.getTotpSecret() != null) {
      user.setTotpSecret(null);
      user.set2faEnabled(false);
    }
  }

  /**
   * {@inheritDoc}
   *
   * @param id id supplied to this method
   * @param mediaAssetId media asset id supplied to this method
   */
  @Override
  public void updateAvatar(final Long id, final Long mediaAssetId) {
    final User user = userService.findByIdOrThrow(id);

    final Optional<MediaUsage> existingUsage =
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
          final MediaAsset oldAsset = usage.getMediaAsset();
          mediaUsageService.delete(usage);
          if (mediaUsageService.usageCountByMediaAssetId(oldAsset.getId()) == 0) {
            mediaAssetFacade.deleteAsset(oldAsset.getId());
          }
        });

    final MediaAsset newAsset = mediaAssetService.findByIdOrThrow(mediaAssetId);

    final MediaUsage usage =
        MediaUsage.builder()
            .mediaAsset(newAsset)
            .entityType(MediaEntityType.USER)
            .entityId(user.getId())
            .usageType(MediaUsageType.AVATAR)
            .build();

    mediaUsageService.save(usage);
  }

  /**
   * {@inheritDoc}
   *
   * @return get all users result
   */
  @Override
  public AllUserResponse getAllUsers() {
    final List<User> users = userService.findAll();

    return new AllUserResponse(mapUsersToUserDtos(users));
  }

  private List<UserDtoWithAvatar> mapUsersToUserDtos(final List<User> users) {
    return users.stream()
        .map(
            user -> {
              final Optional<MediaUsage> avatarUsage =
                  mediaUsageService.findByEntityTypeAndEntityIdAndUsageType(
                      MediaEntityType.USER, user.getId(), MediaUsageType.AVATAR);

              final String avatarUrl =
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
