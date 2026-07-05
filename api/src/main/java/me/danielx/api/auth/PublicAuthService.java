package me.danielx.api.auth;

import java.util.Locale;
import lombok.RequiredArgsConstructor;
import me.danielx.api.auth.dto.LoginRequest;
import me.danielx.api.auth.dto.LoginResponse;
import me.danielx.api.auth.dto.RegisterRequest;
import me.danielx.api.auth.dto.RegisterResponse;
import me.danielx.api.users.User;
import me.danielx.api.users.UserRepository;
import me.danielx.api.users.dto.AuthenticatedUser;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PublicAuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;

  public RegisterResponse register(RegisterRequest request) {
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

    User user =
        User.builder()
            .firstName(firstName)
            .lastName(lastName)
            .email(normalizedEmail)
            .passwordHash(passwordHash)
            .build();

    try {
      User savedUser = userRepository.saveAndFlush(user);
      return RegisterResponse.builder()
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

  public LoginResponse login(LoginRequest request) {
    Authentication authentication =
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken.unauthenticated(
                request.email().trim().toLowerCase(Locale.ROOT), request.password()));

    AuthenticatedUser principal = (AuthenticatedUser) authentication.getPrincipal();

    User user = userRepository.findByPublicId(principal.publicId()).orElseThrow();

    return LoginResponse.builder()
        .publicId(user.getPublicId())
        .email(user.getEmail())
        .isVerified(user.isEmailVerified())
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .build();
  }
}
