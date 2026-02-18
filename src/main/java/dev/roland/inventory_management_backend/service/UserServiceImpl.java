package dev.roland.inventory_management_backend.service;

import dev.roland.inventory_management_backend.dto.ApiResponse;
import dev.roland.inventory_management_backend.dto.user.AddEditUserRequest;
import dev.roland.inventory_management_backend.dto.user.AllUserResponse;
import dev.roland.inventory_management_backend.dto.user.UserDto;
import dev.roland.inventory_management_backend.messageKey.ApiException;
import dev.roland.inventory_management_backend.messageKey.AuthMessageKey;
import dev.roland.inventory_management_backend.messageKey.GenericMessageKey;
import dev.roland.inventory_management_backend.messageKey.UserMessageKey;
import dev.roland.inventory_management_backend.model.User;
import dev.roland.inventory_management_backend.model.enums.UserRole;
import dev.roland.inventory_management_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    /**
     * Retrieves a {@link User} entity from the database that matches the given email address.
     *
     * @param email the email address to look up
     * @return an {@link Optional} containing the found {@link User},
     * or an empty {@link Optional} if no matching entity exists
     */
    @Override
    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Persists a new {@link User} entity or updates an existing one in the database.
     *
     * @param user the {@link User} entity to save or update
     * @return the saved or updated {@link User} entity
     */
    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    /**
     * Retrieves a {@link User} entity from the database that matches the given id.
     *
     * @param userId the id to look up
     * @return an {@link Optional} containing the found {@link User},
     * or an empty {@link Optional} if no matching entity exists
     */
    @Override
    public Optional<User> findUserById(Long userId) {
        return userRepository.findById(userId);
    }

    /**
     * Validates the current authenticated user session.
     *
     * @param auth the {@link Authentication} object automatically injected by Spring Security,
     *             representing the currently authenticated user
     * @return a {@link ResponseEntity} containing an {@link ApiResponse} with a success status
     *         if the session is valid
     * @throws ApiException if the authentication is missing, invalid, or the principal cannot be resolved
     */
    @Override
    public ResponseEntity<ApiResponse<Void>> checkSession(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new ApiException(AuthMessageKey.INVALID_TOKEN);
        }

        User user = userRepository.findByUsername(auth.getName()).orElseThrow(
                () -> new ApiException(AuthMessageKey.INVALID_CREDENTIALS)
        );

        if (user == null) {
            throw new ApiException(AuthMessageKey.INVALID_TOKEN);
        }

        return ResponseEntity.ok(ApiResponse.success(AuthMessageKey.TOKEN_REFRESHED, null));
    }

    /**
     * Returns all the saved users.
     *
     * @return a {@link java.util.List} of {@link UserDto}
     */
    @Override
    public ResponseEntity<ApiResponse<AllUserResponse>> getAllUsers() {
        List<User> users = userRepository.findAll();

        return ResponseEntity.ok(ApiResponse.success(GenericMessageKey.REQUEST_SUCCESS, new AllUserResponse(mapUsersToUserDtos(users))));
    }

    /**
     * Updates the user with that data passed.
     *
     * @param id - the id of the user to update
     * @param updateRequest - the data to update the user
     * @return the updated user in a {@link UserDto}
     */
    @Override
    public ResponseEntity<ApiResponse<UserDto>> updateUser(Long id, AddEditUserRequest updateRequest) {
        User userToUpdate = userRepository.findById(id).orElseThrow(
                () -> new ApiException(UserMessageKey.USER_NOT_FOUND)
        );

        if (!userToUpdate.getEmail().equals(updateRequest.getEmail())) {
            userToUpdate.setOtcSetupComplete(false);
            userToUpdate.setTotpSecret(null);
            userToUpdate.set2faEnabled(false);
            userToUpdate.setPassword(null);
        }

        userToUpdate.setEmail(updateRequest.getEmail());
        userToUpdate.setUsername(updateRequest.getUsername());

        try {
            userToUpdate.setRole(UserRole.valueOf(updateRequest.getRole()));
        } catch (IllegalArgumentException e) {
            throw new ApiException(UserMessageKey.INVALID_ROLE);
        }

        return ResponseEntity.ok(ApiResponse.success(UserMessageKey.UPDATE_SUCCESS, new UserDto(userRepository.save(userToUpdate))));
    }

    private List<UserDto> mapUsersToUserDtos(List<User> users) {
        return users.stream().map(UserDto::new).toList();
    }
}
