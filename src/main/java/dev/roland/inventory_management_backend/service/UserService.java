package dev.roland.inventory_management_backend.service;

import dev.roland.inventory_management_backend.model.User;

import java.util.Optional;

public interface UserService {

    Optional<User> findUserByEmail(String email);
}
