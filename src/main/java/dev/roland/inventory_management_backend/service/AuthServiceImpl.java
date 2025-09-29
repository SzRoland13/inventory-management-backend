package dev.roland.inventory_management_backend.service;

import dev.roland.inventory_management_backend.dto.auth.CheckFirstLoginResponse;
import dev.roland.inventory_management_backend.dto.auth.EmailRequest;
import dev.roland.inventory_management_backend.model.User;
import dev.roland.inventory_management_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;

    @Override
    public ResponseEntity<CheckFirstLoginResponse> checkIfFirstLogin(EmailRequest request) {
        Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());

        if (optionalUser.isEmpty()) {
            return new ResponseEntity<>(new CheckFirstLoginResponse(false, false), HttpStatus.BAD_REQUEST);
        }

        User user = optionalUser.get();

        if (user.getPassword() == null) {
            return new ResponseEntity<>(new CheckFirstLoginResponse(true, true), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new CheckFirstLoginResponse(true, false), HttpStatus.OK);
        }
    }
}
