package me.danielx.api.auth;

import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import me.danielx.api.users.User;
import me.danielx.api.users.UserRepository;
import me.danielx.api.auth.dto.RegisterRequest;
import me.danielx.api.auth.dto.RegisterUserResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PublicAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterUserResponse registerUser(RegisterRequest request) {
        String email = request.email();
        String password = request.password();
        String firstName = request.firstName();
        String lastName = request.lastName();

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        boolean duplicateEmail = userRepository.existsByEmailIgnoreCase(normalizedEmail);

        String passwordHash = passwordEncoder.encode(password);

        if (duplicateEmail) {
            throw new EmailAlreadyExistsException();
        }

        User user = User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(normalizedEmail)
                .passwordHash(passwordHash)
                .build();

        try {
            User savedUser = userRepository.saveAndFlush(user);
            return RegisterUserResponse.builder()
                    .publicId(savedUser.getPublicId())
                    .email(savedUser.getEmail())
                    .emailVerified(savedUser.isEmailVerified())
                    .firstName(savedUser.getFirstName())
                    .lastName(savedUser.getLastName())
                    .createdAt(savedUser.getCreatedAt())
                    .build();
        } catch (DataIntegrityViolationException ex) {
            throw new EmailAlreadyExistsException();
        }
    }
}
