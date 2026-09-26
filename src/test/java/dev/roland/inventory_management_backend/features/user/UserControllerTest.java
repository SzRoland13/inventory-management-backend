package dev.roland.inventory_management_backend.features.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.roland.inventory_management_backend.features.user.dto.AddEditUserRequest;
import dev.roland.inventory_management_backend.features.user.dto.AllUserResponse;
import dev.roland.inventory_management_backend.features.user.dto.AvatarUploadRequest;
import dev.roland.inventory_management_backend.features.user.dto.UserDto;
import dev.roland.inventory_management_backend.features.user.facade.UserFacade;
import dev.roland.inventory_management_backend.features.user.service.UserService;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

  @Mock private UserService userService;
  @Mock private UserFacade userFacade;

  private UserController controller;

  @BeforeEach
  void setUp() {
    controller = new UserController(userService, userFacade);
  }

  @Test
  void registerUpdateAndListEndpointsDelegateToTheMatchingComponent() {
    final AddEditUserRequest request = new AddEditUserRequest();
    final UserDto user = UserDto.builder().id(7L).username("alice").build();
    final AllUserResponse allUsers = new AllUserResponse(java.util.List.of(userWithAvatar()));
    when(userFacade.registerUser(request)).thenReturn(user);
    when(userService.updateUser(7L, request)).thenReturn(user);
    when(userFacade.getAllUsers()).thenReturn(allUsers);

    assertEquals(user, controller.registerUser(request).getBody().getPayload());
    assertEquals(user, controller.updateUser(7L, request).getBody().getPayload());
    assertEquals(allUsers, controller.getAllUsers().getBody().getPayload());

    verify(userFacade).registerUser(request);
    verify(userService).updateUser(7L, request);
    verify(userFacade).getAllUsers();
  }

  @Test
  void accountAndAvatarActionsDelegateAndReturnSuccessResponses() {
    final AvatarUploadRequest avatarRequest =
        AvatarUploadRequest.builder().mediaAssetId(30L).build();

    assertEquals(200, controller.reset2fa(1L).getStatusCode().value());
    assertEquals(200, controller.suspendUser(1L).getStatusCode().value());
    assertEquals(200, controller.activateUser(1L).getStatusCode().value());
    assertEquals(200, controller.resetPassword(1L).getStatusCode().value());
    assertEquals(200, controller.updateAvatar(1L, avatarRequest).getStatusCode().value());

    verify(userFacade).resetUserTwoFactorAuth(1L);
    verify(userFacade).suspendUser(1L);
    verify(userFacade).activateUser(1L);
    verify(userFacade).resetPassword(1L);
    verify(userFacade).updateAvatar(1L, 30L);
  }

  private dev.roland.inventory_management_backend.features.user.dto.UserDtoWithAvatar
      userWithAvatar() {
    return new dev.roland.inventory_management_backend.features.user.dto.UserDtoWithAvatar();
  }
}
