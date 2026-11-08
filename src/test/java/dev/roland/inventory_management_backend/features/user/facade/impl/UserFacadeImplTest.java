package dev.roland.inventory_management_backend.features.user.facade.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import dev.roland.inventory_management_backend.common.configuration.AppConfiguration;
import dev.roland.inventory_management_backend.common.exception.ApiException;
import dev.roland.inventory_management_backend.common.service.EmailService;
import dev.roland.inventory_management_backend.features.auth.CustomUserDetails;
import dev.roland.inventory_management_backend.features.media_asset.MediaAsset;
import dev.roland.inventory_management_backend.features.media_asset.facade.MediaAssetFacade;
import dev.roland.inventory_management_backend.features.media_asset.service.MediaAssetService;
import dev.roland.inventory_management_backend.features.media_usage.MediaUsage;
import dev.roland.inventory_management_backend.features.media_usage.enumeration.MediaEntityType;
import dev.roland.inventory_management_backend.features.media_usage.enumeration.MediaUsageType;
import dev.roland.inventory_management_backend.features.media_usage.service.MediaUsageService;
import dev.roland.inventory_management_backend.features.one_time_code.service.OneTimeCodeService;
import dev.roland.inventory_management_backend.features.user.User;
import dev.roland.inventory_management_backend.features.user.dto.AddEditUserRequest;
import dev.roland.inventory_management_backend.features.user.dto.UserDto;
import dev.roland.inventory_management_backend.features.user.enumeration.UserRole;
import dev.roland.inventory_management_backend.features.user.enumeration.UserStatus;
import dev.roland.inventory_management_backend.features.user.mail.UserInvitationMailable;
import dev.roland.inventory_management_backend.features.user.message.UserMessageKey;
import dev.roland.inventory_management_backend.features.user.service.UserService;

@ExtendWith(MockitoExtension.class)
class UserFacadeImplTest {

  @Mock private UserService userService;
  @Mock private EmailService emailService;
  @Mock private MediaAssetService mediaAssetService;
  @Mock private MediaUsageService mediaUsageService;
  @Mock private MediaAssetFacade mediaAssetFacade;
  @Mock private OneTimeCodeService oneTimeCodeService;
  @Mock private AppConfiguration appConfiguration;

  private UserFacadeImpl facade;

  @BeforeEach
  void setUp() {
    Mockito.lenient().when(appConfiguration.getFrontendUrl()).thenReturn("https://app.example.com");
    facade =
        new UserFacadeImpl(
            userService,
            emailService,
            mediaAssetService,
            mediaUsageService,
            mediaAssetFacade,
            oneTimeCodeService,
            appConfiguration);
    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken(
                new CustomUserDetails(9L, "admin", "", UserRole.ADMIN, UserStatus.ACTIVE), null));
    Mockito.lenient()
        .when(userService.findByIdOrThrow(9L))
        .thenReturn(
            User.builder()
                .company(
                    dev.roland.inventory_management_backend.features.company.Company.builder()
                        .id(2L)
                        .build())
                .build());
  }

  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void registrationCreatesSetupRequiredUserWithRequestedRole() {
    final AddEditUserRequest request = request("new-user", "new@example.com", "MANAGER");
    when(userService.save(ArgumentMatchers.any(User.class)))
        .thenAnswer(
            invocation -> {
              final User savedUser = invocation.getArgument(0);
              savedUser.setId(1L);
              return savedUser;
            });

    final var response = facade.registerUser(request);

    assertEquals("new-user", response.getUsername());
    verify(userService)
        .save(
            ArgumentMatchers.argThat(
                user ->
                    user.getRole() == UserRole.MANAGER
                        && user.getStatus() == UserStatus.SETUP_REQUIRED
                        && user.getPassword() == null));
    verify(emailService)
        .enqueue(
            ArgumentMatchers.argThat(
                mailable ->
                    mailable instanceof UserInvitationMailable
                        && mailable.recipient().equals("new@example.com")
                        && mailable.templateVariables().get("username").equals("new-user")));
  }

  @Test
  void registrationRejectsUnknownRoleWithoutSaving() {
    final ApiException exception =
        assertThrows(
            ApiException.class,
            () -> facade.registerUser(request("user", "user@example.com", "OWNER")));

    assertEquals(UserMessageKey.INVALID_ROLE, exception.getMessageKey());
    verify(userService, never()).save(ArgumentMatchers.any(User.class));
    verify(emailService, never()).enqueue(ArgumentMatchers.any());
  }

  @Test
  void resetTwoFactorAuthRequiresAnExistingConfiguration() {
    final User user = User.builder().id(7L).build();
    when(userService.findByIdOrThrow(7L)).thenReturn(user);

    final ApiException exception =
        assertThrows(ApiException.class, () -> facade.resetUserTwoFactorAuth(7L));

    assertEquals(UserMessageKey.TWO_FA_NOT_ENABLED, exception.getMessageKey());
    verify(userService, never()).save(user);
  }

  @Test
  void resetTwoFactorAuthClearsSecretAndEnabledFlag() {
    final User user = User.builder().id(7L).totpSecret("secret").is2faEnabled(true).build();
    when(userService.findByIdOrThrow(7L)).thenReturn(user);

    facade.resetUserTwoFactorAuth(7L);

    assertNull(user.getTotpSecret());
    assertEquals(false, user.is2faEnabled());
    verify(userService).save(user);
  }

  @Test
  void suspendedUserCannotBeSuspendedAgain() {
    final User user = User.builder().id(8L).status(UserStatus.SUSPENDED).build();
    when(userService.findByIdOrThrow(8L)).thenReturn(user);

    final ApiException exception = assertThrows(ApiException.class, () -> facade.suspendUser(8L));

    assertEquals(UserMessageKey.USER_ALREADY_SUSPENDED, exception.getMessageKey());
    verify(userService, never()).save(user);
  }

  @Test
  void suspendClearsAuthenticationStateAndActivateReturnsToSetupRequired() {
    final User user =
        User.builder()
            .id(8L)
            .username("suspended-user")
            .email("suspended@example.com")
            .status(UserStatus.ACTIVE)
            .password("hashed")
            .totpSecret("secret")
            .is2faEnabled(true)
            .isOtcSetupComplete(true)
            .build();
    when(userService.findByIdOrThrow(8L)).thenReturn(user);

    facade.suspendUser(8L);

    assertEquals(UserStatus.SUSPENDED, user.getStatus());
    assertNull(user.getPassword());
    assertNull(user.getTotpSecret());
    assertEquals(false, user.is2faEnabled());
    assertEquals(false, user.isOtcSetupComplete());
    verify(userService).save(user);

    user.setStatus(UserStatus.SUSPENDED);
    facade.activateUser(8L);
    assertEquals(UserStatus.SETUP_REQUIRED, user.getStatus());
  }

  @Test
  void passwordResetRequiresCompletedOneTimeCodeSetup() {
    final User user = User.builder().id(9L).isOtcSetupComplete(false).build();
    when(userService.findByIdOrThrow(9L)).thenReturn(user);

    final ApiException exception = assertThrows(ApiException.class, () -> facade.resetPassword(9L));

    assertEquals(UserMessageKey.PASSWORD_NOT_SET, exception.getMessageKey());
    verify(userService, never()).save(user);
  }

  @Test
  void passwordResetClearsCredentialsAndReturnsAccountToSetup() {
    final User user =
        User.builder()
            .id(9L)
            .username("reset-user")
            .email("reset@example.com")
            .status(UserStatus.ACTIVE)
            .password("hashed")
            .isOtcSetupComplete(true)
            .build();
    when(userService.findByIdOrThrow(9L)).thenReturn(user);

    facade.resetPassword(9L);

    assertNull(user.getPassword());
    assertEquals(false, user.isOtcSetupComplete());
    assertEquals(UserStatus.SETUP_REQUIRED, user.getStatus());
    verify(userService).save(user);
  }

  @Test
  void activationRejectsAnAccountThatIsNotSuspended() {
    when(userService.findByIdOrThrow(10L))
        .thenReturn(User.builder().id(10L).status(UserStatus.ACTIVE).build());

    final ApiException exception = assertThrows(ApiException.class, () -> facade.activateUser(10L));

    assertEquals(UserMessageKey.USER_NOT_SUSPENDED, exception.getMessageKey());
    verify(userService, never()).save(ArgumentMatchers.any(User.class));
  }

  @Test
  void updatingEmailIssuesANewPasswordSetupInvitation() {
    final User existingUser =
        User.builder().id(13L).username("alice").email("old@example.com").build();
    final User updatedUser =
        User.builder().id(13L).username("alice").email("new@example.com").build();
    final AddEditUserRequest request = request("alice", "new@example.com", "MANAGER");
    when(userService.findByIdOrThrow(13L)).thenReturn(existingUser, updatedUser);
    when(userService.updateUser(13L, request))
        .thenReturn(UserDto.builder().id(13L).email("new@example.com").build());

    facade.updateUser(13L, request);

    verify(oneTimeCodeService).save(ArgumentMatchers.any());
    verify(emailService).enqueue(ArgumentMatchers.any(UserInvitationMailable.class));
  }

  @Test
  void updatingOtherUserFieldsDoesNotSendANewPasswordSetupInvitation() {
    final User existingUser =
        User.builder().id(14L).username("alice").email("alice@example.com").build();
    final AddEditUserRequest request = request("alice-2", "alice@example.com", "MANAGER");
    when(userService.findByIdOrThrow(14L)).thenReturn(existingUser);
    when(userService.updateUser(14L, request))
        .thenReturn(UserDto.builder().id(14L).email("alice@example.com").build());

    facade.updateUser(14L, request);

    verify(oneTimeCodeService, never()).save(ArgumentMatchers.any());
    verify(emailService, never()).enqueue(ArgumentMatchers.any(UserInvitationMailable.class));
  }

  @Test
  void updatingAvatarSkipsSameAssetAndReplacesSharedOldAssetWithoutDeletingIt() {
    final User user = User.builder().id(11L).build();
    final MediaAsset oldAsset = MediaAsset.builder().id(20L).build();
    final MediaAsset newAsset = MediaAsset.builder().id(21L).build();
    when(userService.findByIdOrThrow(11L)).thenReturn(user);
    when(mediaUsageService.findByEntityTypeAndEntityIdAndUsageType(
            MediaEntityType.USER, 11L, MediaUsageType.AVATAR))
        .thenReturn(
            Optional.of(MediaUsage.builder().mediaAsset(oldAsset).build()),
            Optional.of(MediaUsage.builder().mediaAsset(oldAsset).build()));
    when(mediaUsageService.usageCountByMediaAssetId(20L)).thenReturn(1L);
    when(mediaAssetService.findByIdOrThrow(21L)).thenReturn(newAsset);

    facade.updateAvatar(11L, 20L);
    facade.updateAvatar(11L, 21L);

    verify(mediaAssetFacade, never()).deleteAsset(20L);
    verify(mediaUsageService).delete(ArgumentMatchers.any(MediaUsage.class));
    verify(mediaUsageService)
        .save(
            ArgumentMatchers.argThat(
                usage ->
                    usage.getMediaAsset() == newAsset
                        && usage.getEntityType() == MediaEntityType.USER
                        && usage.getEntityId().equals(11L)
                        && usage.getUsageType() == MediaUsageType.AVATAR));
  }

  @Test
  void allUserResponseMapsAvatarPreviewWhenUsageExists() {
    final User user = User.builder().id(12L).username("bob").build();
    final MediaAsset asset = MediaAsset.builder().id(22L).build();
    final MediaUsage usage = MediaUsage.builder().mediaAsset(asset).build();
    when(userService.findAll()).thenReturn(List.of(user));
    when(mediaUsageService.findByEntityTypeAndEntityIdAndUsageType(
            MediaEntityType.USER, 12L, MediaUsageType.AVATAR))
        .thenReturn(Optional.of(usage));
    when(mediaAssetFacade.getPreview(22L))
        .thenReturn(
            new dev.roland.inventory_management_backend.features.media_asset.dto
                .MediaPreviewResponse(22L, "https://avatar", null));

    final var response = facade.getAllUsers();

    assertEquals("https://avatar", response.getUsers().get(0).getAvatarUrl());
  }

  private AddEditUserRequest request(final String username, final String email, final String role) {
    final AddEditUserRequest request = new AddEditUserRequest();
    request.setUsername(username);
    request.setEmail(email);
    request.setRole(role);
    return request;
  }
}
