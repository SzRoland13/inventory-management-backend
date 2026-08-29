package dev.roland.inventory_management_backend.features.user.facade.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.common.exception.ApiException;
import dev.roland.inventory_management_backend.dto.user.AddEditUserRequest;
import dev.roland.inventory_management_backend.dto.user.AllUserResponse;
import dev.roland.inventory_management_backend.dto.user.UserDto;
import dev.roland.inventory_management_backend.dto.user.UserDtoWithAvatar;
import dev.roland.inventory_management_backend.enums.MediaEntityType;
import dev.roland.inventory_management_backend.enums.MediaUsageType;
import dev.roland.inventory_management_backend.enums.UserRole;
import dev.roland.inventory_management_backend.enums.UserStatus;
import dev.roland.inventory_management_backend.features.media_asset.MediaAsset;
import dev.roland.inventory_management_backend.features.media_asset.facade.MediaAssetFacade;
import dev.roland.inventory_management_backend.features.media_asset.service.MediaAssetService;
import dev.roland.inventory_management_backend.features.media_usage.MediaUsage;
import dev.roland.inventory_management_backend.features.media_usage.service.MediaUsageService;
import dev.roland.inventory_management_backend.features.user.User;
import dev.roland.inventory_management_backend.features.user.facade.UserFacade;
import dev.roland.inventory_management_backend.features.user.message.UserMessageKey;
import dev.roland.inventory_management_backend.features.user.service.UserService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserFacadeImpl implements UserFacade {

  private final UserService userService;
  private final MediaAssetService mediaAssetService;
  private final MediaUsageService mediaUsageService;
  private final MediaAssetFacade mediaAssetFacade;

  /** {@inheritDoc} */
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

  /** {@inheritDoc} */
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

  /** {@inheritDoc} */
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

  /** {@inheritDoc} */
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

  /** {@inheritDoc} */
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

  /** {@inheritDoc} */
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

  /** {@inheritDoc} */
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

  /** {@inheritDoc} */
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
