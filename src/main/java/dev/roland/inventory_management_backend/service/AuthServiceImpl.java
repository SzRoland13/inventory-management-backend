package dev.roland.inventory_management_backend.service;

import dev.roland.inventory_management_backend.dto.auth.CheckFirstLoginResponse;
import dev.roland.inventory_management_backend.dto.auth.EmailRequest;
import dev.roland.inventory_management_backend.dto.auth.PasswordSetupRequest;
import dev.roland.inventory_management_backend.model.User;
import dev.roland.inventory_management_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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
            return ResponseEntity.badRequest().body(new CheckFirstLoginResponse(false, false));
        }

        User user = optionalUser.get();

        if (user.getPassword() == null) {
            return ResponseEntity.ok(new CheckFirstLoginResponse(true, true));
        } else {
            return ResponseEntity.ok(new CheckFirstLoginResponse(true, false));
        }
    }

    @Override
    public ResponseEntity<Void> handleSetupOfNewPassword(PasswordSetupRequest request) {
        Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());

        if (optionalUser.isEmpty() || optionalUser.get().getPassword() != null) {
            return ResponseEntity.badRequest().build();
        }

        User user = optionalUser.get();

        if (!request.getPassword().equals(request.getRepeatPassword())) {
            return ResponseEntity.badRequest().build();
        }

        user.setPassword(request.getPassword());

        userRepository.save(user);
        return ResponseEntity.ok().build();
    }


}
