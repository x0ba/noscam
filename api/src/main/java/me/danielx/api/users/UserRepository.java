package me.danielx.api.users;

import java.util.Optional;
import java.util.UUID;

import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
  public Optional<User> findByPublicId(UUID publicId);

  @Override
  @NonNull
  public Optional<User> findById(@NonNull Long id);

  public Optional<User> findByEmailIgnoreCase(String email);

  boolean existsByEmailIgnoreCase(String email);
}
