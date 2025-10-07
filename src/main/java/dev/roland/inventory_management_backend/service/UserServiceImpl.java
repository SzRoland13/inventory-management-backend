package dev.roland.inventory_management_backend.service;

import dev.roland.inventory_management_backend.model.User;
import dev.roland.inventory_management_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}
