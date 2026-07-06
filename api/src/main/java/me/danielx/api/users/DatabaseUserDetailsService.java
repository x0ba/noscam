package me.danielx.api.users;

import me.danielx.api.users.dto.AuthenticatedUser;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {
  private final UserRepository userRepository;

  public DatabaseUserDetailsService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String email) {
    String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
    User user =
        userRepository
            .findByEmailIgnoreCase(normalizedEmail)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    return new AuthenticatedUser(
        user.getId(), user.getPublicId(), user.getEmail(), user.getPasswordHash());
  }
}
